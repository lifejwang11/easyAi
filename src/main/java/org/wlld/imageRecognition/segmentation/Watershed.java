package org.wlld.imageRecognition.segmentation;

import org.wlld.MatrixTools.Matrix;
import org.wlld.config.Kernel;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.tools.ArithUtil;

import java.util.*;

/**
 * @author lidapeng
 * @description 分水岭
 * @date 10:25 上午 2020/1/13
 */
public class Watershed {
    private Matrix matrix;//RGB范数图像
    private Matrix rainfallMap;//降雨图
    private Matrix regionMap;//分区图
    private int xSize;//单元高度
    private int ySize;//单元宽度
    private double th = Kernel.th;//灰度阈值
    private double regionTh = Kernel.Region_Th;//绝对行数或者列数是否录入的阈值
    private int regionSize = Kernel.Region_Dif;
    private int regionNub = Kernel.Region_Nub;//一张图分多少份
    private Map<Integer, RegionBody> regionBodyMap = new HashMap<>();
    private double rainTh = 2;
    private int xMax;
    private int yMax;
    private int id = 1;
    private List<Specifications> specifications;

    public Watershed(Matrix matrix, List<Specifications> specifications, TempleConfig templeConfig) throws Exception {
        if (matrix != null && specifications != null && specifications.size() > 0) {
            th = templeConfig.gethTh();
            regionTh = templeConfig.getRegionTh();
            regionNub = templeConfig.getRegionNub();
            this.matrix = matrix;
            this.specifications = specifications;
            xSize = matrix.getX() / regionNub;
            ySize = matrix.getY() / regionNub;
            System.out.println("xSize===" + xSize + ",ysize===" + ySize);
            rainfallMap = new Matrix(matrix.getX(), matrix.getY());
            regionMap = new Matrix(regionNub, regionNub);
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

    public List<RegionBody> rainfall() throws Exception {//开始降雨
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
        List<RegionBody> regionBodies = new ArrayList<>();
        for (Map.Entry<Integer, RegionBody> entry : regionBodyMap.entrySet()) {
            RegionBody regionBody = entry.getValue();
            if (check(regionBody.getMinX(), regionBody.getMinY(), regionBody.getMaxX(), regionBody.getMaxY())) {
                regionBodies.add(regionBody);
            }
        }
        return regionBodies;
    }

    private boolean check(int minX, int minY, int maxX, int maxY) {
        boolean isRight = false;
        for (Specifications specification : specifications) {
            int width = maxY - minY;
            int height = maxX - minX;
            if (width >= specification.getWidth() && height >= specification.getHeight()) {
                isRight = true;
                break;
            }
        }
        return isRight;
    }

    private void setType(int type, int x, int y) throws Exception {
        for (int i = x; i < x + regionSize; i++) {
            for (int j = y; j < y + regionSize; j++) {
                if (regionMap.getNumber(i, j) != 0) {
                    regionMap.setNub(i, j, type);
                }
            }
        }
    }

    private void lineRegion() throws Exception {
        int x = regionMap.getX();
        int y = regionMap.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                int type = (int) regionMap.getNumber(i, j);
                if (type > 1) {
                    RegionBody regionBody = regionBodyMap.get(type);
                    for (int t = 0; t < 8; t++) {
                        int row = 0;
                        int column = 0;
                        switch (t) {
                            case 0://上
                                row = i - 1;
                                break;
                            case 1://左
                                column = j - 1;
                                break;
                            case 2://下
                                row = i + 1;
                                break;
                            case 3://右
                                column = j + 1;
                                break;
                            case 4://左上
                                column = j - 1;
                                row = i - 1;
                                break;
                            case 5://左下
                                column = j - 1;
                                row = i + 1;
                                break;
                            case 6://右下
                                column = j + 1;
                                row = i + 1;
                                break;
                            case 7://右上
                                column = j + 1;
                                row = i - 1;
                                break;
                        }
                        if (row >= 0 && row < regionNub && column >= 0 && column < regionNub) {
                            int otherType = (int) regionMap.getNumber(row, column);
                            if (otherType > 1 && otherType != type) {
                                RegionBody otherRegionBody = regionBodyMap.get(otherType);
                                regionBody.mergeRegion(otherRegionBody);
                                regionBodyMap.remove(otherType);
                            }
                        }
                    }
                }
            }
        }
    }

