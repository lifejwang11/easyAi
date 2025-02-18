package org.dromara.easyai.transFormer.model;

public class QKVModel {
    private float[][] Q;
    private float[][] K;
    private float[][] V;
    private int selfID;

    public int getSelfID() {
        return selfID;
    }

    public void setSelfID(int selfID) {
        this.selfID = selfID;
    }

    public float[][] getQ() {
        return Q;
    }

    public void setQ(float[][] q) {
        Q = q;
    }

    public float[][] getK() {
        return K;
    }

    public void setK(float[][] k) {
        K = k;
    }

    public float[][] getV() {
        return V;
    }

    public void setV(float[][] v) {
        V = v;
    }
}
