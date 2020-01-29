package org.wlld.imageRecognition;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.config.StudyPattern;
import org.wlld.function.ReLu;
import org.wlld.function.Sigmod;
import org.wlld.imageRecognition.border.BorderBody;
import org.wlld.imageRecognition.border.Frame;
import org.wlld.nerveCenter.NerveManager;
import org.wlld.nerveEntity.ModelParameter;
import org.wlld.nerveEntity.SensoryNerve;
import org.wlld.tools.ArithUtil;

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
    private Map<Integer, BorderBody> borderBodyMap = new HashMap<>();//border特征集合
    private Frame frame;//先验边框
    private double th = 0.6;//标准阈值
    private boolean boxReady = false;//边框已经学习完毕
    private double iouTh = 0.5;//IOU阈值

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

    public Map<Integer, BorderBody> getBorderBodyMap() {
        return borderBodyMap;
    }

    private void border(BorderBody borderBody) throws Exception {
        Matrix parameter = borderBody.getX();//参数矩阵
        Matrix tx = borderBody.getTx();
        Matrix ty = borderBody.getTy();
        Matrix th = borderBody.getTh();
        Matrix tw = borderBody.getTw();
        borderBody.setxW(MatrixOperation.getLinearRegression(parameter, tx));
        borderBody.setyW(MatrixOperation.getLinearRegression(parameter, ty));
        borderBody.setwW(MatrixOperation.getLinearRegression(parameter, tw));
        borderBody.sethW(MatrixOperation.getLinearRegression(parameter, th));
        boxReady = true;
    }

    public void boxStudy() throws Exception {//边框回归 学习结束之后最后进行调用
        if (borderBodyMap.size() > 0) {
            for (Map.Entry<Integer, BorderBody> entry : borderBodyMap.entrySet()) {
                border(entry.getValue());
            }
        } else {
            throw new Exception("boder not study");
        }

    }

    public void setBorderBodyMap(Map<Integer, BorderBody> borderBodyMap) {
        this.borderBodyMap = borderBodyMap;
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
        Map<Integer, Matrix> matrixMap = new HashMap<>();//主键与期望矩阵的映射
        while (width > 5 && height > 5) {
            width = width / 3;
            height = height / 3;
            deep++;
        }

        //加载各识别分类的期望矩阵
        double nub = ArithUtil.div(10, classificationNub);//每个分类期望参数的跨度
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
        initNerveManager(initPower, width * height, 2);
        convolutionNerveManager.setMatrixMap(matrixMap);//给卷积网络管理器注入期望矩阵
        convolutionNerveManager.init(initPower, true);
    }

    public ModelParameter getModel() throws Exception {//获取模型参数
        ModelParameter modelParameter = nerveManager.getModelParameter();
        if (studyPattern == StudyPattern.Accuracy_Pattern) {
            ModelParameter modelParameter1 = convolutionNerveManager.getModelParameter();
            modelParameter.setDymNerveStudies(modelParameter1.getDymNerveStudies());
            modelParameter.setDymOutNerveStudy(modelParameter1.getDymOutNerveStudy());
        }
        if (isHavePosition) {//存在边框学习模型参数
            modelParameter.setBorderBodyMap(borderBodyMap);
            modelParameter.setFrame(frame);
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

    public double getCutThreshold() {
        return cutThreshold;
    }

    //注入模型参数
    public void insertModel(ModelParameter modelParameter) throws Exception {
        nerveManager.insertModelParameter(modelParameter);
        if (studyPattern == StudyPattern.Accuracy_Pattern) {
            convolutionNerveManager.insertModelParameter(modelParameter);
        }
        Frame frame = modelParameter.getFrame();
        Map<Integer, BorderBody> borderBodyMap = modelParameter.getBorderBodyMap();
        if (frame != null && borderBodyMap != null && borderBodyMap.size() > 0) {
            isHavePosition = true;
            this.frame = frame;
            this.borderBodyMap = borderBodyMap;
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
