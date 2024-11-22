package org.dromara.easyai.function;

import org.dromara.easyai.i.ActiveFunction;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class ReLuTwo implements ActiveFunction {
    @Override
    public double function(double x) {
        if (x > 0) {
            return x * 0.05;
        } else {
            return x * 0.03;
        }
    }

    @Override
    public double functionG(double out) {
        if (out > 0) {
            return 0.05;
        } else {
            return 0.03;
        }
    }
}
