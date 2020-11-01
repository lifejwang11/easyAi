package org.wlld.param;

import org.wlld.imageRecognition.modelEntity.DeepMappingBody;
import org.wlld.imageRecognition.segmentation.RgbRegression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 菜品识别实体类
 */
public class Food {
    private int shrink = 60;//收缩参数
    private double rowMark = 0.12;//行痕迹过滤
    private double columnMark = 0.25;//列痕迹过滤
    private List<RgbRegression> trayBody = new ArrayList<>();//托盘实体参数
    private int regressionNub = 10000;//回归次数
    private double trayTh = 0.1;//托盘回归阈值
    private int regionSize = 5;//纹理区域大小
    private int step = 1;//特征取样步长
    private int speciesNub = 24;//种类数
    private DeepMappingBody deepMappingBody;//特征映射

    public DeepMappingBody getDeepMappingBody() {
        return deepMappingBody;
    }

    public void setDeepMappingBody(DeepMappingBody deepMappingBody) {
        this.deepMappingBody = deepMappingBody;
    }

    public int getSpeciesNub() {
        return speciesNub;
    }

    public void setSpeciesNub(int speciesNub) {
        this.speciesNub = speciesNub;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
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

    public int getShrink() {
        return shrink;
    }

    public void setShrink(int shrink) {
        this.shrink = shrink;
    }
}
