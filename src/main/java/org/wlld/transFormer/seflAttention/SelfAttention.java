package org.wlld.transFormer.seflAttention;

import org.wlld.matrixTools.Matrix;
import org.wlld.matrixTools.MatrixOperation;
import org.wlld.transFormer.model.QKVModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SelfAttention {//自注意力层
    private final Map<Long, MyFeature> featureMatrix = new HashMap<>();//特征矩阵
    private Matrix powerQ;//q权重矩阵
    private Matrix powerK;//k权重矩阵
    private Matrix powerV;//v权重矩阵
    private final int wordVectorDimension;//特征矩阵维度
    private final int depth;//深度
    private final double studyPoint;//学习率
    private final int selfID;
    private final boolean encoder;//是否为编码器模块

    public int getSelfID() {
        return selfID;
    }

    public SelfAttention(double studyPoint, int depth, int wordVectorDimension, int selfID, boolean encoder) throws Exception {
        this.studyPoint = studyPoint;
        this.depth = depth;
        this.encoder = encoder;
        this.wordVectorDimension = wordVectorDimension;
        this.selfID = selfID;
        powerQ = initPowerMatrix(wordVectorDimension);
        powerK = initPowerMatrix(wordVectorDimension);
        powerV = initPowerMatrix(wordVectorDimension);
    }

    public void insertModel(QKVModel qkvModel) throws Exception {
        insertPower(qkvModel.getQ(), powerQ);
        insertPower(qkvModel.getK(), powerK);
        insertPower(qkvModel.getV(), powerV);
    }

    private void insertPower(double[][] modelPower, Matrix power) throws Exception {
        for (int i = 0; i < power.getX(); i++) {
            for (int j = 0; j < power.getY(); j++) {
                power.setNub(i, j, modelPower[i][j]);
            }
        }
    }

    public QKVModel getModel() {
        QKVModel qkvModel = new QKVModel();
        qkvModel.setQ(powerQ.getMatrix());
        qkvModel.setK(powerK.getMatrix());
        qkvModel.setV(powerV.getMatrix());
        qkvModel.setSelfID(selfID);
        return qkvModel;
    }

    private Matrix margeFeature(Matrix matrixFirst, Matrix matrixSecond) throws Exception {//合并前后特征
        Matrix feature = new Matrix(matrixFirst.getX() + matrixSecond.getX(), wordVectorDimension);
        for (int i = 0; i < feature.getX(); i++) {
            for (int j = 0; j < feature.getY(); j++) {
                if (i == 0) {
                    feature.setNub(i, j, matrixFirst.getNumber(0, j));
                } else {
                    feature.setNub(i, j, matrixSecond.getNumber(i - 1, j));
                }
            }
        }
        return feature;
    }


    public AttentionError backError(Matrix feature, long eventID) throws Exception {//返回误差
        MyFeature featureBody = this.featureMatrix.get(eventID);
        MatrixOperation.mathMul(feature, studyPoint);
        Matrix q = featureBody.q;
        Matrix kt = featureBody.kt;
        Matrix v = featureBody.v;
        Matrix qkt = featureBody.qkt;
        Matrix errorV = MatrixOperation.matrixMulPd(feature, qkt, v, false);//先求V的偏导
        Matrix subQktMax = MatrixOperation.matrixMulPd(feature, qkt, v, true);
        Matrix grMatrix = matrixSoftMaxPd(qkt, subQktMax);//对softMax做误差求导
        Matrix errorKt = MatrixOperation.matrixMulPd(grMatrix, q, kt, false);
        Matrix errorQ = MatrixOperation.matrixMulPd(grMatrix, q, kt, true);
        Matrix errorK = MatrixOperation.transPosition(errorKt);
        ErrorFeature QPower = updateError(errorQ, featureBody.allFeature, powerQ);
        Matrix leftMatrix = featureBody.allFeature;
        if (!encoder && depth > 1) {//大于一层的解码器
            leftMatrix = featureBody.encoderFeature;
        }
        ErrorFeature KPower = updateError(errorK, leftMatrix, powerK);
        ErrorFeature VPower = updateError(errorV, leftMatrix, powerV);
        powerQ = QPower.powerMatrix;//更新权重
        powerK = KPower.powerMatrix;
        powerV = VPower.powerMatrix;
        AttentionError attentionError = new AttentionError();
        Matrix nextFeatureError;//下一层误差
        Matrix lastEncoderError = null;//编码器最后一层误差
        if (!encoder && depth > 1) {//大于一层的解码器
            nextFeatureError = QPower.errorFeatureMatrix;
            lastEncoderError = MatrixOperation.add(KPower.errorFeatureMatrix, VPower.errorFeatureMatrix);
        } else {
            nextFeatureError = MatrixOperation.add(MatrixOperation.add(QPower.errorFeatureMatrix, KPower.errorFeatureMatrix),
                    VPower.errorFeatureMatrix);
        }
        attentionError.setNextFeatureError(nextFeatureError);
        attentionError.setLastEncoderError(lastEncoderError);
        this.featureMatrix.remove(eventID);//清除老数据
        return attentionError;
    }

    private ErrorFeature updateError(Matrix errorMatrix, Matrix feature, Matrix powerMatrix) throws Exception {//调整误差
        Matrix errorPower = MatrixOperation.matrixMulPd(errorMatrix, feature, powerMatrix, false);
        Matrix featureError = MatrixOperation.matrixMulPd(errorMatrix, feature, powerMatrix, true);
        Matrix nextPowerMatrix = MatrixOperation.add(powerMatrix, errorPower);
        ErrorFeature errorFeature = new ErrorFeature();
        errorFeature.errorFeatureMatrix = featureError;
        errorFeature.powerMatrix = nextPowerMatrix;
        return errorFeature;
    }

    private Matrix matrixSoftMaxPd(Matrix qkt, Matrix errorMatrix) throws Exception {//对矩阵的softMax求导
        double param = Math.sqrt(wordVectorDimension);
        int x = qkt.getX();
        int y = qkt.getY();
        Matrix grMatrix = new Matrix(x, y);
        for (int i = 0; i < x; i++) {
            Matrix qr = qkt.getRow(i);//该行的行向量
            for (int j = 0; j < y; j++) {
                double jValue = qr.getNumber(0, j);//遍历qr每一个元素，分别对他们求偏导
                int z = qr.getY();
                double sigma = 0;
                for (int k = 0; k < z; k++) {
                    double kValue = qr.getNumber(0, k);//遍历qr每一个元素，分别对他们求偏导
                    double error = errorMatrix.getNumber(i, k);
                    double er;
                    if (k != j) {
                        er = -error * kValue * jValue;
                    } else {
                        er = jValue * (1 - jValue) * error;
                    }
                    sigma = sigma + er;
                }
                double gr = sigma / param;//该位置梯度
                grMatrix.setNub(i, j, gr);
            }
        }
        return grMatrix;
    }


    private void mask(Matrix matrix) throws Exception {
        int x = matrix.getX();
        int y = matrix.getY();
        for (int i = 0; i < x; i++) {
            for (int j = i + 1; j < y; j++) {
                matrix.setNub(i, j, -10000D);
            }
        }
    }

    private Matrix countSelfAttention(long eventID, boolean isStudy) throws Exception {//进行注意力层正向计算
        MyFeature featureBody = this.featureMatrix.get(eventID);
        Matrix myFeature = featureBody.allFeature;
        Matrix kvFeature;
        if (!encoder && depth > 1) {//大于1层的解码模块 使用 编码器输出的矩阵来生成kv矩阵
            kvFeature = featureBody.encoderFeature;
        } else {
            kvFeature = featureBody.allFeature;
        }
        Matrix q = MatrixOperation.mulMatrix(myFeature, powerQ);
        Matrix k = MatrixOperation.mulMatrix(kvFeature, powerK);
        Matrix v = MatrixOperation.mulMatrix(kvFeature, powerV);
        Matrix kt = MatrixOperation.transPosition(k);//k转置
        Matrix qkt = MatrixOperation.mulMatrix(q, kt);
        MatrixOperation.mathDiv(qkt, Math.sqrt(wordVectorDimension));
        //做蒙版
        if (depth == 1 && !encoder) {//第一层解码器 需要先做蒙版操作
            mask(qkt);
        }
        softMax(qkt);
        Matrix result = MatrixOperation.mulMatrix(qkt, v);
        if (!isStudy) {
            this.featureMatrix.remove(eventID);
        } else {
            featureBody.q = q;
            featureBody.kt = kt;
            featureBody.v = v;
            featureBody.qkt = qkt;
        }
        return result;
    }

    private double getRowSoftMaxSigma(Matrix row) throws Exception {
        double sigma = 0;
        for (int i = 0; i < row.getY(); i++) {
            double value = row.getNumber(0, i);
            sigma = Math.exp(value) + sigma;
        }
        return sigma;
    }

    private void softMax(Matrix matrix) throws Exception {//进行softMax处理
        for (int i = 0; i < matrix.getX(); i++) {
            Matrix row = matrix.getRow(i);
            double sigma = getRowSoftMaxSigma(row);
            for (int j = 0; j < matrix.getY(); j++) {
                double self = row.getNumber(0, j);
                double eSelf = Math.exp(self);
                matrix.setNub(i, j, eSelf / sigma);
            }
        }
    }

    public EventBody sendMatrixFeature(long eventID, boolean isStudy, Matrix feature, Matrix encoderFeature) throws Exception {
        EventBody eventBody = new EventBody();
        eventBody.setEventID(eventID);
        eventBody.setSelfID(selfID);
        MyFeature myFeature = new MyFeature();
        myFeature.allFeature = feature;
        myFeature.encoderFeature = encoderFeature;
        featureMatrix.put(eventID, myFeature);
        eventBody.setFeatureMatrix(countSelfAttention(eventID, isStudy));
        return eventBody;
    }


    private Matrix initPowerMatrix(int wordVectorDimension) throws Exception {//初始化权重矩阵
        Random random = new Random();
        Matrix power = new Matrix(wordVectorDimension, wordVectorDimension);
        for (int i = 0; i < wordVectorDimension; i++) {
            for (int j = 0; j < wordVectorDimension; j++) {
                power.setNub(i, j, random.nextDouble() / wordVectorDimension);
            }
        }
        return power;
    }

    static class MyFeature {
        Matrix allFeature;//后方传送特征
        Matrix encoderFeature;//编码器传送特征
        Matrix q;
        Matrix kt;
        Matrix v;
        Matrix qkt;
    }

    static class ErrorFeature {
        Matrix errorFeatureMatrix;
        Matrix powerMatrix;
    }
}
