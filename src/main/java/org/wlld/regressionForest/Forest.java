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
    private Matrix matrixAll;//全矩阵
    private double gNorm;//新维度的摸
    private Forest father;//父级
    private Map<Integer, Forest> forestMap;//尽头列表
    private int id;//本节点的id
    private boolean isRemove = false;//是否已经被移除了
    private boolean notRemovable = false;//不可移除

    public Forest(int featureSize, double shrinkParameter, Matrix pc, Map<Integer, Forest> forestMap
            , int id) {
        this.featureSize = featureSize;
        this.shrinkParameter = shrinkParameter;
        this.pc = pc;
        w = new double[featureSize];
        this.forestMap = forestMap;
        this.id = id;
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

    private double[] findG() throws Exception {//寻找新的切入维度
        // 先尝试从原有维度切入
        int xSize = conditionMatrix.getX();
        int ySize = conditionMatrix.getY();
        matrixAll = new Matrix(xSize, ySize);
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                if (j < ySize - 1) {
                    matrixAll.setNub(i, j, conditionMatrix.getNumber(i, j));
                } else {
                    matrixAll.setNub(i, j, resultMatrix.getNumber(i, 0));
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
            double var = dc(g);//计算方差
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
                Matrix parameter = matrixAll.getRow(j);
                double dist = transG(g, parameter, gNorm);
                var[j] = dist;
            }
            double variance = dc(var);
            if (variance > max) {
                max = variance;
                pc1 = g;
            }
        }
        //找到非原始基最离散的新基:
        if (max > maxOld) {//使用新基
            isOldG = false;
        } else {//使用原有基
            isOldG = true;
            oldGId = type;
        }
        return findTwo(xSize);
    }

    private double transG(Matrix g, Matrix parameter, double gNorm) throws Exception {//将数据映射到新基
        //先求内积
        double innerProduct = MatrixOperation.innerProduct(g, parameter);
        return innerProduct / gNorm;
    }

    private double[] findTwo(int dataSize) throws Exception {
        Matrix matrix;//创建一个列向量
        double[] data = new double[dataSize];
        if (isOldG) {//使用原有基
            if (oldGId == featureSize - 1) {//从结果矩阵提取数据
                matrix = resultMatrix;
            } else {//从条件矩阵中提取数据
                matrix = conditionMatrix.getColumn(oldGId);
            }
            //将数据塞入数组
            for (int i = 0; i < dataSize; i++) {
                data[i] = matrix.getNumber(i, 0);
            }
        } else {//使用转换基
            int x = matrixAll.getX();
            gNorm = MatrixOperation.getNorm(pc1);
            for (int i = 0; i < x; i++) {
                Matrix parameter = matrixAll.getRow(i);
                double dist = transG(pc1, parameter, gNorm);
                data[i] = dist;
            }
        }
        Arrays.sort(data);//对数据进行排序
        return data;
    }

    private double getDist(double[] data, double[] w) {
        int len = data.length;
        double sigma = 0;
        for (int i = 0; i < len; i++) {
            double sub = data[i] - w[i];
            sigma = sigma + Math.pow(sub, 2);
        }
        return sigma / len;
    }

    public void pruning() {//进行后剪枝，跟父级进行比较
        System.out.println("执行了剪枝====");
        if (!notRemovable) {
            Forest fatherForest = this.getFather();
            double[] fatherW = fatherForest.getW();
            double sub = getDist(w, fatherW);
            if (sub < shrinkParameter) {//需要剪枝,通知父级

            } else {//通知父级，不需要剪枝,并将父级改为不可移除

            }
        }
    }

    public void getSonMessage(boolean isPruning, int myId) {//进行剪枝
        if (isPruning) {//剪枝
            if (myId == id * 2) {//左节点
                forestLeft = null;
            } else {//右节点
                forestRight = null;
            }
        } else {//不剪枝,将自己变为不可剪枝状态
            notRemovable = true;
        }
    }

    public void cut() throws Exception {
        int y = resultMatrix.getX();
        if (y > 200) {
            System.out.println("-======================");
            double[] dm = findG();
            int z = y / 2;
            median = dm[z];
            int rightNub = 0;
            int leftNub = 0;
            for (int i = 0; i < dm.length; i++) {
                if (dm[i] > median) {
                    rightNub++;
                } else {
                    leftNub++;
                }
            }
            int leftId = 2 * id;
            int rightId = leftId + 1;
            forestLeft = new Forest(featureSize, shrinkParameter, pc, forestMap, leftId);
            forestRight = new Forest(featureSize, shrinkParameter, pc, forestMap, rightId);
            forestMap.put(leftId, forestLeft);
            forestMap.put(rightId, forestRight);
            forestRight.setFather(this);
            forestLeft.setFather(this);
            Matrix conditionMatrixLeft = new Matrix(leftNub, featureSize);//条件矩阵左
            Matrix conditionMatrixRight = new Matrix(rightNub, featureSize);//条件矩阵右
            Matrix resultMatrixLeft = new Matrix(leftNub, 1);//结果矩阵左
            Matrix resultMatrixRight = new Matrix(rightNub, 1);//结果矩阵右
            forestLeft.setConditionMatrix(conditionMatrixLeft);
            forestLeft.setResultMatrix(resultMatrixLeft);
            forestRight.setConditionMatrix(conditionMatrixRight);
            forestRight.setResultMatrix(resultMatrixRight);
            int leftIndex = 0;//左矩阵添加行数
            int rightIndex = 0;//右矩阵添加行数
            for (int i = 0; i < y; i++) {
                double nub;
                if (isOldG) {//使用原有基
                    nub = matrixAll.getNumber(i, oldGId);
                } else {//使用新基
                    Matrix parameter = matrixAll.getRow(i);
                    nub = transG(pc1, parameter, gNorm);
                }
                if (nub > median) {//进入右森林并计算右森林结果矩阵方差
                    for (int j = 0; j < featureSize; j++) {//进入右森林的条件矩阵
                        conditionMatrixRight.setNub(rightIndex, j, conditionMatrix.getNumber(i, j));
                    }
                    resultMatrixRight.setNub(rightIndex, 0, resultMatrix.getNumber(i, 0));
                    rightIndex++;
                } else {//进入左森林并计算左森林结果矩阵方差
                    for (int j = 0; j < featureSize; j++) {//进入右森林的条件矩阵
                        conditionMatrixLeft.setNub(leftIndex, j, conditionMatrix.getNumber(i, j));
                    }
                    resultMatrixLeft.setNub(leftIndex, 0, resultMatrix.getNumber(i, 0));
                    leftIndex++;
                }
            }
            //分区完成
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

    public Forest getFather() {
        return father;
    }

    public void setFather(Forest father) {
        this.father = father;
    }

    public boolean isRemove() {
        return isRemove;
    }

    public void setRemove(boolean remove) {
        isRemove = remove;
    }

    public boolean isNotRemovable() {
        return notRemovable;
    }

    public void setNotRemovable(boolean notRemovable) {
        this.notRemovable = notRemovable;
    }
}
