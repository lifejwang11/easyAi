package org.dromara.easyai.resnet;


import java.util.List;

/**
 * @author lidapeng
 * @time 2025/4/11 13:48
 * @des 残差块卷积权重
 */
public class ResConvPower {
    private ConvLay firstConvPower;//第一层卷积权重
    private ConvLay secondConvPower;//第二层卷积权重
    private List<List<Float>> oneConvPower;//1v1卷积层


    public ConvLay getFirstConvPower() {
        return firstConvPower;
    }

    public void setFirstConvPower(ConvLay firstConvPower) {
        this.firstConvPower = firstConvPower;
    }

    public ConvLay getSecondConvPower() {
        return secondConvPower;
    }

    public void setSecondConvPower(ConvLay secondConvPower) {
        this.secondConvPower = secondConvPower;
    }

    public List<List<Float>> getOneConvPower() {
        return oneConvPower;
    }

    public void setOneConvPower(List<List<Float>> oneConvPower) {
        this.oneConvPower = oneConvPower;
    }
}
