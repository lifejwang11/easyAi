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
    private Matrix dymSelfPower1;//自身权重一阶距
    private Matrix dymSelfPower2;//自身权重二阶距
    private Matrix dymOtherPower1;//邻居权重一阶距
    private Matrix dymOtherPower2;//邻居权重二阶距
    private Matrix dymBais1;//偏置矩阵一阶距
    private Matrix dymBais2;//偏置矩阵二阶距
    private float arf;//缩放系数

    public Matrix getDymSelfPower1() {
        return dymSelfPower1;
    }

    public void setDymSelfPower1(Matrix dymSelfPower1) {
        this.dymSelfPower1 = dymSelfPower1;
    }

    public Matrix getDymSelfPower2() {
        return dymSelfPower2;
    }

    public void setDymSelfPower2(Matrix dymSelfPower2) {
        this.dymSelfPower2 = dymSelfPower2;
    }

    public Matrix getDymOtherPower1() {
        return dymOtherPower1;
    }

    public void setDymOtherPower1(Matrix dymOtherPower1) {
        this.dymOtherPower1 = dymOtherPower1;
    }

    public Matrix getDymOtherPower2() {
        return dymOtherPower2;
    }

    public void setDymOtherPower2(Matrix dymOtherPower2) {
        this.dymOtherPower2 = dymOtherPower2;
    }

    public Matrix getDymBais1() {
        return dymBais1;
    }

    public void setDymBais1(Matrix dymBais1) {
        this.dymBais1 = dymBais1;
    }

    public Matrix getDymBais2() {
        return dymBais2;
    }

    public void setDymBais2(Matrix dymBais2) {
        this.dymBais2 = dymBais2;
    }

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
