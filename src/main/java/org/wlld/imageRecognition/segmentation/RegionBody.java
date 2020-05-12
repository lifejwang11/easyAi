package org.wlld.imageRecognition.segmentation;

/**
 * @author lidapeng
 * @description 分区实体
 */
public class RegionBody {
    private int minX = -1;
    private int minY = -1;
    private int maxX;
    private int maxY;

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
