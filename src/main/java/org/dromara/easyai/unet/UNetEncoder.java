package org.dromara.easyai.unet;

import org.dromara.easyai.conv.ConvCount;
import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.nerveEntity.ConvParameter;
import org.dromara.easyai.nerveEntity.ConvSize;

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
    private final int channelNo;//卷积层数
    private List<Matrix> decodeErrorMatrix;//从解码器传来的误差矩阵
    private final ActiveFunction activeFunction;
    private UNetEncoder afterEncoder;//下一个编码器
    private UNetEncoder beforeEncoder;//上一个编码器
    private UNetDecoder decoder;//下一个解码器
    private final int xSize;
    private final int ySize;
    private final float oneStudyRate;
    private final float gaMa;
    private final float gMaxTh;
    private final boolean aoTu;

    public UNetEncoder(int kerSize, int channelNo, int deep, ActiveFunction activeFunction
            , float studyRate, int xSize, int ySize, float oneStudyRate, float gaMa, float gMaxTh, boolean aoTu) throws Exception {//核心大小
        Random random = new Random();
        this.xSize = xSize;
        this.aoTu = aoTu;
        this.gMaxTh = gMaxTh;
        this.gaMa = gaMa;
        this.ySize = ySize;
        this.oneStudyRate = oneStudyRate;
        this.studyRate = studyRate;
        this.kerSize = kerSize;
        this.activeFunction = activeFunction;
        this.deep = deep;
        this.channelNo = channelNo;
        List<Matrix> nerveMatrixList = convParameter.getNerveMatrixList();
        List<ConvSize> convSizeList = convParameter.getConvSizeList();
        List<Matrix> dymStudyRateList = convParameter.getDymStudyRateList();
        for (int i = 0; i < channelNo; i++) {
            initNervePowerMatrix(random, nerveMatrixList, dymStudyRateList);
            convSizeList.add(new ConvSize());
        }
        if (deep == 1) {
            List<List<Float>> oneConvPowers = new ArrayList<>();
            List<List<Float>> oneDymStudyRateList = new ArrayList<>();
            for (int k = 0; k < channelNo; k++) {
                List<Float> oneConvPower = new ArrayList<>();
                List<Float> oneDymStudyRate = new ArrayList<>();
                oneConvPowers.add(oneConvPower);
                oneDymStudyRateList.add(oneDymStudyRate);
                //通道数
                int channelNum = 3;
                for (int i = 0; i < channelNum; i++) {
                    oneConvPower.add(random.nextFloat() / channelNum);
                    oneDymStudyRate.add(0f);
                }
            }
            convParameter.setOneDymStudyRateList(oneDymStudyRateList);
            convParameter.setOneConvPower(oneConvPowers);
        }
    }

    public ConvParameter getConvParameter() {
        return convParameter;
    }

    protected void setDecodeErrorMatrix(List<Matrix> decodeErrorMatrix) {
        this.decodeErrorMatrix = decodeErrorMatrix;
    }

    protected List<Matrix> getAfterConvMatrix(long eventID) {//卷积后的矩阵
        List<Matrix> outMatrixList = convParameter.getFeatureMap().get(eventID);
        convParameter.getFeatureMap().remove(eventID);
        return outMatrixList;
    }

    //发送特征三通道矩阵
    public void sendThreeChannel(long eventID, OutBack outBack, ThreeChannelMatrix feature, ThreeChannelMatrix featureE,
                                 boolean study) throws Exception {
        if (study && featureE == null) {
            throw new Exception("训练时期望矩阵不能为空");
        }
        if (feature.getX() != xSize && feature.getY() != ySize) {
            throw new Exception("输入图片尺寸与初始化参数不一致");
        }
        List<Matrix> matrixList = new ArrayList<>();
        matrixList.add(feature.getMatrixR());
        matrixList.add(feature.getMatrixG());
        matrixList.add(feature.getMatrixB());
        if (study) {
            convParameter.setFeatureMatrixList(matrixList);
        }
        sendMatrixList(eventID, outBack, featureE, matrixList, study, feature);
    }

    protected void sendFeature(long eventID, OutBack outBack, ThreeChannelMatrix featureE,
                               List<Matrix> myFeatures, boolean study, ThreeChannelMatrix backGround) throws Exception {
        List<Matrix> convMatrixList = downConvAndPooling(myFeatures, convParameter, channelNo, activeFunction, kerSize, true, eventID);
        if (afterEncoder != null) {//后面还有编码器，继续向后传递
            afterEncoder.sendFeature(eventID, outBack, featureE, convMatrixList, study, backGround);
        } else {//向解码器传递
            decoder.sendFeature(eventID, outBack, featureE, convMatrixList, study, backGround);
        }
    }

    protected void backError(List<Matrix> errorMatrix) throws Exception {//接收误差
        List<Matrix> errorList = backDownPoolingByList(errorMatrix, convParameter.getOutX(), convParameter.getOutY());//池化误差返回
        List<Matrix> errorMatrixList = matrixOperation.addMatrixList(errorList, decodeErrorMatrix);
        List<Matrix> myErrorMatrix = backAllDownConv(convParameter, errorMatrixList, studyRate, activeFunction, channelNo, kerSize,
                gaMa, gMaxTh, aoTu);
        if (beforeEncoder != null) {
            beforeEncoder.backError(myErrorMatrix);
        } else {//最后一层 调整1v1卷积
            backOneConvByList(myErrorMatrix, convParameter.getFeatureMatrixList(), convParameter.getOneConvPower(), oneStudyRate
                    , convParameter.getOneDymStudyRateList(), gaMa, gMaxTh, aoTu);
        }
    }

    public void sendMatrixList(long eventID, OutBack outBack, ThreeChannelMatrix featureE, List<Matrix> feature,
                               boolean study, ThreeChannelMatrix backGround) throws Exception {
        List<Matrix> myFeatures = manyOneConv(feature, convParameter.getOneConvPower());//矩阵重新调整维度
        List<Matrix> convMatrixList = downConvAndPooling(myFeatures, convParameter, channelNo, activeFunction, kerSize, true, eventID);
        if (afterEncoder != null) {//后面还有编码器，继续向后传递
            afterEncoder.sendFeature(eventID, outBack, featureE, convMatrixList, study, backGround);
        } else {//向解码器传递
            decoder.sendFeature(eventID, outBack, featureE, convMatrixList, study, backGround);
        }
    }

    private void initNervePowerMatrix(Random random, List<Matrix> nervePowerMatrixList, List<Matrix> dymStudyRageList) throws Exception {
        int convSize = kerSize * kerSize;
        Matrix nervePowerMatrix = new Matrix(convSize, 1);
        for (int i = 0; i < convSize; i++) {
            float power = random.nextFloat() / kerSize;
            nervePowerMatrix.setNub(i, 0, power);
        }
        dymStudyRageList.add(new Matrix(convSize, 1));
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
