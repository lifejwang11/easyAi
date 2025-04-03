package org.dromara.easyai.transFormer.model;

import java.util.List;

public class TransFormerModel {
    private List<CodecBlockModel> encoderBlockModels;//编码器模块
    private List<CodecBlockModel> decoderBlockModels;//解码器模块
    private FirstDecoderModel firstDecoderBlockModel;//第一个解码器模块
    private LineBlockModel lineBlockModel;//线性分类层
    private TransWordVectorModel transWordVectorModel;//词向量模型

    public TransWordVectorModel getTransWordVectorModel() {
        return transWordVectorModel;
    }

    public void setTransWordVectorModel(TransWordVectorModel transWordVectorModel) {
        this.transWordVectorModel = transWordVectorModel;
    }

    public List<CodecBlockModel> getEncoderBlockModels() {
        return encoderBlockModels;
    }

    public void setEncoderBlockModels(List<CodecBlockModel> encoderBlockModels) {
        this.encoderBlockModels = encoderBlockModels;
    }

    public List<CodecBlockModel> getDecoderBlockModels() {
        return decoderBlockModels;
    }

    public void setDecoderBlockModels(List<CodecBlockModel> decoderBlockModels) {
        this.decoderBlockModels = decoderBlockModels;
    }

    public FirstDecoderModel getFirstDecoderBlockModel() {
        return firstDecoderBlockModel;
    }

    public void setFirstDecoderBlockModel(FirstDecoderModel firstDecoderBlockModel) {
        this.firstDecoderBlockModel = firstDecoderBlockModel;
    }

    public LineBlockModel getLineBlockModel() {
        return lineBlockModel;
    }

    public void setLineBlockModel(LineBlockModel lineBlockModel) {
        this.lineBlockModel = lineBlockModel;
    }
}
