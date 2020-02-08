package org.wlld.imageRecognition;

import org.wlld.MatrixTools.Matrix;
import org.wlld.config.StudyPattern;
import org.wlld.function.ReLu;
import org.wlld.function.Sigmod;
import org.wlld.imageRecognition.border.*;
import org.wlld.imageRecognition.modelEntity.BoxList;
import org.wlld.imageRecognition.modelEntity.KBorder;
import org.wlld.imageRecognition.modelEntity.LvqModel;
import org.wlld.imageRecognition.modelEntity.MatrixModel;
import org.wlld.nerveCenter.NerveManager;
import org.wlld.nerveEntity.ModelParameter;
import org.wlld.nerveEntity.SensoryNerve;
import org.wlld.tools.ArithUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TempleConfig {
    private NerveManager nerveManager;//神经网络管理器
    private NerveManager convolutionNerveManager;//卷积神经网络管理器
    private double cutThreshold = 10;//切割阈值默认值
    private int row = 5;//行的最小比例
    private int column = 3;//列的最小比例
    private int deep = 2;//默认深度
    private int classificationNub = 1;//分类的数量
    private int studyPattern;//学习模式
    private boolean isHavePosition = false;//是否需要锁定物体位置
    private LVQ lvq;//模型需要返回,精准模式下的原型聚类
    private Frame frame;//先验边框
    private double th = 0.6;//标准阈值
    private boolean boxReady = false;//边框已经学习完毕
    private double iouTh = 0.5;//IOU阈值
    private int lvqNub = 50;//lvq循环次数，默认50
    //边框聚类集合 模型需要返回
    private Map<Integer, KClustering> kClusteringMap = new HashMap<>();

    public Map<Integer, KClustering> getkClusteringMap() {
        return kClusteringMap;
    }

    public int getLvqNub() {
        return lvqNub;
    }

    public void setLvqNub(int lvqNub) {
        this.lvqNub = lvqNub;
    }

    public double getIouTh() {
        return iouTh;
    }

    public void setIouTh(double iouTh) {
        this.iouTh = iouTh;
    }

    public boolean isBoxReady() {
        return boxReady;
    }

    public double getTh() {
        return th;
    }

    public void setTh(double th) {
        this.th = th;
    }

    public Frame getFrame() {
        return frame;
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
    }

    public void startLvq() throws Exception {//进行量化
        if (studyPattern == StudyPattern.Accuracy_Pattern) {
            lvq.start();
        }
    }

    public LVQ getLvq() {
        return lvq;
    }

    public void boxStudy() throws Exception {//边框回归 学习结束之后最后进行调用
        if (isHavePosition) {
            for (Map.Entry<Integer, KClustering> entry : kClusteringMap.entrySet()) {
                entry.getValue().start();
            }
            boxReady = true;
        }

    }

    public NerveManager getNerveManager() {
        return nerveManager;
    }

    public void setNerveManager(NerveManager nerveManager) {
        this.nerveManager = nerveManager;
    }

    public boolean isHavePosition() {
        return isHavePosition;
    }

    public void setHavePosition(boolean havePosition) {
        isHavePosition = havePosition;
    }

    public NerveManager getConvolutionNerveManager() {
        return convolutionNerveManager;
    }

    public int getStudyPattern() {
        return studyPattern;
    }

    public void init(int studyPattern, boolean initPower, int width, int height
            , int classificationNub) throws Exception {//初始化配置模板
        this.classificationNub = classificationNub;
        this.studyPattern = studyPattern;
        if (isHavePosition) {
            for (int i = 1; i < classificationNub + 1; i++) {
                kClusteringMap.put(i, new KClustering(10));
            }
        }
        switch (studyPattern) {
            case StudyPattern.Speed_Pattern://速度学习模式
                initModelVision(initPower, width, height);
                break;
            case StudyPattern.Accuracy_Pattern://精准学习模式
                initConvolutionVision(initPower, width, height);
                break;
        }
    }

    private void initModelVision(boolean initPower, int width, int height) throws Exception {//初始标准模板视觉
        double d;
        if (width > height) {
            d = ArithUtil.div(height, width);
            column = 5;
            row = (int) (d * column);
        } else {
            d = ArithUtil.div(width, height);
            row = 5;
            column = (int) (d * row);
        }
        initNerveManager(initPower, row * column, deep);
    }

    private void initNerveManager(boolean initPower, int sensoryNerveNub
            , int deep) throws Exception {
        nerveManager = new NerveManager(sensoryNerveNub, 6,
                classificationNub, deep, new Sigmod(), false);
        nerveManager.init(initPower, false);
    }

    private void initConvolutionVision(boolean initPower, int width, int height) throws Exception {
        int deep = 0;
        lvq = new LVQ(classificationNub + 1, lvqNub);
        Map<Integer, Matrix> matrixMap = new HashMap<>();//主键与期望矩阵的映射
        while (width > 5 && height > 5) {
            width = width / 3;
            height = height / 3;
            deep++;
        }
        //加载各识别分类的期望矩阵
        matrixMap.put(0, new Matrix(height, width));
        double nub = 10;//每个分类期望参数的跨度
        for (int k = 0; k < classificationNub; k++) {
            Matrix matrix = new Matrix(height, width);//初始化期望矩阵
            double t = (k + 1) * nub;//期望矩阵的分类参数数值
            for (int i = 0; i < height; i++) {//给期望矩阵注入期望参数
                for (int j = 0; j < width - 1; j++) {
                    matrix.setNub(i, j, t);
                }
            }
            matrixMap.put(k + 1, matrix);
        }
        convolutionNerveManager = new NerveManager(1, 1,
                1, deep - 1, new ReLu(), true);
        convolutionNerveManager.setMatrixMap(matrixMap);//给卷积网络管理器注入期望矩阵
        convolutionNerveManager.init(initPower, true);
    }

    private List<MatrixModel> getLvqModel(MatrixBody[] matrixBodies) throws Exception {
        List<MatrixModel> matrixModelList = new ArrayList<>();
        for (int i = 0; i < matrixBodies.length; i++) {
            MatrixBody matrixBody = matrixBodies[i];
            Matrix matrix = matrixBody.getMatrix();
            MatrixModel matrixModel = new MatrixModel();
            List<Double> rowVector = rowVectorToList(matrix);
            matrixModel.setId(matrixBody.getId());
            matrixModel.setRowVector(rowVector);
            matrixModelList.add(matrixModel);
        }
        return matrixModelList;
    }

    //行向量转LIST
    private List<Double> rowVectorToList(Matrix matrix) throws Exception {
        List<Double> list = new ArrayList<>();
        for (int j = 0; j < matrix.getY(); j++) {
            list.add(matrix.getNumber(0, j));
        }
        return list;
    }

    private Map<Integer, KBorder> kToBody() throws Exception {
        Map<Integer, KBorder> borderMap = new HashMap<>();
        for (Map.Entry<Integer, KClustering> entry : kClusteringMap.entrySet()) {
            KClustering kClustering = entry.getValue();
            KBorder kBorder = new KBorder();
            borderMap.put(entry.getKey(), kBorder);
            if (kClustering.isReady()) {
                kBorder.setLength(kClustering.getLength());
                kBorder.setSpeciesQuantity(kClustering.getSpeciesQuantity());
                Map<Integer, BoxList> myPosition = new HashMap<>();
                List<List<Double>> lists = new ArrayList<>();
                kBorder.setLists(lists);
                kBorder.setPositionMap(myPosition);
                Map<Integer, Box> positionMap = kClustering.getPositionMap();
                Matrix[] matrices = kClustering.getMatrices();
                for (Map.Entry<Integer, Box> entryBox : positionMap.entrySet()) {
                    Box box = entryBox.getValue();
                    BoxList boxList = new BoxList();
                    List<Double> furList = rowVectorToList(box.getMatrix());
                    List<Double> borderList = rowVectorToList(box.getMatrixPosition());
                    boxList.setList(furList);
                    boxList.setPositionList(borderList);
                    myPosition.put(entryBox.getKey(), boxList);
                }
                for (int i = 0; i < matrices.length; i++) {
                    lists.add(rowVectorToList(matrices[i]));
                }

            } else {
                throw new Exception("not ready");
            }
        }

        return borderMap;
    }

    public ModelParameter getModel() throws Exception {//获取模型参数
        ModelParameter modelParameter = new ModelParameter();
        if (studyPattern == StudyPattern.Accuracy_Pattern) {
            ModelParameter modelParameter1 = convolutionNerveManager.getModelParameter();
            modelParameter.setDymNerveStudies(modelParameter1.getDymNerveStudies());
            modelParameter.setDymOutNerveStudy(modelParameter1.getDymOutNerveStudy());
            //获取LVQ模型
            if (lvq.isReady()) {
                LvqModel lvqModel = new LvqModel();
                lvqModel.setLength(lvq.getLength());
                lvqModel.setTypeNub(lvq.getTypeNub());
                lvqModel.setMatrixModelList(getLvqModel(lvq.getModel()));
                modelParameter.setLvqModel(lvqModel);
            }

        } else if (studyPattern == StudyPattern.Speed_Pattern) {
            ModelParameter modelParameter1 = nerveManager.getModelParameter();
            modelParameter.setDepthNerves(modelParameter1.getDepthNerves());
            modelParameter.setOutNerves(modelParameter1.getOutNerves());
        }
        if (isHavePosition) {//存在边框学习模型参数
            Map<Integer, KBorder> kBorderMap = kToBody();
            modelParameter.setFrame(frame);
            modelParameter.setBorderMap(kBorderMap);
        }
        return modelParameter;
    }

    public List<SensoryNerve> getSensoryNerves() {//获取感知神经元
        return nerveManager.getSensoryNerves();
    }

    public void setStudy(double studyPoint) throws Exception {//设置学习率
        nerveManager.setStudyPoint(studyPoint);
    }

    public void setStudyList(List<Double> list) {//设置每一层不同的学习率
        if (studyPattern == StudyPattern.Accuracy_Pattern) {
            //给卷积层设置层学习率
            convolutionNerveManager.setStudyList(list);
        } else if (studyPattern == StudyPattern.Speed_Pattern) {
            //给全连接层设置学习率
            nerveManager.setStudyList(list);
        }
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    //list转行向量
    private Matrix listToRowVector(List<Double> list) throws Exception {
        Matrix matrix = new Matrix(1, list.size());
        for (int i = 0; i < list.size(); i++) {
            matrix.setNub(0, i, list.get(i));
        }
        return matrix;
    }

    //注入模型参数
    public void insertModel(ModelParameter modelParameter) throws Exception {
        if (studyPattern == StudyPattern.Accuracy_Pattern) {
            convolutionNerveManager.insertModelParameter(modelParameter);
            //LVQ模型参数注入
            LvqModel lvqModel = modelParameter.getLvqModel();
            if (lvqModel != null) {
                int length = lvqModel.getLength();
                int typeNub = lvqModel.getTypeNub();
                List<MatrixModel> matrixModels = lvqModel.getMatrixModelList();
                if (length > 0 && typeNub > 0 && matrixModels != null &&
                        matrixModels.size() > 0) {
                    MatrixBody[] model = new MatrixBody[matrixModels.size()];
                    for (int i = 0; i < model.length; i++) {
                        MatrixModel matrixModel = matrixModels.get(i);
                        MatrixBody matrixBody = new MatrixBody();
                        matrixBody.setId(matrixModel.getId());
                        matrixBody.setMatrix(listToRowVector(matrixModel.getRowVector()));
                        model[i] = matrixBody;
                    }
                    lvq.setLength(length);
                    lvq.setTypeNub(typeNub);
                    lvq.setReady(true);
                    lvq.setModel(model);
                }

            }
        } else if (studyPattern == StudyPattern.Speed_Pattern) {
            nerveManager.insertModelParameter(modelParameter);
        }
        if (isHavePosition) {
            if (modelParameter.getFrame() != null) {
                frame = modelParameter.getFrame();
            }
            //边框K均值模型注入
            Map<Integer, KBorder> borderMap = modelParameter.getBorderMap();
            if (borderMap != null && borderMap.size() > 0) {
                boxReady = true;
                for (Map.Entry<Integer, KBorder> entry : borderMap.entrySet()) {
                    int key = entry.getKey();
                    KClustering kClustering = kClusteringMap.get(key);
                    KBorder kBorder = entry.getValue();
                    List<List<Double>> lists = kBorder.getLists();
                    Map<Integer, BoxList> boxListMap = kBorder.getPositionMap();
                    if (lists != null && boxListMap != null && lists.size() > 0 && boxListMap.size() > 0) {
                        kClustering.setReady(true);
                        Matrix[] matrices = new Matrix[lists.size()];
                        Map<Integer, Box> boxMap = kClustering.getPositionMap();
                        for (int i = 0; i < lists.size(); i++) {
                            Matrix matrix = listToRowVector(lists.get(i));
                            matrices[i] = matrix;
                        }
                        kClustering.setMatrices(matrices);
                        kClustering.setLength(kBorder.getLength());
                        kClustering.setSpeciesQuantity(kBorder.getSpeciesQuantity());
                        for (Map.Entry<Integer, BoxList> boxEntry : boxListMap.entrySet()) {
                            Box box = new Box();
                            BoxList boxList = boxEntry.getValue();
                            box.setMatrix(listToRowVector(boxList.getList()));
                            box.setMatrixPosition(listToRowVector(boxList.getPositionList()));
                            boxMap.put(boxEntry.getKey(), box);
                        }
                    }

                }
            }
        }
    }

    public void setCutThreshold(double cutThreshold) {
        this.cutThreshold = cutThreshold;
    }

    public int getDeep() {
        return deep;
    }

    public void setDeep(int deep) {
        this.deep = deep;
    }

    public int getClassificationNub() {
        return classificationNub;
    }

    public void setClassificationNub(int classificationNub) {
        this.classificationNub = classificationNub;
    }

}
