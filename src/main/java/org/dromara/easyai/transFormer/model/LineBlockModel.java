package org.dromara.easyai.transFormer.model;

import java.util.List;

public class LineBlockModel {
    private List<float[][]> hiddenNervesModel;//隐层model
    private List<float[][]> outNervesModel;//输出层model

    public List<float[][]> getHiddenNervesModel() {
        return hiddenNervesModel;
    }

    public void setHiddenNervesModel(List<float[][]> hiddenNervesModel) {
        this.hiddenNervesModel = hiddenNervesModel;
    }

    public List<float[][]> getOutNervesModel() {
        return outNervesModel;
    }

    public void setOutNervesModel(List<float[][]> outNervesModel) {
        this.outNervesModel = outNervesModel;
    }
}
