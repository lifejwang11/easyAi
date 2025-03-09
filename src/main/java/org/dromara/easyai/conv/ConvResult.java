package org.dromara.easyai.conv;

import org.dromara.easyai.matrixTools.Matrix;

/**
 * @author lidapeng
 * @time 2025/2/27 13:17
 */
public class ConvResult {
    private Matrix leftMatrix;//计算结果的左乘矩阵
    private Matrix resultMatrix;//计算结果矩阵
    private Matrix nervePowerMatrix;//权重矩阵
    private Matrix residualError;//残差误差

    public Matrix getResidualError() {
        return residualError;
    }

    public void setResidualError(Matrix residualError) {
        this.residualError = residualError;
    }

    public Matrix getNervePowerMatrix() {
        return nervePowerMatrix;
    }

    public void setNervePowerMatrix(Matrix nervePowerMatrix) {
        this.nervePowerMatrix = nervePowerMatrix;
    }

    public Matrix getLeftMatrix() {
        return leftMatrix;
    }

    public void setLeftMatrix(Matrix leftMatrix) {
        this.leftMatrix = leftMatrix;
    }

    public Matrix getResultMatrix() {
        return resultMatrix;
    }

    public void setResultMatrix(Matrix resultMatrix) {
        this.resultMatrix = resultMatrix;
    }
}
