package org.dromara.easyai.recommend;

import org.dromara.easyai.matrixTools.Matrix;

/**
 * @author lidapeng
 * @time 2026/7/11 14:26
 */
public class GnnPower {
    private Matrix selfPower;//自身权重
    private Matrix otherPower;//邻居权重
    private Matrix bais;//偏置项矩阵
    private float arf;//缩放系数

    public float getArf() {
        return arf;
    }

    public void setArf(float arf) {
        this.arf = arf;
    }

    public Matrix getBais() {
        return bais;
    }

    public void setBais(Matrix bais) {
        this.bais = bais;
    }

    public Matrix getSelfPower() {
        return selfPower;
    }

    public void setSelfPower(Matrix selfPower) {
        this.selfPower = selfPower;
    }

    public Matrix getOtherPower() {
        return otherPower;
    }

    public void setOtherPower(Matrix otherPower) {
        this.otherPower = otherPower;
    }
}
