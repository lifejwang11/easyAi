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
        long a = System.currentTimeMillis();
        for (int i = 1; i < x; i++) {
            for (int j = 1; j < y; j++) {
                if (regionMap.getNumber(i, j) == 0.0) {
                    connect(i, j);
                }
            }
        }
        long b = System.currentTimeMillis();
        long c = b - a;
        System.out.println("耗时：" + c);
        System.out.println("第一次分区数量：" + regionBodyList.size());
    }

    private void mergeMST() throws Exception {//合并选区
        int x = matrix.getX() - 1;
        int y = matrix.getY() - 1;
        for (int i = 1; i < x; i++) {
            for (int j = 1; j < y; j++) {
                regionMap.getNumber(i, j);
            }
        }
    }
}
