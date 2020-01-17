package org.wlld.imageRecognition;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.config.StudyPattern;
import org.wlld.function.ReLu;
import org.wlld.function.Sigmod;
import org.wlld.i.OutBack;
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
    private OutBack outBack;

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
        nerveManager.init(initPower, false, null);
        nerveManager.setOutBack(outBack);
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
        convolutionNerveManager.init(initPower, true, nerveManager);
    }

    public ModelParameter getModel() {//获取模型参数
        return nerveManager.getModelParameter();
    }

    public List<SensoryNerve> getSensoryNerves() {//获取感知神经元
        return nerveManager.getSensoryNerves();
    }

    public void setStudy(double studyPoint) throws Exception {//设置学习率
        nerveManager.setStudyPoint(studyPoint);
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
    public void insertModel(ModelParameter modelParameter) {
        nerveManager.insertModelParameter(modelParameter);
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

    public OutBack getOutBack() {
        return outBack;
    }

    public void setOutBack(OutBack outBack) {
        this.outBack = outBack;
    }
}
