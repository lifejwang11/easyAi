package org.wlld.entity;


import org.wlld.MatrixTools.Matrix;
import org.wlld.i.OutBack;

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
    public void backWord(String word, long eventId) {
        this.word = word;
    }

    @Override
    public void getBackMatrix(Matrix matrix, long eventId) {
        this.matrix = matrix;
    }

    @Override
    public void getWordVector(int id, double w) {

    }
}
