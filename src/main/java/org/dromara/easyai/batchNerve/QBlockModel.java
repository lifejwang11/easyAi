package org.dromara.easyai.batchNerve;


/**
 * @author lidapeng
 * @time 2026/1/7 14:46
 */
public class QBlockModel {
    private Float[] btaModel;
    private Float[] powerModel;

    public Float[] getBtaModel() {
        return btaModel;
    }

    public void setBtaModel(Float[] btaModel) {
        this.btaModel = btaModel;
    }

    public Float[] getPowerModel() {
        return powerModel;
    }

    public void setPowerModel(Float[] powerModel) {
        this.powerModel = powerModel;
    }
}
