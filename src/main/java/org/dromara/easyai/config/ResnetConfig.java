package org.dromara.easyai.config;

/**
 * @author lidapeng
 * @time 2025/4/11 10:53
 * @des resNet 配置参数
 */
public class ResnetConfig {
    private int size;//图像尺寸 图像必须为正方形
    private float studyRate = 0.0025f;//全局学习率
    private int regularModel = RZ.NOT_RZ;//正则模式
    private float regular = 0;//正则系数
    private int hiddenNerveNumber = 16;//线性层隐层神经元数量
    private int typeNumber = 2;//分类数量
    private boolean softMax = true;//分类还是拟合
    private boolean showLog = true;//是否打印参数
    private int channelNo = 2;//通道数
    private int hiddenDeep = 1;//线性层隐层神经元深度
    private int minFeatureSize = 5;//卷积层最小特征大小
    private float gaMa = 0.9f;//自适应学习率衰减系数
    private float GMaxTh = 0.9f;//梯度最大值
    private boolean auto = true;//是否使用自适应学习率

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public float getGMaxTh() {
        return GMaxTh;
    }

    public void setGMaxTh(float GMaxTh) {
        this.GMaxTh = GMaxTh;
    }

    public float getGaMa() {
        return gaMa;
    }

    public void setGaMa(float gaMa) {
        this.gaMa = gaMa;
    }

    public int getMinFeatureSize() {
        return minFeatureSize;
    }

    public void setMinFeatureSize(int minFeatureSize) {
        this.minFeatureSize = minFeatureSize;
    }

    public int getHiddenDeep() {
        return hiddenDeep;
    }

    public void setHiddenDeep(int hiddenDeep) {
        this.hiddenDeep = hiddenDeep;
    }

    public int getChannelNo() {
        return channelNo;
    }

    public void setChannelNo(int channelNo) {
        this.channelNo = channelNo;
    }

    public boolean isShowLog() {
        return showLog;
    }

    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public float getStudyRate() {
        return studyRate;
    }

    public void setStudyRate(float studyRate) {
        this.studyRate = studyRate;
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

    public int getHiddenNerveNumber() {
        return hiddenNerveNumber;
    }

    public void setHiddenNerveNumber(int hiddenNerveNumber) {
        this.hiddenNerveNumber = hiddenNerveNumber;
    }

    public int getTypeNumber() {
        return typeNumber;
    }

    public void setTypeNumber(int typeNumber) {
        this.typeNumber = typeNumber;
    }

    public boolean isSoftMax() {
        return softMax;
    }

    public void setSoftMax(boolean softMax) {
        this.softMax = softMax;
    }
}
