package org.wlld.imageRecognition.segmentation;

import org.wlld.MatrixTools.Matrix;
import org.wlld.i.PsoFunction;
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

    public FeatureMapping(Map<Integer, List<Matrix>> featureMap) {
        this.featureMap = featureMap;
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

    private double compareSame(List<double[]> testFeatures) {//比较同类找最大值
        int size = testFeatures.size();
        double max = 0;
        for (int i = 0; i < size; i++) {
            double min = -1;
            double[] feature = testFeatures.get(i);
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    double dist = getDist(feature, testFeatures.get(j));
                    if (min < 0 || dist < min) {
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

    private double compareDifferent(List<double[]> featureSame, List<double[]> featureDifferent) {
        double min = -1;//比较异类取最小值
        for (double[] myFeature : featureSame) {
            for (double[] feature : featureDifferent) {
                double dist = getDist(myFeature, feature);
                if (min < 0 || dist < min) {
                    min = dist;
                }
            }
        }
        return min;
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
            double sameMax = compareSame(myFeature);//同类最大值
            double differentMin = compareDifferent(myFeature, featureDifferent);//异类最小值
            double sub = differentMin - sameMax;
            //double sub = getSub(myFeature, featureDifferent);
            sigma = sigma + sub;
        }
        return sigma;
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
