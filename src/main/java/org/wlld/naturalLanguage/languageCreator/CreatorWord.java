package org.wlld.naturalLanguage.languageCreator;

import org.wlld.matrixTools.Matrix;
import org.wlld.i.OutBack;

import java.util.List;


public class CreatorWord implements OutBack {
    private int id;
    private double maxOut = -2;

    public int getId() {
        return id;
    }

    public double getMaxOut() {
        return maxOut;
    }

    public void clear() {
        maxOut = -2;
    }

    @Override
    public void getBack(double out, int id, long eventId) {
        if (out > maxOut) {
            maxOut = out;
            this.id = id;
        }
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
