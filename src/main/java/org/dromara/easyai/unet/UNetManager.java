package org.dromara.easyai.unet;


import org.dromara.easyai.config.UNetConfig;
import org.dromara.easyai.conv.ConvCount;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @time 2025/2/25 20:19
 * @des Unet网络管理器
 */
public class UNetManager extends ConvCount {
    private List<UNetEncoder> encoderList = new ArrayList<>();
    private List<UNetDecoder> decoderList = new ArrayList<>();
    private final int xSize;
    private final int ySize;
    private final int kernLen;
    private final int minFeatureValue;
    private final int convTimes;
    private final int deep;

    public UNetManager(UNetConfig uNetConfig) {
        this.xSize = uNetConfig.getXSize();
        this.ySize = uNetConfig.getYSize();
        this.kernLen = uNetConfig.getKerSize();
        this.minFeatureValue = uNetConfig.getMinFeatureValue();
        this.convTimes = uNetConfig.getConvTimes();
        this.deep = getConvMyDep(xSize, ySize, kernLen, minFeatureValue, convTimes);//编码器深度深度
    }

    private void initEncoder() {

    }
}
