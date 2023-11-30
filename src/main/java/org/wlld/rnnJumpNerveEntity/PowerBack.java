package org.wlld.rnnJumpNerveEntity;


import org.wlld.MatrixTools.Matrix;
import org.wlld.i.OutBack;

import java.util.List;

public class PowerBack implements OutBack {
    private double out;
    private int id;
    private List<Integer> powerList;

    public List<Integer> getPowerList() {
        return powerList;
    }

    public PowerBack() {
    }

    public PowerBack(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public double getOut() {
        return out;
    }

    @Override
    public void getBack(double out, int id, long eventId) {
        if (id == this.id) {
            this.out = out;
        }
    }

    @Override
    public void backPower(List<Integer> powerList, long eventId) {
        this.powerList = powerList;
    }

    @Override
    public void getBackMatrix(Matrix matrix, long eventId) {

    }

    @Override
    public void getWordVector(int id, double w) {

    }
}
