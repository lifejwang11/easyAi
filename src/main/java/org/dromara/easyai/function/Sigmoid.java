package org.dromara.easyai.function;


import org.dromara.easyai.i.ActiveFunction;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class Sigmoid implements ActiveFunction {
    @Override
    public float function(float x) {
        return 1 / (1 + (float)Math.exp(-x));
    }

    @Override
    public float functionG(float out) {
        return out * (1 - out);
    }
}
