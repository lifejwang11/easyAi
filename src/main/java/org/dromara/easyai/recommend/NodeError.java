package org.dromara.easyai.recommend;

import org.dromara.easyai.matrixTools.Matrix;

/**
 * @author lidapeng
 * @time 2026/7/16 16:06
 */
public class NodeError {
    private Matrix errorPower;//权重误差
    private Matrix errorBais;//偏置项误差
    private int addTimes;//次数

    public int getAddTimes() {
        return addTimes;
    }

    public void setAddTimes(int addTimes) {
        this.addTimes = addTimes;
    }

    public Matrix getErrorPower() {
        return errorPower;
    }

    public void setErrorPower(Matrix errorPower) {
        this.errorPower = errorPower;
    }

    public Matrix getErrorBais() {
        return errorBais;
    }

    public void setErrorBais(Matrix errorBais) {
        this.errorBais = errorBais;
    }
}
