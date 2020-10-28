package org.wlld.imageRecognition.border;

import org.wlld.imageRecognition.MeanClustering;
import org.wlld.imageRecognition.RGBNorm;
import org.wlld.imageRecognition.TempleConfig;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class GMClustering extends MeanClustering {

    public GMClustering(int speciesQuantity, TempleConfig templeConfig) throws Exception {
        super(speciesQuantity, templeConfig);
    }

    @Override
    public void start(boolean isRegression) throws Exception {
        super.start(isRegression);
        for (RGBNorm rgbNorm : matrices) {//高斯系数初始化
            rgbNorm.gm();
        }
        for (int i = 0; i < 50; i++) {
            gmClustering();
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
