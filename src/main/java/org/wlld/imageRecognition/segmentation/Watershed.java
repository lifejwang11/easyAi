package org.wlld.imageRecognition.segmentation;

import org.wlld.MatrixTools.Matrix;
import org.wlld.config.Kernel;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.imageRecognition.ThreeChannelMatrix;
import org.wlld.param.Cutting;
import org.wlld.tools.ArithUtil;

import java.util.*;

/**
 * @author lidapeng
 * @description 分水岭
 * @date 10:25 上午 2020/1/13
 */
public class Watershed {
    private Matrix matrix;//RGB范数图像
    private Matrix matrixR;//r通道
    private Matrix matrixG;//g通道
    private Matrix matrixB;//b通道
    private Matrix rainfallMap;//降雨图
    private Matrix regionMap;//分区图
    private int xSize;//单元高度
    private int ySize;//单元宽度
    private double th;//灰度阈值
    private int regionNub;//一张图分多少份
    private Map<Integer, RegionBody> regionBodyMap = new HashMap<>();
    private double rainTh = 0;
    private int xMax;
    private int yMax;
    private double maxRain;
    private double width;
    private double height;
    private double edgeSize = 0;//边缘提取多少份
    private double maxIou;//最大iou
    private double rowMark;//行过滤
    private double columnMark;//列过滤
    private List<Specifications> specifications;//过滤候选区参数
    private List<RgbRegression> trayBody;//托盘参数
    private double trayTh;

    public Watershed(ThreeChannelMatrix matrix, List<Specifications> specifications, TempleConfig templeConfig) throws Exception {
        if (matrix != null && specifications != null && specifications.size() > 0) {
            Cutting cutting = templeConfig.getCutting();
            th = cutting.getTh();
            regionNub = cutting.getRegionNub();
            maxRain = cutting.getMaxRain();
            this.matrix = matrix.getMatrixRGB();
            matrixR = matrix.getMatrixR();
            matrixG = matrix.getMatrixG();
            matrixB = matrix.getMatrixB();
            this.specifications = specifications;
            this.trayBody = templeConfig.getFood().getTrayBody();
            if (templeConfig.getEdge() > 0) {
                edgeSize = templeConfig.getEdge();
            }
            rowMark = templeConfig.getFood().getRowMark();
            columnMark = templeConfig.getFood().getColumnMark();
            width = this.matrix.getY();
            height = this.matrix.getX();
            xSize = this.matrix.getX() / regionNub;
            ySize = this.matrix.getY() / regionNub;
            maxIou = templeConfig.getCutting().getMaxIou();
            trayTh = templeConfig.getFood().getTrayTh();
            // System.out.println("xSize===" + xSize + ",ysize===" + ySize);
            rainfallMap = new Matrix(this.matrix.getX(), this.matrix.getY());
            regionMap = new Matrix(regionNub, regionNub);
            xMax = rainfallMap.getX() - 1;
            yMax = rainfallMap.getY() - 1;
        } else {
            throw new Exception("matrix is null");
        }
    }

