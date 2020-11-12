package org.wlld.function;

import org.wlld.i.ActiveFunction;
import org.wlld.tools.ArithUtil;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class Sigmoid implements ActiveFunction {
    @Override
    public double function(double x) {
        return ArithUtil.div(1, ArithUtil.add(1, Math.exp(-x)));
    }

    @Override
    public double functionG(double out) {
        return ArithUtil.mul(out, ArithUtil.sub(1, out));
    }
}
