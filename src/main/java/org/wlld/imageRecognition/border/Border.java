package org.wlld.imageRecognition.border;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.tools.ArithUtil;

import java.util.Map;

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
    private double modelHeight;//标准高度
    private double modelWidth;//标准宽度
    private TempleConfig templeConfig;

    public Border(TempleConfig templeConfig, int imageWidth, int imageHeight) {
        modelWidth = imageWidth;
        modelHeight = imageHeight;
        this.templeConfig = templeConfig;
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

    public void end(Matrix matrix, int id) throws Exception {//长宽
        height = maxX - minX;
        width = maxY - minY;
        KClustering kClustering = templeConfig.getKClusteringMap().get(id);
        Matrix positionMatrix = new Matrix(1, 4);
        //多元线性回归的四个输出值
        double tx = ArithUtil.div(minX, modelHeight);
        double ty = ArithUtil.div(minY, modelWidth);
        double tw = Math.log(ArithUtil.div(width, modelWidth));
        double th = Math.log(ArithUtil.div(height, modelHeight));
        Box box = new Box();
        //进行参数汇集 矩阵转化为行向量
        matrix = MatrixOperation.matrixToVector(matrix, true);
        positionMatrix.setNub(0, 0, tx);
        positionMatrix.setNub(0, 1, ty);
        positionMatrix.setNub(0, 2, tw);
        positionMatrix.setNub(0, 3, th);
        box.setMatrix(matrix);
        box.setMatrixPosition(positionMatrix);
        kClustering.setMatrixList(box);
    }

}
