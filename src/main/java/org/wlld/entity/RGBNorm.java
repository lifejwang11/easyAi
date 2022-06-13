package org.wlld.entity;

import org.wlld.MatrixTools.Matrix;
import org.wlld.tools.ArithUtil;
import org.wlld.tools.RgbRegression;

import java.util.*;

public class RGBNorm {
    private double[] rgbAll;
    private double norm;
    private int nub;
    private double[] rgb;//均值
    private double[] rgbUp;
    private List<double[]> rgbs = new ArrayList<>();//需要对它进行排序
    private List<Double> powers = new ArrayList<>();//需要对它进行排序
    private RgbRegression rgbRegression;
    private int len;
    private double varAll = 1;
    private Matrix avgMatrix;//均值矩阵
    private Matrix varMatrix;//方差矩阵
    private double gmParameter;//gm混合系数
    private double probabilitySigma = 0;//后验概率求和

    public double getVarAll() {
        return varAll;
    }

    public Matrix getAvgMatrix() {
        return avgMatrix;
    }

    public Matrix getVarMatrix() {
        return varMatrix;
    }

    public List<double[]> getRgbs() {
        return rgbs;
    }

    public RgbRegression getRgbRegression() {
        return rgbRegression;
    }

    public void setRgbRegression(RgbRegression rgbRegression) {
        this.rgbRegression = rgbRegression;
    }

    public RGBNorm(double[] rgb, int len) {
        this.len = len;
        rgbAll = new double[len];
        this.rgb = new double[len];
        this.rgbUp = rgb;
        gmParameter = new Random().nextDouble();
    }

    public RGBNorm() {
    }

    public void syn() {
        rgbUp = rgb;
    }

    public void clearRGB() {
        probabilitySigma = 0;
        rgbs.clear();
        powers.clear();
    }

    //特征及权重
    public void setGmFeature(double[] feature, double probability) {
        rgbs.add(feature);
        powers.add(probability);
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
        boolean isPower = powers.size() > 1;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double power;
                if (!isPower) {
                    power = 1;
                } else {
                    power = powers.get(j);
                }
                double nub = avgMatrix.getNumber(i, 0) + matrix.getNumber(i, j) * power;
                avgMatrix.setNub(i, 0, nub);
            }
        }
        for (int i = 0; i < x; i++) {
            double nub;
            if (probabilitySigma > 0) {
                nub = avgMatrix.getNumber(i, 0) / probabilitySigma;
            } else {
                nub = avgMatrix.getNumber(i, 0) / y;
            }
            avgMatrix.setNub(i, 0, nub);
        }
        return avgMatrix;
    }

    private Matrix getVariance(Matrix matrix, Matrix avgMatrix) throws Exception {
        int x = matrix.getX();
        int y = matrix.getY();
        Matrix varMatrix = new Matrix(x, 1);
        boolean isPower = powers.size() > 1;
        for (int i = 0; i < x; i++) {
            double avg = avgMatrix.getNumber(i, 0);
            for (int j = 0; j < y; j++) {
                double power;
                if (!isPower) {
                    power = 1;
                } else {
                    power = powers.get(j);
                }
                double sub = Math.pow(matrix.getNumber(i, j) - avg, 2) * power;
                varMatrix.setNub(i, 0, sub + varMatrix.getNumber(i, 0));
            }
        }
        for (int i = 0; i < x; i++) {
            double nub;
            if (probabilitySigma > 0) {
                nub = varMatrix.getNumber(i, 0) / probabilitySigma;
            } else {
                nub = varMatrix.getNumber(i, 0) / y;
            }
            varMatrix.setNub(i, 0, nub);
        }
        return varMatrix;
    }

    public double[] getFeature() throws Exception {
        int avgLen = avgMatrix.getX();
        int length = avgLen * 2 + 1;
        double[] feature = new double[length];
        for (int i = 0; i < length - 1; i++) {
            if (i < avgLen) {
                feature[i] = avgMatrix.getNumber(i, 0);
            } else {
                int t = i - avgLen;
                feature[i] = varMatrix.getNumber(t, 0);
            }
        }
        feature[length - 1] = gmParameter;
        return feature;
    }

    public void insertFeature(double[] feature) throws Exception {
        int length = feature.length - 1;
        int size = length / 2;
        avgMatrix = new Matrix(size, 1);
        varMatrix = new Matrix(size, 1);
        gmParameter = feature[length];
        varAll = 1;
        for (int i = 0; i < length; i++) {
            if (i < size) {
                avgMatrix.setNub(i, 0, feature[i]);
            } else {
                int t = i - size;
                double var = feature[i];
                varMatrix.setNub(t, 0, var);
                varAll = varAll * Math.sqrt(var);
            }
        }
    }

    public void gm() throws Exception {//混合高斯聚类模型参数计算
        int size = rgbs.size();
        if (size > 0) {
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
    }

    public double getGMProbability(double[] feature) throws Exception {//计算正态分布概率密度
        double zSigma = 0;
        int size = feature.length;
        for (int i = 0; i < size; i++) {
            double sub = Math.pow(feature[i] - avgMatrix.getNumber(i, 0), 2) / varMatrix.getNumber(i, 0);
            zSigma = sub + zSigma;
        }
        double one = 1 / (Math.pow(Math.sqrt(Math.PI * 2), size) * varAll);
        double two = Math.exp(zSigma * -0.5);
        double n = one * two * gmParameter;
        return n;
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
