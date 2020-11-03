package org.wlld.imageRecognition.modelEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 混合高斯切割区域
 */
public class GMBody {
    private int type;//类别
    private List<Integer> pixels = new ArrayList<>();
    private int pixelNub = 0;
    private int nub;

    public int getNub() {
        return nub;
    }

    public void setNub(int nub) {
        this.nub = nub;
    }

    public GMBody(int type, int x, int y) {
        this.type = type;
        pixelNub++;
        pixels.add((x << 12) | y);
    }

    public int getPixelNub() {
        return pixelNub;
    }

    public int getType() {
        return type;
    }

    private boolean isAdjacent(int x, int y, int i, int j) {
        boolean adjacent = false;
        if (Math.abs(x - i) == 1 || Math.abs(y - j) == 1) {
            adjacent = true;
        }
        return adjacent;
    }

    public boolean insertRgb(int x, int y, int type) {
        boolean isRight = false;
        if (this.type == type) {
            for (int pixel : pixels) {
                int i = (pixel >> 12) & 0xfff;
                int j = pixel & 0xfff;
                if (isAdjacent(x, y, i, j)) {//相邻
                    isRight = true;
                    pixelNub++;
                    pixels.add((x << 12) | y);
                    break;
                }
            }
        }
        return isRight;
    }
}
