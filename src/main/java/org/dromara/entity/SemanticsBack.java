package org.dromara.entity;


import org.dromara.matrixTools.Matrix;
import org.dromara.i.OutBack;

import java.util.List;

public class SemanticsBack implements OutBack {
    private Matrix matrix;
    private String word;

    public String getWord() {
        return word;
    }

    public Matrix getMatrix() {
        return matrix;
    }

    @Override
    public void getBack(double out, int id, long eventId) {

    }

    @Override
    public void getSoftMaxBack(long eventId, List<Double> softMax) {

    }


    @Override
    public void backWord(String word, long eventId) {
        this.word = word;
    }

    @Override
    public void getBackMatrix(Matrix matrix, int id, long eventId) {
        this.matrix = matrix;
    }

    @Override
    public void getWordVector(int id, double w) {

    }
}
