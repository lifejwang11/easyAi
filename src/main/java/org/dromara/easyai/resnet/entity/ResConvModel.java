package org.dromara.easyai.resnet.entity;

import java.util.List;

/**
 * @author lidapeng
 * @time 2025/4/18 08:49
 */
public class ResConvModel {
    private List<NormModel> normModelList;
    private List<Float[]> convPowerModelList;

    public List<NormModel> getNormModelList() {
        return normModelList;
    }

    public void setNormModelList(List<NormModel> normModelList) {
        this.normModelList = normModelList;
    }

    public List<Float[]> getConvPowerModelList() {
        return convPowerModelList;
    }

    public void setConvPowerModelList(List<Float[]> convPowerModelList) {
        this.convPowerModelList = convPowerModelList;
    }
}
