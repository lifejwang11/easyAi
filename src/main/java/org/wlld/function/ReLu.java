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
        return x * 0.1;
    }

    @Override
    public double functionG(double out) {
        return 0.1;
    }
}
