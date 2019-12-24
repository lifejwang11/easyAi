package org.wlld.function;

import org.wlld.tools.ArithUtil;

/**
 * @author lidapeng
 * 激活函数实现类
 * @date 12:29 下午 2019/12/22
 */
public class ActiveFunction {
    public double sigmoid(double x) {//sigmoid
        return ArithUtil.div(1, ArithUtil.add(1, Math.exp(-x)));
    }

    public double sigmoidG(double out) {
        return ArithUtil.mul(out, ArithUtil.sub(1, out));
    }
}
