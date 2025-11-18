package org.dromara.easyai.bayesian;

import org.dromara.easyai.randomForest.DataTable;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NativeBayesian {
    private DataTable dataTable;
    // 存储每个类别的先验概率
    private final Map<Integer, Double> priorProbabilities;
    // 存储每个特征在每个类别下的条件概率
    private final Map<String, Map<Integer, Map<Integer, Double>>> conditionalProbabilities;

    public NativeBayesian() {
        priorProbabilities = new ConcurrentHashMap<>();
        conditionalProbabilities = new ConcurrentHashMap<>();
    }

    public NativeBayesian(DataTable dataTable) {
        this();
        this.dataTable = dataTable;
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public int classify(Object object) {
        // 存储每个类别的后验概率
        Map<Integer, Double> posteriorProbabilities = new ConcurrentHashMap<>();
        try {
            // 获取所有可能的类别
            List<Integer> classValues = dataTable.getTable().get(dataTable.getKey());
            Set<Integer> uniqueClasses = new HashSet<>(classValues);

            // 遍历每个类别
            for (int classValue : uniqueClasses) {
                double posteriorProb = priorProbabilities.get(classValue);
                // 遍历每个特征
                for (String feature : dataTable.getKeyType()) {
                    if (!feature.equals(dataTable.getKey())) {
                        String methodName = "get" + feature.substring(0, 1).toUpperCase() + feature.substring(1);
                        Method method = object.getClass().getMethod(methodName);
                        int featureValue = (int) method.invoke(object);
                        // 计算条件概率
                        posteriorProb *= conditionalProbabilities.get(feature).get(classValue).getOrDefault(featureValue, 0.0);
                    }
                }
                posteriorProbabilities.put(classValue, posteriorProb);
            }

            // 找到后验概率最大的类别
            int maxClass = -1;
            double maxProb = -1;
            for (Map.Entry<Integer, Double> entry : posteriorProbabilities.entrySet()) {
                if (entry.getValue() > maxProb) {
                    maxProb = entry.getValue();
                    maxClass = entry.getKey();
                }
            }
            return maxClass;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void study() {

        // 计算先验概率
        List<Integer> classValues = dataTable.getTable().get(dataTable.getKey());
        Map<Integer, Integer> classCounts = new ConcurrentHashMap<>();
        for (int value : classValues) {
            classCounts.put(value, classCounts.getOrDefault(value, 0) + 1);
        }
        for (Map.Entry<Integer, Integer> entry : classCounts.entrySet()) {
            priorProbabilities.put(entry.getKey(), (double) entry.getValue() / dataTable.getLength());
        }

        // 计算条件概率
        for (String feature : dataTable.getKeyType()) {
            if (!feature.equals(dataTable.getKey())) {
                Map<Integer, Map<Integer, Integer>> featureCounts = new ConcurrentHashMap<>();
                List<Integer> featureValues = dataTable.getTable().get(feature);
                for (int i = 0; i < dataTable.getLength(); i++) {
                    int classValue = classValues.get(i);
                    int featureValue = featureValues.get(i);
                    featureCounts.computeIfAbsent(classValue, k -> new ConcurrentHashMap<>()).put(featureValue, featureCounts.get(classValue).getOrDefault(featureValue, 0) + 1);
                }
                Map<Integer, Map<Integer, Double>> featureProbabilities = new ConcurrentHashMap<>();
                for (Map.Entry<Integer, Map<Integer, Integer>> entry : featureCounts.entrySet()) {
                    int classValue = entry.getKey();
                    Map<Integer, Integer> counts = entry.getValue();
                    Map<Integer, Double> probabilities = new ConcurrentHashMap<>();
                    for (Map.Entry<Integer, Integer> countEntry : counts.entrySet()) {
                        int featureValue = countEntry.getKey();
                        int count = countEntry.getValue();
                        probabilities.put(featureValue, (double) count / classCounts.get(classValue));
                    }
                    featureProbabilities.put(classValue, probabilities);
                }
                conditionalProbabilities.put(feature, featureProbabilities);
            }
        }
    }
}

