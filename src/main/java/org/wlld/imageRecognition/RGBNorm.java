package org.wlld.imageRecognition;

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
    private double[] rgb;
    private double[] rgbUp;
    private List<double[]> rgbs = new ArrayList<>();//需要对它进行排序
    private RgbRegression rgbRegression;
    private int len;

    public List<double[]> getRgbs() {
        return rgbs;
    }

    public RgbRegression getRgbRegression() {
        return rgbRegression;
    }

    public void setRgbRegression(RgbRegression rgbRegression) {
        this.rgbRegression = rgbRegression;
    }

    RGBNorm(double[] rgb, int len) {
        this.len = len;
        rgbAll = new double[len];
        this.rgb = new double[len];
        this.rgbUp = rgb;
    }

    public void syn() {
        rgbUp = rgb;
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
