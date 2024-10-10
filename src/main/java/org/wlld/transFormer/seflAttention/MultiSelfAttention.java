package org.wlld.transFormer.seflAttention;

import org.wlld.matrixTools.Matrix;
import org.wlld.matrixTools.MatrixOperation;
import org.wlld.i.OutBack;
import org.wlld.transFormer.CodecBlock;
import org.wlld.transFormer.model.MultiSelfAttentionModel;
import org.wlld.transFormer.model.QKVModel;

import java.util.*;

public class MultiSelfAttention {//多头自注意力层
    private final CodecBlock codecBlock;//本层壳子
    private final List<SelfAttention> selfAttentions = new ArrayList<>();
    private LayNorm layNorm;
    private final double studyPoint;
    private Matrix powerMatrix;//权重矩阵
    private final int multiNumber;//头数
    private final int wordVectorDimension;//维度
    private Matrix featureMatrix;//接受到的特征矩阵
    private final int depth;//深度
    private final boolean encoder;
    private final int maxLength;//序列最大长度
    private final boolean selfTimeCode;//使用自增时间序列编码
    private final MatrixOperation matrixOperation;

    public void setLayNorm(LayNorm layNorm) {
        this.layNorm = layNorm;
    }

    public int getDepth() {
        return depth;
    }

    private QKVModel getQKV(List<QKVModel> qkvModelList, int selfID) {
        QKVModel myQKV = null;
        for (QKVModel qkvModel : qkvModelList) {
            if (qkvModel.getSelfID() == selfID) {
                myQKV = qkvModel;
                break;
            }
        }
        return myQKV;
    }

    public void insertModel(MultiSelfAttentionModel multiSelfAttentionModel) throws Exception {
        insertPower(multiSelfAttentionModel.getPowerModel(), powerMatrix);
        List<QKVModel> qkvModelList = multiSelfAttentionModel.getQkvModelList();
        for (int i = 0; i < selfAttentions.size(); i++) {
            QKVModel qkvModel = getQKV(qkvModelList, i);
            if (qkvModel != null) {
                selfAttentions.get(i).insertModel(qkvModel);
            } else {
                throw new Exception("模型与激活参数不匹配!内存与模型文件的多头数量不一致！");
            }
        }
    }

    private void insertPower(double[][] modelPower, Matrix power) throws Exception {
        for (int i = 0; i < power.getX(); i++) {
            for (int j = 0; j < power.getY(); j++) {
                power.setNub(i, j, modelPower[i][j]);
            }
        }
    }

    public MultiSelfAttentionModel getModel() {
        MultiSelfAttentionModel multiSelfAttentionModel = new MultiSelfAttentionModel();
        List<QKVModel> qkvModelList = new ArrayList<>();
        for (SelfAttention selfAttention : selfAttentions) {
            qkvModelList.add(selfAttention.getModel());
        }
        multiSelfAttentionModel.setPowerModel(powerMatrix.getMatrix());
        multiSelfAttentionModel.setQkvModelList(qkvModelList);
        multiSelfAttentionModel.setDepth(depth);
        return multiSelfAttentionModel;
    }

    private void mergeFeatureMatrix(Matrix myMultiFeature, Matrix matrix, int index) throws Exception {//拼接多头特征
        int startY = wordVectorDimension * index;
        int endY = startY + wordVectorDimension;
        for (int i = 0; i < matrix.getX(); i++) {
            for (int j = startY; j < endY; j++) {
                myMultiFeature.setNub(i, j, matrix.getNumber(i, j - startY));
            }
        }
    }

    private List<Matrix> splitMatrix(Matrix subFeature) {//对多头矩阵进行拆分
        List<Matrix> matrixList = new ArrayList<>();
        int maxDeep = subFeature.getX();
        for (int i = 0; i < selfAttentions.size(); i++) {
            Matrix matrix = subFeature.getSonOfMatrix(0, i * wordVectorDimension, maxDeep, wordVectorDimension);
            matrixList.add(matrix);
        }
        return matrixList;
    }

    public void backError(Matrix allErrorMatrix, long eventID) throws Exception {
        Matrix error = matrixOperation.mathMulBySelf(allErrorMatrix, studyPoint);
        //求多头自注意力层权重矩阵的偏导矩阵
        Matrix subPower = matrixOperation.matrixMulPd(error, featureMatrix, powerMatrix, false);
        Matrix subFeature = matrixOperation.matrixMulPd(allErrorMatrix, featureMatrix, powerMatrix, true);
        powerMatrix = matrixOperation.add(powerMatrix, subPower);//更新权重矩阵
        List<Matrix> matrixList = splitMatrix(subFeature);//拆分矩阵
        Matrix allNextFeatureError = null;
        Matrix allLastEncoderError = null;
        for (int i = 0; i < selfAttentions.size(); i++) {//将误差回传到每一个自注意力层
            AttentionError attentionError = getSefAttentionBySelfID(i).backError(matrixList.get(i), eventID);
            Matrix nextFeatureError = attentionError.getNextFeatureError();
            if (allNextFeatureError == null) {
                allNextFeatureError = nextFeatureError;
            } else {
                allNextFeatureError = matrixOperation.add(allNextFeatureError, nextFeatureError);
            }
            if (!encoder && depth > 1) {//深度大于1的解码器
                Matrix lastEncoderError = attentionError.getLastEncoderError();
                if (allLastEncoderError == null) {
                    allLastEncoderError = lastEncoderError;
                } else {
                    allLastEncoderError = matrixOperation.add(allLastEncoderError, lastEncoderError);
                }
            }
        }
        if (!encoder && depth > 1) {//给最后一层编码器传送特征，等待回传
            codecBlock.backLastEncoderError(allLastEncoderError);
        }
        if (codecBlock != null) {
            codecBlock.backCodecError(allNextFeatureError, eventID, allErrorMatrix);//将下层误差发送
        }
    }


