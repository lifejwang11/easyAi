package org.wlld.yolo;

import org.wlld.MatrixTools.Matrix;
import org.wlld.i.OutBack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YoloTypeBack implements OutBack {
    private double out = -1;
    private int id = 0;


    public double getOut() {
        return out;
    }

    public int getId() {
        return id;
    }

    public void clear() {
        out = -1;
        id = 0;
    }

    @Override
    public void getBack(double out, int id, long eventId) {
        if (out > this.out) {
            this.out = out;
            this.id = id;
        }
    }

    @Override
    public void getSoftMaxBack(double out, int id, long eventId, List<Double> softMax) {
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
