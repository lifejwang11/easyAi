package org.wlld.imageRecognition;

import org.wlld.MatrixTools.Matrix;
import org.wlld.i.OutBack;

public class MaxPoint implements OutBack {
    private int id;
    private double point = -1;

    public void setTh(double th) {
        point = th;
    }

    public int getId() {
        return id;
    }

    @Override
    public void getBack(double out, int id, long eventId) {
        if (out > point) {
            point = out;
            this.id = id;
        }
    }

    @Override
    public void getBackMatrix(Matrix matrix, long eventId) {

    }
}
