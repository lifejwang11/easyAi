package org.dromara.easyai.conv;

import org.dromara.easyai.matrixTools.Matrix;

import java.util.List;

/**
 * @author lidapeng
 * @time 2025/2/27 13:17
 */
public class ConvResult {
    private Matrix leftMatrix;//计算结果的左乘矩阵
    private Matrix resultMatrix;//计算结果矩阵
    private Matrix nervePowerMatrix;//权重矩阵
    private Matrix residualError;//残差误差
    private List<Matrix> resultMatrixList;//计算结果矩阵集合
    private List<Matrix> leftMatrixList;//计算结果的左乘矩阵集合

    public List<Matrix> getResultMatrixList() {
        return resultMatrixList;
    }

    public void setResultMatrixList(List<Matrix> resultMatrixList) {
        this.resultMatrixList = resultMatrixList;
    }

    public List<Matrix> getLeftMatrixList() {
        return leftMatrixList;
    }

    public void setLeftMatrixList(List<Matrix> leftMatrixList) {
        this.leftMatrixList = leftMatrixList;
    }

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
