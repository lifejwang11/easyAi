package org.wlld.i;

public interface PsoFunction {//粒子群回调函数

    //根据参数返回函数值
    double getResult(double[] parameter,int id) throws Exception;
}
