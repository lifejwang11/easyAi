package org.dromara.easyai.transFormer.model;

import java.util.List;

public class MultiSelfAttentionModel {
    private float[][] powerModel;
    private List<QKVModel> qkvModelList;
    private int depth;

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public float[][] getPowerModel() {
        return powerModel;
    }

    public void setPowerModel(float[][] powerModel) {
        this.powerModel = powerModel;
    }

    public List<QKVModel> getQkvModelList() {
        return qkvModelList;
    }

    public void setQkvModelList(List<QKVModel> qkvModelList) {
        this.qkvModelList = qkvModelList;
    }
}
