package org.wlld.entity;


import org.wlld.MatrixTools.Matrix;
import org.wlld.i.OutBack;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class ConvBack implements OutBack {
    private Matrix matrix;

    public Matrix getMatrix() {
        return matrix;
    }

    @Override
    public void getBack(double out, int id, long eventId) {
    }

    @Override
    public void getBackMatrix(Matrix matrix, long eventId) {
        this.matrix = matrix;
    }
}
