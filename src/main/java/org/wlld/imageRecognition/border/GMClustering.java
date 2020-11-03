package org.wlld.imageRecognition.border;

import org.wlld.MatrixTools.Matrix;
import org.wlld.imageRecognition.MeanClustering;
import org.wlld.imageRecognition.RGBNorm;


/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class GMClustering extends MeanClustering {
    private double regionSize;//单区域面积

    public double getRegionSize() {
        return regionSize;
    }

    public void setRegionSize(double regionSize) {
        this.regionSize = regionSize;
    }

    public GMClustering(int speciesQuantity) throws Exception {
        super(speciesQuantity);
    }

    public double getProbabilityDensity(double[] feature) throws Exception {//获取总概率密度
        double sigma = 0;
        for (RGBNorm rgbNorm : matrices) {
            sigma = sigma + rgbNorm.getGMProbability(feature);
        }
        return sigma;
    }

    @Override
    public void start() throws Exception {
        super.start();
        for (RGBNorm rgbNorm : matrices) {//高斯系数初始化
            rgbNorm.gm();
        }
        for (int i = 0; i < 50; i++) {
            gmClustering();
        }
    }

    public void insertParameter(Matrix matrix) throws Exception {
        int y = matrix.getY();
        int size = y / speciesQuantity;
        for (int i = 0; i <= y - size; i += size) {
            double[] feature = new double[size];
            RGBNorm rgbNorm = new RGBNorm();
            matrices.add(rgbNorm);
            for (int j = i; j < i + size; j++) {
                feature[j - i] = matrix.getNumber(0, i);
            }
            rgbNorm.insertFeature(feature);
        }
    }

    private void clear() {
        for (RGBNorm rgbNorm : matrices) {//高斯系数初始化
            rgbNorm.clearRGB();
        }
    }

    private void gmClustering() throws Exception {//进行gm聚类
        clear();
        for (double[] rgb : matrixList) {//遍历当前集合
            double allProbability = 0;//全概率
            double[] pro = new double[speciesQuantity];
            for (int i = 0; i < speciesQuantity; i++) {
                RGBNorm rgbNorm = matrices.get(i);
                double probability = rgbNorm.getGMProbability(rgb);
                //System.out.println("pro===" + probability);
                allProbability = allProbability + probability;
                pro[i] = probability;
            }
            //求每个簇的后验概率
            for (int i = 0; i < speciesQuantity; i++) {
                pro[i] = pro[i] / allProbability;
            }
            //判断概率最大的簇
            int index = 0;
            double max = 0;
            for (int i = 0; i < speciesQuantity; i++) {
                if (pro[i] > max) {
                    max = pro[i];
                    index = i;
                }
            }
            //注入特征
            matrices.get(index).setGmFeature(rgb, pro[index]);
        }
        for (RGBNorm rgbNorm : matrices) {//高斯系数初始化
            rgbNorm.gm();
        }
    }
}
