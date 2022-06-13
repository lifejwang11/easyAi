package org.wlld.config;


import org.wlld.entity.RGB;

import java.util.List;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class FoodConfig2 {
    private double avgTh = 6 / 255D;//平均像素阈值6/ 15
    private double trainTh = 15 / 255D;//训练阈值
    //分水岭参数
    private double avgRainTh = 10 / 255D;//分水岭平均像素阈值7
    private double rainTh = 0.7;//降雨密度图
    private int regionNub = 100;//区域大小
    private double brightnessMinTh = 3 / 255D;//亮度阈值
    private double brightnessMaxTh = 180 / 255D;//亮度阈值120
    private int cutMaxXSize = 400;//分水岭切割最大取样X400
    private int cutMaxYSize = 400;//分水岭切割最大取样Y400
    private double meanIouTh = 0.2;//分组0.2 iou阈值
    private int minXSizeTh = 125;//分水岭过滤最小框
    private int minYSizeTh = 125;//分水岭过滤最小框
    private double minXTrustBoxSize = 170;//最小无条件信任框X
    private double minYTrustBoxSize = 170;//最小无条件信任框Y
    private double plateTh = 15 / 255D;//餐盘排除阈值
    private List<RGB> plateRgbList;//餐盘rgb

    public double getAvgTh() {
        return avgTh;
    }

    public void setAvgTh(double avgTh) {
        this.avgTh = avgTh;
    }

    public double getTrainTh() {
        return trainTh;
    }

    public void setTrainTh(double trainTh) {
        this.trainTh = trainTh;
    }

    public double getAvgRainTh() {
        return avgRainTh;
    }

    public void setAvgRainTh(double avgRainTh) {
        this.avgRainTh = avgRainTh;
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

    public double getBrightnessMinTh() {
        return brightnessMinTh;
    }

    public void setBrightnessMinTh(double brightnessMinTh) {
        this.brightnessMinTh = brightnessMinTh;
    }

    public double getBrightnessMaxTh() {
        return brightnessMaxTh;
    }

    public void setBrightnessMaxTh(double brightnessMaxTh) {
        this.brightnessMaxTh = brightnessMaxTh;
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

    public double getMeanIouTh() {
        return meanIouTh;
    }

    public void setMeanIouTh(double meanIouTh) {
        this.meanIouTh = meanIouTh;
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

    public double getMinXTrustBoxSize() {
        return minXTrustBoxSize;
    }

    public void setMinXTrustBoxSize(double minXTrustBoxSize) {
        this.minXTrustBoxSize = minXTrustBoxSize;
    }

    public double getMinYTrustBoxSize() {
        return minYTrustBoxSize;
    }

    public void setMinYTrustBoxSize(double minYTrustBoxSize) {
        this.minYTrustBoxSize = minYTrustBoxSize;
    }

    public double getPlateTh() {
        return plateTh;
    }

    public void setPlateTh(double plateTh) {
        this.plateTh = plateTh;
    }

    public List<RGB> getPlateRgbList() {
        return plateRgbList;
    }

    public void setPlateRgbList(List<RGB> plateRgbList) {
        this.plateRgbList = plateRgbList;
    }
}
