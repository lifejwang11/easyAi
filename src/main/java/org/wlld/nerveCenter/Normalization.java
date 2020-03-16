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
    private long number;
    private double avg;
    private double sigma;

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public void avg() {
        if (avg == 0) {
            avg = ArithUtil.div(sigma, number);
        }
        System.out.println(avg);
    }

    public void putFeature(double nub) {
        if (nub != 0) {
            sigma = ArithUtil.add(sigma, nub);
            number++;
        }
    }
}
