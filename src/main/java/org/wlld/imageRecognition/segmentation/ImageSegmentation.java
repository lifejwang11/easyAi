package org.wlld.imageRecognition.segmentation;

import org.wlld.MatrixTools.Matrix;
import org.wlld.imageRecognition.ThreeChannelMatrix;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lidapeng
 * @description 阈值分区
 * @date 10:25 上午 2020/1/13
 */
public class ImageSegmentation {
    private Matrix matrixR;
    private Matrix matrixG;
    private Matrix matrixB;
    private Map<Integer, RegionBody> regionBodyList = new HashMap<>();
    private int id = 1;

    public ImageSegmentation(ThreeChannelMatrix threeChannelMatrix) throws Exception {
        matrixR = threeChannelMatrix.getMatrixR();
        matrixG = threeChannelMatrix.getMatrixG();
        matrixB = threeChannelMatrix.getMatrixB();
        int x = matrixR.getX();
        int y = matrixR.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double r = matrixR.getNumber(i, j);
                double g = matrixG.getNumber(i, j);
                double b = matrixB.getNumber(i, j);

            }
        }
    }

    public int getMin(double[] array) {
        double min = array[0];
        int minIdx = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                minIdx = i;
                min = array[i];
            }
        }
        return minIdx;
    }

}