    private void getRegion() throws Exception {
        int x = regionMap.getX();
        int y = regionMap.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                int type = (int) regionMap.getNumber(i, j);
                if (type == 1) {
                    id++;
                    RegionBody regionBody = new RegionBody(regionMap, id);
                    regionBody.setPoint(i, j);
                    regionBodyMap.put(id, regionBody);
                    for (int t = 0; t < 8; t++) {
                        int row = 0;
                        int column = 0;
                        switch (t) {
                            case 0://上
                                row = i - 1;
                                break;
                            case 1://左
                                column = j - 1;
                                break;
                            case 2://下
                                row = i + 1;
                                break;
                            case 3://右
                                column = j + 1;
                                break;
                            case 4://左上
                                column = j - 1;
                                row = i - 1;
                                break;
                            case 5://左下
                                column = j - 1;
                                row = i + 1;
                                break;
                            case 6://右下
                                column = j + 1;
                                row = i + 1;
                                break;
                            case 7://右上
                                column = j + 1;
                                row = i - 1;
                                break;
                        }
                        //  System.out.println("row==" + row + ",column==" + column + ",region==" + regionNub);
                        if (row >= 0 && row < regionNub && column >= 0 && column < regionNub) {
                            double nub = regionMap.getNumber(row, column);
                            if (nub == 1) {
                                regionBody.setPoint(row, column);
                            }
                        }
                    }
                }
            }
        }
    }

    private void mergeRegion() throws Exception {//区域合并
        int x = regionMap.getX() - regionSize + 1;
        int y = regionMap.getY() - regionSize + 1;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                Matrix matrix = regionMap.getSonOfMatrix(i, j, regionSize, regionSize);
                int type = 0;
                int state = 0;
                for (int k = 0; k < matrix.getX(); k++) {
                    for (int l = 0; l < matrix.getY(); l++) {
                        int nub = (int) matrix.getNumber(k, l);
                        if (nub > 1 && type == 0) {//存在大于1的数
                            type = nub;
                            state = state | (1 << 1);
                        } else if (nub == 1) {
                            state = state | 1;
                        }
                    }
                }
                if ((state & 2) != 0) {//存在大于1的数
                    setType(type, i, j);
                } else if ((state & 1) != 0) {//不存在大于1的数，但是存在1,生成新的ID
                    id++;
                    regionBodyMap.put(id, new RegionBody(regionMap, id));
                    setType(id, i, j);
                }

            }
        }
    }

    private void mergeRegions() throws Exception {
        for (Map.Entry<Integer, RegionBody> entry : regionBodyMap.entrySet()) {
            RegionBody regionBody = entry.getValue();
            if (!regionBody.isDestroy()) {
                int key = entry.getKey();//156
                if (key == 155) {
                    //System.out.println("155===");
                }
                for (Map.Entry<Integer, RegionBody> entry2 : regionBodyMap.entrySet()) {
                    int minX = regionBody.getMinX();
                    int maxX = regionBody.getMaxX();
                    int minY = regionBody.getMinY();
                    int maxY = regionBody.getMaxY();
                    RegionBody regionBody1 = entry2.getValue();
                    int testKey = entry2.getKey();
                    if (testKey == 204) {
                        int a = 0;
                    }
                    if (testKey != key && !regionBody1.isDestroy()) {
                        int otherMinX = regionBody1.getMinX();
                        int otherMaxX = regionBody1.getMaxX();
                        int otherMinY = regionBody1.getMinY();
                        int otherMaxY = regionBody1.getMaxY();
                        //boolean one = (otherMaxY >= maxY && maxY > otherMinY) || (otherMaxY < maxY && otherMaxY > minY);
                        boolean two = maxX + xSize >= otherMinX || (otherMaxX + xSize >= minX && maxX > otherMaxX);
                        if (two) {//这个两个区域进行合并
                            regionBody1.setDestroy(true, regionBody.getType());//这个区域被合并了
                            regionBody.setPoint(otherMinX, otherMinY);
                            regionBody.setPoint(otherMaxX, otherMaxY);
                            //regionBody.setX(otherMinX);
                            //regionBody.setX(otherMaxX);
                        }
                    }
                }
            }
        }
    }

    private void createRegion() throws Exception {
        int x = regionMap.getX();
        int y = regionMap.getY();
        for (int i = 0; i < x; i++) {
            Map<Integer, Integer> map = new HashMap<>();
            for (int j = 0; j < y; j++) {
                int type = (int) regionMap.getNumber(i, j);
                if (type > 1) {
                    if (map.containsKey(type)) {
                        map.put(type, map.get(type) + 1);
                    } else {
                        map.put(type, 1);
                    }
                }
            }
            for (Map.Entry<Integer, RegionBody> entry : regionBodyMap.entrySet()) {
                int type = entry.getKey();
                RegionBody regionBody = entry.getValue();
                if (map.containsKey(type)) {//如果这个类型存在
                    int nub = map.get(type);
                    double point = ArithUtil.div(nub, regionNub);
                    if (point > regionTh) {
                        regionBody.setX(i * xSize);
                    }
                }
            }
        }
        for (int j = 0; j < y; j++) {
            Map<Integer, Integer> map = new HashMap<>();
            for (int i = 0; i < x; i++) {
                int type = (int) regionMap.getNumber(i, j);
                if (type > 1) {
                    if (map.containsKey(type)) {
                        map.put(type, map.get(type) + 1);
                    } else {
                        map.put(type, 1);
                    }
                }
            }
            for (Map.Entry<Integer, RegionBody> entry : regionBodyMap.entrySet()) {
                int type = entry.getKey();
                RegionBody regionBody = entry.getValue();
                if (map.containsKey(type)) {//如果这个类型存在
                    int nub = map.get(type);
                    double point = ArithUtil.div(nub, regionNub);
                    if (point > regionTh) {
                        regionBody.setY(j * ySize);
                    }
                }
            }

        }
    }

    private void sigmaPixel() throws Exception {//生成降雨密度图
        int x = matrix.getX();
        int y = matrix.getY();
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
//                if (i == 648 && j > 700) {
//                    System.out.println("x==" + i + ",y==" + j + ",cover==" + cover);
//                    System.out.println("=========");
//                }
                if (cover > th) {//降雨密度图
                    regionMap.setNub(i / xSize, j / ySize, 1);
                }
            }
        }
        mergeRegion();
        System.out.println(regionMap.getString());
        createRegion();
        mergeRegions();
    }

    private int getMinIndex(double[] array, double mySelf) {//获取最小值
        int minIdx = 0;
        for (int i = 0; i < array.length; i++) {
            double nub = array[i];
            if (nub > -1 && nub < mySelf - rainTh && nub < Kernel.maxRain) {
                minIdx = minIdx | (1 << i);
            }
        }
        return minIdx;
    }
}
