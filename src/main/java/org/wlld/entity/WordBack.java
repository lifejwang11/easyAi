package org.wlld.entity;


import org.wlld.matrixTools.Matrix;
import org.wlld.i.OutBack;

import java.util.List;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class WordBack implements OutBack {
    private int id;
    private double out = -2;

    public void clear() {
        out = -2;
        id = 0;
    }

    public double getOut() {
        return out;
    }

    public int getId() {
        return id;
    }

    @Override
    public void getBack(double out, int id, long eventId) {
        if (out > this.out) {
            this.out = out;
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
