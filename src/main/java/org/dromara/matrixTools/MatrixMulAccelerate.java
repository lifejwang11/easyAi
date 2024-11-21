package org.dromara.matrixTools;

import java.util.concurrent.CountDownLatch;

public class MatrixMulAccelerate implements Runnable {
    private final Matrix matrix1;
    private final Matrix matrix2;
    private final Matrix matrix;
    private final int i;
    private final int j;
    private final CountDownLatch countDownLatch;

    public MatrixMulAccelerate(Matrix matrix1, Matrix matrix2, Matrix matrix, int i, int j
            , CountDownLatch countDownLatch) {
        this.matrix1 = matrix1;
        this.matrix2 = matrix2;
        this.matrix = matrix;
        this.i = i;
        this.j = j;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            Matrix matrixRow = matrix1.getRow(i);//行向量
            Matrix matrixColumn = matrix2.getColumn(j);
            double columnAllNumber = 0;//对每一项的乘积求和
            for (int h = 0; h < matrixColumn.getX(); h++) {
                double columnNumber = matrixColumn.getNumber(h, 0);
                double rowNumber = matrixRow.getNumber(0, h);
                double nowNumber = columnNumber * rowNumber;
                columnAllNumber = columnAllNumber + nowNumber;
            }
            matrix.setNub(i, j, columnAllNumber);
            countDownLatch.countDown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
