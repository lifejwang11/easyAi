package org.wlld.imageRecognition.segmentation;

import org.wlld.MatrixTools.Matrix;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lidapeng
 * @description 阈值分区
 * @date 10:25 上午 2020/1/13
 */
public class ImageSegmentation {
    private Matrix regionMap;
    private Matrix matrix;
    private Map<Integer, RegionBody> regionBodyList = new HashMap<>();
    private int id = 1;

    public ImageSegmentation(Matrix matrix) {
        this.matrix = matrix;
        regionMap = new Matrix(matrix.getX(), matrix.getY());
    }

    public int getMin(double[] array) {
        double min = array[0];
        int minIdx = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                minIdx = i;
                min = array[i];
            }
        }
        return minIdx;
    }

    private void connect(int x, int y) throws Exception {
        double self = matrix.getNumber(x, y);
        double rightPixel = matrix.getNumber(x, y + 1);
        double leftPixel = matrix.getNumber(x, y - 1);
        double bottomPixel = matrix.getNumber(x + 1, y);
        double topPixel = matrix.getNumber(x - 1, y);

        double right = Math.abs(rightPixel - self);
        double left = Math.abs(leftPixel - self);
        double bottom = Math.abs(bottomPixel - self);
        double top = Math.abs(topPixel - self);
        double[] array = new double[]{left, right, top, bottom};
        double[] pixelArray = new double[]{leftPixel, rightPixel, topPixel, bottomPixel};
        int minIndex = getMin(array);
        int i, j;
        switch (minIndex) {
            case 0:
                i = x;
                j = y - 1;
                break;
            case 1:
                i = x;
                j = y + 1;
                break;
            case 2:
                i = x - 1;
                j = y;
                break;
            case 3:
                i = x + 1;
                j = y;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + minIndex);
        }
        int type = (int) regionMap.getNumber(i, j);
        if (type > 0) {//有选区了
            RegionBody regionBody = regionBodyList.get(type);
            regionBody.insertPosition(x, y, regionMap);
            regionBody.setPixel(self);
        } else {//链接最小方向的节点没有属于任何区域
            RegionBody regionBody = new RegionBody(id);
            regionBody.insertPosition(x, y, regionMap);
            regionBody.insertPosition(i, j, regionMap);
            regionBody.setPixel(pixelArray[minIndex]);
            regionBody.setPixel(self);
            regionBodyList.put(id, regionBody);
            id++;
        }
    }

    public void createMST() throws Exception {
        int x = matrix.getX() - 1;
        int y = matrix.getY() - 1;
        for (int i = 1; i < x; i++) {
            for (int j = 1; j < y; j++) {
                if (regionMap.getNumber(i, j) == 0.0) {
                    connect(i, j);
                }
            }
        }
        for (int i = 0; i < 3; i++) {
            mergeMSTX();
        }
    }


    private void mergeMSTX() throws Exception {//合并选区
        int x = matrix.getX() - 1;
        int y = matrix.getY() - 1;
        int now = (int) regionMap.getNumber(1, 1);
        int nowX = 1;
        int nowY = 1;
        for (int i = 1; i < x; i++) {
            for (int j = 1; j < y; j++) {
                int self = (int) regionMap.getNumber(i, j);
                if (now != self) {//遇到不同选区 区域NOW尝试与区域self合并
                    double nowPoint = matrix.getNumber(nowX, nowY);
                    double selfPoint = matrix.getNumber(i, j);
                    RegionBody nowMST = regionBodyList.get(now);
                    RegionBody selfMST = regionBodyList.get(self);
                    double nowDiff = nowMST.getMaxDiff();
                    double selfDiff = selfMST.getMaxDiff();
                    double diff = Math.abs(nowPoint - selfPoint);//当前边的差异性
                    double minDiff;
                    if (nowDiff > selfDiff) {//差异性最小的是SELF
                        minDiff = selfDiff;
                    } else {//差异性最小的是NOW
                        minDiff = nowDiff;
                    }
                    if (diff <= minDiff) {//进行合并
                        nowMST.merge(selfMST, regionMap);
                        regionBodyList.remove(self);
                    } else {//不进行合并
                        now = self;
                    }
                }
                nowX = i;
                nowY = j;
            }
        }
    }
}
