package org.wlld.entity;

import org.wlld.matrixTools.Matrix;
import org.wlld.i.OutBack;

import java.util.ArrayList;
import java.util.List;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class WordMatrix implements OutBack {
    private final double[] vector;

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

    public List<Double> getList() {
        List<Double> list = new ArrayList<>();
        for (double v : vector) {
            list.add(v);
        }
        return list;
    }

    @Override
    public void getBack(double out, int id, long eventId) {

    }

    @Override
    public void getSoftMaxBack(long eventId, List<Double> softMax) {

    }


    @Override
    public void backWord(String word, long eventId) {

    }


    @Override
    public void getBackMatrix(Matrix matrix, long eventId) {

    }

    @Override
    public void getWordVector(int id, double w) {
        vector[id - 1] = w;
    }
}
