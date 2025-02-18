package org.dromara.easyai.tools;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;

import java.util.*;

public class Knn extends MatrixOperation {//KNN分类器
    private Map<Integer, List<Matrix>> featureMap = new HashMap<>();
    private int length;//向量长度(需要返回)
    private final int nub;//选择几个人投票

    public Knn(int nub) {
        this.nub = nub;
    }

    public void setFeatureMap(Map<Integer, List<Matrix>> featureMap) {
        this.featureMap = featureMap;
    }

    public Map<Integer, List<Matrix>> getFeatureMap() {
        return featureMap;
    }

    public void removeType(int type) {
        featureMap.remove(type);
    }

    public void revoke(int type, int nub) {//撤销一个类别最新的
        List<Matrix> list = featureMap.get(type);
        for (int i = 0; i < nub; i++) {
            list.remove(list.size() - 1);
        }
    }

    public int getNub(int type) {//获取该分类模型的数量
        int nub = 0;
        List<Matrix> list = featureMap.get(type);
        if (list != null) {
            nub = list.size();
        }
        return nub;
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

    private void compare(float[] values, int[] types, float value, int type) {
        for (int i = 0; i < values.length; i++) {
            float val = values[i];
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
        float[] dists = new float[nub];
        // System.out.println("测试：" + vector.getString());
        int[] types = new int[nub];
        for (int i = 0; i < nub; i++) {
            dists[i] = -1;
        }
        for (Map.Entry<Integer, List<Matrix>> entry : featureMap.entrySet()) {
            int type = entry.getKey();
            List<Matrix> matrices = entry.getValue();
            for (Matrix matrix : matrices) {
                float dist = getEDist(matrix, vector);
                compare(dists, types, dist, type);
            }
        }
        System.out.println(Arrays.toString(types));
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
