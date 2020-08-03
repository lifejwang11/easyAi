package org.wlld.imageRecognition;

import org.wlld.imageRecognition.segmentation.RgbRegression;
import org.wlld.tools.ArithUtil;

import java.util.ArrayList;
import java.util.List;

public class RGBNorm {
    private double[] rgbAll = new double[3];
    private double norm;
    private int nub;
    private double[] rgb = new double[3];
    private double[] rgbUp;
    private List<double[]> rgbs = new ArrayList<>();
    private RgbRegression rgbRegression;

    public RgbRegression getRgbRegression() {
        return rgbRegression;
    }

    public void setRgbRegression(RgbRegression rgbRegression) {
        this.rgbRegression = rgbRegression;
    }

    RGBNorm(double[] rgb) {
        this.rgbUp = rgb;
    }

    public void syn() {
        rgbUp = rgb;
    }

    public List<double[]> getRgbs() {
        return rgbs;
    }

    public void clear() {
        rgbAll = new double[3];
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

    public double getNorm() {
        return norm;
    }

    public double[] getRgb() {
        return rgb;
    }
}
