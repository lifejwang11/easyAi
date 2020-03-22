package org.wlld.imageRecognition;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.config.Classifier;
import org.wlld.config.RZ;
import org.wlld.config.StudyPattern;
import org.wlld.function.ReLu;
import org.wlld.function.Sigmod;
import org.wlld.function.Tanh;
import org.wlld.i.ActiveFunction;
import org.wlld.imageRecognition.border.*;
import org.wlld.imageRecognition.modelEntity.BoxList;
import org.wlld.imageRecognition.modelEntity.KBorder;
import org.wlld.imageRecognition.modelEntity.LvqModel;
import org.wlld.imageRecognition.modelEntity.MatrixModel;
import org.wlld.nerveCenter.NerveManager;
import org.wlld.nerveCenter.Normalization;
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
    private NerveManager convolutionNerveManagerR;//R卷积神经网络管理器
    private NerveManager convolutionNerveManagerG;//G卷积神经网络管理器
    private NerveManager convolutionNerveManagerB;//B卷积神经网络管理器
    private boolean isAccurate = false;//是否保留精度
    private int row = 5;//行的最小比例
    private int column = 3;//列的最小比例
    private int deep = 2;//默认深度
    private int classificationNub = 2;//分类的数量
    private int studyPattern;//学习模式
    private boolean isHavePosition = false;//是否需要锁定物体位置
    private LVQ lvq;//模型需要返回,精准模式下的原型聚类
    private Frame frame;//先验边框
    private double th = 0.6;//标准阈值
    private boolean boxReady = false;//边框已经学习完毕
    private double iouTh = 0.5;//IOU阈值
    private int lvqNub = 10;//lvq循环次数，默认30
    private VectorK vectorK;//特征向量均值类
    private boolean isThreeChannel = false;//是否启用三通道
    private int classifier = Classifier.VAvg;//默认分类类别使用的是向量均值分类
    private Normalization normalization = new Normalization();//统一归一化
    private double avg = 0;//覆盖均值
    private int sensoryNerveNub;//输入神经元个数
    private boolean isShowLog = false;
    private ActiveFunction activeFunction = new Tanh();
    private double studyPoint = 0;
    private double matrixWidth = 5;//期望矩阵间隔
    private int rzType = RZ.NOT_RZ;//正则化类型，默认不进行正则化
    private double lParam = 0;//正则参数
    private int hiddenNerveNub = 9;//隐层神经元个数

    public void setHiddenNerveNub(int hiddenNerveNub) {//设置隐层宽度
        this.hiddenNerveNub = hiddenNerveNub;
    }

    public void setRzType(int rzType) {//设置正则化函数
        this.rzType = rzType;
    }

    public void setlParam(double lParam) {//设置正则参数
        this.lParam = lParam;
    }

    public void setMatrixWidth(double matrixWidth) {//设置卷积层正则参数
        this.matrixWidth = matrixWidth;
    }

    public void setStudyPoint(double studyPoint) {//设置学习率
        this.studyPoint = studyPoint;
    }

    public void setActiveFunction(ActiveFunction activeFunction) {//设置激活函数
        this.activeFunction = activeFunction;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public Normalization getNormalization() {//获取归一化类
        return normalization;
    }

    public TempleConfig() {
    }

    //边框聚类集合 模型需要返回
    public TempleConfig(boolean isThreeChannel, boolean isAccurate) {
        this.isThreeChannel = isThreeChannel;
        this.isAccurate = isAccurate;
    }

    public int getClassifier() {
        return classifier;
    }

    public void setClassifier(int classifier) {//设置最后使用的分类器
        this.classifier = classifier;
    }

    public VectorK getVectorK() {//获取均值特征矩阵
        return vectorK;
    }

    public NerveManager getConvolutionNerveManagerR() {
        return convolutionNerveManagerR;
    }

    public NerveManager getConvolutionNerveManagerG() {
        return convolutionNerveManagerG;
    }

    public NerveManager getConvolutionNerveManagerB() {
        return convolutionNerveManagerB;
    }

    public void finishStudy() throws Exception {//结束学习
        switch (classifier) {
            case Classifier.LVQ:
                lvq.start();
                break;
            case Classifier.VAvg:
                vectorK.study();
                break;
        }
        if (isHavePosition) {
            for (Map.Entry<Integer, KClustering> entry : kClusteringMap.entrySet()) {
                entry.getValue().start();
            }
            boxReady = true;
        }

    }

    public void isShowLog(boolean isShowLog) {//是否打印学习数据
        this.isShowLog = isShowLog;
    }

    public boolean isShowLog() {
        return isShowLog;
    }

    public void startLvq() throws Exception {
        switch (classifier) {
            case Classifier.LVQ:
                lvq.start();
                break;
            case Classifier.VAvg:
                vectorK.study();
                break;
        }
        if (isHavePosition) {
            for (Map.Entry<Integer, KClustering> entry : kClusteringMap.entrySet()) {
                entry.getValue().start();
            }
            boxReady = true;
        }
    }

    private Map<Integer, KClustering> kClusteringMap = new HashMap<>();

    public Map<Integer, KClustering> getKClusteringMap() {
        return kClusteringMap;
    }

    public void setLvqNub(int lvqNub) {//设置LVQ循环次数
        this.lvqNub = lvqNub;
    }

    public double getIouTh() {
        return iouTh;
    }

    public void setIouTh(double iouTh) {//设置IOU阈值
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

    public void setFrame(Frame frame) {//设置视窗
        this.frame = frame;
    }

    public LVQ getLvq() {
        return lvq;
    }

    public NerveManager getNerveManager() {
        return nerveManager;
    }

    public boolean isHavePosition() {
        return isHavePosition;
    }

    public void setHavePosition(boolean havePosition) {//设置定位服务
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
            case StudyPattern.Cover_Pattern://覆盖学习模式
                initNerveManager(initPower, 9, deep, studyPoint);
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
        initNerveManager(initPower, row * column, deep, studyPoint);
    }

    private void initNerveManager(boolean initPower, int sensoryNerveNub
            , int deep, double studyPoint) throws Exception {
        nerveManager = new NerveManager(sensoryNerveNub, hiddenNerveNub,
                classificationNub, deep, activeFunction,
                false, isAccurate, studyPoint, rzType, lParam);
        nerveManager.init(initPower, false, isShowLog);
    }

    private void initConvolutionVision(boolean initPower, int width, int height) throws Exception {//精准模式
        int deep = 0;
        Map<Integer, Matrix> matrixMap = new HashMap<>();//主键与期望矩阵的映射
        while (width > 5 && height > 5) {
            width = width / 3;
            height = height / 3;
            deep++;
        }
        switch (classifier) {
            case Classifier.DNN:
                int nub = height * width;
                if (isThreeChannel) {
                    nub = nub * 3;
                }
                initNerveManager(true, nub, this.deep, studyPoint);
                break;
            case Classifier.LVQ:
                lvq = new LVQ(classificationNub, lvqNub);
                break;
            case Classifier.VAvg:
                sensoryNerveNub = height * width;
                vectorK = new VectorK(sensoryNerveNub);
                break;
        }
        //加载各识别分类的期望矩阵
        matrixMap.put(0, new Matrix(height, width));
        for (int k = 1; k <= classificationNub; k++) {
            Matrix matrix = new Matrix(height, width);//初始化期望矩阵
            double t = k * matrixWidth;//期望矩阵的分类参数数值
            for (int i = 0; i < height; i++) {//给期望矩阵注入期望参数
                for (int j = 0; j < width; j++) {
                    matrix.setNub(i, j, t);
                }
            }
            matrixMap.put(k, matrix);
        }
        if (!isThreeChannel) {
            convolutionNerveManager = initNerveManager(matrixMap, initPower, deep);
        } else {//启用三通道
            convolutionNerveManagerR = initNerveManager(matrixMap, initPower, deep);
            convolutionNerveManagerG = initNerveManager(matrixMap, initPower, deep);
            convolutionNerveManagerB = initNerveManager(matrixMap, initPower, deep);
        }
    }

    private NerveManager initNerveManager(Map<Integer, Matrix> matrixMap, boolean initPower, int deep) throws Exception {
        //初始化卷积神经网络
        NerveManager convolutionNerveManager = new NerveManager(1, 1,
                1, deep - 1, new ReLu(),
                true, isAccurate, studyPoint, rzType, lParam);
        convolutionNerveManager.setMatrixMap(matrixMap);//给卷积网络管理器注入期望矩阵
        convolutionNerveManager.init(initPower, true, isShowLog);
        return convolutionNerveManager;
    }

    private List<MatrixModel> getLvqModel(MatrixBody[] matrixBodies) throws Exception {
        List<MatrixModel> matrixModelList = new ArrayList<>();
        for (int i = 0; i < matrixBodies.length; i++) {
            MatrixBody matrixBody = matrixBodies[i];
            Matrix matrix = matrixBody.getMatrix();
            MatrixModel matrixModel = new MatrixModel();
            List<Double> rowVector = MatrixOperation.rowVectorToList(matrix);
            matrixModel.setId(matrixBody.getId());
            matrixModel.setRowVector(rowVector);
            matrixModelList.add(matrixModel);
        }
        return matrixModelList;
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
                    List<Double> furList = MatrixOperation.rowVectorToList(box.getMatrix());
                    List<Double> borderList = MatrixOperation.rowVectorToList(box.getMatrixPosition());
                    boxList.setList(furList);
                    boxList.setPositionList(borderList);
                    myPosition.put(entryBox.getKey(), boxList);
                }
                for (int i = 0; i < matrices.length; i++) {
                    lists.add(MatrixOperation.rowVectorToList(matrices[i]));
                }

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
            switch (classifier) {
                case Classifier.LVQ:
                    if (lvq.isReady()) {
                        LvqModel lvqModel = new LvqModel();
                        lvqModel.setLength(lvq.getLength());
                        lvqModel.setTypeNub(lvq.getTypeNub());
                        lvqModel.setMatrixModelList(getLvqModel(lvq.getModel()));
                        modelParameter.setLvqModel(lvqModel);
                    }
                    break;
                case Classifier.VAvg:
                    if (vectorK != null) {
                        Map<Integer, List<Double>> map = vectorK.getKMatrix();
                        modelParameter.setMatrixK(map);
                    }
                    break;
                case Classifier.DNN:
                    if (normalization != null) {
                        modelParameter.setDnnAvg(normalization.getAvg());
                    }
                    ModelParameter modelParameter2 = nerveManager.getModelParameter();
                    modelParameter.setDepthNerves(modelParameter2.getDepthNerves());
                    modelParameter.setOutNerves(modelParameter2.getOutNerves());
                    break;
            }

        } else if (studyPattern == StudyPattern.Cover_Pattern) {
            ModelParameter modelParameter1 = nerveManager.getModelParameter();
            modelParameter.setDepthNerves(modelParameter1.getDepthNerves());
            modelParameter.setOutNerves(modelParameter1.getOutNerves());
            modelParameter1.setAvg(avg);
        }
        if (isHavePosition && kClusteringMap != null && kClusteringMap.size() > 0) {//存在边框学习模型参数
            Map<Integer, KBorder> kBorderMap = kToBody();
            modelParameter.setFrame(frame);
            modelParameter.setBorderMap(kBorderMap);
        }
        return modelParameter;
    }

    public List<SensoryNerve> getSensoryNerves() {//获取感知神经元
        return nerveManager.getSensoryNerves();
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

    //注入模型参数
    public void insertModel(ModelParameter modelParameter) throws Exception {
        if (studyPattern == StudyPattern.Accuracy_Pattern) {
            convolutionNerveManager.insertModelParameter(modelParameter);
            //LVQ模型参数注入
            switch (classifier) {
                case Classifier.LVQ:
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
                                matrixBody.setMatrix(MatrixOperation.listToRowVector(matrixModel.getRowVector()));
                                model[i] = matrixBody;
                            }
                            lvq.setLength(length);
                            lvq.setTypeNub(typeNub);
                            lvq.setReady(true);
                            lvq.setModel(model);
                        }
                    }
                    break;
                case Classifier.VAvg:
                    vectorK = new VectorK(sensoryNerveNub);
                    vectorK.insertKMatrix(modelParameter.getMatrixK());
                    break;
                case Classifier.DNN:
                    nerveManager.insertModelParameter(modelParameter);
                    normalization = new Normalization();
                    normalization.setAvg(modelParameter.getDnnAvg());
                    break;
            }

        } else if (studyPattern == StudyPattern.Speed_Pattern) {
            nerveManager.insertModelParameter(modelParameter);
        } else if (studyPattern == StudyPattern.Cover_Pattern) {
            nerveManager.insertModelParameter(modelParameter);
            avg = modelParameter.getAvg();
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
                            Matrix matrix = MatrixOperation.listToRowVector(lists.get(i));
                            matrices[i] = matrix;
                        }
                        kClustering.setMatrices(matrices);
                        kClustering.setLength(kBorder.getLength());
                        kClustering.setSpeciesQuantity(kBorder.getSpeciesQuantity());
                        for (Map.Entry<Integer, BoxList> boxEntry : boxListMap.entrySet()) {
                            Box box = new Box();
                            BoxList boxList = boxEntry.getValue();
                            box.setMatrix(MatrixOperation.listToRowVector(boxList.getList()));
                            box.setMatrixPosition(MatrixOperation.listToRowVector(boxList.getPositionList()));
                            boxMap.put(boxEntry.getKey(), box);
                        }
                    }
                }
            }
        }
    }

    public void setDeep(int deep) {
        this.deep = deep;
    }

}
