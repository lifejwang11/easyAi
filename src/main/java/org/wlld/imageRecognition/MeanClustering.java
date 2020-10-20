package org.wlld.imageRecognition;

import org.wlld.param.Food;

import java.util.*;

//K均值聚类
public class MeanClustering {
    private List<double[]> matrixList = new ArrayList<>();//聚类集合
    private int length;//向量长度(模型需要返回)
    private int speciesQuantity;//种类数量(模型需要返回)
    private List<RGBNorm> matrices = new ArrayList<>();//均值K模型(模型需要返回)
    private int size = 10000;
    private TempleConfig templeConfig;
    private int sensoryNerveNub;//神经元个数
    private List<MeanClustering> kList = new ArrayList<>();

    public List<RGBNorm> getMatrices() {
        return matrices;
    }

    public MeanClustering(int speciesQuantity, TempleConfig templeConfig, boolean isFirst) throws Exception {
        this.speciesQuantity = speciesQuantity;//聚类的数量
        Food food = templeConfig.getFood();
        size = food.getRegressionNub();
        this.templeConfig = templeConfig;
//        if (isFirst) {
//            for (int i = 0; i < speciesQuantity; i++) {
//                kList.add(new MeanClustering(10, templeConfig, false));
//            }
//        }
    }

    public void setColor(double[] color) throws Exception {
        if (matrixList.size() == 0) {
            matrixList.add(color);
            length = color.length;
            sensoryNerveNub = templeConfig.getFeatureNub() * length;
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

    private double[] getListAvg(List<double[]> list) {
        int len = list.get(0).length;
        double[] sigma = new double[len];
        for (double[] rgb : list) {
            for (int i = 0; i < len; i++) {
                sigma[i] = sigma[i] + rgb[i];
            }
        }
        int size = list.size();
        for (int i = 0; i < len; i++) {
            sigma[i] = sigma[i] / size;
        }
        return sigma;
    }

    private List<double[]> listK(List<double[]> listOne, int nub) {
        int size = listOne.size();
        int oneSize = size / nub;//几份取一份平均值
        //System.out.println("oneSize==" + oneSize);
        List<double[]> allList = new ArrayList<>();
        for (int i = 0; i <= size - oneSize; i += oneSize) {
            double[] avg = getListAvg(listOne.subList(i, i + oneSize));
            allList.add(avg);
        }
        return allList;
    }

    private List<double[]> startBp() {
        int times = 2000;
        int index = 0;
        List<double[]> features = new ArrayList<>();
        List<List<double[]>> lists = new ArrayList<>();
        for (int j = 0; j < matrices.size(); j++) {
            List<double[]> listOne = matrices.get(j).getRgbs();
            // List<double[]> list = listK(listOne, times);
            //System.out.println(listOne.size());
            List<double[]> list = listOne.subList(index, times + index);
            lists.add(list);
        }
        for (int j = 0; j < times; j++) {
            double[] feature = new double[sensoryNerveNub];
            for (int i = 0; i < lists.size(); i++) {
                double[] data = lists.get(i).get(j);
                int len = data.length;
                for (int k = 0; k < len; k++) {
                    feature[i * len + k] = data[k];
                }
            }
            features.add(feature);
        }
        return features;
    }

    private List<double[]> startRegression() throws Exception {//开始聚类回归
        for (int i = 0; i < matrices.size(); i++) {
            List<double[]> list = matrices.get(i).getRgbs();
            MeanClustering k = kList.get(i);
            for (double[] rgb : list) {
                k.setColor(rgb);
            }
            k.start(false);
        }
        //遍历子聚类
        int times = 2000;
        Random random = new Random();
        List<double[]> features = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            double[] feature = new double[sensoryNerveNub];
            for (int k = 0; k < kList.size(); k++) {
                MeanClustering mean = kList.get(k);
                List<RGBNorm> rgbNorms = mean.getMatrices();
                double[] rgb = rgbNorms.get(random.nextInt(rgbNorms.size())).getRgb();
                int rgbLen = rgb.length;
                for (int t = 0; t < rgbLen; t++) {
                    int index = k * rgbLen + t;
                    feature[index] = rgb[t];
                }
            }
            //System.out.println(Arrays.toString(feature));
            features.add(feature);
        }
        return features;
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
            for (int i = 0; i < 50; i++) {
                averageMatrix();
                isNext = isNext();
                if (isNext && i < 49) {
                    clear();
                } else {
                    break;
                }
            }
            RGBSort rgbSort = new RGBSort();
            Collections.sort(matrices, rgbSort);
//            for (RGBNorm rgbNorm : matrices) {
//                rgbNorm.finish();
//            }
            // return startBp();
        } else {
            throw new Exception("matrixList number less than 2");
        }
    }
}
