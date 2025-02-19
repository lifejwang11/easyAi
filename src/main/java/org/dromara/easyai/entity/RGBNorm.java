package org.dromara.easyai.entity;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.tools.RgbRegression;

import java.util.*;

public class RGBNorm {
    private float[] rgbAll;
    private float norm;
    private int nub;
    private float[] rgb;//均值
    private float[] rgbUp;
    private List<float[]> rgbs = new ArrayList<>();//需要对它进行排序
    private List<Float> powers = new ArrayList<>();//需要对它进行排序
    private RgbRegression rgbRegression;
    private int len;
    private float varAll = 1;
    private Matrix avgMatrix;//均值矩阵
    private Matrix varMatrix;//方差矩阵
    private float gmParameter;//gm混合系数
    private float probabilitySigma = 0;//后验概率求和

    public float getVarAll() {
        return varAll;
    }

    public Matrix getAvgMatrix() {
        return avgMatrix;
    }

    public Matrix getVarMatrix() {
        return varMatrix;
    }

    public List<float[]> getRgbs() {
        return rgbs;
    }

    public RgbRegression getRgbRegression() {
        return rgbRegression;
    }

    public void setRgbRegression(RgbRegression rgbRegression) {
        this.rgbRegression = rgbRegression;
    }

    public RGBNorm(float[] rgb, int len) {
        this.len = len;
        rgbAll = new float[len];
        this.rgb = new float[len];
        this.rgbUp = rgb;
        gmParameter = new Random().nextFloat();
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
    public void setGmFeature(float[] feature, float probability) {
        rgbs.add(feature);
        powers.add(probability);
        probabilitySigma = probability + probabilitySigma;
    }

    public void clear() {
        rgbAll = new float[len];
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

    public float getEDist(float[] x) {
        float[] y = new float[x.length];
        for (int i = 0; i < y.length; i++) {
            y[i] = x[i] - rgbUp[i];
        }
        float sigma = 0;
        for (int i = 0; i < y.length; i++) {
            sigma = sigma + (float)Math.pow(y[i], 2);
        }
        return (float)Math.sqrt(sigma);
    }

    public void setColor(float[] rgb) {
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
                float power;
                if (!isPower) {
                    power = 1;
                } else {
                    power = powers.get(j);
                }
                float nub = avgMatrix.getNumber(i, 0) + matrix.getNumber(i, j) * power;
                avgMatrix.setNub(i, 0, nub);
            }
        }
        for (int i = 0; i < x; i++) {
            float nub;
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
            float avg = avgMatrix.getNumber(i, 0);
            for (int j = 0; j < y; j++) {
                float power;
                if (!isPower) {
                    power = 1;
                } else {
                    power = powers.get(j);
                }
                float sub = (float) ((float)Math.pow(matrix.getNumber(i, j) - avg, 2) * power);
                varMatrix.setNub(i, 0, sub + varMatrix.getNumber(i, 0));
            }
        }
        for (int i = 0; i < x; i++) {
            float nub;
            if (probabilitySigma > 0) {
                nub = varMatrix.getNumber(i, 0) / probabilitySigma;
            } else {
                nub = varMatrix.getNumber(i, 0) / y;
            }
            varMatrix.setNub(i, 0, nub);
        }
        return varMatrix;
    }

    public float[] getFeature() throws Exception {
        int avgLen = avgMatrix.getX();
        int length = avgLen * 2 + 1;
        float[] feature = new float[length];
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

    public void insertFeature(float[] feature) throws Exception {
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
                float var = feature[i];
                varMatrix.setNub(t, 0, var);
                varAll = varAll * (float)Math.sqrt(var);
            }
        }
    }

    public void gm() throws Exception {//混合高斯聚类模型参数计算
        int size = rgbs.size();
        if (size > 0) {
            Matrix matrix = new Matrix(len, size);
            for (int i = 0; i < size; i++) {
                float[] rgb = rgbs.get(i);
                for (int j = 0; j < len; j++) {
                    matrix.setNub(j, i, rgb[j]);
                }
            }
            avgMatrix = average(matrix);//均值矩阵
            varMatrix = getVariance(matrix, avgMatrix);//方差矩阵
            varAll = 1;
            for (int i = 0; i < len; i++) {
                float var = (float)Math.sqrt(varMatrix.getNumber(i, 0));
                varAll = varAll * var;
            }
            //确认新的混合系数
            if (probabilitySigma > 0) {
                gmParameter = probabilitySigma / rgbs.size();
            }
        }
    }

    public float getGMProbability(float[] feature) throws Exception {//计算正态分布概率密度
        float zSigma = 0;
        int size = feature.length;
        for (int i = 0; i < size; i++) {
            float sub = (float)Math.pow(feature[i] - avgMatrix.getNumber(i, 0), 2) / varMatrix.getNumber(i, 0);
            zSigma = sub + zSigma;
        }
        float one = 1 / ((float)Math.pow((float)Math.sqrt((float)Math.PI * 2), size) * varAll);
        float two = (float)Math.exp(zSigma * -0.5);
        float n = one * two * gmParameter;
        return n;
    }

    public float getGmParameter() {
        return gmParameter;
    }

    public void setGmParameter(float gmParameter) {
        this.gmParameter = gmParameter;
    }

    public void norm() {//范长计算
        float sigma = 0;
        if (nub > 0) {
            for (int i = 0; i < rgb.length; i++) {
                float rgbc = rgbAll[i] / nub;
                rgb[i] = rgbc;
                sigma = sigma + (float)Math.pow(rgbc, 2);
            }
            norm = (float)Math.sqrt(sigma);
        }
    }

    public void finish() {//进行排序
        RGBListSort rgbListSort = new RGBListSort();
        Collections.sort(rgbs, rgbListSort);
    }

    public float getNorm() {
        return norm;
    }

    public float[] getRgb() {
        return rgb;
    }

    class RGBListSort implements Comparator<float[]> {
        @Override
        public int compare(float[] o1, float[] o2) {
            float o1Norm = 0;
            float o2Norm = 0;
            for (int i = 0; i < o1.length; i++) {
                o1Norm = o1Norm + (float)Math.pow(o1[i], 2);
                o2Norm = o2Norm + (float)Math.pow(o2[i], 2);
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
