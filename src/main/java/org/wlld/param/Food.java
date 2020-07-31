package org.wlld.param;

import java.util.ArrayList;
import java.util.List;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 菜品识别实体类
 */
public class Food {
    private int shrink = 60;//收缩参数
    private int times = 10;//聚类增强次数
    private double rowMark = 0.12;//行痕迹
    private double columnMark = 0.25;//列过滤

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

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }
}
