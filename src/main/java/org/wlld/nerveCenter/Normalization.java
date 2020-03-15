package org.wlld.nerveCenter;

import org.wlld.tools.ArithUtil;

/**
 * 集中归一化
 *
 * @author lidapeng
 * @description
 * @date 1:29 下午 2020/3/15
 */
public class Normalization {
    private double max;
    private double min;
    private long number;
    private double avg;
    private double sigma;

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public double getAvg() {
        return avg;
    }

    public void avg() {
        avg = ArithUtil.div(sigma, number);
        //System.out.println("avg==" + avg);
    }

    public void putFeature(double nub) {
        if (nub > max) {
            max = nub;
        }
        if (min == 0 || (nub != 0 && nub < min)) {
            min = nub;
        }
        if (nub != 0) {
            sigma = ArithUtil.add(sigma, nub);
            number++;
        }
    }
}
