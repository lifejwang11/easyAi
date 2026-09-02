package org.dromara.easyai.config;

/**
 * @author lidapeng
 * @time 2026/8/27 13:47
 */
public class FpnConfig {
    private float pth = 0.7f;//概率阈值
    private float trustTh = 0.6f;//可信度阈值
    private int batchSize = 32;//线性层批量数值
    private float iouTh = 0.05f;
    private int startDeep;//fpn开始部署层数 此处以上需要单独配置 包括此处
    private int size;//尺寸大小
    private int typeNumber;//分类数量
    private int allDeep;//总深度
    private float studyRate;//学习率
    private float gMaxTh;//权重裁剪阈值
    private float layerCutTh;//层梯度裁剪阈值
    private int deep;//线性层深度
    private boolean showLog;//是否打印日志
    private int channelNo;//输入通道数
    private boolean needFeature;

    public boolean isNeedFeature() {
        return needFeature;
    }

    public void setNeedFeature(boolean needFeature) {
        this.needFeature = needFeature;
    }

    public float getIouTh() {
        return iouTh;
    }

    public void setIouTh(float iouTh) {
        this.iouTh = iouTh;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
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

    public int getDeep() {
        return deep;
    }

    public void setDeep(int deep) {
        this.deep = deep;
    }

    public int getAllDeep() {
        return allDeep;
    }

    public void setAllDeep(int allDeep) {
        this.allDeep = allDeep;
    }

    public float getStudyRate() {
        return studyRate;
    }

    public void setStudyRate(float studyRate) {
        this.studyRate = studyRate;
    }

    public float getgMaxTh() {
        return gMaxTh;
    }

    public void setgMaxTh(float gMaxTh) {
        this.gMaxTh = gMaxTh;
    }

    public float getLayerCutTh() {
        return layerCutTh;
    }

    public void setLayerCutTh(float layerCutTh) {
        this.layerCutTh = layerCutTh;
    }

    public int getStartDeep() {
        return startDeep;
    }

    public void setStartDeep(int startDeep) {
        this.startDeep = startDeep;
    }

    public float getPth() {
        return pth;
    }

    public void setPth(float pth) {
        this.pth = pth;
    }

    public float getTrustTh() {
        return trustTh;
    }

    public void setTrustTh(float trustTh) {
        this.trustTh = trustTh;
    }

    public int getTypeNumber() {
        return typeNumber;
    }

    public void setTypeNumber(int typeNumber) {
        this.typeNumber = typeNumber;
    }
}
