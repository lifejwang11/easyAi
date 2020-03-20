package org.wlld.function;

import org.wlld.i.ActiveFunction;
import org.wlld.tools.ArithUtil;

public class Tanh implements ActiveFunction {
    @Override
    public double function(double x) {
        double x1 = Math.exp(x);
        double x2 = Math.exp(-x);
        double son = ArithUtil.sub(x1, x2);
        double mother = ArithUtil.add(x1, x2);
        return ArithUtil.div(son, mother);
    }

    @Override
    public double functionG(double out) {
        return ArithUtil.sub(1, Math.pow(function(out), 2));
    }
}
