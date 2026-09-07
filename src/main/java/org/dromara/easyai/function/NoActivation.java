package org.dromara.easyai.function;

import org.dromara.easyai.i.ActiveFunction;

/**
 * @author lidapeng
 * @time 2026/9/3 17:57
 */
public class NoActivation implements ActiveFunction {
    @Override
    public float function(float x) {
        return x;
    }

    @Override
    public float functionG(float out) {
        return 1;
    }
}
