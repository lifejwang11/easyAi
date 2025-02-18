package org.dromara.easyai.function;


import org.dromara.easyai.i.ActiveFunction;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class ELu implements ActiveFunction {
    private final float a;

    public ELu(float a) {
        this.a = a;
    }

    @Override
    public float function(float x) {
        if (x > 0) {
            return x;
        } else {
            return (float) (a * ((float)Math.exp(x) - 1));
        }
    }

    @Override
    public float functionG(float out) {
        if (out > 0) {
            return 1;
        } else {
            return out + a;
        }
    }
}
