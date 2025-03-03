package org.dromara.easyai.unet;

import org.dromara.easyai.conv.ConvCount;
import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.nerveEntity.ConvParameter;

import java.util.*;

/**
 * @author lidapeng
 * @time 2025/3/2 07:51
 * @des unet编码器
 */
public class UNetEncoder extends ConvCount {
    private final ConvParameter convParameter = new ConvParameter();//内存中卷积层模型及临时数据
    private final MatrixOperation matrixOperation = new MatrixOperation();
    private final int kerSize;
    private final float studyRate;//学习率
    private final int deep;//当前深度
    private final int channelNum;//通道数
    private final int convTimes;//卷积层数
    private Matrix decodeErrorMatrix;//从解码器传来的误差矩阵
    private final ActiveFunction activeFunction;
    private UNetEncoder afterEncoder;//下一个编码器
    private UNetEncoder beforeEncoder;//上一个编码器
    private UNetDecoder decoder;//下一个解码器

    public UNetEncoder(int kerSize, int convTimes, int deep, int channelNum, ActiveFunction activeFunction
            , float studyRate) throws Exception {//核心大小
        Random random = new Random();
        this.studyRate = studyRate;
        this.kerSize = kerSize;
        this.activeFunction = activeFunction;
        this.deep = deep;
        this.channelNum = channelNum;
        this.convTimes = convTimes;
        List<Float> oneConvPower = convParameter.getOneConvPower();
        List<Matrix> nerveMatrixList = convParameter.getNerveMatrixList();
        for (int i = 0; i < convTimes; i++) {
            initNervePowerMatrix(random, nerveMatrixList);
        }
        if (deep == 1) {
            for (int i = 0; i < channelNum; i++) {
                oneConvPower.add(random.nextFloat() / channelNum);
            }
        }
    }

    protected void setDecodeErrorMatrix(Matrix decodeErrorMatrix) {
        this.decodeErrorMatrix = decodeErrorMatrix;
    }

    protected Matrix getAfterConvMatrix(long eventID) {//卷积后的矩阵
        Matrix outMatrix = convParameter.getFeatureMap().get(eventID);
        convParameter.getFeatureMap().remove(eventID);
        return outMatrix;
    }

    //发送特征三通道矩阵
    public void sendThreeChannel(long eventID, OutBack outBack, ThreeChannelMatrix feature, ThreeChannelMatrix featureE,
                                 boolean study) throws Exception {
        if (channelNum != 3) {
            throw new Exception("使用本方法，通道数必须设置为3");
        }
        if (study && featureE == null) {
            throw new Exception("训练时期望矩阵不能为空");
        }
        List<Matrix> matrixList = new ArrayList<>();
        matrixList.add(feature.getMatrixR());
        matrixList.add(feature.getMatrixG());
        matrixList.add(feature.getMatrixB());
        sendMatrixList(eventID, outBack, featureE, matrixList, study);
    }

    protected void sendFeature(long eventID, OutBack outBack, ThreeChannelMatrix featureE,
                               Matrix myFeature, boolean study) throws Exception {
        Matrix convMatrix = downConvAndPooling(myFeature, convParameter, convTimes, activeFunction, kerSize, true, eventID);
        if (afterEncoder != null) {//后面还有编码器，继续向后传递
            afterEncoder.sendFeature(eventID, outBack, featureE, convMatrix, study);
        } else {//向解码器传递
            decoder.sendFeature(eventID, outBack, featureE, convMatrix, study);
        }
    }

    protected void backError(Matrix errorMatrix) throws Exception {//接收误差
        Matrix error = backDownPooling(errorMatrix, convParameter.getOutX(), convParameter.getOutY());//池化误差返回
        Matrix myError = matrixOperation.add(error, decodeErrorMatrix);
        Matrix myErrorMatrix = backAllDownConv(convParameter, myError, studyRate, activeFunction, convTimes, kerSize);
        if (beforeEncoder != null) {
            beforeEncoder.backError(myErrorMatrix);
        }
    }

    public void sendMatrixList(long eventID, OutBack outBack, ThreeChannelMatrix featureE, List<Matrix> feature, boolean study) throws Exception {
        Matrix myFeature = oneConv(feature, convParameter.getOneConvPower());//降维矩阵
        Matrix convMatrix = downConvAndPooling(myFeature, convParameter, convTimes, activeFunction, kerSize, true, eventID);
        if (afterEncoder != null) {//后面还有编码器，继续向后传递
            afterEncoder.sendFeature(eventID, outBack, featureE, convMatrix, study);
        } else {//向解码器传递
            decoder.sendFeature(eventID, outBack, featureE, convMatrix, study);
        }
    }

    private void initNervePowerMatrix(Random random, List<Matrix> nervePowerMatrixList) throws Exception {
        int convSize = kerSize * kerSize;
        Matrix nervePowerMatrix = new Matrix(convSize, 1);
        for (int i = 0; i < convSize; i++) {
            float power = random.nextFloat() / kerSize;
            nervePowerMatrix.setNub(i, 0, power);
        }
        nervePowerMatrixList.add(nervePowerMatrix);
    }

    public UNetEncoder getAfterEncoder() {
        return afterEncoder;
    }

    public void setAfterEncoder(UNetEncoder afterEncoder) {
        this.afterEncoder = afterEncoder;
    }

    public UNetEncoder getBeforeEncoder() {
        return beforeEncoder;
    }

    public void setBeforeEncoder(UNetEncoder beforeEncoder) {
        this.beforeEncoder = beforeEncoder;
    }

    public UNetDecoder getDecoder() {
        return decoder;
    }

    public void setDecoder(UNetDecoder decoder) {
        this.decoder = decoder;
    }

}
