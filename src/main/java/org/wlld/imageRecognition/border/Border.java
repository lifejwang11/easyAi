package org.wlld.imageRecognition.border;

import org.wlld.MatrixTools.Matrix;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.test.Ma;
import org.wlld.tools.ArithUtil;

/**
 * @author lidapeng
 * @description 图像边框
 * @date 10:37 上午 2020/1/22
 */
public class Border {
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;
    private int width;//宽度
    private int height;//高度
    private double modelX;//标准左上角X坐标
    private double modelY;//标准左上角Y坐标
    private double modelHeight;//标准高度
    private double modelWidth;//标准宽度

    Border(TempleConfig templeConfig, int imageWidth, int imageHeight) {
        int region = templeConfig.getRegion();//将一个区域分成几份
        double row = ArithUtil.div(imageHeight, region);//每一行的像素
        double column = ArithUtil.div(imageWidth, region);//每一列的像素
        int divRow = templeConfig.getRegionRow();//检测物体行占据的份数
        int divColumn = templeConfig.getRegionColumn();//检测物体列占据的份数
        modelHeight = ArithUtil.mul(divRow, row);
        modelWidth = ArithUtil.mul(divColumn, column);
        double regionRow = (region - divRow) / 2;//左上角X份数
        double regionColumn = (region - divColumn) / 2;//左上角Y份数
        modelX = ArithUtil.mul(regionRow, row);
        modelY = ArithUtil.mul(regionColumn, column);
    }

    //输入像素值查找边框四角坐标
    public void setPosition(int x, int y) {
        if (minX > x || minX == 0) {
            minX = x;
        }
        if (minY > y || minY == 0) {
            minY = y;
        }
        if (x > maxX) {
            maxX = x;
        }
        if (y > maxY) {
            maxY = y;
        }
    }

    public void end(Matrix matrix) {//长宽
        height = maxX - minX;
        width = maxY - minY;
        //多元线性回归的四个输出值
        double tx = ArithUtil.div(ArithUtil.sub(minX, modelX), modelHeight);
        double ty = ArithUtil.div(ArithUtil.sub(minY, modelY), modelWidth);
        double tw = Math.log(ArithUtil.div(width, modelWidth));
        double th = Math.log(ArithUtil.div(height, modelHeight));
        //进行参数汇集

    }

}
