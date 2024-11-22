package org.dromara.easyai.tools;



import org.dromara.easyai.matrixTools.Matrix;

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
    private int type;
    private int xSize;
    private int ySize;
    private List<Integer> pointList = new ArrayList<>();
    private Matrix regionMap;//分区图

    RegionBody(Matrix regionMap, int type, int xSize, int ySize) {
        //System.out.println("type===" + type);
        this.regionMap = regionMap;
        this.type = type;
        this.xSize = xSize;
        this.ySize = ySize;
    }

    public List<Integer> getPointList() {
        return pointList;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void mergeRegion(RegionBody regionBody) throws Exception {
        List<Integer> list = regionBody.getPointList();
        for (int pixel : list) {
            int x = pixel >> 12;
            int y = pixel & 0xfff;
            setPoint(x, y);
        }
    }

    public void setPoint(int x, int y) throws Exception {
        if (x < minX || minX == -1) {
            minX = x;
        }
        if (x > maxX) {
            maxX = x;
        }
        if (y < minY || minY == -1) {
            minY = y;
        }
        if (y > maxY) {
            maxY = y;
        }
        int pixel = x << 12 | y;
        pointList.add(pixel);
        //System.out.println("type==" + type);
        regionMap.setNub(x, y, type);
    }

    public void setX(int x) {
        if (x < minX || minX == -1) {
            minX = x;
        }
        if (x > maxX) {
            maxX = x;
        }
    }

    public void setY(int y) {
        if (y < minY || minY == -1) {
            minY = y;
        }
        if (y > maxY) {
            maxY = y;
        }
    }

    public int getMinX() {
        return minX * xSize;
    }

    public int getMinY() {
        return minY * ySize;
    }

    public int getMaxX() {
        return maxX * xSize;
    }

    public int getMaxY() {
        return maxY * ySize;
    }
}
