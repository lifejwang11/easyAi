package org.dromara.easyai.config;


/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class WaterConfig {
    //分水岭参数
    private double rainTh = 0.7;//降雨密度图
    private int regionNub = 100;//区域大小
    private int cutMaxXSize = 400;//分水岭切割最大取样X400
    private int cutMaxYSize = 400;//分水岭切割最大取样Y400
    private int minXSizeTh = 125;//分水岭过滤最小框
    private int minYSizeTh = 125;//分水岭过滤最小框
    private double high = 30 / 255D;//高曝阈值
    private double myR;//排除R
    private double myG;//排除G
    private double myB;//排除B

    public double getMyR() {
        return myR;
    }

    public void setMyR(double myR) {
        this.myR = myR;
    }

    public double getMyG() {
        return myG;
    }

    public void setMyG(double myG) {
        this.myG = myG;
    }

    public double getMyB() {
        return myB;
    }

    public void setMyB(double myB) {
        this.myB = myB;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
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
