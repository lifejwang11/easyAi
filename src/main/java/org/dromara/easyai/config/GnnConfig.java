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
    private boolean cutLayGaMaxTh = true;//是否做层梯度裁切
    private int nodeSize = 0;//节点数量
    private int jumpTimes = 2;//跳跃数

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
