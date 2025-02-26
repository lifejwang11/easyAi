package org.dromara.easyai.unet;

import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;

/**
 * @author lidapeng
 * @time 2025/2/26 10:52
 * @des unet运算超类
 */
public abstract class UNetCount {
    private final ActiveFunction activeFunction;
    private final double studyRate;//学习率
    private MatrixOperation matrixOperation = new MatrixOperation();
    private Matrix nervePowerMatrix;//卷积核
    private int kerSize;

    public UNetCount(final ActiveFunction activeFunction, double studyRate, int kerSize) {
        this.activeFunction = activeFunction;
        this.studyRate = studyRate;
        this.kerSize = kerSize;
    }

    private void initPower() {
        nervePowerMatrix = new Matrix(kerSize * kerSize, 1);
    }

}
