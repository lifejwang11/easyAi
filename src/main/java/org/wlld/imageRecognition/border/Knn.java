package org.wlld.imageRecognition.border;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;

import java.util.*;

public class Knn {//KNN分类器
    private Map<Integer, List<Matrix>> featureMap = new HashMap<>();
    private int length;//向量长度(需要返回)
    private int nub;//选择几个人投票

    public Knn(int nub) {
        this.nub = nub;
    }

    public Map<Integer, List<Matrix>> getFeatureMap() {
        return featureMap;
    }

    public void removeType(int type) {
        featureMap.remove(type);
    }

    public void insertMatrix(Matrix vector, int tag) throws Exception {
        if (vector.isVector() && vector.isRowVector()) {
            if (featureMap.size() == 0) {
                List<Matrix> list = new ArrayList<>();
                list.add(vector);
                featureMap.put(tag, list);
                length = vector.getY();
            } else {
                if (length == vector.getY()) {
                    if (featureMap.containsKey(tag)) {
                        featureMap.get(tag).add(vector);
                    } else {
                        List<Matrix> list = new ArrayList<>();
                        list.add(vector);
                        featureMap.put(tag, list);
                    }
                } else {
                    throw new Exception("vector length is different");
                }
            }
        } else {
            throw new Exception("this matrix is not vector or rowVector");
        }
    }

    private void compare(double[] values, int[] types, double value, int type) {
        for (int i = 0; i < values.length; i++) {
            double val = values[i];
            if (val < 0) {
                values[i] = value;
                types[i] = type;
                break;
            } else {
                if (value < val) {
                    for (int j = values.length - 2; j >= i; j--) {
                        values[j + 1] = values[j];
                        types[j + 1] = types[j];
                    }
                    values[i] = value;
                    types[i] = type;
                    break;
                }
            }
        }
    }

    public int getType(Matrix vector) throws Exception {//识别分类
        int ty = 0;
        double[] dists = new double[nub];
        // System.out.println("测试：" + vector.getString());
        int[] types = new int[nub];
        for (int i = 0; i < nub; i++) {
            dists[i] = -1;
        }
        for (Map.Entry<Integer, List<Matrix>> entry : featureMap.entrySet()) {
            int type = entry.getKey();
            List<Matrix> matrices = entry.getValue();
            for (Matrix matrix : matrices) {
                double dist = MatrixOperation.errorNub(vector, matrix, 0);
                // double dist = MatrixOperation.getEDist(matrix, vector);
                compare(dists, types, dist, type);
            }
        }
        //System.out.println(Arrays.toString(types));
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nub; i++) {
            int type = types[i];
            if (map.containsKey(type)) {
                map.put(type, map.get(type) + 1);
            } else {
                map.put(type, 1);
            }
        }
        int max = 0;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            int value = entry.getValue();
            int type = entry.getKey();
            if (value > max) {
                ty = type;
                max = value;
            }
        }
        return ty;
    }
}
