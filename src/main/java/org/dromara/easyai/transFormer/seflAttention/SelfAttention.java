package org.dromara.easyai.transFormer.seflAttention;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.transFormer.model.QKVModel;

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
    private final float studyPoint;//学习率
    private final int selfID;
    private final boolean encoder;//是否为编码器模块
    private final MatrixOperation matrixOperation;

    public int getSelfID() {
        return selfID;
    }

    public SelfAttention(float studyPoint, int depth, int wordVectorDimension, int selfID, boolean encoder
            , int coreNumber) throws Exception {
        matrixOperation = new MatrixOperation(coreNumber);
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

    private void insertPower(float[][] modelPower, Matrix power) throws Exception {
        for (int i = 0; i < power.getX(); i++) {
            for (int j = 0; j < power.getY(); j++) {
                power.setNub(i, j, modelPower[i][j]);
            }
        }
    }

    public QKVModel getModel() throws Exception {
        QKVModel qkvModel = new QKVModel();
        qkvModel.setQ(powerQ.getMatrix());
        qkvModel.setK(powerK.getMatrix());
        qkvModel.setV(powerV.getMatrix());
        qkvModel.setSelfID(selfID);
        return qkvModel;
    }


    public AttentionError backError(Matrix feature, long eventID) throws Exception {//返回误差
        Matrix myError = matrixOperation.mathMulBySelf(feature, studyPoint);
        MyFeature featureBody = this.featureMatrix.get(eventID);
        Matrix q = featureBody.q;
        Matrix kt = featureBody.kt;
        Matrix v = featureBody.v;
        Matrix qkt = featureBody.qkt;
        Matrix errorV = matrixOperation.matrixMulPd(myError, qkt, v, false);//先求V的偏导
        Matrix subQktMax = matrixOperation.matrixMulPd(feature, qkt, v, true);
        Matrix grMatrix = matrixOperation.matrixSoftMaxPd(qkt, subQktMax, wordVectorDimension);//对softMax做误差求导
        if (depth == 1 && !encoder) {
            backMask(grMatrix);
        }
        Matrix errorKt = matrixOperation.matrixMulPd(grMatrix, q, kt, false);
        Matrix errorQ = matrixOperation.matrixMulPd(grMatrix, q, kt, true);
        Matrix errorK = matrixOperation.transPosition(errorKt);
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
            lastEncoderError = matrixOperation.add(KPower.errorFeatureMatrix, VPower.errorFeatureMatrix);
        } else {
            nextFeatureError = matrixOperation.addThreeMatrix(QPower.errorFeatureMatrix, KPower.errorFeatureMatrix
                    , VPower.errorFeatureMatrix);
        }
        attentionError.setNextFeatureError(nextFeatureError);
        attentionError.setLastEncoderError(lastEncoderError);
        this.featureMatrix.remove(eventID);//清除老数据
        return attentionError;
    }

    private ErrorFeature updateError(Matrix errorMatrix, Matrix feature, Matrix powerMatrix) throws Exception {//调整误差
        Matrix errorPower = matrixOperation.matrixMulPd(errorMatrix, feature, powerMatrix, false);
        Matrix featureError = matrixOperation.matrixMulPd(errorMatrix, feature, powerMatrix, true);
        Matrix nextPowerMatrix = matrixOperation.add(powerMatrix, errorPower);
        ErrorFeature errorFeature = new ErrorFeature();
        errorFeature.errorFeatureMatrix = featureError;
        errorFeature.powerMatrix = nextPowerMatrix;
        return errorFeature;
    }

    private void backMask(Matrix matrix) throws Exception {
        int x = matrix.getX();
        int y = matrix.getY();
        for (int i = 0; i < x; i++) {
            for (int j = i + 1; j < y; j++) {
                matrix.setNub(i, j, 0f);
            }
        }
    }

    private void mask(Matrix matrix) throws Exception {
        int x = matrix.getX();
        int y = matrix.getY();
        for (int i = 0; i < x; i++) {
            for (int j = i + 1; j < y; j++) {
                matrix.setNub(i, j, -1000f);
            }
        }
    }

    private Matrix countSelfAttention(long eventID, boolean isStudy) throws Exception {//进行注意力层正向计算
        MyFeature featureBody = this.featureMatrix.get(eventID);
        Matrix myFeature = featureBody.allFeature;
        Matrix kvFeature;
        if (!encoder && depth > 1) {//大于1层的解码模块 使用 编码器输出的矩阵来生成kv矩阵
            kvFeature = featureBody.encoderFeature;
            //System.out.println(kvFeature);
        } else {
            kvFeature = featureBody.allFeature;
        }
        Matrix q = matrixOperation.mulMatrix(myFeature, powerQ);
        Matrix k = matrixOperation.mulMatrix(kvFeature, powerK);
        Matrix v = matrixOperation.mulMatrix(kvFeature, powerV);
        Matrix kt = matrixOperation.transPosition(k);//k转置
        Matrix qkt = matrixOperation.mulMatrix(q, kt);
        matrixOperation.mathDiv(qkt, (float) Math.sqrt(wordVectorDimension));
        //做蒙版
        if (depth == 1 && !encoder) {//第一层解码器 需要先做蒙版操作
            mask(qkt);
        }
        matrixOperation.softMax(qkt);
        Matrix result = matrixOperation.mulMatrix(qkt, v);
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
                power.setNub(i, j, random.nextFloat() / wordVectorDimension);
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
