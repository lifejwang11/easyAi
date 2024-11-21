package org.dromara.transFormer.model;

public class FirstDecoderModel {
    private MultiSelfAttentionModel multiSelfAttentionModel;//注意力层model
    private LayNormModel attentionLayNormModel;//残差1层model

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
}
