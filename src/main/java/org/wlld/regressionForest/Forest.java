package org.wlld.regressionForest;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.tools.Frequency;

import java.util.*;


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
    private double resultVariance;//结果矩阵方差
    private double median;//结果矩阵中位数
    private double shrinkParameter;//方差收缩参数
    private Matrix pc;//需要映射的基的集合
    private Matrix pc1;//需要映射的基
    private double[] w;
    private boolean isOldG = true;//是否使用老基
    private int oldGId = 0;//老基的id

    public Forest(int featureSize, double shrinkParameter, Matrix pc) {
        this.featureSize = featureSize;
        this.shrinkParameter = shrinkParameter;
        this.pc = pc;
        w = new double[featureSize];
    }

    public double getMedian() {
        return median;
    }

    public double getResultVariance() {
        return resultVariance;
    }

    public void setResultVariance(double resultVariance) {
        this.resultVariance = resultVariance;
    }

    //检测中位数median有多少个一样的值
    private int getEqualNub(double median, double[] dm) {
        int equalNub = 0;
        for (int i = 0; i < dm.length; i++) {
            if (median == dm[i]) {
                equalNub++;
            }
        }
        return equalNub;
    }

    private void findG() throws Exception {//寻找新的切入维度
        // 先尝试从原有维度切入
        int xSize = conditionMatrix.getX();
        int ySize = conditionMatrix.getY();
        Matrix matrix = new Matrix(xSize, ySize);
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                if (j < ySize - 1) {
                    matrix.setNub(i, j, conditionMatrix.getNumber(i, j));
                } else {
                    matrix.setNub(i, j, resultMatrix.getNumber(i, 0));
                }
            }
        }
        double maxOld = 0;
        int type = 0;
        for (int i = 0; i < featureSize; i++) {
            double[] g = new double[conditionMatrix.getX()];
            for (int j = 0; j < g.length; j++) {
                if (i < featureSize - 1) {
                    g[j] = conditionMatrix.getNumber(j, i);
                } else {
                    g[j] = resultMatrix.getNumber(j, 0);
                }
            }
            double var = variance(g);//计算方差
            if (var > maxOld) {
                maxOld = var;
                type = i;
            }
        }
        int x = pc.getX();
        double max = 0;
        for (int i = 0; i < x; i++) {
            Matrix g = pc.getRow(i);
            double gNorm = MatrixOperation.getNorm(g);
            double[] var = new double[xSize];
            for (int j = 0; j < xSize; j++) {
                Matrix parameter = matrix.getRow(j);
                double dist = transG(g, parameter, gNorm);
                var[j] = dist;
            }
            double variance = variance(var);
            if (variance > max) {
                max = variance;
                pc1 = g;
            }
        }
        //找到非原始基最离散的新基
        if (max > maxOld) {//使用新基
            isOldG = false;
        } else {//使用原有基
            isOldG = true;
            oldGId = type;
        }
    }

    private double transG(Matrix g, Matrix parameter, double gNorm) throws Exception {//将数据映射到新基
        //先求内积
        double innerProduct = MatrixOperation.innerProduct(g, parameter);
        return innerProduct / gNorm;
    }

    public void cut() throws Exception {
        int y = resultMatrix.getX();
        if (y > 4) {
            double[] dm = new double[y];
            for (int i = 0; i < y; i++) {
                dm[i] = resultMatrix.getNumber(i, 0);
            }
            Arrays.sort(dm);//排序
            int z = y / 2;
            median = dm[z];
            //检测中位数median有多少个一样的值
            int equalNub = getEqualNub(median, dm);
            //System.out.println("equalNub==" + equalNub + ",y==" + y);
            forestLeft = new Forest(featureSize, shrinkParameter, pc);
            forestRight = new Forest(featureSize, shrinkParameter, pc);
            Matrix conditionMatrixLeft = new Matrix(z + equalNub, featureSize);//条件矩阵左
            Matrix conditionMatrixRight = new Matrix(y - z - equalNub, featureSize);//条件矩阵右
            Matrix resultMatrixLeft = new Matrix(z + equalNub, 1);//结果矩阵左
            Matrix resultMatrixRight = new Matrix(y - z - equalNub, 1);//结果矩阵右
            forestLeft.setConditionMatrix(conditionMatrixLeft);
            forestLeft.setResultMatrix(resultMatrixLeft);
            forestRight.setConditionMatrix(conditionMatrixRight);
            forestRight.setResultMatrix(resultMatrixRight);
            int leftIndex = 0;//左矩阵添加行数
            int rightIndex = 0;//右矩阵添加行数
            double[] resultLeft = new double[z + equalNub];
            double[] resultRight = new double[y - z - equalNub];
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
            System.out.println("var==" + variance + ",leftVar==" + leftVar + ",rightVar==" + rightVar);
            if (leftVar > variance && rightVar > variance) {//不进行拆分，回退
                forestLeft = null;
                forestRight = null;
                median = 0;
            } else {
                forestLeft.setResultVariance(leftVar);
                forestRight.setResultVariance(rightVar);
            }
        }
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
