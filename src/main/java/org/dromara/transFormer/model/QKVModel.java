package org.dromara.transFormer.model;

public class QKVModel {
    private double[][] Q;
    private double[][] K;
    private double[][] V;
    private int selfID;

    public int getSelfID() {
        return selfID;
    }

    public void setSelfID(int selfID) {
        this.selfID = selfID;
    }

    public double[][] getQ() {
        return Q;
    }

    public void setQ(double[][] q) {
        Q = q;
    }

    public double[][] getK() {
        return K;
    }

    public void setK(double[][] k) {
        K = k;
    }

    public double[][] getV() {
        return V;
    }

    public void setV(double[][] v) {
        V = v;
    }
}
