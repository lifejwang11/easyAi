package org.wlld.imageRecognition.modelEntity;

import org.wlld.MatrixTools.Matrix;
import org.wlld.i.OutBack;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class RgbBack implements OutBack {
    private int id = 0;
    private double out = 0;

    public void clear() {
        out = 0;
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
    public void getBackMatrix(Matrix matrix, long eventId) {

    }

    public int getId() {
        return id;
    }
}
