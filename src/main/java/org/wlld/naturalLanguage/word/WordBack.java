package org.wlld.naturalLanguage.word;

import org.wlld.i.OutBack;
import org.wlld.matrixTools.Matrix;

import java.util.List;

public class WordBack implements OutBack {
    private int id;

    public int getId() {
        return id;
    }

    @Override
    public void getBack(double out, int id, long eventId) {
        this.id = id;
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

    }
}
