package org.dromara.easyai.config;


/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class WaterConfig {
    //分水岭参数
    private float rainTh = 0.4f;//降雨密度图
    private int regionNub = 100;//区域大小
    private int cutMaxXSize = 2000;//分水岭切割最大取样X400
    private int cutMaxYSize = 2000;//分水岭切割最大取样Y400
    private int minXSizeTh = 100;//分水岭过滤最小框
    private int minYSizeTh = 100;//分水岭过滤最小框
    private float th = 2 / 255f;//落差阈值

    public float getTh() {
        return th;
    }

    public void setTh(float th) {
        this.th = th;
    }

    public float getRainTh() {
        return rainTh;
    }

    public void setRainTh(float rainTh) {
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
