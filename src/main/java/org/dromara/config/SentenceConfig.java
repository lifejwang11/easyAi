package org.dromara.config;


/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 参数配置
 */
public class SentenceConfig {
    private int typeNub = 11;//分类数量
    private int wordVectorDimension = 21;//语义分类网络词向量嵌入维度
    private int qaWordVectorDimension = 66;//问答模型此项链嵌入维度
    private int maxWordLength = 20;//最长字数
    private double weStudyPoint = 0.01;//词向量学习学习率
    private double weLParam = 0.01;//正则系数
    private int rzModel = RZ.NOT_RZ;//正则模式
    private boolean showLog = true;
    private int minLength = 5;
    private double trustPowerTh = 0.5;//权重可信任阈值
    private int maxAnswerLength = 20;//最大回答长度
    private double sentenceTrustPowerTh = 0.2;//语句生成可信赖阈值
    private int times = 10;//增加训练数量
    private double param = 0.01;//线性层正则系数
    private int keyWordNerveDeep = 3;

    public int getRzModel() {
        return rzModel;
    }

    public void setRzModel(int rzModel) {
        this.rzModel = rzModel;
    }

    public int getQaWordVectorDimension() {
        return qaWordVectorDimension;
    }

    public void setQaWordVectorDimension(int qaWordVectorDimension) {
        this.qaWordVectorDimension = qaWordVectorDimension;
    }

    public int getKeyWordNerveDeep() {
        return keyWordNerveDeep;
    }

    public void setKeyWordNerveDeep(int keyWordNerveDeep) {
        this.keyWordNerveDeep = keyWordNerveDeep;
    }

    public double getParam() {
        return param;
    }

    public void setParam(double param) {
        this.param = param;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public double getSentenceTrustPowerTh() {
        return sentenceTrustPowerTh;
    }

    public void setSentenceTrustPowerTh(double sentenceTrustPowerTh) {
        this.sentenceTrustPowerTh = sentenceTrustPowerTh;
    }

    public int getMaxAnswerLength() {
        return maxAnswerLength;
    }

    public void setMaxAnswerLength(int maxAnswerLength) {
        this.maxAnswerLength = maxAnswerLength;
    }

    public double getTrustPowerTh() {
        return trustPowerTh;
    }

    public void setTrustPowerTh(double trustPowerTh) {
        this.trustPowerTh = trustPowerTh;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxWordLength() {
        return maxWordLength;
    }

    public void setMaxWordLength(int maxWordLength) {
        this.maxWordLength = maxWordLength;
    }

    public boolean isShowLog() {
        return showLog;
    }

    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
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
}
