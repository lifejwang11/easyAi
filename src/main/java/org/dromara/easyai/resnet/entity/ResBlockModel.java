package org.dromara.easyai.resnet.entity;

/**
 * @author lidapeng
 * @time 2025/4/18 09:14
 */
public class ResBlockModel {
    private DoubleResConvModel firstResConvModel;
    private DoubleResConvModel secondResConvModel;
    private ResConvModel firstConvModel;

    public DoubleResConvModel getFirstResConvModel() {
        return firstResConvModel;
    }

    public void setFirstResConvModel(DoubleResConvModel firstResConvModel) {
        this.firstResConvModel = firstResConvModel;
    }

    public DoubleResConvModel getSecondResConvModel() {
        return secondResConvModel;
    }

    public void setSecondResConvModel(DoubleResConvModel secondResConvModel) {
        this.secondResConvModel = secondResConvModel;
    }

    public ResConvModel getFirstConvModel() {
        return firstConvModel;
    }

    public void setFirstConvModel(ResConvModel firstConvModel) {
        this.firstConvModel = firstConvModel;
    }
}
