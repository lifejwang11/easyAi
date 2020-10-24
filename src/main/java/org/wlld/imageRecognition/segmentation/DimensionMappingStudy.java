package org.wlld.imageRecognition.segmentation;

import org.wlld.MatrixTools.Matrix;
import org.wlld.i.PsoFunction;
import org.wlld.imageRecognition.RGBNorm;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.pso.PSO;

import java.util.*;


/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 维度映射
 */
public class DimensionMappingStudy {
    private double getDist(Matrix data1, Matrix data2) throws Exception {
        int size = data1.getY();
        double sigma = 0;
        for (int i = 0; i < size; i++) {
            sigma = sigma + Math.pow(data1.getNumber(0, i) - data2.getNumber(0, i), 2);
        }
        return Math.sqrt(sigma);
    }

    private double compareSame(List<Matrix> testFeatures) throws Exception {//比较同类找最小值
        int size = testFeatures.size();
        double max = 0;
        for (int i = 0; i < size; i++) {
            Matrix feature = testFeatures.get(i);
            double min = -1;
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    double dist = getDist(feature, testFeatures.get(j));
                    if (min == -1 || dist < min) {
                        min = dist;
                    }
                }
            }
            if (min > max) {
                max = min;
            }
        }
        return max;
    }

    private double compareDifferent(List<Matrix> featureSame, List<Matrix> featureDifferent) throws Exception {
        double min = -1;//比较异类取最小值
        for (Matrix myFeature : featureSame) {
            for (Matrix feature : featureDifferent) {
                double dist = getDist(myFeature, feature);
                if (min < 0 || dist < min) {
                    min = dist;
                }
            }
        }
        return min;
    }

    class MinType {
        private double minDist;
        private int type;
    }

    public void selfTest(TempleConfig templeConfig) throws Exception {//对模型数据进行自检测
        Map<Integer, List<Matrix>> featureMap = templeConfig.getKnn().getFeatureMap();
        Map<Integer, Double> maxMap = new HashMap<>();//保存与该类别最大间距
        Map<Integer, List<MinType>> minMap = new HashMap<>();//保存与该类别最相似的类别,及距离值
        for (Map.Entry<Integer, List<Matrix>> entry : featureMap.entrySet()) {
            int key = entry.getKey();
            List<MinType> minTypes = new ArrayList<>();
            List<Matrix> myFeatures = entry.getValue();
            double sameMax = compareSame(myFeatures);//同类之间的最大值
            maxMap.put(key, sameMax);
            for (Map.Entry<Integer, List<Matrix>> entry2 : featureMap.entrySet()) {
                int testKey = entry2.getKey();
                if (testKey != key) {//找出最小距离
                    List<Matrix> features = entry2.getValue();
                    MinType minType = new MinType();
                    double minDist = compareDifferent(myFeatures, features);//该类别的最小值
                    if (minDist < sameMax) {
                        minType.minDist = minDist;
                        minType.type = testKey;
                        minTypes.add(minType);
                    }
                }
            }
            minMap.put(key, minTypes);
        }
        for (Map.Entry<Integer, Double> entry : maxMap.entrySet()) {
            int key = entry.getKey();
            double maxValue = entry.getValue();
            System.out.println("=============================================");
            System.out.println("类别：" + key + ",最大类间距为：" + maxValue);
            List<MinType> minTypes = minMap.get(key);
            for (int i = 0; i < minTypes.size(); i++) {
                MinType minType = minTypes.get(i);
                System.out.println("相近类:" + minType.type + ",最小类间距为：" + minType.minDist);
            }
        }
    }

    public void start(TempleConfig templeConfig) throws Exception {
        Map<Integer, List<Matrix>> featureMap = templeConfig.getKnn().getFeatureMap();
        FeatureMapping featureMapping = new FeatureMapping(featureMap);
        int dimensionNub = templeConfig.getFeatureNub() * 3 * 2;//PSO维度
        //创建粒子群
        PSO pso = new PSO(dimensionNub, null, null, 200, 100,
                featureMapping, 0.4, 2, 2, true, 0.2, 0.01);
        List<double[]> mappings = pso.start();
        int size = mappings.size();
        double[] mappingSigma = new double[dimensionNub];
        for (int i = 0; i < size; i++) {
            double[] mapping = mappings.get(i);
            for (int j = 0; j < mapping.length; j++) {
                mappingSigma[j] = mappingSigma[j] + mapping[j];
            }
        }
        for (int i = 0; i < mappingSigma.length; i++) {
            mappingSigma[i] = mappingSigma[i] / size;
        }
        templeConfig.getFood().setMappingParameter(mappingSigma);
        //还要把所有已存在的knn特征完成映射
        featureMapping(featureMap, mappingSigma, templeConfig);
    }

    private void featureMapping(Map<Integer, List<Matrix>> featureMap, double[] mapping
            , TempleConfig templeConfig) throws Exception {
        Map<Integer, List<Matrix>> featureMap2 = new HashMap<>();
        for (Map.Entry<Integer, List<Matrix>> entry : featureMap.entrySet()) {
            int key = entry.getKey();
            List<Matrix> features = entry.getValue();
            List<Matrix> mappingFeatures = new ArrayList<>();
            for (Matrix matrix : features) {
                mappingFeatures.add(mapping(matrix, mapping));
            }
            featureMap2.put(key, mappingFeatures);
        }
        templeConfig.getKnn().setFeatureMap(featureMap2);
    }

    private Matrix mapping(Matrix feature, double[] mapping) throws Exception {
        int size = feature.getY();
        Matrix matrix = new Matrix(1, size);
        for (int i = 0; i < size; i++) {
            double nub = feature.getNumber(0, i) * mapping[i] + mapping[size + i];
            matrix.setNub(0, i, nub);
        }
        return matrix;
    }
}
