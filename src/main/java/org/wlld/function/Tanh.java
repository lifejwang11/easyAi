package org.wlld.function;

import org.wlld.i.ActiveFunction;
import org.wlld.tools.ArithUtil;

public class Tanh implements ActiveFunction {
    @Override
    public double function(double x) {
        double son = ArithUtil.sub(Math.exp(x), Math.exp(-x));
        double mother = ArithUtil.add(Math.exp(x), Math.exp(-x));
        return ArithUtil.div(son, mother);
    }

    @Override
    public double functionG(double out) {
        return ArithUtil.sub(1, Math.pow(function(out), 2));
    }
}
