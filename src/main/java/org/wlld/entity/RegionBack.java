package org.wlld.entity;


import org.wlld.MatrixTools.Matrix;
import org.wlld.i.OutBack;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class RegionBack implements OutBack {
    private int id;
    private double point = -2;

    public void setId(int id) {
        this.id = id;
    }

    public void clear() {
        id = 0;
        point = -2;
    }

    public int getId() {
        return id;
    }

    public double getPoint() {
        return point;
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
