package org.dromara.easyai.matrixTools;

public class QRMatrix {//分解矩阵
    private Matrix Q;
    private Matrix R;

    public Matrix getQ() {
        return Q;
    }

    public void setQ(Matrix q) {
        Q = q;
    }

    public Matrix getR() {
        return R;
    }

    public void setR(Matrix r) {
        R = r;
    }
}
