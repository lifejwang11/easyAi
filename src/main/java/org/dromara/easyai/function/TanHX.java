package org.dromara.easyai.function;


import org.dromara.easyai.i.ActiveFunction;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class TanHX implements ActiveFunction {
    @Override
    public double function(double x) {
        x = x * 0.038;
        double x1 = Math.exp(x);
        double x2 = Math.exp(-x);
        double son = x1 - x2;// ArithUtil.sub(x1, x2);
        double mother = x1 + x2;// ArithUtil.add(x1, x2);
        return son / mother;//ArithUtil.div(son, mother);
    }

    @Override
    public double functionG(double out) {
        return 1 - Math.pow(out, 2);
    }
}
