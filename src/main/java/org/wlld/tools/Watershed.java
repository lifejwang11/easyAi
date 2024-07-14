package org.wlld.tools;
import org.wlld.matrixTools.Matrix;
import org.wlld.config.FoodConfig2;
import org.wlld.config.Kernel;
import org.wlld.entity.Box;
import org.wlld.entity.RGB;
import org.wlld.entity.ThreeChannelMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @description 分水岭
 * @date 10:25 上午 2020/1/13
 */
public class Watershed {
    private Matrix matrix;//RGB范数图像
    private Matrix rainfallMap;//降雨图
    private Matrix regionMap;//分区图
    private List<double[]> colors;//背景颜色集合
    private double avgTh;//平均像素阈值
    private int xSize;//单元高度
    private int ySize;//单元宽度
    private double rainTh;//灰度阈值
    private int regionNub;//一张图分多少份
    private Map<Integer, RegionBody> regionBodyMap = new HashMap<>();
    private Matrix matrixR;
    private Matrix matrixG;
    private Matrix matrixB;
    private int xMax;
    private int yMax;
    private double brightnessMinTh;//亮度阈值
    private double brightnessMaxTh;//亮度阈值
    private int cutMinXSize;//分水岭切割最小取样X
    private int cutMinYSize;//分水岭切割最小取样Y
    private int cutMaxXSize;//分水岭切割最大取样X
    private int cutMaxYSize;//分水岭切割最大取样Y
    private List<RGB> plateRgbList;
    private Matrix regionBack;//背景图
    private double plateTh;//餐盘排除阈值

    public Watershed(ThreeChannelMatrix matrix, FoodConfig2 config, List<double[]> colors) throws Exception {
        if (matrix != null) {
            matrixR = matrix.getMatrixR();
            matrixG = matrix.getMatrixG();
            matrixB = matrix.getMatrixB();
            plateTh = config.getPlateTh();
            this.colors = colors;
            avgTh = config.getAvgRainTh();
            cutMaxXSize = config.getCutMaxXSize();
            cutMaxYSize = config.getCutMaxYSize();
            cutMinXSize = config.getMinXSizeTh() + 2;
            cutMinYSize = config.getMinYSizeTh() + 2;
            rainTh = config.getRainTh();//降雨密度图
            regionNub = config.getRegionNub();//区域大小
            brightnessMinTh = config.getBrightnessMinTh();//亮度阈值
            brightnessMaxTh = config.getBrightnessMaxTh();//亮度阈值
            plateRgbList = config.getPlateRgbList();
            this.matrix = matrix.getH();
            xSize = this.matrix.getX() / regionNub;
            ySize = this.matrix.getY() / regionNub;
            rainfallMap = new Matrix(this.matrix.getX(), this.matrix.getY());
            regionMap = new Matrix(regionNub, regionNub);
            regionBack = new Matrix(regionNub, regionNub);
            xMax = rainfallMap.getX() - 1;
            yMax = rainfallMap.getY() - 1;
            init();
        } else {
            throw new Exception("matrix is null");
        }
    }

    private void init() throws Exception {
        for (int i = 0; i < xSize * regionNub; i += xSize) {
            for (int j = 0; j < ySize * regionNub; j += ySize) {
                double avgR = matrixR.getSonOfMatrix(i, j, xSize, ySize).getAVG();
                double avgG = matrixG.getSonOfMatrix(i, j, xSize, ySize).getAVG();
                double avgB = matrixB.getSonOfMatrix(i, j, xSize, ySize).getAVG();
                if (isBackGroundByRGB(avgR, avgG, avgB)) {//是背景就给1
                    regionBack.setNub(i / xSize, j / ySize, 1);
                }
            }
        }
    }

    private boolean isBackGroundByRGB(double r, double g, double b) {
        boolean isBackGroundRegion = true;
        double minError = -1;
        for (double[] rgb : colors) {
            for (int i = 0; i < 3; i++) {
                double error = (Math.abs(rgb[0] - r) + Math.abs(rgb[1] - g) + Math.abs(rgb[2] - b)) / 3;
                if (minError < 0 || error < minError) {
                    minError = error;
                }
            }
        }
        if (minError > avgTh) {
            isBackGroundRegion = false;
        }
        return isBackGroundRegion;
    }

