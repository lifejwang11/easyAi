package org.dromara.easyai.test;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.matrixTools.QRMatrix;

public class Test {
    public static void main(String[] args) throws Exception {
        MatrixOperation matrixOperation = new MatrixOperation();
        Matrix matrix = new Matrix(3, 3, "[3,14,9]#[6,23,3]#[6,12,15]#");
        QRMatrix qrMatrix = matrixOperation.qrd(matrix);
        Matrix Q = qrMatrix.getQ();
        Matrix R = qrMatrix.getR();
        Matrix re = matrixOperation.mulMatrix(Q, R);
        System.out.println(matrix.getString());
        System.out.println(Q.getString());
        System.out.println(R.getString());
        System.out.println(re.getString());
    }
}