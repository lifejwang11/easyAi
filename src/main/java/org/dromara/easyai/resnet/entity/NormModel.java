package org.dromara.easyai.resnet.entity;

/**
 * @author lidapeng
 * @time 2025/4/18 08:44
 */
public class NormModel {
    private Float[] btaParameter;
    private Float[] powerParameter;

    public Float[] getBtaParameter() {
        return btaParameter;
    }

    public void setBtaParameter(Float[] btaParameter) {
        this.btaParameter = btaParameter;
    }

    public Float[] getPowerParameter() {
        return powerParameter;
    }

    public void setPowerParameter(Float[] powerParameter) {
        this.powerParameter = powerParameter;
    }
}
