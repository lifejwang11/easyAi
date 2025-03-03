package org.dromara.easyai.unet;

import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.i.OutBack;

/**
 * @author lidapeng
 * @time 2025/3/3 16:48
 * @des UNet输入入口类
 */
public class UNetInput {
    private final UNetEncoder uNetEncoder;

    public UNetInput(UNetEncoder uNetEncoder) {
        this.uNetEncoder = uNetEncoder;
    }

    public void postMessage(long eventID, OutBack outBack, ThreeChannelMatrix feature, ThreeChannelMatrix featureE,
                            boolean study) throws Exception {
        uNetEncoder.sendThreeChannel(eventID, outBack, feature, featureE, study);
    }
}
