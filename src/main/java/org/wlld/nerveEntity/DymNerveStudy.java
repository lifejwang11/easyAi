package org.wlld.nerveEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @description 动态神经元模型参数
 * @date 8:14 上午 2020/1/18
 */
public class DymNerveStudy {
    private List<Double> list = new ArrayList<>();
    private double threshold;//此神经元的阈值需要取出

    public List<Double> getList() {
        return list;
    }

    public void setList(List<Double> list) {
        this.list = list;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}
