package org.dromara.i;

/**
 * @author lidapeng
 * @description 激活函数接口
 * @date 8:52 上午 2020/1/11
 */
public interface ActiveFunction {
    double function(double x);//激活函数

    double functionG(double out);//激活函数的导函数
}
