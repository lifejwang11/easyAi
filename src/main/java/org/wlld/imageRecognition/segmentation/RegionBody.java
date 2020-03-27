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
    private int maxX = -1;
    private int minY = -1;
    private int maxY = -1;
    private int point = 0xFFF;
    private int bit = 4 * 3;
    private int id;//本区域主键
    private double minPixel = -1;//最小像素值
    private double maxPixel = 0;//最大像素值
    private List<Integer> pixels = new ArrayList<>();

    RegionBody(int id) {
        this.id = id;
    }

    public void merge(RegionBody body, Matrix regionMap) throws Exception {//合并区域
        List<Integer> myPixels = body.getPixels();
        setPixel(body.getMaxPixel());
        setPixel(body.getMinPixel());
        for (int pixel : myPixels) {
            int x = pixel >> bit;
            int y = pixel & point;
            insertPosition(x, y, regionMap);
        }

    }

    public void setPixel(double pixel) {
        if (pixel < minPixel || minPixel == -1) {
            minPixel = pixel;
        }
        if (pixel > maxPixel) {
            maxPixel = pixel;
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
        //给区域地图中的像素分配区域ID
        regionMap.setNub(x, y, id);
    }

    public double getMinPixel() {
        return minPixel;
    }

    public double getMaxPixel() {
        return maxPixel;
    }

    public List<Integer> getPixels() {
        return pixels;
    }
}
