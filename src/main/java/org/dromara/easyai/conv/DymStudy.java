package org.dromara.easyai.conv;

import org.dromara.easyai.matrixTools.Matrix;

/**
 * @author lidapeng
 * @time 2025/4/19 09:24
 * @des 动态学习处理
 */
public class DymStudy {
    private final float gaMa = 0.9f;

    public Matrix getErrorMatrixByStudy(float studyRage, Matrix sMatrix, Matrix gMatrix) throws Exception {//获取动态学习率
        int x = sMatrix.getX();
        int y = sMatrix.getY();
        Matrix errorMatrix = new Matrix(x, y);
        if (x == gMatrix.getX() && y == gMatrix.getY()) {
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    float s = sMatrix.getNumber(i, j);
                    float g = gMatrix.getNumber(i, j);
                    float sNext = gaMa * s + (1 - gaMa) * (float) Math.pow(g, 2);
                    sMatrix.setNub(i, j, sNext);
                    float myStudyRate = studyRage / (float) Math.sqrt(sNext + 0.00000001f);
                    errorMatrix.setNub(i, j, myStudyRate * g);
                }
            }
        }
        return errorMatrix;
    }

}
