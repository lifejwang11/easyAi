package org.wlld.regressionForest;

import org.wlld.MatrixTools.Matrix;
import org.wlld.tools.Frequency;

import java.util.Arrays;


/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 分段切割容器
 */
public class Forest extends Frequency {
    private Matrix conditionMatrix;//条件矩阵
    private Matrix resultMatrix;//结果矩阵
    private Forest forestLeft;//左森林
    private Forest forestRight;//右森林
    private int featureSize;
    private double min;//下限
    private double max;//上限
    private double resultVariance;//结果矩阵方差
    private double median;//结果矩阵中位数
    private double shrinkParameter;//方差收缩参数
    private double[] w;

    public Forest(int featureSize, double shrinkParameter) {
        this.featureSize = featureSize;
        this.shrinkParameter = shrinkParameter;
        w = new double[featureSize];
    }

    public double getResultVariance() {
        return resultVariance;
    }

    public void setResultVariance(double resultVariance) {
        this.resultVariance = resultVariance;
    }

    public void cut() throws Exception {
        int y = resultMatrix.getY();
        if (y > 4) {
            double[] dm = new double[y];
            for (int i = 0; i < y; i++) {
                dm[i] = resultMatrix.getNumber(i, 0);
            }
            Arrays.sort(dm);//排序
            int z = y / 2;
            median = dm[z];
            forestLeft = new Forest(featureSize, shrinkParameter);
            forestRight = new Forest(featureSize, shrinkParameter);
            Matrix conditionMatrixLeft = new Matrix(z, featureSize);//条件矩阵左
            Matrix conditionMatrixRight = new Matrix(y - z, featureSize);//条件矩阵右
            Matrix resultMatrixLeft = new Matrix(z, 1);//结果矩阵左
            Matrix resultMatrixRight = new Matrix(y - z, 1);//结果矩阵右
            forestLeft.setConditionMatrix(conditionMatrixLeft);
            forestLeft.setResultMatrix(resultMatrixLeft);
            forestRight.setConditionMatrix(conditionMatrixRight);
            forestRight.setConditionMatrix(resultMatrixRight);
            int leftIndex = 0;//左矩阵添加行数
            int rightIndex = 0;//右矩阵添加行数
            double[] resultLeft = new double[z];
            double[] resultRight = new double[y - z];
            for (int i = 0; i < y; i++) {
                double nub = resultMatrix.getNumber(i, 0);//结果矩阵
                if (nub > median) {//进入右森林并计算右森林结果矩阵方差
                    for (int j = 0; j < featureSize; j++) {//进入右森林的条件矩阵
                        conditionMatrixRight.setNub(rightIndex, j, conditionMatrix.getNumber(i, j));
                    }
                    resultRight[rightIndex] = nub;
                    resultMatrixRight.setNub(rightIndex, 0, nub);
                    rightIndex++;
                } else {//进入左森林并计算左森林结果矩阵方差
                    for (int j = 0; j < featureSize; j++) {//进入右森林的条件矩阵
                        conditionMatrixLeft.setNub(leftIndex, j, conditionMatrix.getNumber(i, j));
                    }
                    resultLeft[leftIndex] = nub;
                    resultMatrixLeft.setNub(leftIndex, 0, nub);
                    leftIndex++;
                }
            }
            //分区完成，计算两棵树结果矩阵的方差
            double leftVar = variance(resultLeft);
            double rightVar = variance(resultRight);
            double variance = resultVariance * shrinkParameter;
            if (leftVar < variance || rightVar < variance) {//继续拆分
                double[] left = getLimit(resultLeft);
                double[] right = getLimit(resultRight);
                forestLeft.setMin(left[0]);
                forestLeft.setMax(left[1]);
                forestRight.setMin(right[0]);
                forestRight.setMax(right[1]);
            } else {//不继续拆分
                forestLeft = null;
                forestRight = null;
            }
        }
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public Matrix getConditionMatrix() {
        return conditionMatrix;
    }

    public void setConditionMatrix(Matrix conditionMatrix) {
        this.conditionMatrix = conditionMatrix;
    }

    public Matrix getResultMatrix() {
        return resultMatrix;
    }

    public void setResultMatrix(Matrix resultMatrix) {
        this.resultMatrix = resultMatrix;
    }

    public double[] getW() {
        return w;
    }

    public void setW(double[] w) {
        this.w = w;
    }

    public Forest getForestLeft() {
        return forestLeft;
    }

    public Forest getForestRight() {
        return forestRight;
    }
}
