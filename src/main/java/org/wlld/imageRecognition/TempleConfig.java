package org.wlld.imageRecognition;

import org.wlld.i.OutBack;
import org.wlld.nerveCenter.NerveManager;
import org.wlld.nerveEntity.ModelParameter;
import org.wlld.nerveEntity.SensoryNerve;

import java.util.List;


public class TempleConfig {
    private NerveManager nerveManager;//神经网络管理器
    public double cutThreshold = 10;//切割阈值默认值
    public int row = 5;//行的最小比例
    public int column = 3;//列的最小比例
    public int deep = 2;//默认深度
    public int classificationNub = 1;//分类的数量
    private OutBack outBack;

    public void initNerveManager(boolean initPower) throws Exception {//初始化神经网络
        if (row >= 5 || column >= 5) {
            nerveManager = new NerveManager(row * column, 6, classificationNub, deep);
            nerveManager.init(initPower);
            nerveManager.setOutBack(outBack);
        } else {
            throw new Exception("row or column is too min");
        }
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

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
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
