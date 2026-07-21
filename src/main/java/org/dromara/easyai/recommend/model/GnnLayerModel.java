package org.dromara.easyai.recommend.model;

import java.util.List;

/**
 * @author lidapeng
 * @time 2026/7/20 08:39
 */
public class GnnLayerModel {
    private List<NodeModel> powerModelList;

    public List<NodeModel> getPowerModelList() {
        return powerModelList;
    }

    public void setPowerModelList(List<NodeModel> powerModelList) {
        this.powerModelList = powerModelList;
    }
}
