package org.dromara.easyai.unet;

import java.util.List;

/**
 * @author lidapeng
 * @time 2025/3/3 15:00
 * @des UNet模型
 */
public class UNetModel {
    private List<ConvModel> encoderModels;
    private List<ConvModel> decoderModels;

    public List<ConvModel> getEncoderModels() {
        return encoderModels;
    }

    public void setEncoderModels(List<ConvModel> encoderModels) {
        this.encoderModels = encoderModels;
    }

    public List<ConvModel> getDecoderModels() {
        return decoderModels;
    }

    public void setDecoderModels(List<ConvModel> decoderModels) {
        this.decoderModels = decoderModels;
    }
}
