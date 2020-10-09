package org.wlld.imageRecognition.segmentation;

import org.wlld.MatrixTools.Matrix;
import org.wlld.imageRecognition.MeanClustering;
import org.wlld.imageRecognition.RGBNorm;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.imageRecognition.ThreeChannelMatrix;

import java.util.List;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class WFilter {
    private List<RGBNorm> rgbNorms;

    public void filter(ThreeChannelMatrix threeChannelMatrix, TempleConfig templeConfig,
                       int speciesQuantity) throws Exception {
        MeanClustering meanClustering = new MeanClustering(speciesQuantity, templeConfig);
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        Matrix matrixG = threeChannelMatrix.getMatrixG();
        Matrix matrixB = threeChannelMatrix.getMatrixB();
        int x = matrixR.getX();
        int y = matrixR.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double[] color = new double[]{matrixR.getNumber(i, j), matrixG.getNumber(i, j), matrixB.getNumber(i, j)};
                meanClustering.setColor(color);
            }
        }
        meanClustering.start(true);
        rgbNorms = meanClustering.getMatrices();

    }

    private void getDist() {
        double min = -1;
        for (RGBNorm rgbNorm : rgbNorms) {
            double[] rgb = rgbNorm.getRgb();

        }
    }

    private double dist(double[] a, double[] b) {
        double sigma = 0;
        for (int i = 0; i < a.length; i++) {
            double sub = Math.pow(a[i] - b[i], 2);
            sigma = sigma + sub;
        }
        return sigma / a.length;
    }
}
