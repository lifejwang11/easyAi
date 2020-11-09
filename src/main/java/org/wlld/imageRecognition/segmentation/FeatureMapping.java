package org.wlld.imageRecognition.segmentation;

import org.wlld.MatrixTools.Matrix;
import org.wlld.i.PsoFunction;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.tools.Frequency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 特征映射
 */
public class FeatureMapping extends Frequency implements PsoFunction {
    private Map<Integer, List<Matrix>> featureMap;//特征
    private Map<Integer, List<Matrix>> food = new HashMap<>();//干食品特征
    private Map<Integer, List<Matrix>> notFood = new HashMap<>();//非干食品特征

    private boolean isFood(int[] foodTypes, int key) {
        boolean isFood = false;
        for (int type : foodTypes) {
            if (type == key) {
                isFood = true;
                break;
            }
        }
        return isFood;
    }

    public FeatureMapping(Map<Integer, List<Matrix>> featureMap, TempleConfig templeConfig) {
        this.featureMap = featureMap;
        int[] foodTypes = templeConfig.getFood().getFoodType();
        for (Map.Entry<Integer, List<Matrix>> entry : featureMap.entrySet()) {
            int key = entry.getKey();
            if (isFood(foodTypes, key)) {
                food.put(key, entry.getValue());
            } else {
                notFood.put(key, entry.getValue());
            }
        }
    }

    @Override
    public double getResult(double[] parameter, int id) throws Exception {

        return featuresMapping(parameter);
    }

    private double[] mapping(Matrix matrix, double[] parameter) throws Exception {
        int size = matrix.getY();
        double[] mapping = new double[size];
        for (int i = 0; i < size; i++) {
            mapping[i] = matrix.getNumber(0, i) * parameter[i] + parameter[size + i];
        }
        return mapping;
    }

    private double getDist(double[] data1, double[] data2) {
        int size = data1.length;
        double sigma = 0;
        for (int i = 0; i < size; i++) {
            sigma = sigma + Math.pow(data1[i] - data2[i], 2);
        }
        return Math.sqrt(sigma);
    }

    private double getSub(List<double[]> featureSame, List<double[]> featureDifferent) {
        double minSub = 0;
        for (int i = 0; i < featureSame.size(); i++) {
            double[] sameFeature = featureSame.get(i);
            double differentMin = -1;//与异类相比的最小值
            double sameMin = -1;//与同类相比的最小值
            for (int j = 0; j < featureDifferent.size(); j++) {
                double[] differentFeature = featureDifferent.get(j);
                double dist = getDist(sameFeature, differentFeature);
                if (differentMin < 0 || dist < differentMin) {
                    differentMin = dist;
                }
            }
            for (int k = 0; k < featureSame.size(); k++) {
                if (k != i) {
                    double[] feature = featureSame.get(k);
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

    private double compareDist2(Map<Integer, List<double[]>> foodMapping,
                                List<double[]> featureDifferent) {
        double sigma = 0;
        for (Map.Entry<Integer, List<double[]>> entry : foodMapping.entrySet()) {
            List<double[]> myFeature = entry.getValue();//同类别
            double sub = getSub(myFeature, featureDifferent);
            sigma = sigma + sub;
        }
        return sigma;
    }

    private double compareDist(Map<Integer, List<double[]>> mapping) {//比较距离
        double sigma = 0;
        for (Map.Entry<Integer, List<double[]>> entry : mapping.entrySet()) {
            int key = entry.getKey();
            List<double[]> myFeature = entry.getValue();
            List<double[]> featureDifferent = new ArrayList<>();
            for (Map.Entry<Integer, List<double[]>> entry2 : mapping.entrySet()) {
                if (entry2.getKey() != key) {
                    featureDifferent.addAll(entry2.getValue());
                }
            }
            double sub = getSub(myFeature, featureDifferent);
            sigma = sigma + sub;
        }
        return sigma;
    }

    private double featuresMapping2(double[] parameter) throws Exception {//对当前所有数据进行映射
        Map<Integer, List<double[]>> mapping = new HashMap<>();
        List<double[]> different = new ArrayList<>();
        for (Map.Entry<Integer, List<Matrix>> entry : food.entrySet()) {
            int key = entry.getKey();
            List<Matrix> features = entry.getValue();//当前类别的所有特征
            List<double[]> featureList = new ArrayList<>();
            for (Matrix matrix : features) {
                featureList.add(mapping(matrix, parameter));
            }
            mapping.put(key, featureList);
        }
        for (Map.Entry<Integer, List<Matrix>> entry : notFood.entrySet()) {
            List<Matrix> features = entry.getValue();//当前类别的所有特征
            for (Matrix matrix : features) {
                different.add(mapping(matrix, parameter));
            }
        }
        return compareDist2(mapping, different);
    }

    private double featuresMapping(double[] parameter) throws Exception {//对当前所有数据进行映射
        Map<Integer, List<double[]>> mapping = new HashMap<>();
        for (Map.Entry<Integer, List<Matrix>> entry : featureMap.entrySet()) {
            int key = entry.getKey();
            List<Matrix> features = entry.getValue();//当前类别的所有特征
            List<double[]> featureList = new ArrayList<>();
            for (Matrix matrix : features) {
                featureList.add(mapping(matrix, parameter));
            }
            mapping.put(key, featureList);
        }
        return compareDist(mapping);
    }
}