    private boolean isBackGround(int x, int y) throws Exception {//判断是否是背景
        int rx = x / xSize;
        int ry = y / ySize;
        if (rx >= regionNub) {
            rx = regionNub - 1;
        }
        if (ry >= regionNub) {
            ry = regionNub - 1;
        }
        boolean isPlate = false;
        if (plateRgbList != null) {//白色餐盘不需要特别处理
            for (RGB rgb : plateRgbList) {
                double rc = Math.abs(matrixR.getNumber(x, y) - rgb.getR());
                double gc = Math.abs(matrixG.getNumber(x, y) - rgb.getG());
                double bc = Math.abs(matrixB.getNumber(x, y) - rgb.getB());
                double th = (rc + gc + bc) / 3;
                if (th < plateTh) {//是餐盘
                    isPlate = true;
                    break;
                }
            }
        }
        return regionBack.getNumber(rx, ry) > 0.5 || isPlate;//是背景
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
            top = Kernel.rgbN;
            leftTop = Kernel.rgbN;
            rightTop = Kernel.rgbN;
        }
        if (y == 0) {
            leftTop = Kernel.rgbN;
            left = Kernel.rgbN;
            leftBottom = Kernel.rgbN;
        }
        if (x == xMax) {
            leftBottom = Kernel.rgbN;
            bottom = Kernel.rgbN;
            rightBottom = Kernel.rgbN;
        }
        if (y == yMax) {
            rightTop = Kernel.rgbN;
            right = Kernel.rgbN;
            rightBottom = Kernel.rgbN;
        }
        if (top == -1 && rainfallMap.getNumber(x - 1, y) == 0) {
            if (isBackGround(x - 1, y)) {//是背景
                top = Kernel.rgbN;
            } else {//不是背景
                top = matrix.getNumber(x - 1, y);
            }
        }
        if (left == -1 && rainfallMap.getNumber(x, y - 1) == 0) {
            if (isBackGround(x, y - 1)) {
                left = Kernel.rgbN;
            } else {
                left = matrix.getNumber(x, y - 1);
            }
        }
        if (bottom == -1 && rainfallMap.getNumber(x + 1, y) == 0) {
            if (isBackGround(x + 1, y)) {
                bottom = Kernel.rgbN;
            } else {
                bottom = matrix.getNumber(x + 1, y);
            }
        }
        if (right == -1 && rainfallMap.getNumber(x, y + 1) == 0) {
            if (isBackGround(x, y + 1)) {
                right = Kernel.rgbN;
            } else {
                right = matrix.getNumber(x, y + 1);
            }
        }
        if (leftTop == -1 && rainfallMap.getNumber(x - 1, y - 1) == 0) {
            if (isBackGround(x - 1, y - 1)) {
                leftTop = Kernel.rgbN;
            } else {
                leftTop = matrix.getNumber(x - 1, y - 1);
            }
        }
        if (leftBottom == -1 && rainfallMap.getNumber(x + 1, y - 1) == 0) {
            if (isBackGround(x + 1, y - 1)) {
                leftBottom = Kernel.rgbN;
            } else {
                leftBottom = matrix.getNumber(x + 1, y - 1);
            }
        }
        if (rightTop == -1 && rainfallMap.getNumber(x - 1, y + 1) == 0) {
            if (isBackGround(x - 1, y + 1)) {
                rightTop = Kernel.rgbN;
            } else {
                rightTop = matrix.getNumber(x - 1, y + 1);
            }
        }
        if (rightBottom == -1 && rainfallMap.getNumber(x + 1, y + 1) == 0) {
            if (isBackGround(x + 1, y + 1)) {
                rightBottom = Kernel.rgbN;
            } else {
                rightBottom = matrix.getNumber(x + 1, y + 1);
            }

        }
        return new double[]{top, left, bottom, right, leftTop, leftBottom, rightBottom, rightTop};
    }

    private int[] rain(int x, int y) throws Exception {//先往下降，直到不能再降了为止
        //有两种情况停止：1，最小值是自身。2，周围已经灌满水了,包括自身
        double[] pixels = getPixels(x, y);
        int[] point = new int[8];
        double mySelf;
        if (isBackGround(x, y)) {
            mySelf = Kernel.rgbN;
        } else {
            mySelf = matrix.getNumber(x, y);
        }
        int index = getMinIndex(pixels, mySelf);//最低点下标
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

    public List<Box> rainfall() throws Exception {//开始降雨
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
        sigmaPixel();
        List<Box> boxes = new ArrayList<>();
        for (Map.Entry<Integer, RegionBody> entry : regionBodyMap.entrySet()) {
            RegionBody regionBody = entry.getValue();
            int minX = regionBody.getMinX();
            int maxX = regionBody.getMaxX();
            int minY = regionBody.getMinY();
            int maxY = regionBody.getMaxY();
            int xSize = maxX - minX;
            int ySize = maxY - minY;
            if (xSize >= cutMinXSize && ySize >= cutMinYSize && xSize < cutMaxXSize && ySize < cutMaxYSize) {
                Box box = new Box();
                box.setX(minX);
                box.setY(minY);
                box.setRealX(minX + xSize / 2);
                box.setRealY(minY + ySize / 2);
                box.setxSize(xSize);
                box.setySize(ySize);
                boxes.add(box);
            }
        }
        return boxes;
    }

    private void merge() throws Exception {
        int xSize = regionMap.getX();
        int ySize = regionMap.getY();
        for (int i = 0; i < xSize - 1; i++) {//132
            List<Integer> list = new ArrayList<>();
            for (int j = 0; j < ySize; j++) {
                int type = (int) regionMap.getNumber(i, j);
                if (type > 1 && i + 1 < regionNub) {
                    int otherType = (int) regionMap.getNumber(i + 1, j);
                    if (otherType > 1 && otherType != type) {
                        if (!list.contains(otherType)) {
                            RegionBody myRegion = regionBodyMap.get(type);
                            RegionBody otherRegion = regionBodyMap.get(otherType);
                            myRegion.mergeRegion(otherRegion);
                            regionBodyMap.remove(otherType);
                            list.add(otherType);
                        }
                    }

                }
            }
        }
    }

    private void createMerge() throws Exception {
        int x = regionMap.getX();
        int y = regionMap.getY();
        int t = 0;
        boolean isZero = false;
        for (int i = 0; i < x; i++) {
            if (!isZero) {
                t++;
            }
            boolean isFirstOne = false;
            for (int j = 0; j < y; j++) {
                int type = (int) regionMap.getNumber(i, j);
                if (type == 1) {
                    RegionBody regionBody;
                    if (regionBodyMap.containsKey(t)) {
                        regionBody = regionBodyMap.get(t);
                    } else {
                        regionBody = new RegionBody(regionMap, t, xSize, ySize);
                        regionBodyMap.put(t, regionBody);
                    }
                    regionBody.setPoint(i, j);
                    isFirstOne = true;
                    isZero = false;
                } else if (isFirstOne) {
                    if (!isZero) {
                        t++;
                    }
                    isZero = true;
                }
            }
        }
    }

    private void sigmaPixel() throws Exception {//生成降雨密度图
        int size = xSize * ySize;
        for (int i = 0; i < xSize * regionNub; i += xSize) {
            for (int j = 0; j < ySize * regionNub; j += ySize) {
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
                if (cover > rainTh) {//降雨密度图
                    regionMap.setNub(i / xSize, j / ySize, 1);
                }
            }
        }
        //System.out.println(regionMap.getString());
        createMerge();//提取候选区
        merge();//合并候选区
    }

    private int getMinIndex(double[] array, double mySelf) {//获取最小值
        int minIdx = 0;
        for (int i = 0; i < array.length; i++) {
            double nub = array[i];
            if (nub > -1 && nub < mySelf && nub > brightnessMinTh && nub < brightnessMaxTh) {
                minIdx = minIdx | (1 << i);
            }
        }
        return minIdx;
    }
}