    private boolean isTray(int x, int y) throws Exception {
        boolean isTray = false;
        if (trayBody != null && trayBody.size() > 0) {
            double[] rgb = new double[]{matrixR.getNumber(x, y) / 255, matrixG.getNumber(x, y) / 255,
                    matrixB.getNumber(x, y) / 255};
            for (RgbRegression rgbRegression : trayBody) {
                double dist = rgbRegression.getDisError(rgb);
                if (dist < trayTh) {
                    isTray = true;
                    break;
                }
            }
        }
        return isTray;
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
            if (isTray(x - 1, y)) {
                top = Kernel.rgbN;
            } else {
                top = matrix.getNumber(x - 1, y);
            }
        }
        if (left == -1 && rainfallMap.getNumber(x, y - 1) == 0) {
            if (isTray(x, y - 1)) {
                left = Kernel.rgbN;
            } else {
                left = matrix.getNumber(x, y - 1);
            }
        }
        if (bottom == -1 && rainfallMap.getNumber(x + 1, y) == 0) {
            if (isTray(x + 1, y)) {
                bottom = Kernel.rgbN;
            } else {
                bottom = matrix.getNumber(x + 1, y);
            }
        }
        if (right == -1 && rainfallMap.getNumber(x, y + 1) == 0) {
            if (isTray(x, y + 1)) {
                right = Kernel.rgbN;
            } else {
                right = matrix.getNumber(x, y + 1);
            }
        }
        if (leftTop == -1 && rainfallMap.getNumber(x - 1, y - 1) == 0) {
            if (isTray(x - 1, y - 1)) {
                leftTop = Kernel.rgbN;
            } else {
                leftTop = matrix.getNumber(x - 1, y - 1);
            }
        }
        if (leftBottom == -1 && rainfallMap.getNumber(x + 1, y - 1) == 0) {
            if (isTray(x + 1, y - 1)) {
                leftBottom = Kernel.rgbN;
            } else {
                leftBottom = matrix.getNumber(x + 1, y - 1);
            }
        }
        if (rightTop == -1 && rainfallMap.getNumber(x - 1, y + 1) == 0) {
            if (isTray(x - 1, y + 1)) {
                rightTop = Kernel.rgbN;
            } else {
                rightTop = matrix.getNumber(x - 1, y + 1);
            }
        }
        if (rightBottom == -1 && rainfallMap.getNumber(x + 1, y + 1) == 0) {
            if (isTray(x + 1, y + 1)) {
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
        for (RegionBody regionBody : regionBodies) {
            int minX = regionBody.getMinX();
            int maxX = regionBody.getMaxX();
            int minY = regionBody.getMinY();
            int maxY = regionBody.getMaxY();
            System.out.println("minX==" + minX + ",minY==" + minY + ",maxX==" + maxX + ",maxY==" + maxY);
        }
        return iou(regionBodies);
    }

    private List<RegionBody> iou(List<RegionBody> regionBodies) {
        List<Integer> list = new ArrayList<>();
        double maxMinX, minMaxX, maxMinY, minMaxY;
        for (int i = 0; i < regionBodies.size(); i++) {
            if (!list.contains(i)) {
                RegionBody regionBody = regionBodies.get(i);
                int minX1 = regionBody.getMinX();
                int minY1 = regionBody.getMinY();
                int maxX1 = regionBody.getMaxX();
                int maxY1 = regionBody.getMaxY();
                int width1 = maxY1 - minY1;
                int height1 = maxX1 - minX1;
                double s1 = width1 * height1;
                for (int j = 0; j < regionBodies.size(); j++) {
                    if (j != i && !list.contains(j)) {
                        RegionBody body = regionBodies.get(j);
                        int minX2 = body.getMinX();
                        int minY2 = body.getMinY();
                        int maxX2 = body.getMaxX();
                        int maxY2 = body.getMaxY();
                        int width2 = maxY2 - minY2;
                        int height2 = maxX2 - minX2;
                        double s2 = width2 * height2;
                        double[] row = new double[]{minX1, maxX1, minX2, maxX2};
                        double[] col = new double[]{minY1, maxY1, minY2, maxY2};
                        Arrays.sort(row);
                        Arrays.sort(col);
                        double rowSub = row[3] - row[0];
                        double colSub = col[3] - col[0];
                        double width = width1 + width2;
                        double height = height1 + height2;
                        double widthSub = width - colSub;
                        double heightSub = height - rowSub;
                        if (widthSub < 0) {
                            widthSub = 0;
                        }
                        if (heightSub < 0) {
                            heightSub = 0;
                        }
                        double intersectS = widthSub * heightSub;//相交面积
                        double s1Point = intersectS / s1;
                        double s2Point = intersectS / s2;
                        if (s1Point > maxIou) {
                            list.add(i);
                        }
                        if (s2Point > maxIou) {
                            list.add(j);
                        }
                    }
                }
            }
        }
        List<RegionBody> regionBodies2 = new ArrayList<>();
        for (int i = 0; i < regionBodies.size(); i++) {
            if (!list.contains(i)) {
                regionBodies2.add(regionBodies.get(i));
            }
        }
        return regionBodies2;
    }

    private boolean check(int minX, int minY, int maxX, int maxY) {
        boolean isRight = false;
        for (Specifications specification : specifications) {
            int width = maxY - minY;
            int height = maxX - minX;
            boolean isCenter = true;
            if (edgeSize > 0) {//边缘过滤
                double top = this.height / edgeSize;//上边缘界限
                double left = this.width / edgeSize;//左边缘界限
                double bottom = this.height - top;//下边缘界限
                double right = this.width - left;//右边缘界限
                isCenter = maxX > top && maxY > left && minX < bottom && minY < right;
            }
            if (width >= specification.getMinWidth() && height >= specification.getMinHeight()
                    && width <= specification.getMaxWidth() && height <= specification.getMaxHeight()
                    && isCenter) {
                isRight = true;
                break;
            }
        }
        return isRight;
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

    private void pixFilter() throws Exception {//进行像素过滤
        int x = regionMap.getX();
        int y = regionMap.getY();
        for (int j = 0; j < y; j++) {
            double sigma = 0.0;
            for (int i = 0; i < x; i++) {
                if (regionMap.getNumber(i, j) > 0.1) {
                    sigma++;
                }
            }
            double cover = sigma / x;
            if (cover < columnMark) {
                for (int k = 0; k < x; k++) {
                    regionMap.setNub(k, j, 0.0);
                }
            }
        }
        for (int i = 0; i < x; i++) {//从行读
            double sigma = 0.0;
            for (int j = 0; j < y; j++) {
                if (regionMap.getNumber(i, j) > 0.1) {
                    sigma++;
                }
            }
            double cover = sigma / y;
            if (cover < rowMark) {
                for (int k = 0; k < y; k++) {
                    regionMap.setNub(i, k, 0.0);
                }
            }
        }
    }

    private void sigmaPixel() throws Exception {//生成降雨密度图
//        int x = matrix.getX();
//        int y = matrix.getY();
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
                if (cover > th) {//降雨密度图
                    regionMap.setNub(i / xSize, j / ySize, 1);
                }
            }
        }
        //System.out.println(regionMap.getString());
        pixFilter();//痕迹过滤
        createMerge();//提取候选区
        merge();//合并候选区
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
