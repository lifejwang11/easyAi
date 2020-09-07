package org.wlld.regressionForest;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.tools.Frequency;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 回归森林
 */
public class RegressionForest extends Frequency {
    private double[] w;
    private Matrix conditionMatrix;//条件矩阵
    private Matrix resultMatrix;//结果矩阵
    private Forest forest;
    private int featureNub;//特征数量
    private int xIndex = 0;//记录插入位置
    private double[] results;//结果数组
    private double min;//结果最小值
    private double max;//结果最大值

    public RegressionForest(int size, int featureNub) throws Exception {//初始化
        if (size > 0 && featureNub > 0) {
            this.featureNub = featureNub;
            w = new double[size];
            results = new double[size];
            conditionMatrix = new Matrix(size, featureNub);
            resultMatrix = new Matrix(size, 1);
            forest = new Forest(featureNub, 0.9);
            forest.setW(w);
            forest.setConditionMatrix(conditionMatrix);
            forest.setResultMatrix(resultMatrix);
        } else {
            throw new Exception("size and featureNub too small");
        }
    }

    public void getDist(double[] feature, double result) {//获取特征误差结果

    }

    public void insertFeature(double[] feature, double result) throws Exception {//插入数据
        if (feature.length == featureNub) {
            for (int i = 0; i < featureNub; i++) {
                if (i < featureNub - 1) {
                    conditionMatrix.setNub(xIndex, i, feature[i]);
                } else {
                    results[xIndex] = result;
                    conditionMatrix.setNub(xIndex, i, 1.0);
                    resultMatrix.setNub(xIndex, 0, result);
                }
            }
            xIndex++;
        } else {
            throw new Exception("feature length is not equals");
        }
    }

    public void start() throws Exception {//开始进行分段
        if (forest != null) {
            double[] limit = getLimit(results);
            min = limit[0];
            max = limit[1];
            start(forest);
        } else {
            throw new Exception("rootForest is null");
        }
    }

    private void start(Forest forest) throws Exception {
        forest.cut();
        Forest forestLeft = forest.getForestLeft();
        Forest forestRight = forest.getForestRight();
        if (forestLeft != null && forestRight != null) {
            start(forestLeft);
            start(forestRight);
        }

    }

    public void regression() throws Exception {//开始进行回归
        if (forest != null) {
            regressionTree(forest);
        } else {
            throw new Exception("rootForest is null");
        }
    }

    private void regressionTree(Forest forest) throws Exception {
        regression(forest);
        Forest forestLeft = forest.getForestLeft();
        Forest forestRight = forest.getForestRight();
        if (forestLeft != null && forestRight != null) {
            regressionTree(forestLeft);
            regressionTree(forestRight);
        }

    }

    private void regression(Forest forest) throws Exception {
        Matrix conditionMatrix = forest.getConditionMatrix();
        Matrix resultMatrix = forest.getResultMatrix();
        double[] w = forest.getW();
        Matrix ws = MatrixOperation.getLinearRegression(conditionMatrix, resultMatrix);
        for (int i = 0; i < ws.getX(); i++) {
            w[i] = ws.getNumber(i, 0);
        }
    }
}