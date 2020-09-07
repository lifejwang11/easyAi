package org.wlld.regressionForest;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 回归森林
 */
public class RegressionForest {
    private double[] w;
    private Matrix conditionMatrix;//条件矩阵
    private Matrix resultMatrix;//结果矩阵
    private int featureNub;//特征数量
    private int xIndex = 0;//记录插入位置

    public RegressionForest(int size, int featureNub) throws Exception {//初始化
        if (size > 0 && featureNub > 0) {
            this.featureNub = featureNub;
            w = new double[size];
            conditionMatrix = new Matrix(size, featureNub);
            resultMatrix = new Matrix(size, 1);
        } else {
            throw new Exception("size and featureNub too small");
        }
    }

    public void insertFeature(double[] feature, double result) throws Exception {//插入数据
        if (feature.length == featureNub) {
            for (int i = 0; i < featureNub; i++) {
                if (i < featureNub - 1) {
                    conditionMatrix.setNub(xIndex, i, feature[i]);
                } else {
                    conditionMatrix.setNub(xIndex, i, 1.0);
                    resultMatrix.setNub(xIndex, 0, result);
                }
            }
            xIndex++;
        } else {
            throw new Exception("feature length is not equals");
        }
    }

    public void regression() throws Exception {//开始进行回归
        if (xIndex > 0) {
            Matrix ws = MatrixOperation.getLinearRegression(conditionMatrix, resultMatrix);
            for (int i = 0; i < ws.getX(); i++) {
                w[i] = ws.getNumber(i, 0);
            }
        } else {
            throw new Exception("regression matrix size is zero");
        }
    }
}