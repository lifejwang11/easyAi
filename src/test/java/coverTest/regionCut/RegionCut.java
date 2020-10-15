package coverTest.regionCut;

import org.wlld.Ma;
import org.wlld.MatrixTools.Matrix;
import org.wlld.config.Kernel;
import org.wlld.imageRecognition.ThreeChannelMatrix;

import java.util.HashMap;
import java.util.Map;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 分区切割
 */
public class RegionCut {
    private Matrix matrixH;
    private Matrix regionMatrix;//分区地图
    private int fatherX;
    private int fatherY;
    private int size;
    private int id = 1;//分区id
    private Map<Integer, Double> minMap = new HashMap<>();//保存最小值
    private Map<Integer, Double> maxMap = new HashMap<>();//保存最大值

    public RegionCut(Matrix matrixH, int fatherX, int fatherY, int size) {
        this.matrixH = matrixH;
        this.fatherX = fatherX;
        this.fatherY = fatherY;
        this.size = size;
        regionMatrix = new Matrix(matrixH.getX(), matrixH.getY());
    }

    private void setLimit(int id, double pixel) {
        double min = minMap.get(id);
        double max = maxMap.get(id);
        if (pixel > max) {
            maxMap.put(id, pixel);
        }
        if (pixel < min) {
            minMap.put(id, pixel);
        }
    }

