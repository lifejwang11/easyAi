package org.dromara.easyai.matrixTools;

public class SVDBody {//svd特征
    private Matrix matrixU;
    private Matrix matrixVT;
    private Matrix feature;

    public Matrix getMatrixU() {
        return matrixU;
    }

    public void setMatrixU(Matrix matrixU) {
        this.matrixU = matrixU;
    }

    public Matrix getMatrixVT() {
        return matrixVT;
    }

    public void setMatrixVT(Matrix matrixVT) {
        this.matrixVT = matrixVT;
    }

    public Matrix getFeature() {
        return feature;
    }

    public void setFeature(Matrix feature) {
        this.feature = feature;
    }
}
