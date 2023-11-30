package org.wlld.entity;


import org.wlld.MatrixTools.Matrix;
import org.wlld.i.OutBack;

import java.util.List;

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
    public void backPower(List<Integer> powerList, long eventId) {

    }

    @Override
    public void getBackMatrix(Matrix matrix, long eventId) {
        this.matrix = matrix;
    }

    @Override
    public void getWordVector(int id, double w) {

    }
}
