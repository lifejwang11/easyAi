package org.wlld;

import org.wlld.MatrixTools.Matrix;
import org.wlld.i.OutBack;

/**
 * @author lidapeng
 * @description
 * @date 2:12 下午 2020/1/7
 */
public class Ma implements OutBack {
    private double out = 0;
    private int id;

    public void clear() {
        out = 0;
        id = 0;
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
    public void getBackMatrix(Matrix matrix, long eventId) {

    }
}
