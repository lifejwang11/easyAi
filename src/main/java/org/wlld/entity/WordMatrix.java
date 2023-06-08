package org.wlld.entity;

import org.wlld.MatrixTools.Matrix;
import org.wlld.i.OutBack;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class WordMatrix implements OutBack {
    private double[] vector;

    public WordMatrix(int size) {
        vector = new double[size];
    }

    public Matrix getVector() throws Exception {
        Matrix matrix = new Matrix(1, vector.length);
        for (int i = 0; i < vector.length; i++) {
            matrix.setNub(0, i, vector[i]);
        }
        return matrix;
    }

    @Override
    public void getBack(double out, int id, long eventId) {

    }

    @Override
    public void getBackMatrix(Matrix matrix, long eventId) {

    }

    @Override
    public void getWordVector(int id, double w) {
        vector[id - 1] = w;
    }
}
