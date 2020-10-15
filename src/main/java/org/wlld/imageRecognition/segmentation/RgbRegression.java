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
    private Matrix RGB;//rgb矩阵
    private int xIndex = 0;//记录插入数量
    private boolean isRegression = false;//是否进行了回归
    private int regionNub;
    private int x;
    private int y;

    public Matrix getRGB() {
        return RGB;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Matrix getRGMatrix() {
        return RG;
    }

    public Matrix getBMatrix() {
        return B;
    }

    public void clear(int size) {
        RG = new Matrix(size, 3);
        RGB = new Matrix(size, 3);
        B = new Matrix(size, 1);
        xIndex = 0;
        regionNub = size;
    }

    public int getRegionNub() {
        return regionNub;
    }

    public void setRegionNub(int regionNub) {
        this.regionNub = regionNub;
    }

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
        RGB = new Matrix(size, 3);
        B = new Matrix(size, 1);
        regionNub = size;
        xIndex = 0;
    }

    public void mergeRegion(RgbRegression rgbRegression) throws Exception {
        Matrix myRG = rgbRegression.getRGMatrix();
        Matrix myB = rgbRegression.getBMatrix();
        int nub = rgbRegression.getRegionNub();//合并过来的数据量
        int size = nub + regionNub;//扩容后新的数据量
//        Matrix NRG = new Matrix(size, 3);
//        Matrix NB = new Matrix(size, 1);
//        for (int i = 0; i < size; i++) {
//            Matrix RGT;
//            Matrix BT;
//            int t = i;
//            if (i < regionNub) {//加载本来的数据
//                RGT = RG;
//                BT = B;
//            } else {
//                RGT = myRG;
//                BT = myB;
//                t = t - regionNub;
//            }
//            for (int j = 0; j < 3; j++) {
//                NRG.setNub(i, j, RGT.getNumber(t, j));
//            }
//            NB.setNub(i, 0, BT.getNumber(t, 0));
//        }
        regionNub = size;
//        RG = NRG;
//        B = NB;
//        regression();//最新数据进行回归
    }

    public void insertRGB(double[] rgb) throws Exception {//rgb插入矩阵
        if (rgb.length == 3) {
            RGB.setNub(xIndex, 0, rgb[0]);
            RGB.setNub(xIndex, 1, rgb[1]);
            RGB.setNub(xIndex, 2, rgb[2]);
            RG.setNub(xIndex, 0, rgb[0]);
            RG.setNub(xIndex, 1, rgb[1]);
            RG.setNub(xIndex, 2, 1.0);
            B.setNub(xIndex, 0, rgb[2]);
            xIndex++;
        } else {
            throw new Exception("rgb length is not equals three");
        }
    }

    public boolean regression() throws Exception {//开始进行回归
        if (xIndex > 0) {
            Matrix ws = MatrixOperation.getLinearRegression(RG, B);
            if (ws.getX() == 1 && ws.getY() == 1) {//矩阵奇异
                isRegression = false;
            } else {
                wr = ws.getNumber(0, 0);
                wg = ws.getNumber(1, 0);
                b = ws.getNumber(2, 0);
                isRegression = true;
            }
            return isRegression;
            // System.out.println("wr==" + wr + ",wg==" + wg + ",b==" + b);
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
