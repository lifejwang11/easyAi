package org.wlld.imageRecognition;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.config.Kernel;

/**
 * @author lidapeng
 * 图像卷积
 * @date 9:23 上午 2020/1/2
 */
public class Convolution {
    public Matrix getFeatures(Matrix matrix, int maxNub) throws Exception {
        do {
            matrix = convolution(matrix, Kernel.ALL_Two);
        }
        while (matrix.getX() > maxNub && matrix.getY() > maxNub);
        //已经不可以再缩小了，最后做一层卷积，然后提取最大值
        return matrix;
    }

    private Matrix convolution(Matrix matrix, Matrix kernel) throws Exception {
        int x = matrix.getX() - 2;//求导后矩阵的行数
        int y = matrix.getY() - 2;//求导后矩阵的列数
        Matrix myMatrix = new Matrix(x, y);//最终合成矩阵
        for (int i = 0; i < x; i++) {//遍历行
            for (int j = 0; j < y; j++) {//遍历每行的列
                double dm = MatrixOperation.convolution(matrix, kernel, i, j);
                if (dm > 0) {//存在边缘
                    myMatrix.setNub(i, j, dm);
                }
            }
        }
        return late(myMatrix);
    }

    public Matrix late(Matrix matrix) throws Exception {//迟化处理
        int xn = matrix.getX();
        int yn = matrix.getY();
        int x = xn / 2;//求导后矩阵的行数
        int y = yn / 2;//求导后矩阵的列数
        Matrix myMatrix = new Matrix(x, y);//迟化后的矩阵
        for (int i = 0; i < xn - 2; i += 2) {
            for (int j = 0; j < yn - 2; j += 2) {
                Matrix matrix1 = matrix.getSonOfMatrix(i, j, 2, 2);
                double maxNub = 0;
                for (int t = 0; t < matrix1.getX(); t++) {
                    for (int k = 0; k < matrix1.getY(); k++) {
                        double nub = matrix1.getNumber(t, k);
                        if (nub > maxNub) {
                            maxNub = nub;
                        }
                    }
                }
                //迟化的最大值是 MAXNUB
                myMatrix.setNub(i / 2, j / 2, maxNub);
            }
        }
        return myMatrix;
    }
}
