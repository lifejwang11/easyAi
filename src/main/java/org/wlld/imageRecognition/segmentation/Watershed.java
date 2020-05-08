package org.wlld.imageRecognition.segmentation;

import org.wlld.MatrixTools.Matrix;
import org.wlld.config.Kernel;

import java.util.*;

/**
 * @author lidapeng
 * @description 分水岭
 * @date 10:25 上午 2020/1/13
 */
public class Watershed {
    private Matrix matrix;//RGB范数图像
    private Matrix rainfallMap;//降雨图
    private Matrix rainDensityMap;//降雨密度图
    private Matrix regionMap;//分区图
    private int xSize;//单元高度
    private int ySize;//单元宽度
    private double th = Kernel.th;//灰度阈值
    private List<RegionBody> regionList = new ArrayList<>();
    private int xMax;
    private int yMax;

    public Watershed(Matrix matrix) throws Exception {
        if (matrix != null) {
            this.matrix = matrix;
            rainfallMap = new Matrix(matrix.getX(), matrix.getY());
            rainDensityMap = new Matrix(matrix.getX() / 30, matrix.getY() / 30);
            xMax = rainfallMap.getX() - 1;
            yMax = rainfallMap.getY() - 1;
        } else {
            throw new Exception("matrix is null");
        }
    }

    private double[] getPixels(int x, int y) throws Exception {
        //八方向取值
        double left = -1;//左边
        double leftTop = -1;//左上
        double leftBottom = -1;//左下
        double right = -1;//右边
        double rightTop = -1;//右上
        double rightBottom = -1;//右下
        double top = -1;//上边
        double bottom = -1;//下边
        if (x == 0) {
            top = 255;
            leftTop = 255;
            rightTop = 255;
        }
        if (y == 0) {
            leftTop = 255;
            left = 255;
            leftBottom = 255;
        }
        if (x == xMax) {
            leftBottom = 255;
            bottom = 255;
            rightBottom = 255;
        }
        if (y == yMax) {
            rightTop = 255;
            right = 255;
            rightBottom = 255;
        }
        if (top == -1 && rainfallMap.getNumber(x - 1, y) == 0) {
            top = matrix.getNumber(x - 1, y);
        }
        if (left == -1 && rainfallMap.getNumber(x, y - 1) == 0) {
            left = matrix.getNumber(x, y - 1);
        }
        if (bottom == -1 && rainfallMap.getNumber(x + 1, y) == 0) {
            bottom = matrix.getNumber(x + 1, y);
        }
        if (right == -1 && rainfallMap.getNumber(x, y + 1) == 0) {
            right = matrix.getNumber(x, y + 1);
        }
        if (leftTop == -1 && rainfallMap.getNumber(x - 1, y - 1) == 0) {
            leftTop = matrix.getNumber(x - 1, y - 1);
        }
        if (leftBottom == -1 && rainfallMap.getNumber(x + 1, y - 1) == 0) {
            leftBottom = matrix.getNumber(x + 1, y - 1);
        }
        if (rightTop == -1 && rainfallMap.getNumber(x - 1, y + 1) == 0) {
            rightTop = matrix.getNumber(x - 1, y + 1);
        }
        if (rightBottom == -1 && rainfallMap.getNumber(x + 1, y + 1) == 0) {
            rightBottom = matrix.getNumber(x + 1, y + 1);
        }
        return new double[]{top, left, bottom, right, leftTop, leftBottom, rightBottom, rightTop};
    }

    private int[] rain(int x, int y) throws Exception {//先往下降，直到不能再降了为止
        //有两种情况停止：1，最小值是自身。2，周围已经灌满水了,包括自身
        double[] pixels = getPixels(x, y);
        int[] point = new int[8];
        double mySelf = matrix.getNumber(x, y);
        int index = getMinIndex(pixels, mySelf);//最低点下标
        //System.out.println("x==" + x + "y==" + y + ",arrays==" + Arrays.toString(pixels) + ",index==" + index);
        int row;
        int column;
        if (index > 0) {//存在可向下蔓延的点
            for (int i = 0; i < 8; i++) {
                int t = index & (1 << i);
                if (t > 0) {
                    row = x;
                    column = y;
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
                    int pixel = row << 12 | column;
                    //等待继续往下沉降的点
                    point[i] = pixel;
                    //System.out.println("y===" + y);
                    //降雨图修改
                    rainfallMap.setNub(row, column, 1);
                }
            }
        }
        return point;
    }

    private void pull(List<Integer> list, int[] points) {
        for (int point : points) {
            if (point != 0) {
                list.add(point);
            }
        }
    }

    private void fall(int i, int j) throws Exception {
        List<Integer> list = new ArrayList<>();
        list.add((i << 12) | j);
        do {
            List<Integer> list2 = new ArrayList<>();
            for (int pixel : list) {
                int x = pixel >> 12;
                int y = pixel & 0xfff;
                int[] nodes = rain(x, y);
                pull(list2, nodes);
            }
            list = list2;
        } while (list.size() > 0);

    }

    public void rainfall() throws Exception {//开始降雨
        int x = matrix.getX();
        int y = matrix.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if (rainfallMap.getNumber(i, j) == 0) {//进行降雨
                    fall(i, j);
                }
            }
        }
        //进行区域提取
        xSize = x / 30;
        ySize = y / 30;
        System.out.println("xSize==" + xSize + ",ySize==" + ySize);
        sigmaPixel();
    }


    private void getPosition() throws Exception {
        int x = rainDensityMap.getX();
        int y = rainDensityMap.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double nub = rainDensityMap.getNumber(i, j);
            }
        }
    }

    private void sigmaPixel() throws Exception {//生成降雨密度图
        int x = matrix.getX();
        int y = matrix.getY();
        int size = xSize * ySize;
        for (int i = 0; i <= x - xSize; i += xSize) {
            for (int j = 0; j <= y - ySize; j += ySize) {
                Matrix myMatrix = rainfallMap.getSonOfMatrix(i, j, xSize, ySize);
                int sigma = 0;
                for (int t = 0; t < xSize; t++) {
                    for (int f = 0; f < ySize; f++) {
                        if (myMatrix.getNumber(t, f) > 0.1) {
                            sigma++;
                        }
                    }
                }
                double cover = (double) sigma / (double) size;//降雨率产生剧烈波动时则出现坐标
                if (cover > th) {//降雨密度图
                    rainDensityMap.setNub(i / 30, j / 30, 1);
                }
            }
        }
    }

    private int getMinIndex(double[] array, double mySelf) {//获取最小值
        int minIdx = 0;
        for (int i = 0; i < array.length; i++) {
            double nub = array[i];
            if (nub > -1 && nub < mySelf) {
                minIdx = minIdx | (1 << i);
            }
        }
        return minIdx;
    }
}
