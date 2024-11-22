package org.dromara.easyai.transFormer.model;

import java.util.List;

public class LineBlockModel {
    private List<double[][]> hiddenNervesModel;//隐层model
    private List<double[][]> outNervesModel;//输出层model

    public List<double[][]> getHiddenNervesModel() {
        return hiddenNervesModel;
    }

    public void setHiddenNervesModel(List<double[][]> hiddenNervesModel) {
        this.hiddenNervesModel = hiddenNervesModel;
    }

    public List<double[][]> getOutNervesModel() {
        return outNervesModel;
    }

    public void setOutNervesModel(List<double[][]> outNervesModel) {
        this.outNervesModel = outNervesModel;
    }
}
