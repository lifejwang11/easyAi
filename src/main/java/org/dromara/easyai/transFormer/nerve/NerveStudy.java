package org.dromara.easyai.transFormer.nerve;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lidapeng
 * @description
 * @date 3:36 下午 2020/1/8
 */
public class NerveStudy {
    private Map<String, Float> dendrites = new HashMap<>();//上一层权重(需要取出)
    private float threshold;//此神经元的阈值需要取出

    public Map<String, Float> getDendrites() {
        return dendrites;
    }

    public void setDendrites(Map<String, Float> dendrites) {
        this.dendrites = dendrites;
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }
}
