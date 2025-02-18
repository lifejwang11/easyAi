package org.dromara.easyai.transFormer.model;


public class LayNormModel {
    private float[][] bTa;//模型需要保存
    private float[][] power;//模型需要保存

    public float[][] getbTa() {
        return bTa;
    }

    public void setbTa(float[][] bTa) {
        this.bTa = bTa;
    }

    public float[][] getPower() {
        return power;
    }

    public void setPower(float[][] power) {
        this.power = power;
    }
}
