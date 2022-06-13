package org.wlld.function;


import org.wlld.i.ActiveFunction;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class ELu implements ActiveFunction {
    @Override
    public double function(double x) {
        if (x > 0) {
            x = x * 0.1;
        } else {
            x = 0.1 * (Math.exp(x) - 1);
        }
        return x;
    }

    @Override
    public double functionG(double out) {
        if (out > 0) {
            return 0.1;
        } else {
            return out + 0.1;
        }
    }
}
