package org.dromara.easyai.i;

public interface PsoFunction {//粒子群回调函数

    //根据参数返回函数值
    float getResult(float[] parameter,int id) throws Exception;
}
