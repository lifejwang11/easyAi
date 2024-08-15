package org.wlld.yolo;

import org.wlld.matrixTools.Matrix;
import org.wlld.i.OutBack;

import java.util.List;

public class PositionBack implements OutBack {
    private double distX;
    private double distY;
    private double width;
    private double height;
    private double trust;

    public double getTrust() {
        return trust;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getDistX() {
        return distX;
    }

    public double getDistY() {
        return distY;
    }

    @Override
    public void getBack(double out, int id, long eventId) {
        switch (id) {
            case 1:
                distX = out;
                break;
            case 2:
                distY = out;
                break;
            case 3:
                width = out;
                break;
            case 4:
                height = out;
                break;
            case 5:
                trust = out;
                break;
        }
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
