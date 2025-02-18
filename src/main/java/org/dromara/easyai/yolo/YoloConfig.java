package org.dromara.easyai.yolo;

import org.dromara.easyai.config.RZ;

public class YoloConfig {
    private int windowWidth = 90;//检测窗口宽
    private int windowHeight = 140;//检测窗口高
    private int typeNub = 10;//类别数量
    private int hiddenNerveNub = 16;//线性层隐层神经元数量
    private float lineStudy = 0.01f;//线性层学习率
    private int kernelSize = 3;//卷积核尺寸
    private boolean showLog = false;//是否打印学习过程中的log
    private float convStudy = 0.01f;//卷积层学习率
    private int enhance = 1;//数据增强
    private float iouTh = 0.05f;//合并框交并比阈值
    private float containIouTh = 0.15f;//是否包含样本交并比阈值
    private float pth = 0.4f;//可信概率阈值
    private float stepReduce = 0.25f;//训练步长收缩系数
    private float checkStepReduce = 0.5f;//检测步长收缩系数
    private float regular = 0;//正则系数
    private int regularModel = RZ.NOT_RZ;//正则模式
    private int coreNumber = 1;//是否使用多核并行计算进行提速

    public int getCoreNumber() {
        return coreNumber;
    }

    public void setCoreNumber(int coreNumber) {
        this.coreNumber = coreNumber;
    }

    public int getRegularModel() {
        return regularModel;
    }

    public void setRegularModel(int regularModel) {
        this.regularModel = regularModel;
    }

    public float getStepReduce() {
        return stepReduce;
    }

    public void setStepReduce(float stepReduce) {
        this.stepReduce = stepReduce;
    }

    public float getRegular() {
        return regular;
    }

    public void setRegular(float regular) {
        this.regular = regular;
    }

    public float getCheckStepReduce() {
        return checkStepReduce;
    }

    public void setCheckStepReduce(float checkStepReduce) {
        this.checkStepReduce = checkStepReduce;
    }

    public float getPth() {
        return pth;
    }

    public void setPth(float pth) {
        this.pth = pth;
    }

    public float getContainIouTh() {
        return containIouTh;
    }

    public void setContainIouTh(float containIouTh) {
        this.containIouTh = containIouTh;
    }

    public float getIouTh() {
        return iouTh;
    }

    public void setIouTh(float iouTh) {
        this.iouTh = iouTh;
    }


    public int getEnhance() {
        return enhance;
    }

    public void setEnhance(int enhance) {
        this.enhance = enhance;
    }

    public int getKernelSize() {
        return kernelSize;
    }

    public void setKernelSize(int kernelSize) {
        this.kernelSize = kernelSize;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
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

    public float getLineStudy() {
        return lineStudy;
    }

    public void setLineStudy(float lineStudy) {
        this.lineStudy = lineStudy;
    }

    public boolean isShowLog() {
        return showLog;
    }

    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
    }

    public float getConvStudy() {
        return convStudy;
    }

    public void setConvStudy(float convStudy) {
        this.convStudy = convStudy;
    }
}
