package org.wlld.config;


import org.wlld.entity.SentenceModel;

/**
 */
public class SentenceConfig {
    private SentenceModel sentenceModel;
    private int typeNub = 11;//分类数量
    private int wordVectorDimension = 21;//词向量嵌入维度
    private int maxWordLength = 20;//最长字数
    private double weStudyPoint = 0.01;//词向量学习学习率
    private double weLParam = 0.001;//词向量正则系数
    private boolean showLog = true;
    private int minLength = 4;
    private double trustPowerTh = 0.5;//权重可信任阈值
    private int keyWordNerveDeep = 6;

    public int getKeyWordNerveDeep() {
        return keyWordNerveDeep;
    }

    public void setKeyWordNerveDeep(int keyWordNerveDeep) {
        this.keyWordNerveDeep = keyWordNerveDeep;
    }

    public SentenceModel getSentenceModel() {
        return sentenceModel;
    }

    public void setSentenceModel(SentenceModel sentenceModel) {
        this.sentenceModel = sentenceModel;
    }

    public int getTypeNub() {
        return typeNub;
    }

    public void setTypeNub(int typeNub) {
        this.typeNub = typeNub;
    }

    public int getWordVectorDimension() {
        return wordVectorDimension;
    }

    public void setWordVectorDimension(int wordVectorDimension) {
        this.wordVectorDimension = wordVectorDimension;
    }

    public int getMaxWordLength() {
        return maxWordLength;
    }

    public void setMaxWordLength(int maxWordLength) {
        this.maxWordLength = maxWordLength;
    }

    public double getWeStudyPoint() {
        return weStudyPoint;
    }

    public void setWeStudyPoint(double weStudyPoint) {
        this.weStudyPoint = weStudyPoint;
    }

    public double getWeLParam() {
        return weLParam;
    }

    public void setWeLParam(double weLParam) {
        this.weLParam = weLParam;
    }

    public boolean isShowLog() {
        return showLog;
    }

    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public double getTrustPowerTh() {
        return trustPowerTh;
    }

    public void setTrustPowerTh(double trustPowerTh) {
        this.trustPowerTh = trustPowerTh;
    }
}