    private void firstCut() throws Exception {//进行第一次切割
        int x = matrixH.getX();
        int y = matrixH.getY();
        int size = x * y;
        System.out.println("像素数量：" + size);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double regionId = regionMatrix.getNumber(i, j);
                if (regionId < 0.5) {//该像素没有被连接
                    boolean isStop;
                    double self = matrixH.getNumber(i, j);//灰度值
                    regionMatrix.setNub(i, j, id);
                    minMap.put(id, self);
                    maxMap.put(id, self);
                    //System.out.println(regionMatrix.getString());
                    int xi = i;
                    int yj = j;
                    do {
                        double mySelf = matrixH.getNumber(xi, yj);//灰度值
                        int pixel = pixelLine(xi, yj, mySelf);
                        int column = pixel & 0xfff;
                        int row = (pixel >> 12) & 0xfff;
                        double type = regionMatrix.getNumber(row, column);
                        if (type < 0.5) {//可以连接
                            regionMatrix.setNub(row, column, id);//进行连接
                            setLimit(id, mySelf);
                            double mySelfSon = matrixH.getNumber(row, column);//灰度值
                            int pixelOther = pixelLine(row, column, mySelfSon);
                            int column2 = pixelOther & 0xfff;
                            int row2 = (pixelOther >> 12) & 0xfff;
                            isStop = row2 == xi && column2 == yj;
                            xi = row;
                            yj = column;
                        } else {//已经被连接了，跳出
                            isStop = true;
                        }
                    } while (!isStop);
                    id++;
                }
            }
        }
        System.out.println("第一次选区数量：" + id);
    }

    public void secondCut() throws Exception {//二切
        int x = matrixH.getX();
        int y = matrixH.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double key = regionMatrix.getNumber(i, j);//与周围八方向比较看是否有异类
                getOther(i, j, (int) key, matrixH.getNumber(i, j));
            }
        }
    }

    private void updateType(int type, int toType) throws Exception {
        int x = regionMatrix.getX();
        int y = regionMatrix.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if (regionMatrix.getNumber(i, j) == type) {
                    regionMatrix.setNub(i, j, toType);
                }
            }
        }
    }

    private void getOther(int x, int y, int key, double self) throws Exception {
        double[] pixels = getPixels(x, y, false);
        for (int i = 0; i < pixels.length; i++) {
            int pix = (int) pixels[i];
            if (pix > 0 && pix != key) {//接壤的非同类
                double min = minMap.get(pix);
                double max = maxMap.get(pix);
                double maxDist = max - min;
                int row = x;
                int column = y;
                switch (i) {
                    case 0://上
                        row = x - 1;
                        break;
                    case 1://左
                        column = y - 1;
                        break;
                    case 2://下
                        row = x + 1;
                        break;
                    case 3://右
                        column = y + 1;
                        break;
                    case 4://左上
                        column = y - 1;
                        row = x - 1;
                        break;
                    case 5://左下
                        column = y - 1;
                        row = x + 1;
                        break;
                    case 6://右下
                        column = y + 1;
                        row = x + 1;
                        break;
                    case 7://右上
                        column = y + 1;
                        row = x - 1;
                        break;
                }
                double dist = Math.abs(matrixH.getNumber(row, column) - self);
                if (dist < maxDist * 0.2) {//两个选区可以合并
                    setLimit(key, min);
                    setLimit(key, max);
                    updateType(pix, key);
                    id--;
                }
                break;
            }
        }
    }

    public void start() throws Exception {
        firstCut();//初切
        System.out.println("区域数量1：" + id);
        for (int i = 0; i < 1; i++) {
            secondCut();//二切
        }
        System.out.println("区域数量2：" + id);
        System.out.println(regionMatrix.getString());
    }

    private int pixelLine(int x, int y, double self) throws Exception {
        double[] pixels = getPixels(x, y, true);
        int minIndex = getMinIndex(pixels, self);
        int row = x;
        int column = y;
        switch (minIndex) {
            case 0://上
                row = x - 1;
                break;
            case 1://左
                column = y - 1;
                break;
            case 2://下
                row = x + 1;
                break;
            case 3://右
                column = y + 1;
                break;
            case 4://左上
                column = y - 1;
                row = x - 1;
                break;
            case 5://左下
                column = y - 1;
                row = x + 1;
                break;
            case 6://右下
                column = y + 1;
                row = x + 1;
                break;
            case 7://右上
                column = y + 1;
                row = x - 1;
                break;
        }
        return row << 12 | column;
    }

    private double[] getPixels(int x, int y, boolean isFirst) throws Exception {
        double left = 1, leftTop = 1, leftBottom = 1, right = 1, rightTop = 1, rightBottom = 1, top = 1, bottom = 1;
        Matrix matrix;
        if (isFirst) {
            matrix = matrixH;
        } else {
            matrix = regionMatrix;
        }
        if (x == 0) {
            top = -1;
            leftTop = -1;
            rightTop = -1;
        }
        if (y == 0) {
            leftTop = -1;
            left = -1;
            leftBottom = -1;
        }
        if (x == size - 1) {
            leftBottom = -1;
            bottom = -1;
            rightBottom = -1;
        }
        if (y == size - 1) {
            rightTop = -1;
            right = -1;
            rightBottom = -1;
        }
        if (top > 0) {
            top = matrix.getNumber(x - 1, y);
        }
        if (left > 0) {
            left = matrix.getNumber(x, y - 1);
        }
        if (right > 0) {
            right = matrix.getNumber(x, y + 1);
        }
        if (bottom > 0) {
            bottom = matrix.getNumber(x + 1, y);
        }
        if (leftTop > 0) {
            leftTop = matrix.getNumber(x - 1, y - 1);
        }
        if (leftBottom > 0) {
            leftBottom = matrix.getNumber(x + 1, y - 1);
        }
        if (rightTop > 0) {
            rightTop = matrix.getNumber(x - 1, y + 1);
        }
        if (rightBottom > 0) {
            rightBottom = matrix.getNumber(x + 1, y + 1);
        }
        return new double[]{top, left, bottom, right, leftTop, leftBottom, rightBottom, rightTop};

    }

    private int getMinIndex(double[] array, double self) {//获取最小值
        double min = -1;
        int minIdx = 0;
        for (int i = 0; i < array.length; i++) {
            double nub = array[i];
            if (nub > 0) {
                nub = Math.abs(nub - self);
                if (min < 0 || nub < min) {
                    min = nub;
                    minIdx = i;
                }
            }
        }
        return minIdx;
    }
}