    private SelfAttention getSefAttentionBySelfID(int selfID) {
        SelfAttention mySelfAttention = null;
        for (SelfAttention selfAttention : selfAttentions) {
            if (selfAttention.getSelfID() == selfID) {
                mySelfAttention = selfAttention;
                break;
            }
        }
        return mySelfAttention;
    }

    private Matrix countMultiSelfAttention(List<EventBody> eventBodies, boolean isStudy) throws Exception {
        int one = wordVectorDimension * multiNumber;
        Matrix myMultiFeature = null;
        for (int i = 0; i < eventBodies.size(); i++) {
            EventBody eventBody = getEventBodyBySelfID(i, eventBodies);
            Matrix matrix = eventBody.getFeatureMatrix();
            if (i == 0) {
                myMultiFeature = new Matrix(matrix.getX(), one);
            }
            mergeFeatureMatrix(myMultiFeature, matrix, i);
        }
        Matrix out = matrixOperation.mulMatrix(myMultiFeature, powerMatrix);
        if (isStudy) {
            featureMatrix = myMultiFeature;//保存训练时输入的特征矩阵
        }
        return out;
    }

    private EventBody getEventBodyBySelfID(int selfID, List<EventBody> eventBodies) {
        EventBody eventBody = null;
        for (EventBody myEventBody : eventBodies) {
            if (myEventBody.getSelfID() == selfID) {
                eventBody = myEventBody;
                break;
            }
        }
        return eventBody;
    }

    private void addTimeCode(Matrix feature) throws Exception {
        int x = feature.getX();
        int y = feature.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                int k = j / 2;
                double wk = 1 / (Math.pow(10000, 2D * k / y));
                double pe;
                if (j % 2 == 0) {//当列数是偶数
                    pe = Math.sin(wk * i);
                } else {//当列数是奇数
                    pe = Math.cos(wk * i);
                }
                double value = feature.getNumber(i, j) + pe;
                feature.setNub(i, j, value);
            }
        }
    }

    private void addTimeCodeBySelf(Matrix feature) throws Exception {//添加时间编码
        double timeStep = 1D / maxLength;
        int x = feature.getX();
        int y = feature.getY();
        for (int i = 1; i < x; i++) {
            double step = i * timeStep;
            for (int j = 0; j < y; j++) {
                double value = feature.getNumber(i, j) + step;
                feature.setNub(i, j, value);
            }
        }
    }

    public void sendMatrixMessage(long eventID, Matrix feature, boolean isStudy
            , OutBack outBack, List<Integer> E, Matrix encoderFeature) throws Exception {//从输入神经元
        if (depth == 1) {//如果是第一层，则添加时间序列参数
            if (selfTimeCode) {
                addTimeCodeBySelf(feature);
            } else {
                addTimeCode(feature);
            }
        }
        List<EventBody> eventBodies = new ArrayList<>();
        for (SelfAttention selfAttention : selfAttentions) {
            EventBody eventBody = selfAttention.sendMatrixFeature(eventID, isStudy, feature, encoderFeature);
            eventBodies.add(eventBody);
        }
        Matrix matrix = countMultiSelfAttention(eventBodies, isStudy);//多头输出
        layNorm.addNorm(feature, matrix, eventID, isStudy, outBack, E, encoderFeature);//进第一个残差层
    }


    public MultiSelfAttention(int multiNumber, double studyPoint, int depth, int wordVectorDimension, boolean encoder,
                              CodecBlock codecBlock, int maxLength, boolean selfTimeCode, int coreNumber) throws Exception {
        Random random = new Random();
        matrixOperation = new MatrixOperation(coreNumber);
        this.selfTimeCode = selfTimeCode;
        this.maxLength = maxLength;
        this.codecBlock = codecBlock;
        this.encoder = encoder;
        int yiZhi = wordVectorDimension * multiNumber;
        this.studyPoint = studyPoint;
        this.wordVectorDimension = wordVectorDimension;
        this.multiNumber = multiNumber;
        this.depth = depth;
        for (int k = 0; k < multiNumber; k++) {
            SelfAttention selfAttention = new SelfAttention(studyPoint, depth, wordVectorDimension, k, encoder, coreNumber);
            selfAttentions.add(selfAttention);
        }
        powerMatrix = new Matrix(yiZhi, wordVectorDimension);
        int x = powerMatrix.getX();
        int y = powerMatrix.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                powerMatrix.setNub(i, j, random.nextDouble() / yiZhi);
            }
        }
    }

}
