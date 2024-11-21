package org.dromara.transFormer.model;


public class LayNormModel {
    private double[][] bTa;//模型需要保存
    private double[][] power;//模型需要保存

    public double[][] getbTa() {
        return bTa;
    }

    public void setbTa(double[][] bTa) {
        this.bTa = bTa;
    }

    public double[][] getPower() {
        return power;
    }

    public void setPower(double[][] power) {
        this.power = power;
    }
}
