package org.dromara.easyai.resnet.fpn;

import org.dromara.easyai.matrixTools.Matrix;

/**
 * @author lidapeng
 * @time 2026/8/26 17:39
 * @des fpn标注
 */
public class FpnTag {
    private Matrix typeMatrix;//类别矩阵
    private Matrix widthMatrix;
    private Matrix heightMatrix;
    private Matrix distXMatrix;
    private Matrix distYMatrix;
    private Matrix trustMatrix;

    public Matrix getTypeMatrix() {
        return typeMatrix;
    }

    public void setTypeMatrix(Matrix typeMatrix) {
        this.typeMatrix = typeMatrix;
    }

    public Matrix getWidthMatrix() {
        return widthMatrix;
    }

    public void setWidthMatrix(Matrix widthMatrix) {
        this.widthMatrix = widthMatrix;
    }

    public Matrix getHeightMatrix() {
        return heightMatrix;
    }

    public void setHeightMatrix(Matrix heightMatrix) {
        this.heightMatrix = heightMatrix;
    }

    public Matrix getDistXMatrix() {
        return distXMatrix;
    }

    public void setDistXMatrix(Matrix distXMatrix) {
        this.distXMatrix = distXMatrix;
    }

    public Matrix getDistYMatrix() {
        return distYMatrix;
    }

    public void setDistYMatrix(Matrix distYMatrix) {
        this.distYMatrix = distYMatrix;
    }

    public Matrix getTrustMatrix() {
        return trustMatrix;
    }

    public void setTrustMatrix(Matrix trustMatrix) {
        this.trustMatrix = trustMatrix;
    }
}
