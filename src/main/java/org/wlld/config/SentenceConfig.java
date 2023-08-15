package org.wlld.config;


import org.wlld.entity.SentenceModel;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 参数配置
 */
public class SentenceConfig {
    private int typeNub = 11;//分类数量
    private int wordVectorDimension = 21;//词向量嵌入维度
    private int maxWordLength = 15;//最长字数
    private double weStudyPoint = 0.01;//词向量学习学习率
    private double weLParam = 0.001;//词向量正则系数
    private int randomNumber = 11;//随机网络数量
    private int nerveDeep = 6;//随机网络一组深度
    private int keyWordNerveDeep = 3;//关键词判断网络深度
    private boolean showLog = true;
    private int dateAug = 12000;
    private int TopNumber = 1;//取最高的几个类别

    public int getKeyWordNerveDeep() {
        return keyWordNerveDeep;
    }

    public void setKeyWordNerveDeep(int keyWordNerveDeep) {
        this.keyWordNerveDeep = keyWordNerveDeep;
    }

    public int getTopNumber() {
        return TopNumber;
    }

    public void setTopNumber(int topNumber) {
        TopNumber = topNumber;
    }

    public int getDateAug() {
        return dateAug;
    }

    public void setDateAug(int dateAug) {
        this.dateAug = dateAug;
    }

    public int getNerveDeep() {
        return nerveDeep;
    }

    public void setNerveDeep(int nerveDeep) {
        this.nerveDeep = nerveDeep;
    }

    public int getRandomNumber() {
        return randomNumber;
    }

    public void setRandomNumber(int randomNumber) {
        this.randomNumber = randomNumber;
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
