package org.wlld.config;

public class TfConfig {
    private int maxLength = 25;//最大语句长度
    private int multiNumber = 8;//多头数量
    private int featureDimension = 50;//词向量维度
    private int allDepth = 6;//深度
    private double studyPoint = 0.001;
    private int typeNumber;
    private boolean showLog = true;
    private int times = 10;//循环增强次数

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMultiNumber() {
        return multiNumber;
    }

    public void setMultiNumber(int multiNumber) {
        this.multiNumber = multiNumber;
    }

    public int getFeatureDimension() {
        return featureDimension;
    }

    public void setFeatureDimension(int featureDimension) {
        this.featureDimension = featureDimension;
    }

    public int getAllDepth() {
        return allDepth;
    }

    public void setAllDepth(int allDepth) {
        this.allDepth = allDepth;
    }

    public double getStudyPoint() {
        return studyPoint;
    }

    public void setStudyPoint(double studyPoint) {
        this.studyPoint = studyPoint;
    }

    public int getTypeNumber() {
        return typeNumber;
    }

    public void setTypeNumber(int typeNumber) {
        this.typeNumber = typeNumber;
    }

    public boolean isShowLog() {
        return showLog;
    }

    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
    }
}