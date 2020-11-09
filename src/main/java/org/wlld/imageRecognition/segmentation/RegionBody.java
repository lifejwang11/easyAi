package org.wlld.imageRecognition.segmentation;

import org.wlld.MatrixTools.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private Map<Integer, Integer> typeNub;//干食及数量

    public Map<Integer, Integer> getTypeNub() {
        return typeNub;
    }

    public void setTypeNub(Map<Integer, Integer> typeNub) {
        this.typeNub = typeNub;
    }

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
