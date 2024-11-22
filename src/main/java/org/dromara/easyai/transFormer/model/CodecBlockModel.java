package org.dromara.easyai.transFormer.model;


import java.util.List;

public class CodecBlockModel {
    private MultiSelfAttentionModel multiSelfAttentionModel;//注意力层model
    private LayNormModel attentionLayNormModel;//残差1层model
    private List<double[][]> fistNervesModel;//FNN层第一层model
    private List<double[][]> secondNervesModel;//FNN层第二层model
    private LayNormModel lineLayNormModel;//残差层最后2层model

    public MultiSelfAttentionModel getMultiSelfAttentionModel() {
        return multiSelfAttentionModel;
    }

    public void setMultiSelfAttentionModel(MultiSelfAttentionModel multiSelfAttentionModel) {
        this.multiSelfAttentionModel = multiSelfAttentionModel;
    }

    public LayNormModel getAttentionLayNormModel() {
        return attentionLayNormModel;
    }

    public void setAttentionLayNormModel(LayNormModel attentionLayNormModel) {
        this.attentionLayNormModel = attentionLayNormModel;
    }

    public List<double[][]> getFistNervesModel() {
        return fistNervesModel;
    }

    public void setFistNervesModel(List<double[][]> fistNervesModel) {
        this.fistNervesModel = fistNervesModel;
    }

    public List<double[][]> getSecondNervesModel() {
        return secondNervesModel;
    }

    public void setSecondNervesModel(List<double[][]> secondNervesModel) {
        this.secondNervesModel = secondNervesModel;
    }

    public LayNormModel getLineLayNormModel() {
        return lineLayNormModel;
    }

    public void setLineLayNormModel(LayNormModel lineLayNormModel) {
        this.lineLayNormModel = lineLayNormModel;
    }
}
