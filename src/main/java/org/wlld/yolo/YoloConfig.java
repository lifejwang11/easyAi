package org.wlld.yolo;

import org.wlld.config.RZ;

public class YoloConfig {
    private int windowWidth = 90;//检测窗口宽
    private int windowHeight = 140;//检测窗口高
    private int typeNub = 10;//类别数量
    private int hiddenNerveNub = 16;//线性层隐层神经元数量
    private double lineStudy = 0.01;//线性层学习率
    private int kernelSize = 3;//卷积核尺寸
    private boolean showLog = false;//是否打印学习过程中的log
    private double convStudy = 0.01;//卷积层学习率
    private int enhance = 1;//数据增强
    private double iouTh = 0.05;//合并框交并比阈值
    private double containIouTh = 0.15;//是否包含样本交并比阈值
    private double pth = 0.4;//可信概率阈值
    private double stepReduce = 0.25;//训练步长收缩系数
    private double checkStepReduce = 0.5;//检测步长收缩系数
    private double regular = 0;//正则系数
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

    public double getStepReduce() {
        return stepReduce;
    }

    public void setStepReduce(double stepReduce) {
        this.stepReduce = stepReduce;
    }

    public double getRegular() {
        return regular;
    }

    public void setRegular(double regular) {
        this.regular = regular;
    }

    public double getCheckStepReduce() {
        return checkStepReduce;
    }

    public void setCheckStepReduce(double checkStepReduce) {
        this.checkStepReduce = checkStepReduce;
    }

    public double getPth() {
        return pth;
    }

    public void setPth(double pth) {
        this.pth = pth;
    }

    public double getContainIouTh() {
        return containIouTh;
    }

    public void setContainIouTh(double containIouTh) {
        this.containIouTh = containIouTh;
    }

    public double getIouTh() {
        return iouTh;
    }

    public void setIouTh(double iouTh) {
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

    public double getLineStudy() {
        return lineStudy;
    }

    public void setLineStudy(double lineStudy) {
        this.lineStudy = lineStudy;
    }

    public boolean isShowLog() {
        return showLog;
    }

    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
    }

    public double getConvStudy() {
        return convStudy;
    }

    public void setConvStudy(double convStudy) {
        this.convStudy = convStudy;
    }
}
