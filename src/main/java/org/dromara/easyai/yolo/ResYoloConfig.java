package org.dromara.easyai.yolo;

import org.dromara.easyai.config.RZ;

/**
 * @author lidapeng
 * @time 2026/1/20 10:06
 * @des 基于resnet做分类网络的 配置类
 */
public class ResYoloConfig {
    private int windowSize = 100;//检测窗口宽
    private int typeNub = 10;//类别数量
    private int hiddenNerveNub = 16;//线性层隐层神经元数量
    private int positionHiddenNerveNub = 16;//位置网络线性层隐层神经元数量
    private float studyRate = 0.0001f;//学习率
    private float positionStudyRate = 0.0025f;
    private boolean showLog = false;//是否打印学习过程中的log
    private int enhance = 1;//数据增强
    private float iouTh = 0.05f;//合并框交并比阈值
    private float containIouTh = 0.25f;//是否包含样本交并比阈值
    private float pth = 0.4f;//可信概率阈值
    private float stepReduce = 0.25f;//训练步长收缩系数
    private float checkStepReduce = 0.5f;//检测步长收缩系数
    private float regular = 0;//正则系数
    private int regularModel = RZ.NOT_RZ;//正则模式
    private int channelNo = 1;//通道数
    private int positionChannelNo = 1;//位置网络通道数
    private int minFeatureValue = 5;//输出特征维度大小
    private int positionMinFeatureValue = 3;//位置网络输出特征纬度大小
    private float GMaxTh = 1f;//梯度裁剪阈值
    private float layGMaxTh = 10000f;//层梯度裁剪阈值
    private float positionGMaxTh = 100;//位置梯度裁剪阈值
    private float trustTh = 0.1f;//可信度阈值
    private int hiddenDeep = 1;//分类网络线性层隐层神经元深度
    private int positionHiddenDeep = 1;//位置网络的线性层神经元深度
    private int batchSize = 32;//批量训练数量
    private float otherPth = 0.1f;//负样本阈值
    private float backGroundPD = 0.1f;//背景误差惩罚，该数值大于0.9 则相当于不施加惩罚

    public float getLayGMaxTh() {
        return layGMaxTh;
    }

    public void setLayGMaxTh(float layGMaxTh) {
        this.layGMaxTh = layGMaxTh;
    }

    public float getBackGroundPD() {
        return backGroundPD;
    }

    public void setBackGroundPD(float backGroundPD) {
        this.backGroundPD = backGroundPD;
    }

    public float getOtherPth() {
        return otherPth;
    }

    public void setOtherPth(float otherPth) {
        this.otherPth = otherPth;
    }

    public float getPositionGMaxTh() {
        return positionGMaxTh;
    }

    public void setPositionGMaxTh(float positionGMaxTh) {
        this.positionGMaxTh = positionGMaxTh;
    }

    public float getPositionStudyRate() {
        return positionStudyRate;
    }

    public void setPositionStudyRate(float positionStudyRate) {
        this.positionStudyRate = positionStudyRate;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public int getTypeNub() {
        return typeNub;
    }

    public void setTypeNub(int typeNub) {
        this.typeNub = typeNub;
    }

    public int getHiddenNerveNub() {
        return hiddenNerveNub;
    }

    public void setHiddenNerveNub(int hiddenNerveNub) {
        this.hiddenNerveNub = hiddenNerveNub;
    }

    public int getPositionHiddenNerveNub() {
        return positionHiddenNerveNub;
    }

    public void setPositionHiddenNerveNub(int positionHiddenNerveNub) {
        this.positionHiddenNerveNub = positionHiddenNerveNub;
    }

    public float getStudyRate() {
        return studyRate;
    }

    public void setStudyRate(float studyRate) {
        this.studyRate = studyRate;
    }

    public boolean isShowLog() {
        return showLog;
    }

    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
    }

    public int getEnhance() {
        return enhance;
    }

    public void setEnhance(int enhance) {
        this.enhance = enhance;
    }

    public float getIouTh() {
        return iouTh;
    }

    public void setIouTh(float iouTh) {
        this.iouTh = iouTh;
    }

    public float getContainIouTh() {
        return containIouTh;
    }

    public void setContainIouTh(float containIouTh) {
        this.containIouTh = containIouTh;
    }

    public float getPth() {
        return pth;
    }

    public void setPth(float pth) {
        this.pth = pth;
    }

    public float getStepReduce() {
        return stepReduce;
    }

    public void setStepReduce(float stepReduce) {
        this.stepReduce = stepReduce;
    }

    public float getCheckStepReduce() {
        return checkStepReduce;
    }

    public void setCheckStepReduce(float checkStepReduce) {
        this.checkStepReduce = checkStepReduce;
    }

    public float getRegular() {
        return regular;
    }

    public void setRegular(float regular) {
        this.regular = regular;
    }

    public int getRegularModel() {
        return regularModel;
    }

    public void setRegularModel(int regularModel) {
        this.regularModel = regularModel;
    }

    public int getChannelNo() {
        return channelNo;
    }

    public void setChannelNo(int channelNo) {
        this.channelNo = channelNo;
    }

    public int getPositionChannelNo() {
        return positionChannelNo;
    }

    public void setPositionChannelNo(int positionChannelNo) {
        this.positionChannelNo = positionChannelNo;
    }

    public int getMinFeatureValue() {
        return minFeatureValue;
    }

    public void setMinFeatureValue(int minFeatureValue) {
        this.minFeatureValue = minFeatureValue;
    }

    public int getPositionMinFeatureValue() {
        return positionMinFeatureValue;
    }

    public void setPositionMinFeatureValue(int positionMinFeatureValue) {
        this.positionMinFeatureValue = positionMinFeatureValue;
    }

    public float getGMaxTh() {
        return GMaxTh;
    }

    public void setGMaxTh(float GMaxTh) {
        this.GMaxTh = GMaxTh;
    }


    public float getTrustTh() {
        return trustTh;
    }

    public void setTrustTh(float trustTh) {
        this.trustTh = trustTh;
    }

    public int getHiddenDeep() {
        return hiddenDeep;
    }

    public void setHiddenDeep(int hiddenDeep) {
        this.hiddenDeep = hiddenDeep;
    }

    public int getPositionHiddenDeep() {
        return positionHiddenDeep;
    }

    public void setPositionHiddenDeep(int positionHiddenDeep) {
        this.positionHiddenDeep = positionHiddenDeep;
    }
}
