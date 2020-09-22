package org.wlld.regressionForest;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.tools.Frequency;

import java.util.*;

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
    private Matrix pc;//需要映射的基
    private int cosSize = 20;//cos 分成几份
    private TreeMap<Integer, Forest> forestMap = new TreeMap<>();//节点列表

    public int getCosSize() {
        return cosSize;
    }

    public void setCosSize(int cosSize) {
        this.cosSize = cosSize;
    }

    public RegressionForest(int size, int featureNub, double shrinkParameter) throws Exception {//初始化
        if (size > 0 && featureNub > 0) {
            this.featureNub = featureNub;
            w = new double[featureNub];
            results = new double[size];
            conditionMatrix = new Matrix(size, featureNub);
            resultMatrix = new Matrix(size, 1);
            createG();
            forest = new Forest(featureNub, shrinkParameter, pc, forestMap, 1);
            forestMap.put(1, forest);
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
        if (result > median && forest.getForestRight() != null) {//向右走
            forest = forest.getForestRight();
        } else if (result <= median && forest.getForestLeft() != null) {//向左走
            forest = forest.getForestLeft();
        } else {
            return forest;
        }
        return getRegion(forest, result);
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

    private void createG() throws Exception {//生成新基
        double[] cg = new double[featureNub - 1];
        Random random = new Random();
        double sigma = 0;
        for (int i = 0; i < featureNub - 1; i++) {
            double rm = random.nextDouble();
            cg[i] = rm;
            sigma = sigma + Math.pow(rm, 2);
        }
        double cosOne = 1.0D / cosSize;
        double[] ag = new double[cosSize - 1];//装一个维度内所有角度的余弦值
        for (int i = 0; i < cosSize - 1; i++) {
            double cos = cosOne * (i + 1);
            ag[i] = Math.sqrt(sigma / (1 / Math.pow(cos, 2) - 1));
        }
        int x = (cosSize - 1) * featureNub;
        pc = new Matrix(x, featureNub);
        for (int i = 0; i < featureNub; i++) {//遍历所有的固定基
            //以某个固定基摆动的所有新基集合的矩阵
            Matrix matrix = new Matrix(ag.length, featureNub);
            for (int j = 0; j < ag.length; j++) {
                for (int k = 0; k < featureNub; k++) {
                    if (k != i) {
                        if (k < i) {
                            matrix.setNub(j, k, cg[k]);
                        } else {
                            matrix.setNub(j, k, cg[k - 1]);
                        }
                    } else {
                        matrix.setNub(j, k, ag[j]);
                    }
                }
            }
            //将一个固定基内摆动的新基都装到最大的集合内
            int index = (cosSize - 1) * i;
            push(pc, matrix, index);
        }
    }

    //将两个矩阵从上到下进行合并
    private void push(Matrix mother, Matrix son, int index) throws Exception {
        if (mother.getY() == son.getY()) {
            int x = index + son.getX();
            int y = mother.getY();
            int start = 0;
            for (int i = index; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    mother.setNub(i, j, son.getNumber(start, j));
                }
                start++;
            }
        } else {
            throw new Exception("matrix Y is not equals");
        }
    }

    public void insertFeature(double[] feature, double result) throws Exception {//插入数据
        if (feature.length == featureNub - 1) {
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

    public void startStudy() throws Exception {//开始进行分段
        if (forest != null) {
            //计算方差
            forest.setResultVariance(variance(results));
            double[] limit = getLimit(results);
            min = limit[0];
            max = limit[1];
            start(forest);
            //进行回归
            regression();
            //进行剪枝
            pruning();
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

    private void pruning() throws Exception {//剪枝
        //先获取当前最大id
        int max = forestMap.lastKey();
        int layersNub = (int) (Math.log(max) / Math.log(2));//当前的层数
        int lastMin = (int) Math.pow(2, layersNub);//最后一层最小的id
        if (layersNub > 1) {//先遍历最后一层
            for (Map.Entry<Integer, Forest> entry : forestMap.entrySet()) {
                if (entry.getKey() >= lastMin) {
                    Forest forest = entry.getValue();
                    forest.pruning();
                }
            }
        }
        //每一层从下到上进行剪枝
        for (int i = layersNub - 1; i > 0; i++) {
            int min = (int) Math.pow(2, i);//最后一层最小的id
            int maxNub = (int) Math.pow(2, i + 1);
            for (Map.Entry<Integer, Forest> entry : forestMap.entrySet()) {
                int key = entry.getKey();
                if (key >= min && key < maxNub) {//在范围内，进行剪枝
                    entry.getValue().pruning();
                } else if (key >= maxNub) {
                    break;
                }
            }
        }
        //遍历所有节点，将删除的节点移除
        List<Integer> list = new ArrayList<>();
        for (Map.Entry<Integer, Forest> entry : forestMap.entrySet()) {
            int key = entry.getKey();
            Forest forest = entry.getValue();
            if (forest.isRemove()) {
                list.add(key);
            }
        }
        for (int key : list) {
            forestMap.remove(key);
        }
    }

    private void regression() throws Exception {//开始进行回归
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
        Matrix ws = MatrixOperation.getLinearRegression(conditionMatrix, resultMatrix);
        double[] w = forest.getW();
        for (int i = 0; i < ws.getX(); i++) {
            w[i] = ws.getNumber(i, 0);
        }

    }
}