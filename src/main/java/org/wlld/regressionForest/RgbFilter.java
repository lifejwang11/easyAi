package org.wlld.regressionForest;

import org.wlld.MatrixTools.Matrix;
import org.wlld.imageRecognition.ThreeChannelMatrix;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description rgb 替换为权重
 */
public class RgbFilter {

    public void filter(ThreeChannelMatrix threeChannelMatrix, RegressionForest regressionForest) throws Exception {
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        Matrix matrixG = threeChannelMatrix.getMatrixG();
        Matrix matrixB = threeChannelMatrix.getMatrixB();
        int x = matrixR.getX();
        int y = matrixR.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double[] feature = new double[]{matrixR.getNumber(i, j) / 255, matrixG.getNumber(i, j) / 255};
                double result = matrixB.getNumber(i, j);
                regressionForest.insertFeature(feature, result);
            }
        }
        regressionForest.startStudy();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double[] feature = new double[]{matrixR.getNumber(i, j) / 255, matrixG.getNumber(i, j) / 255};
                double result = matrixB.getNumber(i, j);
            }
        }
    }
}
