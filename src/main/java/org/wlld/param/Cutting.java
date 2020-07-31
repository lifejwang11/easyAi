package org.wlld.param;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 图像切割参数实体类
 */
public class Cutting {
    private int regionNub = 200;//一张图行或列分多少个区块
    private double th = 0.88;//灰度阈值
    private double maxRain = 340;//不降雨RGB阈值
    private double maxIou = 1;//最大交并比

    public double getMaxIou() {
        return maxIou;
    }

    public void setMaxIou(double maxIou) {
        this.maxIou = maxIou;
    }

    public int getRegionNub() {
        return regionNub;
    }

    public void setRegionNub(int regionNub) {
        this.regionNub = regionNub;
    }

    public double getTh() {
        return th;
    }

    public void setTh(double th) {
        this.th = th;
    }

    public double getMaxRain() {
        return maxRain;
    }

    public void setMaxRain(double maxRain) {
        this.maxRain = maxRain;
    }
}
