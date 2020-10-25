package org.wlld.imageRecognition.segmentation;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.imageRecognition.border.Knn;
import org.wlld.pso.PSO;

import java.util.*;


/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 维度映射
 */
public class DimensionMappingStudy {
    private Knn knn = new Knn(1);//一个knn分类器，决定进这一层哪个类别
    private int id;//类别id
    private Map<Integer, DimensionMappingStudy> deepMappingMap = new HashMap<>();
    private double[] mappingSigma;//映射层

    private double getDist(Matrix data1, Matrix data2) throws Exception {
        int size = data1.getY();
        double sigma = 0;
        for (int i = 0; i < size; i++) {
            sigma = sigma + Math.pow(data1.getNumber(0, i) - data2.getNumber(0, i), 2);
        }
        return Math.sqrt(sigma);
    }

    class MinTypeSort implements Comparator<MinType> {

        @Override
        public int compare(MinType o1, MinType o2) {
            if (o1.minDist < o2.minDist) {
                return -1;
            } else if (o1.minDist > o2.minDist) {
                return 1;
            }
            return 0;
        }
    }

    class MinType {
        private double minDist;
        private int type;
    }

    private double getSub(List<Matrix> featureSame, List<Matrix> featureDifferent) throws Exception {
        double minSub = 0;
        for (int i = 0; i < featureSame.size(); i++) {
            Matrix sameFeature = featureSame.get(i);
            double differentMin = -1;//与异类相比的最小值
            double sameMin = -1;//与同类相比的最小值
            for (int j = 0; j < featureDifferent.size(); j++) {
                Matrix differentFeature = featureDifferent.get(j);
                double dist = getDist(sameFeature, differentFeature);
                if (differentMin < 0 || dist < differentMin) {
                    differentMin = dist;
                }
            }
            for (int k = 0; k < featureSame.size(); k++) {
                if (k != i) {
                    Matrix feature = featureSame.get(k);
                    double dist = getDist(sameFeature, feature);
                    if (sameMin < 0 || dist < sameMin) {
                        sameMin = dist;
                    }
                }
            }
            double sub = differentMin - sameMin;//异类距离与同类距离的差距,选出其中最小的
            if (i == 0) {
                minSub = sub;
            } else if (sub < minSub) {
                minSub = sub;
            }
        }
        return minSub;
    }

    private void nextDeep(TempleConfig templeConfig) throws Exception {//对模型数据进行自检测
        Map<Integer, List<Matrix>> featureMap = knn.getFeatureMap();
        int nub = featureMap.size() / 2;
        if (nub > 1) {
            MinTypeSort minTypeSort = new MinTypeSort();
            Map<Integer, List<MinType>> minMap = new HashMap<>();//保存与该类别最相似的类别,及距离值
            for (Map.Entry<Integer, List<Matrix>> entry : featureMap.entrySet()) {
                int key = entry.getKey();
                List<MinType> minTypes = new ArrayList<>();
                List<Matrix> myFeatures = entry.getValue();
                for (Map.Entry<Integer, List<Matrix>> entry2 : featureMap.entrySet()) {
                    int testKey = entry2.getKey();
                    if (testKey != key) {//找出最小距离
                        List<Matrix> features = entry2.getValue();
                        MinType minType = new MinType();
                        double minDist = getSub(myFeatures, features);
                        minType.minDist = minDist;
                        minType.type = testKey;
                        minTypes.add(minType);
                    }
                }
                Collections.sort(minTypes, minTypeSort);
                int len = 0;
                for (int i = 0; i < minTypes.size(); i++) {
                    if (minTypes.get(i).minDist < 0) {
                        len++;
                    } else {
                        break;
                    }
                }
                if (len < nub) {
                    minTypes = minTypes.subList(0, nub);
                } else {
                    minTypes = minTypes.subList(0, len);
                }
                minMap.put(key, minTypes);
            }
            for (Map.Entry<Integer, List<MinType>> entry : minMap.entrySet()) {
                if (nub == 2 || nub == 3) {
                    System.out.println("类别===============================" + entry.getKey());
                }
                List<MinType> minTypeList = entry.getValue();
                DimensionMappingStudy dimensionMappingStudy = new DimensionMappingStudy();
                Knn myKnn = dimensionMappingStudy.getKnn();
                for (MinType minType : minTypeList) {
                    if (nub == 2 || nub == 3) {
                        System.out.println("type==" + minType.type + ",dist==" + minType.minDist);
                    }
                    List<Matrix> featureList = featureMap.get(minType.type);
                    for (Matrix matrix : featureList) {
                        myKnn.insertMatrix(matrix, minType.type);
                    }
                }
                deepMappingMap.put(entry.getKey(), dimensionMappingStudy);
            }
            for (Map.Entry<Integer, DimensionMappingStudy> entry : deepMappingMap.entrySet()) {
                entry.getValue().start(templeConfig);
            }
        }
    }

    public int toClassification(Matrix feature) throws Exception {//进行识别 先映射
        feature = mapping(feature, mappingSigma);
        int type = knn.getType(feature);
        if (deepMappingMap.size() > 0 && deepMappingMap.containsKey(type)) {//继续下一层
            return deepMappingMap.get(type).toClassification(feature);
        } else {//返回最后结果
            return type;
        }
    }

    public void start(TempleConfig templeConfig) throws Exception {
        Map<Integer, List<Matrix>> featureMap = knn.getFeatureMap();
        FeatureMapping featureMapping = new FeatureMapping(featureMap);
        int dimensionNub = templeConfig.getFeatureNub() * 3 * 2;//PSO维度
        //创建粒子群
        PSO pso = new PSO(dimensionNub, null, null, 500, 100,
                featureMapping, 0.8, 2, 2, true, 0.2, 0.01);
        List<double[]> mappings = pso.start();
        int size = mappings.size();
        mappingSigma = new double[dimensionNub];
        for (int i = 0; i < size; i++) {
            double[] mapping = mappings.get(i);
            for (int j = 0; j < mapping.length; j++) {
                mappingSigma[j] = mappingSigma[j] + mapping[j];
            }
        }
        for (int i = 0; i < mappingSigma.length; i++) {
            mappingSigma[i] = mappingSigma[i] / size;
        }
        //还要把所有已存在的knn特征完成映射
        featureMapping(featureMap, mappingSigma, templeConfig);
        //进行下一次映射选择
        nextDeep(templeConfig);
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

    public Knn getKnn() {
        return knn;
    }
}
