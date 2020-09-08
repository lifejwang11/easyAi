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
            w = new double[featureNub];
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

    public double getDist(double[] feature, double result) {//获取特征误差结果
        Forest forestFinish;
        if (result <= min) {//直接找下边界区域
            forestFinish = getLimitRegion(forest, false);
        } else if (result >= max) {//直接找到上边界区域
            forestFinish = getLimitRegion(forest, true);
        } else {
            forestFinish = getRegion(forest, result);
        }
        //计算误差
        double[] w = forestFinish.getW();
        double sigma = 0;
        for (int i = 0; i < w.length; i++) {
            double nub;
            if (i < w.length - 1) {
                nub = w[i] * feature[i];
            } else {
                nub = w[i];
            }
            sigma = sigma + nub;
        }
        return Math.abs(result - sigma);
    }

    private Forest getRegion(Forest forest, double result) {
        double median = forest.getMedian();
        if (median > 0) {//进行了拆分
            if (result > median) {//向右走
                forest = forest.getForestRight();
            } else {//向左走
                forest = forest.getForestLeft();
            }
            return getRegion(forest, result);
        } else {//没有拆分
            return forest;
        }
    }

    private Forest getLimitRegion(Forest forest, boolean isMax) {
        Forest forestSon;
        if (isMax) {
            forestSon = forest.getForestRight();
        } else {
            forestSon = forest.getForestLeft();
        }
        if (forestSon != null) {
            return getLimitRegion(forestSon, isMax);
        } else {
            return forest;
        }
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

    private void regression(Forest forest) throws Exception {//对分段进行线性回归
        Matrix conditionMatrix = forest.getConditionMatrix();
        Matrix resultMatrix = forest.getResultMatrix();
        double[] w = forest.getW();
        Matrix ws = MatrixOperation.getLinearRegression(conditionMatrix, resultMatrix);
        for (int i = 0; i < ws.getX(); i++) {
            w[i] = ws.getNumber(i, 0);
        }
    }
}