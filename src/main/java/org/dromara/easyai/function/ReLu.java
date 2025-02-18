package org.dromara.easyai.function;

import org.dromara.easyai.i.ActiveFunction;

/**
 * @author lidapeng
 * @description ReLu 函数
 * @date 8:56 上午 2020/1/11
 */
public class ReLu implements ActiveFunction {
    @Override
    public float function(float x) {
        if (x > 0) {
            return x;
        } else {
            return 0;
        }
    }

    @Override
    public float functionG(float out) {
        if (out > 0) {
            return 1;
        } else {
            return 0;
        }
    }
}
