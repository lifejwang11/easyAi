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
        Map<Integer, BorderBody> borderBodyMap = templeConfig.getBorderBodyMap();
        BorderBody borderBody = borderBodyMap.get(id);
        if (borderBody == null) {
            borderBody = new BorderBody();
            borderBodyMap.put(id, borderBody);
        }
        //拿到参数矩阵
        Matrix matrixX = borderBody.getX();
        Matrix matrixTx = borderBody.getTx();
        Matrix matrixTy = borderBody.getTy();
        Matrix matrixTw = borderBody.getTw();
        Matrix matrixTh = borderBody.getTh();
        //多元线性回归的四个输出值
        double tx = ArithUtil.div(minX, modelHeight);
        double ty = ArithUtil.div(minY, modelWidth);
        double tw = Math.log(ArithUtil.div(width, modelWidth));
        double th = Math.log(ArithUtil.div(height, modelHeight));
        //进行参数汇集 矩阵转化为行向量
        matrix = MatrixOperation.matrixToVector(matrix, true);
        //最后给一层池化层
        matrix = MatrixOperation.getPoolVector(matrix);
        //将参数矩阵的末尾填1
        matrix = MatrixOperation.push(matrix, 1, true);
        if (matrixX == null) {//如果是第一次直接赋值
            matrixX = matrix;
            matrixTx = new Matrix(1, 1);
            matrixTy = new Matrix(1, 1);
            matrixTw = new Matrix(1, 1);
            matrixTh = new Matrix(1, 1);
            matrixTx.setNub(0, 0, tx);
            matrixTy.setNub(0, 0, ty);
            matrixTw.setNub(0, 0, tw);
            matrixTh.setNub(0, 0, th);
        } else {//将新的参数矩阵添加到原来的末尾
            matrixX = MatrixOperation.pushVector(matrixX, matrix, true);
            matrixTx = MatrixOperation.push(matrixTx, tx, false);
            matrixTy = MatrixOperation.push(matrixTy, ty, false);
            matrixTw = MatrixOperation.push(matrixTw, tw, false);
            matrixTh = MatrixOperation.push(matrixTh, th, false);
        }
        borderBody.setX(matrixX);
        borderBody.setTh(matrixTh);
        borderBody.setTw(matrixTw);
        borderBody.setTx(matrixTx);
        borderBody.setTy(matrixTy);
    }

}
