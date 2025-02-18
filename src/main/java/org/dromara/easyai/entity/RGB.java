package org.dromara.easyai.entity;

public class RGB {
    public RGB() {

    }

    public RGB(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    private float r;
    private float g;
    private float b;

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }

    public float getG() {
        return g;
    }

    public void setG(float g) {
        this.g = g;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }
}
