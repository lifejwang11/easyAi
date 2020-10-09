package org.wlld.imageRecognition;

import org.wlld.imageRecognition.segmentation.RgbRegression;

import java.util.*;

//K均值聚类
public class MeanClustering {
    private List<double[]> matrixList = new ArrayList<>();//聚类集合
    private int length;//向量长度(模型需要返回)
    private int speciesQuantity;//种类数量(模型需要返回)
    private List<RGBNorm> matrices = new ArrayList<>();//均值K模型(模型需要返回)
    private int size = 10000;

    public List<RGBNorm> getMatrices() {
        return matrices;
    }

    public MeanClustering(int speciesQuantity, TempleConfig templeConfig) {
        this.speciesQuantity = speciesQuantity;//聚类的数量
        size = templeConfig.getFood().getRegressionNub();
    }

    public void setColor(double[] color) throws Exception {
        if (matrixList.size() == 0) {
            matrixList.add(color);
            length = color.length;
        } else {
            if (length == color.length) {
                matrixList.add(color);
            } else {
                throw new Exception("vector length is different");
            }
        }
    }

    private void averageMatrix() {
        for (double[] rgb : matrixList) {//遍历当前集合
            double min = -1;
            int id = 0;
            for (int i = 0; i < speciesQuantity; i++) {
                RGBNorm rgbNorm = matrices.get(i);
                double dist = rgbNorm.getEDist(rgb);
                if (min == -1 || dist < min) {
                    min = dist;
                    id = i;
                }
            }
            //进簇
            RGBNorm rgbNorm = matrices.get(id);
            rgbNorm.setColor(rgb);
        }
        //重新计算均值
        for (RGBNorm rgbNorm : matrices) {
            rgbNorm.norm();
        }
    }

    private boolean isNext() {
        boolean isNext = false;
        for (RGBNorm rgbNorm : matrices) {
            isNext = rgbNorm.compare();
            if (isNext) {
                break;
            }
        }
        return isNext;
    }

    private void clear() {
        for (RGBNorm rgbNorm : matrices) {
            rgbNorm.clear();
        }
    }

    private void startRegression() throws Exception {//开始聚类回归
        Random random = new Random();
        for (RGBNorm rgbNorm : matrices) {
            RgbRegression rgbRegression = new RgbRegression(size);
            List<double[]> list = rgbNorm.getRgbs();
            for (int i = 0; i < size; i++) {
                double[] rgb = list.get(random.nextInt(list.size()));
                rgb[0] = rgb[0] / 255;
                rgb[1] = rgb[1] / 255;
                rgb[2] = rgb[2] / 255;
                rgbRegression.insertRGB(rgb);
            }
            rgbRegression.regression();
            rgbNorm.setRgbRegression(rgbRegression);
        }
    }

    public void start(boolean isRegression) throws Exception {//开始聚类
        if (matrixList.size() > 1) {
            Random random = new Random();
            for (int i = 0; i < speciesQuantity; i++) {//初始化均值向量
                int index = random.nextInt(matrixList.size());
                double[] rgb = matrixList.get(index);
                RGBNorm rgbNorm = new RGBNorm(rgb, length);
                //要进行深度克隆
                matrices.add(rgbNorm);
            }
            //进行两者的比较
            boolean isNext;
            for (int i = 0; i < 30; i++) {
                averageMatrix();
                isNext = isNext();
                if (isNext && i < 29) {
                    clear();
                } else {
                    break;
                }
            }
            if (isRegression) {
                startRegression();//开始进行回归
            }
        } else {
            throw new Exception("matrixList number less than 2");
        }
    }
}
