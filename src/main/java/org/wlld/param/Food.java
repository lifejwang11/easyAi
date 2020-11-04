package org.wlld.param;

import org.wlld.imageRecognition.CutFood;
import org.wlld.imageRecognition.border.GMClustering;
import org.wlld.imageRecognition.segmentation.DimensionMappingStudy;
import org.wlld.imageRecognition.segmentation.RgbRegression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 菜品识别实体类
 */
public class Food {
    private double rowMark = 0.05;//行痕迹过滤
    private double columnMark = 0.05;//列痕迹过滤
    private List<RgbRegression> trayBody = new ArrayList<>();//托盘实体参数
    private int regressionNub = 10000;//回归次数
    private double trayTh = 0.1;//托盘回归阈值
    private int regionSize = 5;//纹理区域大小
    private double foodFilterTh = 0.8;//干食数量过滤阈值
    private int[] foodType;//干食品类别集合 需激活注入
    private Map<Integer, Double> foodS = new HashMap<>();//干食品类别对应的积分 需激活注入
    private Map<Integer, GMClustering> foodMeanMap = new HashMap<>();//干食类别混高模型 需激活注入
    private Map<Integer, GMClustering> notFoodMeanMap = new HashMap<>();//干食类别混高模型 需激活注入
    private DimensionMappingStudy dimensionMappingStudy;//需激活注入

    public DimensionMappingStudy getDimensionMappingStudy() {
        return dimensionMappingStudy;
    }

    public void setDimensionMappingStudy(DimensionMappingStudy dimensionMappingStudy) {
        this.dimensionMappingStudy = dimensionMappingStudy;
    }

    public Map<Integer, GMClustering> getFoodMeanMap() {
        return foodMeanMap;
    }

    public Map<Integer, GMClustering> getNotFoodMeanMap() {
        return notFoodMeanMap;
    }

    public Map<Integer, Double> getFoodS() {
        return foodS;
    }

    public void setFoodS(Map<Integer, Double> foodS) {
        this.foodS = foodS;
    }

    public int[] getFoodType() {
        return foodType;
    }

    public void setFoodType(int[] foodType) {
        this.foodType = foodType;
    }

    public double getFoodFilterTh() {
        return foodFilterTh;
    }

    public void setFoodFilterTh(double foodFilterTh) {
        this.foodFilterTh = foodFilterTh;
    }

    public int getRegionSize() {
        return regionSize;
    }

    public void setRegionSize(int regionSize) {
        this.regionSize = regionSize;
    }

    public double getTrayTh() {
        return trayTh;
    }

    public void setTrayTh(double trayTh) {
        this.trayTh = trayTh;
    }

    public int getRegressionNub() {
        return regressionNub;
    }

    public void setRegressionNub(int regressionNub) {
        this.regressionNub = regressionNub;
    }

    public List<RgbRegression> getTrayBody() {
        return trayBody;
    }

    public void setTrayBody(List<RgbRegression> trayBody) {
        this.trayBody = trayBody;
    }

    public double getRowMark() {
        return rowMark;
    }

    public void setRowMark(double rowMark) {
        this.rowMark = rowMark;
    }

    public double getColumnMark() {
        return columnMark;
    }

    public void setColumnMark(double columnMark) {
        this.columnMark = columnMark;
    }
}
