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
    private float studyRate = 0.0005f;//学习率
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
    private int minFeatureValue = 5;//输出特征维度大小
    private float GMaxTh = 1f;//梯度裁剪阈值
    private float layGMaxTh = 10000f;//层梯度裁剪阈值
    private float trustTh = 0.1f;//可信度阈值
    private int hiddenDeep = 1;//分类网络线性层隐层神经元深度
    private int batchSize = 32;//批量训练数量
    private float otherPth = 0.1f;//负样本阈值
    private float backGroundPD = 0.1f;//背景误差惩罚，该数值大于0.9 则相当于不施加惩罚
    private int typeDcnDeep = 0;//分类网络 使用可变形卷积深度，0为不可用
    private boolean powerTrust = false;//是否用概率值做可信度
    private boolean softNMS = false;//是否使用软nms
    private float softNMSTh = 0.5f;//如果使用软nms则消除nms阈值

    public boolean isSoftNMS() {
        return softNMS;
    }

    public void setSoftNMS(boolean softNMS) {
        this.softNMS = softNMS;
    }

    public float getSoftNMSTh() {
        return softNMSTh;
    }

    public void setSoftNMSTh(float softNMSTh) {
        this.softNMSTh = softNMSTh;
    }

    public boolean isPowerTrust() {
        return powerTrust;
    }

    public void setPowerTrust(boolean powerTrust) {
        this.powerTrust = powerTrust;
    }

    public int getTypeDcnDeep() {
        return typeDcnDeep;
    }

    public void setTypeDcnDeep(int typeDcnDeep) {
        this.typeDcnDeep = typeDcnDeep;
    }

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

    public int getMinFeatureValue() {
        return minFeatureValue;
    }

    public void setMinFeatureValue(int minFeatureValue) {
        this.minFeatureValue = minFeatureValue;
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

}
