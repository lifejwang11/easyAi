package org.wlld.imageRecognition;

import org.wlld.MatrixTools.Matrix;
import org.wlld.i.OutBack;

public class MaxPoint implements OutBack {
    private int id;
    private double point = -1;
    private int pid = 0;//指定ID的概率
    private double pPoint = 0;

    public void setPid(int pid) {
        this.pid = pid;
    }

    public double getpPoint() {
        return pPoint;
    }

    public void setTh(double th) {
        point = th;
    }

    public int getId() {
        return id;
    }

    @Override
    public void getBack(double out, int id, long eventId) {
        if (pid > 0 && id == pid) {
            pPoint = out;
        }
        if (out > point) {
            point = out;
            this.id = id;
        }
    }

    @Override
    public void getBackMatrix(Matrix matrix, long eventId) {

    }
}
