package org.wlld.function;

import org.wlld.i.ActiveFunction;
import org.wlld.tools.ArithUtil;

public class Tanh implements ActiveFunction {
    @Override
    public double function(double x) {
        double x1 = Math.exp(x);
        double x2 = Math.exp(-x);
        double son = x1 - x2;// ArithUtil.sub(x1, x2);
        double mother = x1 + x2;// ArithUtil.add(x1, x2);
        return son / mother;//ArithUtil.div(son, mother);
    }

    @Override
    public double functionG(double out) {
        return 1 - Math.pow(function(out), 2);//ArithUtil.sub(1, Math.pow(function(out), 2));
    }
}
