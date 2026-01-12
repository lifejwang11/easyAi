package org.dromara.easyai.batchNerve;

import org.dromara.easyai.config.RZ;

/**
 * @author lidapeng
 * @time 2026/1/7 14:57
 */
public class BatchNerveConfig {
    private int inputSize;//输入参数大小
    private int hiddenSize;//隐层神经元大小
    private int outSize;//输出神经元大小
    private boolean softMax = false;//是否需要 softMax
    private float studyRate = 0.001f;//学习率
    private boolean auto = false;//是否启用自适应学习率
    private float gaMa = 0.9f;
    private float GMaxTh = 10;//梯度裁剪阈值
    private int deep = 1;//隐层深度
    private boolean showLog = true;
    private int regularModel = RZ.NOT_RZ;//正则模式
    private float regular = 0.001f;//正则系数

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

    public int getInputSize() {
        return inputSize;
    }

    public void setInputSize(int inputSize) {
        this.inputSize = inputSize;
    }

    public int getHiddenSize() {
        return hiddenSize;
    }

    public void setHiddenSize(int hiddenSize) {
        this.hiddenSize = hiddenSize;
    }

    public int getOutSize() {
        return outSize;
    }

    public void setOutSize(int outSize) {
        this.outSize = outSize;
    }

    public boolean isSoftMax() {
        return softMax;
    }

    public void setSoftMax(boolean softMax) {
        this.softMax = softMax;
    }

    public float getStudyRate() {
        return studyRate;
    }

    public void setStudyRate(float studyRate) {
        this.studyRate = studyRate;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public float getGaMa() {
        return gaMa;
    }

    public void setGaMa(float gaMa) {
        this.gaMa = gaMa;
    }

    public float getGMaxTh() {
        return GMaxTh;
    }

    public void setGMaxTh(float GMaxTh) {
        this.GMaxTh = GMaxTh;
    }

    public int getDeep() {
        return deep;
    }

    public void setDeep(int deep) {
        this.deep = deep;
    }
}
