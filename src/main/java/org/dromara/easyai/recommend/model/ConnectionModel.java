package org.dromara.easyai.recommend.model;

import java.util.List;

/**
 * @author lidapeng
 * @time 2026/7/20 09:13
 */
public class ConnectionModel {
    private List<FeatureModel> featureModels;
    private List<ConnectModel> connectModels;
    private int[] typeArray;

    public List<FeatureModel> getFeatureModels() {
        return featureModels;
    }

    public void setFeatureModels(List<FeatureModel> featureModels) {
        this.featureModels = featureModels;
    }

    public List<ConnectModel> getConnectModels() {
        return connectModels;
    }

    public void setConnectModels(List<ConnectModel> connectModels) {
        this.connectModels = connectModels;
    }

    public int[] getTypeArray() {
        return typeArray;
    }

    public void setTypeArray(int[] typeArray) {
        this.typeArray = typeArray;
    }
}
