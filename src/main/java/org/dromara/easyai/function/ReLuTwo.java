package org.dromara.easyai.function;

import org.dromara.easyai.i.ActiveFunction;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class ReLuTwo implements ActiveFunction {
    @Override
    public float function(float x) {
        if (x > 0) {
            return x * 0.05f;
        } else {
            return x * 0.03f;
        }
    }

    @Override
    public float functionG(float out) {
        if (out > 0) {
            return 0.05f;
        } else {
            return 0.03f;
        }
    }
}
