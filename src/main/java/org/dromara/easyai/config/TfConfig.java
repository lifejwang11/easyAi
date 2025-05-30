package org.dromara.easyai.config;

public class TfConfig {
    private int maxLength = 30;//最大语句长度
    private int multiNumber = 8;//多头数量
    private int featureDimension = 32;//词向量维度
    private int allDepth = 1;//深度
    private float studyRate = 0.004f;
    private boolean showLog = true;
    private int times = 500;//循环增强次数
    private int regularModel = RZ.NOT_RZ;//正则模式
    private float regular = 0;//正则系数
    private String splitWord = null;//词向量默认隔断符，无隔断则会逐字隔断
    private int coreNumber = 1;//是否使用多核并行计算进行提速
    private boolean outAllPro = false;//是否输出全概率，注意，若输出全概率只能用来分类概率,否则将消耗大量内存
    private float timePunValue = 0.5f;//时间惩罚系数
    private int typeNumber = 0;//类别数量
    private boolean norm = true;//生成模式，当为false的时候 使用自定义模式 typeNumber会生效
    public String startWord = "<start>";//开始符
    public String endWord = "<end>";//结束符

    public boolean isNorm() {
        return norm;
    }

    public void setNorm(boolean norm) {
        this.norm = norm;
    }

    public int getTypeNumber() {
        return typeNumber;
    }

    public void setTypeNumber(int typeNumber) {
        this.typeNumber = typeNumber;
    }

    public String getStartWord() {
        return startWord;
    }

    public void setStartWord(String startWord) {
        this.startWord = startWord;
    }

    public String getEndWord() {
        return endWord;
    }

    public void setEndWord(String endWord) {
        this.endWord = endWord;
    }

    public float getTimePunValue() {
        return timePunValue;
    }

    public void setTimePunValue(float timePunValue) {
        this.timePunValue = timePunValue;
    }

    public boolean isOutAllPro() {
        return outAllPro;
    }

    public void setOutAllPro(boolean outAllPro) {
        this.outAllPro = outAllPro;
    }

    public int getCoreNumber() {
        return coreNumber;
    }

    public void setCoreNumber(int coreNumber) {
        this.coreNumber = coreNumber;
    }

    public String getSplitWord() {
        return splitWord;
    }

    public void setSplitWord(String splitWord) {
        this.splitWord = splitWord;
    }

    public int getRegularModel() {
        return regularModel;
    }

    public void setRegularModel(int regularModel) {
        this.regularModel = regularModel;
    }

    public float getRegular() {
        return regular;
    }

    public void setRegular(float regular) {
        this.regular = regular;
    }

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
}
