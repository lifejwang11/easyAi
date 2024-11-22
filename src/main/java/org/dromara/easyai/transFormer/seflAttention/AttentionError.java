package org.dromara.easyai.transFormer.seflAttention;

import org.dromara.easyai.matrixTools.Matrix;

public class AttentionError {
    private Matrix nextFeatureError;//下一层误差
    private Matrix lastEncoderError;//编码器最后一层误差

    public Matrix getNextFeatureError() {
        return nextFeatureError;
    }

    public void setNextFeatureError(Matrix nextFeatureError) {
        this.nextFeatureError = nextFeatureError;
    }

    public Matrix getLastEncoderError() {
        return lastEncoderError;
    }

    public void setLastEncoderError(Matrix lastEncoderError) {
        this.lastEncoderError = lastEncoderError;
    }
}
