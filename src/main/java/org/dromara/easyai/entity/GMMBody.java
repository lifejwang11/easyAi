package org.dromara.easyai.entity;

/**
 * @author lidapeng
 * @time 2025/10/27 16:29
 * @des 混高聚类的概率与类别
 */
public class GMMBody {
    private int type;
    private double power;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }
}
