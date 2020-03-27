package org.wlld.imageRecognition.segmentation;

import org.wlld.MatrixTools.Matrix;

/**
 * @author lidapeng
 * @description 阈值分区
 * @date 10:25 上午 2020/1/13
 */
public class ImageSegmentation {
    public void getFeature(Matrix matrix) throws Exception {//进行切割
        int x = matrix.getX();
        int y = matrix.getY();
        long a = System.currentTimeMillis();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double pixel = matrix.getNumber(i, j);
            }
        }
        long b = System.currentTimeMillis();
        long c = b - a;
        System.out.println(c);
    }
}
