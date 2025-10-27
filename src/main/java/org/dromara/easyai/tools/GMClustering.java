package org.dromara.easyai.tools;

import org.dromara.easyai.entity.*;
import org.dromara.easyai.matrixTools.Matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 混高聚类
 */
public class GMClustering {
    private final Map<Integer, Cluster> clusters = new HashMap<>();
    private final List<Matrix> featureMatrix = new ArrayList<>();
    private final int maxTimes;//最大迭代次数
    private final int xSize;
    private final int ySize;

    public GMClustering(int speciesQuantity, int maxTimes, int xSize, int ySize) throws Exception {
        this.maxTimes = maxTimes;
        this.xSize = xSize;
        this.ySize = ySize;
        double a = 1d / speciesQuantity;
        for (int i = 0; i < speciesQuantity; i++) {
            Cluster cluster = new Cluster();
            cluster.init(a, xSize, ySize, i + 1);
            clusters.put(i + 1, cluster);
        }
    }

    public void insertModel(GMModel gmModel) {
        List<ClusterModel> cmList = gmModel.getGmmBodyList();
        if (cmList.size() == clusters.size()) {
            for (ClusterModel cm : cmList) {
                int key = cm.getKey();
                Cluster cluster = clusters.get(key);
                if (cluster != null) {
                    cluster.insertModel(cm);
                } else {
                    throw new IllegalArgumentException("模型出现损坏，不合法的主键");
                }
            }
        } else {
            throw new IllegalArgumentException("模型的蔟数与构造函数蔟数不匹配");
        }
    }

    public GMModel getModel() {
        GMModel model = new GMModel();
        List<ClusterModel> cmList = new ArrayList<>();
        for (Map.Entry<Integer, Cluster> entry : clusters.entrySet()) {
            cmList.add(entry.getValue().getModel());
        }
        model.setGmmBodyList(cmList);
        return model;
    }

    private double getSigMa(Map<Integer, Double> map, Matrix feature) throws Exception {
        double sigMa = 0;//概率求和
        for (Map.Entry<Integer, Cluster> entry : clusters.entrySet()) {
            Cluster cluster = entry.getValue();
            double power = cluster.getPro(feature);
            sigMa = sigMa + power;
            map.put(entry.getKey(), power);
        }
        return sigMa;
    }

    public GMMBody getType(Matrix feature) throws Exception {//获取所属蔟类别id
        int key = 0;
        if (feature.getX() == xSize && feature.getY() == ySize) {
            GMMBody gmmBody = new GMMBody();
            Map<Integer, Double> map = new HashMap<>();
            double sigMa = getSigMa(map, feature);
            double maxValue = 0;
            for (Map.Entry<Integer, Double> entry : map.entrySet()) {
                double power = entry.getValue() / sigMa;//该样本属于key的概率
                if (power > maxValue) {
                    maxValue = power;
                    key = entry.getKey();
                }
            }
            gmmBody.setType(key);
            gmmBody.setPower(maxValue);
            return gmmBody;
        } else {
            throw new Exception("特征矩阵大小与构造函数预设大小不符");
        }
    }

    private void clusterAllocation() throws Exception {//分配蔟
        for (Matrix matrix : featureMatrix) {//遍历所有蔟
            Map<Integer, Double> map = new HashMap<>();
            double sigMa = getSigMa(map, matrix);
            int key = 0;
            double maxValue = 0;
            for (Map.Entry<Integer, Double> entry : map.entrySet()) {
                double power = entry.getValue() / sigMa;//该样本属于key的概率
                clusters.get(entry.getKey()).insertPower(power);
                if (power > maxValue) {
                    maxValue = power;
                    key = entry.getKey();
                }
            }
            clusters.get(key).insertFeature(matrix);
        }
        for (Map.Entry<Integer, Cluster> entry : clusters.entrySet()) {
            entry.getValue().update();
        }
    }

    public void start() throws Exception {
        for (int i = 0; i < maxTimes; i++) {
            clusterAllocation();
        }
    }

    public void insertParameter(Matrix matrix) throws Exception {
        if (matrix.getX() == xSize && matrix.getY() == ySize) {
            featureMatrix.add(matrix);
        } else {
            throw new Exception("特征矩阵大小与构造函数预设大小不符");
        }
    }

}
