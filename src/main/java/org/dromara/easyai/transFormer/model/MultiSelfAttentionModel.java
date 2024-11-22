package org.dromara.easyai.transFormer.model;

import java.util.List;

public class MultiSelfAttentionModel {
    private double[][] powerModel;
    private List<QKVModel> qkvModelList;
    private int depth;

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public double[][] getPowerModel() {
        return powerModel;
    }

    public void setPowerModel(double[][] powerModel) {
        this.powerModel = powerModel;
    }

    public List<QKVModel> getQkvModelList() {
        return qkvModelList;
    }

    public void setQkvModelList(List<QKVModel> qkvModelList) {
        this.qkvModelList = qkvModelList;
    }
}
