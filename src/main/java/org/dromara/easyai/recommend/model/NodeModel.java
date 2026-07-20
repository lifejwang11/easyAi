package org.dromara.easyai.recommend.model;


/**
 * @author lidapeng
 * @time 2026/7/20 08:36
 */
public class NodeModel {
    private Float[] selfPower;
    private Float[] otherPower;
    private Float[] bais;
    private float arf;
    private int typeId;//类别id

    public Float[] getSelfPower() {
        return selfPower;
    }

    public void setSelfPower(Float[] selfPower) {
        this.selfPower = selfPower;
    }

    public Float[] getOtherPower() {
        return otherPower;
    }

    public void setOtherPower(Float[] otherPower) {
        this.otherPower = otherPower;
    }

    public Float[] getBais() {
        return bais;
    }

    public void setBais(Float[] bais) {
        this.bais = bais;
    }

    public float getArf() {
        return arf;
    }

    public void setArf(float arf) {
        this.arf = arf;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
}
