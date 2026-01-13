package org.dromara.easyai.batchNerve;

import org.dromara.easyai.matrixTools.Matrix;

import java.util.Map;

/**
 * @author lidapeng
 * @time 2026/1/6 14:41
 */
public class FeatureBody {//批量特征
    private Matrix feature;
    private Map<Integer, Float> E;

    public Matrix getFeature() {
        return feature;
    }

    public void setFeature(Matrix feature) {
        this.feature = feature;
    }

    public Map<Integer, Float> getE() {
        return E;
    }

    public void setE(Map<Integer, Float> e) {
        E = e;
    }
}
