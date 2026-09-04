package org.dromara.easyai.yolo;


/**
 * @author lidapeng
 * @time 2026/9/1 17:15
 */
public class YoloFpnConfig {
    private float pth = 0.7f;//概率阈值
    private float trustTh = 0.6f;//可信度阈值
    private float iouTh = 0.05f;//交并比阈值
    private int fpnBatchSize = 32;//线性层批量数值
    private int startDeep = 2;//fpn开始部署层数 此处以上需要单独配置 包括此处
    private int size = 100;
    private int typeNumber = 2;
    private float containIouTh = 0.25f;//是否包含样本交并比阈值
    private float studyRate = 0.0005f;//全局学习率
    private boolean showLog = true;
    private int channelNo = 4;//通道数
    private int pictureSize = 20;//小批量训练数量

    public int getPictureSize() {
        return pictureSize;
    }

    public void setPictureSize(int pictureSize) {
        this.pictureSize = pictureSize;
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

    public float getStudyRate() {
        return studyRate;
    }

    public void setStudyRate(float studyRate) {
        this.studyRate = studyRate;
    }

    public float getContainIouTh() {
        return containIouTh;
    }

    public void setContainIouTh(float containIouTh) {
        this.containIouTh = containIouTh;
    }

    public int getTypeNumber() {
        return typeNumber;
    }

    public void setTypeNumber(int typeNumber) {
        this.typeNumber = typeNumber;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
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

    public float getIouTh() {
        return iouTh;
    }

    public void setIouTh(float iouTh) {
        this.iouTh = iouTh;
    }

    public int getFpnBatchSize() {
        return fpnBatchSize;
    }

    public void setFpnBatchSize(int fpnBatchSize) {
        this.fpnBatchSize = fpnBatchSize;
    }

    public int getStartDeep() {
        return startDeep;
    }

    public void setStartDeep(int startDeep) {
        this.startDeep = startDeep;
    }

}
