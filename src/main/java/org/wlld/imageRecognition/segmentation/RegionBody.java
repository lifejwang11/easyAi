package org.wlld.imageRecognition.segmentation;

import org.wlld.MatrixTools.Matrix;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @description 分区实体
 */
public class RegionBody {
    private int minX = -1;
    private int minY = -1;
    private int maxX;
    private int maxY;
    private List<Integer> pointList = new ArrayList<>();
    private Matrix regionMap;//分区图

    public List<Integer> getPointList() {
        return pointList;
    }

    public RegionBody(Matrix regionMap) {
        this.regionMap = regionMap;
    }

    public void merge(int type, RegionBody regionBody) throws Exception {//区域合并
        List<Integer> points = regionBody.getPointList();
        for (int pixel : points) {
            int x = pixel >> 12;
            int y = pixel & 0xfff;
            setPoint(x, y, type);
        }
    }

    public void setPoint(int x, int y, int type) throws Exception {
        if (x < minX || minX == -1) {
            minX = x;
        }
        if (y < minY || minY == -1) {
            minY = y;
        }
        if (x > maxX) {
            maxX = x;
        }
        if (y > maxY) {
            maxY = y;
        }
        int pixel = x << 12 | y;
        pointList.add(pixel);
        regionMap.setNub(x, y, type);
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }
}
