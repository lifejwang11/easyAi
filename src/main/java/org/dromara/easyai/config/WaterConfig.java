package org.dromara.easyai.config;


/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class WaterConfig {
    //分水岭参数
    private double rainTh = 0.4;//降雨密度图
    private int regionNub = 100;//区域大小
    private int cutMaxXSize = 2000;//分水岭切割最大取样X400
    private int cutMaxYSize = 2000;//分水岭切割最大取样Y400
    private int minXSizeTh = 100;//分水岭过滤最小框
    private int minYSizeTh = 100;//分水岭过滤最小框
    private double th = 2 / 255D;//落差阈值

    public double getTh() {
        return th;
    }

    public void setTh(double th) {
        this.th = th;
    }

    public double getRainTh() {
        return rainTh;
    }

    public void setRainTh(double rainTh) {
        this.rainTh = rainTh;
    }

    public int getRegionNub() {
        return regionNub;
    }

    public void setRegionNub(int regionNub) {
        this.regionNub = regionNub;
    }

    public int getCutMaxXSize() {
        return cutMaxXSize;
    }

    public void setCutMaxXSize(int cutMaxXSize) {
        this.cutMaxXSize = cutMaxXSize;
    }

    public int getCutMaxYSize() {
        return cutMaxYSize;
    }

    public void setCutMaxYSize(int cutMaxYSize) {
        this.cutMaxYSize = cutMaxYSize;
    }

    public int getMinXSizeTh() {
        return minXSizeTh;
    }

    public void setMinXSizeTh(int minXSizeTh) {
        this.minXSizeTh = minXSizeTh;
    }

    public int getMinYSizeTh() {
        return minYSizeTh;
    }

    public void setMinYSizeTh(int minYSizeTh) {
        this.minYSizeTh = minYSizeTh;
    }

}
