package org.wlld.imageRecognition;

import org.wlld.MatrixTools.Matrix;
import org.wlld.imageRecognition.segmentation.RgbRegression;
import org.wlld.tools.ArithUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RGBNorm {
    private double[] rgbAll;
    private double norm;
    private int nub;
    private double[] rgb;//均值
    private double[] rgbUp;
    private List<double[]> rgbs = new ArrayList<>();//需要对它进行排序
    private RgbRegression rgbRegression;
    private int len;
    private double varAll = 1;
    private Matrix avgMatrix;//均值矩阵
    private Matrix varMatrix;//方差矩阵
    private double gmParameter;//gm混合系数
    private double probabilitySigma = 0;//后验概率求和

    public List<double[]> getRgbs() {
        return rgbs;
    }

    public RgbRegression getRgbRegression() {
        return rgbRegression;
    }

    public void setRgbRegression(RgbRegression rgbRegression) {
        this.rgbRegression = rgbRegression;
    }

    RGBNorm(double[] rgb, int len, int speciesQuantity) {
        this.len = len;
        rgbAll = new double[len];
        this.rgb = new double[len];
        this.rgbUp = rgb;
        gmParameter = 1D / speciesQuantity;
    }

    public void syn() {
        rgbUp = rgb;
    }

    public void clearRGB() {
        probabilitySigma = 0;
        rgbs.clear();
    }

    public void setGmFeature(double[] feature, double probability) {
        rgbs.add(feature);
        probabilitySigma = probability + probabilitySigma;
    }

    public void clear() {
        rgbAll = new double[len];
        nub = 0;
        for (int i = 0; i < rgb.length; i++) {
            rgbUp[i] = rgb[i];
        }
        rgbs.clear();
        //System.out.println("clear==" + Arrays.toString(rgbUp));
    }

    public int getNub() {
        return nub;
    }

    public boolean compare() {
        boolean isNext = false;
        for (int i = 0; i < rgb.length; i++) {
            if (rgb[i] != rgbUp[i]) {
                isNext = true;
                break;
            }
        }
        return isNext;
    }

    public double getEDist(double[] x) {
        double[] y = new double[x.length];
        for (int i = 0; i < y.length; i++) {
            y[i] = x[i] - rgbUp[i];
        }
        double sigma = 0;
        for (int i = 0; i < y.length; i++) {
            sigma = sigma + Math.pow(y[i], 2);
        }
        return Math.sqrt(sigma);
    }

    public void setColor(double[] rgb) {
        for (int i = 0; i < rgb.length; i++) {
            rgbAll[i] = rgbAll[i] + rgb[i];
        }
        rgbs.add(rgb);
        nub++;
    }

    private Matrix average(Matrix matrix) throws Exception {//计算平均值
        int x = matrix.getX();
        int y = matrix.getY();
        Matrix avgMatrix = new Matrix(x, 1);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double nub = avgMatrix.getNumber(i, 0) + matrix.getNumber(i, j);
                avgMatrix.setNub(i, 0, nub);
            }
        }
        for (int i = 0; i < x; i++) {
            double nub = avgMatrix.getNumber(i, 0) / y;
            avgMatrix.setNub(i, 0, nub);
        }
        return avgMatrix;
    }

    private Matrix getVariance(Matrix matrix, Matrix avgMatrix) throws Exception {
        int x = matrix.getX();
        int y = matrix.getY();
        Matrix varMatrix = new Matrix(x, 1);
        for (int i = 0; i < x; i++) {
            double avg = avgMatrix.getNumber(i, 0);
            for (int j = 0; j < y; j++) {
                double sub = Math.pow(matrix.getNumber(i, j) - avg, 2);
                varMatrix.setNub(i, 0, sub + varMatrix.getNumber(i, 0));
            }
        }
        for (int i = 0; i < x; i++) {
            double nub = varMatrix.getNumber(i, 0) / y;
            varMatrix.setNub(i, 0, nub);
        }
        return varMatrix;
    }

    public double[] getFeature() throws Exception {
        int avgLen = avgMatrix.getX();
        int length = avgLen * 2 + 1;
        double[] feature = new double[length];
        for (int i = 0; i < length; i++) {
            if (i < length - 1) {
                if (i < avgLen) {
                    feature[i] = avgMatrix.getNumber(i, 0);
                } else {
                    int t = i - avgLen;
                    feature[i] = varMatrix.getNumber(t, 0);
                }
            } else {
                feature[i] = gmParameter;
            }
        }
        return feature;
    }

    public void gm() throws Exception {//混合高斯聚类模型参数计算
        int size = rgbs.size();
        Matrix matrix = new Matrix(len, size);
        for (int i = 0; i < size; i++) {
            double[] rgb = rgbs.get(i);
            for (int j = 0; j < len; j++) {
                matrix.setNub(j, i, rgb[j]);
            }
        }
        avgMatrix = average(matrix);//均值矩阵
        varMatrix = getVariance(matrix, avgMatrix);//方差矩阵
        varAll = 1;
        for (int i = 0; i < len; i++) {
            double var = Math.sqrt(varMatrix.getNumber(i, 0));
            varAll = varAll * var;
        }
        //确认新的混合系数
        if (probabilitySigma > 0) {
            gmParameter = probabilitySigma / rgbs.size();
        }
    }

    public double getGMProbability(double[] feature) throws Exception {//计算正态分布概率
        double zSigma = 0;
        int size = feature.length;
        for (int i = 0; i < size; i++) {
            double sub = Math.pow(feature[i] - avgMatrix.getNumber(i, 0), 2) / varMatrix.getNumber(i, 0);
            zSigma = sub + zSigma;
        }
        double one = 1 / (Math.pow(Math.sqrt(Math.PI * 2), size) * varAll);
        double two = Math.exp(-zSigma / 2);
        return one * two * gmParameter;
    }

    public double getGmParameter() {
        return gmParameter;
    }

    public void setGmParameter(double gmParameter) {
        this.gmParameter = gmParameter;
    }

    public void norm() {//范长计算
        double sigma = 0;
        if (nub > 0) {
            for (int i = 0; i < rgb.length; i++) {
                double rgbc = ArithUtil.div(rgbAll[i], nub);
                rgb[i] = rgbc;
                sigma = sigma + Math.pow(rgbc, 2);
            }
            norm = Math.sqrt(sigma);
        }
    }

    public void finish() {//进行排序
        RGBListSort rgbListSort = new RGBListSort();
        Collections.sort(rgbs, rgbListSort);
    }

    public double getNorm() {
        return norm;
    }

    public double[] getRgb() {
        return rgb;
    }

    class RGBListSort implements Comparator<double[]> {
        @Override
        public int compare(double[] o1, double[] o2) {
            double o1Norm = 0;
            double o2Norm = 0;
            for (int i = 0; i < o1.length; i++) {
                o1Norm = o1Norm + Math.pow(o1[i], 2);
                o2Norm = o2Norm + Math.pow(o2[i], 2);
            }
            if (o1Norm > o2Norm) {
                return 1;
            } else if (o1Norm < o2Norm) {
                return -1;
            }
            return 0;
        }
    }
}
