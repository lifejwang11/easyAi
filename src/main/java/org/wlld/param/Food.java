package org.wlld.param;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 菜品识别实体类
 */
public class Food {
    private double shrink = 0.03;//收缩参数
    private int times = 10;//聚类增强次数
    private int integrate = 5;//聚类集成次数

    public double getShrink() {
        return shrink;
    }

    public void setShrink(double shrink) {
        this.shrink = shrink;
    }

    public int getIntegrate() {
        return integrate;
    }

    public void setIntegrate(int integrate) {
        this.integrate = integrate;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }
}
