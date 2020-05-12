package org.wlld.imageRecognition;

import java.util.List;
import java.util.Map;

public class CoverBody {
    private List<List<Double>> feature;//特征
    private Map<Integer, Double> tag;//标注
    private List<List<Double>> cFeature;//卷积特征
    private int type;

    public int getType() {
        return type;
    }

    public List<List<Double>> getcFeature() {
        return cFeature;
    }

    public void setcFeature(List<List<Double>> cFeature) {
        this.cFeature = cFeature;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<List<Double>> getFeature() {
        return feature;
    }

    public void setFeature(List<List<Double>> feature) {
        this.feature = feature;
    }

    public Map<Integer, Double> getTag() {
        return tag;
    }

    public void setTag(Map<Integer, Double> tag) {
        this.tag = tag;
    }
}
