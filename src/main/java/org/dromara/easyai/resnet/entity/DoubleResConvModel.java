package org.dromara.easyai.resnet.entity;

import java.util.List;

/**
 * @author lidapeng
 * @time 2025/4/18 09:05
 */
public class DoubleResConvModel {
    private ResConvModel firstResConvModel;
    private ResConvModel secondResConvModel;
    private List<List<Float>> oneConvPowerModel;

    public ResConvModel getFirstResConvModel() {
        return firstResConvModel;
    }

    public void setFirstResConvModel(ResConvModel firstResConvModel) {
        this.firstResConvModel = firstResConvModel;
    }

    public ResConvModel getSecondResConvModel() {
        return secondResConvModel;
    }

    public void setSecondResConvModel(ResConvModel secondResConvModel) {
        this.secondResConvModel = secondResConvModel;
    }

    public List<List<Float>> getOneConvPowerModel() {
        return oneConvPowerModel;
    }

    public void setOneConvPowerModel(List<List<Float>> oneConvPowerModel) {
        this.oneConvPowerModel = oneConvPowerModel;
    }
}
