package org.dromara.easyai.i;

/**
 * @author lidapeng
 * @description 激活函数接口
 * @date 8:52 上午 2020/1/11
 */
public interface ActiveFunction {
    float function(float x);//激活函数

    float functionG(float out);//激活函数的导函数
}
