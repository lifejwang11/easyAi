package org.wlld.imageRecognition;

import org.wlld.MatrixTools.Matrix;

public class ThreeChannelMatrix {
    Matrix matrixR;
    Matrix matrixG;
    Matrix matrixB;
    Matrix H;
    Matrix matrixRGB;

    public Matrix getMatrixRGB() {
        return matrixRGB;
    }

    public void setMatrixRGB(Matrix matrixRGB) {
        this.matrixRGB = matrixRGB;
    }

    public Matrix getH() {
        return H;
    }

    public void setH(Matrix h) {
        H = h;
    }

    public Matrix getMatrixR() {
        return matrixR;
    }

    public void setMatrixR(Matrix matrixR) {
        this.matrixR = matrixR;
    }

    public Matrix getMatrixG() {
        return matrixG;
    }

    public void setMatrixG(Matrix matrixG) {
        this.matrixG = matrixG;
    }

    public Matrix getMatrixB() {
        return matrixB;
    }

    public void setMatrixB(Matrix matrixB) {
        this.matrixB = matrixB;
    }
}
