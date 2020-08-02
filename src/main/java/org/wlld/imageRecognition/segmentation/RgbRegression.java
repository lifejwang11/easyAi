package org.wlld.imageRecognition.segmentation;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description rgb回归 B = wr *R + wg * G+b
 */
public class RgbRegression {
    private double wr;
    private double wg;
    private double b;
    private Matrix RG;//rg矩阵
    private Matrix B;//b矩阵
    private int xIndex = 0;//记录插入数量
    private boolean isRegression = false;//是否进行了回归

    public double getWr() {
        return wr;
    }

    public double getWg() {
        return wg;
    }

    public double getB() {
        return b;
    }

    public RgbRegression(int size) {//初始化rgb矩阵
        RG = new Matrix(size, 3);
        B = new Matrix(size, 1);
    }

    public void insertRGB(double[] rgb) throws Exception {//rgb插入矩阵
        if (rgb.length == 3) {
            RG.setNub(xIndex, 0, rgb[0]);
            RG.setNub(xIndex, 1, rgb[1]);
            RG.setNub(xIndex, 2, 1.0);
            B.setNub(xIndex, 0, rgb[2]);
            xIndex++;
        } else {
            throw new Exception("rgb length is not equals three");
        }
    }

    public void regression() throws Exception {//开始进行回归
        if (xIndex > 0) {
            Matrix ws = MatrixOperation.getLinearRegression(RG, B);
            wr = ws.getNumber(0, 0);
            wg = ws.getNumber(1, 0);
            b = ws.getNumber(2, 0);
            isRegression = true;
            System.out.println("wr==" + wr + ",wg==" + wg + ",b==" + b);
        } else {
            throw new Exception("regression matrix size is zero");
        }
    }

    public double getDisError(double[] rgb) throws Exception {//误差距离
        if (isRegression) {
            if (rgb.length == 3) {
                double B = wr * rgb[0] + wg * rgb[1] + b;
                return Math.abs(B - rgb[2]);
            } else {
                throw new Exception("rgb length is not equals three");
            }
        } else {
            throw new Exception("matrix does not regression");
        }
    }
}
