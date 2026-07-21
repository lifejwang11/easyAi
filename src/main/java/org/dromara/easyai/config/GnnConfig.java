package org.dromara.easyai.config;


/**
 * @author lidapeng
 * @time 2026/7/11 11:44
 * @des gnn配置类
 */
public class GnnConfig {
    private int featureLength = 32;//特征维度大小
    private int gnnTypeNumber = 2;//gnn属性类别数量
    private float study = 0.0005f;//学习率
    private float gMaxTh = 1;//权重梯度裁切阈值
    private boolean auto = true;//是否使用自适应学习率
    private float layGMaxTh = 10000;//层梯度裁切阈值
    private boolean layerGCut = false;//是否需要层梯度裁剪
    private boolean cutLayGaMaxTh = true;//是否做层梯度裁切
    private boolean softMax = true;//是否为分类任务
    private int nodeSize = 0;//节点数量
    private int jumpTimes = 2;//跳跃数
    private int outNumber = 2;//输出位
    private int deep = 1;//线性层深度
    private boolean showLog = true;//打印日志
    private int regularModel = RZ.NOT_RZ;//正则模式
    private float regular = 0.001f;//正则系数
    private float otherValue = 1.5f;//邻居缩放超参

    public boolean isLayerGCut() {
        return layerGCut;
    }

    public void setLayerGCut(boolean layerGCut) {
        this.layerGCut = layerGCut;
    }

    public float getOtherValue() {
        return otherValue;
    }

    public void setOtherValue(float otherValue) {
        this.otherValue = otherValue;
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

    public boolean isShowLog() {
        return showLog;
    }

    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
    }

    public int getDeep() {
        return deep;
    }

    public void setDeep(int deep) {
        this.deep = deep;
    }

    public int getOutNumber() {
        return outNumber;
    }

    public void setOutNumber(int outNumber) {
        this.outNumber = outNumber;
    }

    public boolean isSoftMax() {
        return softMax;
    }

    public void setSoftMax(boolean softMax) {
        this.softMax = softMax;
    }

    public int getJumpTimes() {
        return jumpTimes;
    }

    public void setJumpTimes(int jumpTimes) {
        this.jumpTimes = jumpTimes;
    }

    public int getNodeSize() {
        return nodeSize;
    }

    public void setNodeSize(int nodeSize) {
        this.nodeSize = nodeSize;
    }

    public int getFeatureLength() {
        return featureLength;
    }

    public void setFeatureLength(int featureLength) {
        this.featureLength = featureLength;
    }

    public int getGnnTypeNumber() {
        return gnnTypeNumber;
    }

    public void setGnnTypeNumber(int gnnTypeNumber) {
        this.gnnTypeNumber = gnnTypeNumber;
    }

    public float getStudy() {
        return study;
    }

    public void setStudy(float study) {
        this.study = study;
    }

    public float getgMaxTh() {
        return gMaxTh;
    }

    public void setgMaxTh(float gMaxTh) {
        this.gMaxTh = gMaxTh;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public float getLayGMaxTh() {
        return layGMaxTh;
    }

    public void setLayGMaxTh(float layGMaxTh) {
        this.layGMaxTh = layGMaxTh;
    }

    public boolean isCutLayGaMaxTh() {
        return cutLayGaMaxTh;
    }

    public void setCutLayGaMaxTh(boolean cutLayGaMaxTh) {
        this.cutLayGaMaxTh = cutLayGaMaxTh;
    }
}
