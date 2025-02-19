package org.dromara.easyai.function;


import org.dromara.easyai.i.ActiveFunction;

public class Tanh implements ActiveFunction {
    @Override
    public float function(float x) {
        float x1 = (float)Math.exp(x);
        float x2 = (float)Math.exp(-x);
        float son = x1 - x2;// ArithUtil.sub(x1, x2);
        float mother = x1 + x2;// ArithUtil.add(x1, x2);
        return son / mother;//ArithUtil.div(son, mother);
    }

    @Override
    public float functionG(float out) {
        return 1 - (float)Math.pow(out, 2);
    }
}
