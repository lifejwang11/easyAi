package org.dromara.naturalLanguage.word;

import org.dromara.i.OutBack;
import org.dromara.matrixTools.Matrix;

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
    public void getBackMatrix(Matrix matrix, int id, long eventId) {

    }

    @Override
    public void getWordVector(int id, double w) {

    }
}
