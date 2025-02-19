package org.dromara.easyai.tools;


import org.dromara.easyai.entity.RGBNorm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//K均值聚类
public class MeanClustering {
    protected List<float[]> matrixList = new ArrayList<>();//聚类集合
    private int length;//向量长度(模型需要返回)
    protected int speciesQuantity;//种类数量(模型需要返回)
    private final int maxTimes;//最大迭代次数
    protected List<RGBNorm> matrices = new ArrayList<>();//均值K模型(模型需要返回)

    public List<RGBNorm> getMatrices() {
        return matrices;
    }

    public float[] getResultByNorm() {
        MeanSort meanSort = new MeanSort();
        float[] dm = new float[matrices.size() * length];
        matrices.sort(meanSort);
        for (int i = 0; i < matrices.size(); i++) {
            RGBNorm rgbNorm = matrices.get(i);
            float[] rgb = rgbNorm.getRgb();
            for (int j = 0; j < rgb.length; j++) {
                dm[i * rgb.length + j] = rgb[j];
            }
        }
        return dm;
    }

    public MeanClustering(int speciesQuantity, int maxTimes) throws Exception {
        this.speciesQuantity = speciesQuantity;//聚类的数量
        this.maxTimes = maxTimes;
    }

    public void setFeature(float[] feature) throws Exception {
        if (matrixList.isEmpty()) {
            matrixList.add(feature);
            length = feature.length;
        } else {
            if (length == feature.length) {
                matrixList.add(feature);
            } else {
                throw new Exception("vector length is different");
            }
        }
    }

    private void averageMatrix() {
        for (float[] rgb : matrixList) {//遍历当前集合
            float min = -1;
            int id = 0;
            for (int i = 0; i < speciesQuantity; i++) {
                RGBNorm rgbNorm = matrices.get(i);
                float dist = rgbNorm.getEDist(rgb);
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

    public void start() throws Exception {//开始聚类
        if (matrixList.size() > 1) {
            Random random = new Random();
            for (int i = 0; i < speciesQuantity; i++) {//初始化均值向量
                int index = random.nextInt(matrixList.size());
                float[] rgb = matrixList.get(index);
                RGBNorm rgbNorm = new RGBNorm(rgb, length);
                //要进行深度克隆
                matrices.add(rgbNorm);
            }
            //进行两者的比较
            boolean isNext;
            for (int i = 0; i < maxTimes; i++) {
                //System.out.println("聚类：" + i);
                averageMatrix();
                isNext = isNext();
                if (isNext && i < maxTimes - 1) {
                    clear();
                } else {
                    break;
                }
            }
        } else {
            throw new Exception("matrixList number less than 2");
        }
    }
}
