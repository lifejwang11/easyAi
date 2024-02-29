package org.wlld.function;


import org.wlld.i.ActiveFunction;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class ELu implements ActiveFunction {
    private final double a;

    public ELu(double a) {
        this.a = a;
    }

    @Override
    public double function(double x) {
        if (x > 0) {
            return x;
        } else {
            return a * (Math.exp(x) - 1);
        }
    }

    @Override
    public double functionG(double out) {
        if (out > 0) {
            return 1;
        } else {
            return out + a;
        }
    }
}
