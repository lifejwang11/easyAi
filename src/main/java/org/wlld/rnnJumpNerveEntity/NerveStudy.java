package org.wlld.rnnJumpNerveEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lidapeng
 * @description
 * @date 3:36 下午 2020/1/8
 */
public class NerveStudy {
    private Map<String, Double> dendrites = new HashMap<>();//上一层权重(需要取出)
    private double threshold;//此神经元的阈值需要取出

    public Map<String, Double> getDendrites() {
        return dendrites;
    }

    public void setDendrites(Map<String, Double> dendrites) {
        this.dendrites = dendrites;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}
