package org.wlld.imageRecognition.border;

import org.wlld.MatrixTools.Matrix;

/**
 * @author lidapeng
 * @description
 * @date 9:11 上午 2020/2/6
 */
public class Box {
    private Matrix matrix;//特征向量
    private Matrix matrixPosition;//坐标向量

    public Matrix getMatrix() {
        return matrix;
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    public Matrix getMatrixPosition() {
        return matrixPosition;
    }

    public void setMatrixPosition(Matrix matrixPosition) {
        this.matrixPosition = matrixPosition;
    }
}
