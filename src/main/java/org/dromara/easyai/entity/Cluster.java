package org.dromara.easyai.entity;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author lidapeng
 * @time 2025/10/27 09:47
 * @des 聚类蔟
 */
public class Cluster {
    private double a;//混合系数
    private double powerSigma = 0;//权重之和
    private int dataSize = 0;//数据总量
    private Matrix avgMatrix;//均值矩阵
    private Matrix varMatrix;//方差矩阵
    private final List<Matrix> featureList = new ArrayList<>();//所属该蔟的特征矩阵
    private int tx;
    private int ty;
    private int key;//主键

    public ClusterModel getModel() {
        ClusterModel clusterModel = new ClusterModel();
        clusterModel.setA(a);
        clusterModel.setAvgMatrix(avgMatrix.getMatrixModel());
        clusterModel.setVarMatrix(varMatrix.getMatrixModel());
        clusterModel.setKey(key);
        return clusterModel;
    }

    public void insertModel(ClusterModel clusterModel) {
        a = clusterModel.getA();
        avgMatrix.insertMatrixModel(clusterModel.getAvgMatrix());
        varMatrix.insertMatrixModel(clusterModel.getVarMatrix());
        key = clusterModel.getKey();
    }

    public int getKey() {
        return key;
    }

    public void init(double a, int x, int y, int key) throws Exception {//初始化
        tx = x;
        ty = y;
        this.key = key;
        Random random = new Random();
        this.a = a;
        avgMatrix = new Matrix(x, y);
        varMatrix = new Matrix(x, y);
        initMatrix(random, avgMatrix);
        initMatrix(random, varMatrix);
    }

    public void update() throws Exception {//更新指令
        int size = featureList.size();
        if (size > 0) {
            MatrixOperation matrixOperation = new MatrixOperation();
            a = powerSigma / dataSize;//更新混合系数
            powerSigma = 0;
            dataSize = 0;
            Matrix sigmaMatrix = null;
            for (Matrix feature : featureList) {
                if (sigmaMatrix == null) {
                    sigmaMatrix = feature;
                } else {
                    sigmaMatrix = matrixOperation.add(sigmaMatrix, feature);
                }
            }
            matrixOperation.mathDiv(sigmaMatrix, size);
            avgMatrix = sigmaMatrix.copy();//更新均值矩阵
            Matrix varSubMatrix = new Matrix(tx, ty);
            for (Matrix feature : featureList) {
                getVarSubMatrix(sigmaMatrix, feature, varSubMatrix);
            }
            matrixOperation.mathDiv(varSubMatrix, size);
            varMatrix = varSubMatrix.copy();
            featureList.clear();
        }
    }

    private void getVarSubMatrix(Matrix avgMatrix, Matrix featureMatrix, Matrix varSubMatrix) throws Exception {
        int x = featureMatrix.getX();
        int y = featureMatrix.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float sub = avgMatrix.getNumber(i, j) - featureMatrix.getNumber(i, j);
                float value = varSubMatrix.getNumber(i, j);
                float sp = (float) Math.pow(sub, 2) + value;
                varSubMatrix.setNub(i, j, sp);
            }
        }
    }

    public void insertPower(double power) {
        powerSigma = powerSigma + power;
        dataSize++;
    }

    public void insertFeature(Matrix feature) {
        featureList.add(feature);
    }

    public double getPro(Matrix feature) throws Exception {//计算概率结果
        Paramter paramter = getParameter(feature);
        int size = feature.getX() * feature.getY();
        double s = Math.exp(-paramter.z2 / 2);
        double m = Math.pow(Math.sqrt(2 * Math.PI), size) * paramter.o;
        return (s / m) * a;
    }

    private Paramter getParameter(Matrix feature) throws Exception {
        double z2 = 0;
        double o = 1;
        int x = feature.getX();
        int y = feature.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = feature.getNumber(i, j);//该维度特征参数
                float avg = avgMatrix.getNumber(i, j);//该维度均值
                float var = varMatrix.getNumber(i, j);//该维度方差
                z2 = z2 + Math.pow(value - avg, 2) / var;
                o = o * Math.sqrt(var);
            }
        }
        Paramter paramter = new Paramter();
        paramter.o = o;
        paramter.z2 = z2;
        return paramter;
    }

    private void initMatrix(Random random, Matrix matrix) throws Exception {//矩阵初始化
        int x = matrix.getX();
        int y = matrix.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                matrix.setNub(i, j, random.nextFloat());
            }
        }
    }

    public double getA() {
        return a;
    }

    public Matrix getAvgMatrix() {
        return avgMatrix;
    }

    public Matrix getVarMatrix() {
        return varMatrix;
    }

    static class Paramter {
        double z2;
        double o;
    }
}
