package org.wlld.function;

import org.wlld.i.ActiveFunction;

/**
 * @author lidapeng
 * @description ReLu 函数
 * @date 8:56 上午 2020/1/11
 */
public class ReLu implements ActiveFunction {
    @Override
    public double function(double x) {
        if (x < 0) {
            x = 0;
        }
        return x;
    }

    @Override
    public double functionG(double out) {
        if (out <= 0) {
            return 0;
        } else {
            return 1;
        }
    }
}
