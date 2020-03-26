package org.wlld.imageRecognition.segmentation;

import org.wlld.MatrixTools.Matrix;

import java.util.*;

/**
 * @author lidapeng
 * @description 分区实体
 */
public class RegionBody {
    private int minX = -1;
    private int maxX = -1;
    private int minY = -1;
    private int maxY = -1;
    private int point = 0xFFF;
    private int bit = 4 * 3;
    private int id;//本区域主键
    //主键是行数，值是列坐标的集合
    private Map<Integer, List<Integer>> row = new TreeMap<>();
    //主键是列数，值是行坐标的集合
    private Map<Integer, List<Integer>> column = new TreeMap<>();
    private List<Integer> pixels = new ArrayList<>();

    RegionBody(int id) {
        this.id = id;
    }

    public void merge(RegionBody body, Matrix regionMap) throws Exception {//合并区域
        List<Integer> myPixels = body.getPixels();
        for (int pixel : myPixels) {
            int x = pixel >> bit;
            int y = pixel & point;
            insertPosition(x, y, regionMap);
        }
    }

    public void insertPosition(int x, int y, Matrix regionMap) throws Exception {
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
        //行在前，列在后
        int pixel = x << bit | y;
        pixels.add(pixel);
        putXy(row, x, y);
        putXy(column, y, x);
        //给区域地图中的像素分配区域ID
        regionMap.setNub(x, y, id);
    }

    private void putXy(Map<Integer, List<Integer>> map, int x, int y) {
        if (map.containsKey(x)) {
            List<Integer> list = map.get(x);
            list.add(y);
            Collections.sort(list);
        } else {
            List<Integer> pointList = new ArrayList<>();
            pointList.add(y);
            map.put(x, pointList);
        }
    }

    public List<Integer> getPixels() {
        return pixels;
    }

    public Map<Integer, List<Integer>> getRow() {
        return row;
    }

    public Map<Integer, List<Integer>> getColumn() {
        return column;
    }
}
