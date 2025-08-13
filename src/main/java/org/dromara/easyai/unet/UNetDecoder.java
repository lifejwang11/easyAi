package org.dromara.easyai.unet;

import org.dromara.easyai.conv.ConvCount;
import org.dromara.easyai.conv.DymStudy;
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
 * @time 2025/3/2 07:55
 * @des unet解码器
 */
public class UNetDecoder extends ConvCount {
    private final ConvParameter convParameter = new ConvParameter();//内存中卷积层模型及临时数据
    private final MatrixOperation matrixOperation = new MatrixOperation();
    private final int kerSize;
    private final int deep;//当前深度
    private final float studyRate;//学习率
    private final int channelNo;//通道数
    private final boolean lastLay;//是否为最后一层
    private final ActiveFunction activeFunction;
    private UNetDecoder afterDecoder;//下一个解码器
    private UNetDecoder beforeDecoder;//上一个解码器
    private UNetEncoder encoder;//上一个编码器
    private UNetEncoder myUNetEncoder;//同级编码器
    private final ConvSize convSize = new ConvSize();
    private final Cutting cutting;//输出语义切割图像
    private final float oneConvStudyRate;//
    private final float gaMa;
    private final float gMaxTh;
    private final boolean autoStudyRate;//自动学习率

    public UNetDecoder(int kerSize, int deep, int channelNo, ActiveFunction activeFunction
            , boolean lastLay, float studyRate, Cutting cutting, float oneConvStudyRate, float gaMa, float gMaxTh, boolean autoStudyRate) throws Exception {
        this.cutting = cutting;
        this.autoStudyRate = autoStudyRate;
        this.gMaxTh = gMaxTh;
        this.gaMa = gaMa;
        this.kerSize = kerSize;
        this.oneConvStudyRate = oneConvStudyRate;
        this.deep = deep;
        this.studyRate = studyRate;
        this.lastLay = lastLay;
        this.channelNo = channelNo;
        this.activeFunction = activeFunction;
        Random random = new Random();
        List<Matrix> nerveMatrixList = convParameter.getNerveMatrixList();
        List<Matrix> dymStudyRateList = convParameter.getDymStudyRateList();
        List<Matrix> upNeverMatrixList = convParameter.getUpNerveMatrixList();//上卷积采样权重
        List<Matrix> upDYmStudyRateList = convParameter.getUpDymStudyRateList();
        List<ConvSize> convSizeList = convParameter.getConvSizeList();
        for (int i = 0; i < channelNo; i++) {
            int convSize = kerSize * kerSize;
            upDYmStudyRateList.add(new Matrix(1, convSize));
            upNeverMatrixList.add(initUpNervePowerMatrix(random));
            initNervePowerMatrix(random, nerveMatrixList, dymStudyRateList);
            convSizeList.add(new ConvSize());
        }
        if (lastLay) {
            List<Float> oneConvPower = new ArrayList<>();
            List<Float> oneDymStudyRate = new ArrayList<>();
            for (int i = 0; i < channelNo; i++) {
                oneConvPower.add(random.nextFloat() / channelNo);
                oneDymStudyRate.add(0f);
            }
            convParameter.setUpOneDymStudyRateList(oneDymStudyRate);
            convParameter.setUpOneConvPower(oneConvPower);
        }
    }

    public ConvParameter getConvParameter() {
        return convParameter;
    }

    private ThreeChannelMatrix fillColor(ThreeChannelMatrix picture, int heightSize, int widthSize) throws Exception {
        int myFaceHeight = picture.getX();
        int sub = myFaceHeight - heightSize;
        int fillHeight = sub / 2;//高度差
        if (fillHeight == 0) {
            fillHeight = 1;
        }
        ThreeChannelMatrix fillMatrix = null;
        if (sub > 0) {//剪切
            fillMatrix = picture.cutChannel(fillHeight, 0, heightSize, widthSize);
        } else if (sub < 0) {//补0
            fillMatrix = getFaceMatrix(heightSize, widthSize);
            fillMatrix.fill(Math.abs(fillHeight), 0, picture);
        }
        return fillMatrix;
    }

    private ThreeChannelMatrix getFaceMatrix(int height, int width) {
        ThreeChannelMatrix threeChannelMatrix = new ThreeChannelMatrix();
        Matrix matrixR = new Matrix(height, width);
        Matrix matrixG = new Matrix(height, width);
        Matrix matrixB = new Matrix(height, width);
        Matrix matrixH = new Matrix(height, width);
        threeChannelMatrix.setX(height);
        threeChannelMatrix.setY(width);
        threeChannelMatrix.setMatrixR(matrixR);
        threeChannelMatrix.setMatrixG(matrixG);
        threeChannelMatrix.setMatrixB(matrixB);
        threeChannelMatrix.setH(matrixH);
        return threeChannelMatrix;
    }

    private void addFeatures(List<Matrix> encoderFeatures, List<Matrix> myFeatures, boolean study) throws Exception {
        int size = encoderFeatures.size();
        for (int i = 0; i < size; i++) {
            addFeature(encoderFeatures.get(i), myFeatures.get(i), study);
        }
    }

    private void addFeature(Matrix encoderFeature, Matrix myFeature, boolean study) throws Exception {//获取残差块
        if (study) {
            convSize.setXInput(encoderFeature.getX());
            convSize.setYInput(encoderFeature.getY());
        }
        int tx = encoderFeature.getX();
        int ty = encoderFeature.getY();
        int x = myFeature.getX();
        int y = myFeature.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float encoderValue = 0;
                if (i < tx && j < ty) {
                    encoderValue = encoderFeature.getNumber(i, j);
                }
                float value = (myFeature.getNumber(i, j) + encoderValue) / 2;
                myFeature.setNub(i, j, value);
            }
        }
    }

    private void toThreeChannelMatrix(List<Matrix> features, ThreeChannelMatrix featureE, boolean study, OutBack outBack
            , ThreeChannelMatrix backGround) throws Exception {
        int x = features.get(0).getX();
        int y = features.get(0).getY();
        List<Float> upOneConvPower = convParameter.getUpOneConvPower();
        Matrix feature = oneConv(features, upOneConvPower);
        if (study) {//训练
            ThreeChannelMatrix sfe = featureE.scale(true, y);//缩放
            ThreeChannelMatrix fe = fillColor(sfe, x, y);//补0
            if (fe == null) {
                fe = sfe;
            }
            Matrix he = fe.calculateAvgGrayscale();
            Matrix errorMatrix = matrixOperation.sub(he, feature);//总误差
            //先更新分矩阵误差
            List<Matrix> errorMatrixList = new ArrayList<>();
            for (int i = 0; i < channelNo; i++) {
                float power = upOneConvPower.get(i);
                Matrix error = matrixOperation.mathMulBySelf(errorMatrix, power);
                errorMatrixList.add(error);
            }
            DymStudy dymStudy = new DymStudy(gaMa, gMaxTh, autoStudyRate);
            backOneConv(errorMatrix, features, upOneConvPower, oneConvStudyRate, convParameter.getUpOneDymStudyRateList(), dymStudy);//更新1v1卷积核
            backLastError(errorMatrixList);
            //误差矩阵开始back
        } else {//输出
            int mx = backGround.getX();
            int my = backGround.getY();
            int startX = (mx - feature.getX()) / 2;
            int startY = (my - feature.getY()) / 2;
            Matrix myMatrix = new Matrix(mx, my);
            for (int i = startX; i < x; i++) {
                for (int j = startY; j < y; j++) {
                    myMatrix.setNub(i, j, feature.getNumber(i - startX, j - startY));
                }
            }
            ThreeChannelMatrix threeChannelMatrix = new ThreeChannelMatrix();
            threeChannelMatrix.setX(x);
            threeChannelMatrix.setY(y);
            threeChannelMatrix.setMatrixR(myMatrix);
            threeChannelMatrix.setMatrixG(myMatrix);
            threeChannelMatrix.setMatrixB(myMatrix);
            if (cutting != null) {
                cutting.cut(backGround, threeChannelMatrix, outBack);
            } else {
                outBack.getBackThreeChannelMatrix(threeChannelMatrix);
            }
        }
    }

    private void backLastError(List<Matrix> errorMatrixList) throws Exception {//最后一层的误差反向传播
        List<Matrix> errorList = backAllDownConv(convParameter, errorMatrixList, studyRate, activeFunction, channelNo, kerSize, gaMa, gMaxTh
                , autoStudyRate);
        sendEncoderError(errorList);//给同级解码器发送误差
        beforeDecoder.backErrorMatrix(errorList);
    }

    private void sendEncoderError(List<Matrix> errors) throws Exception {//给同级解码器发送误差
        List<Matrix> encoderErrors = new ArrayList<>();
        for (Matrix error : errors) {
            Matrix encoderError = new Matrix(convSize.getXInput(), convSize.getYInput());
            int x = convSize.getXInput();
            int y = convSize.getYInput();
            int tx = error.getX();
            int ty = error.getY();
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    float value = 0;
                    if (i < tx && j < ty) {
                        value = error.getNumber(i, j) / 2;
                    }
                    encoderError.setNub(i, j, value);
                }
            }
            encoderErrors.add(encoderError);
        }
        myUNetEncoder.setDecodeErrorMatrix(encoderErrors);
    }

    protected void backErrorMatrix(List<Matrix> myErrorMatrixList) throws Exception {//接收解码器误差
        //退上池化，退上卷积 退下卷积 并返回编码器误差
        List<Matrix> errorList = backManyUpPooling(myErrorMatrixList);//退上池化
        List<Matrix> errorMatrixList = backManyUpConv(errorList, kerSize, convParameter, studyRate, activeFunction, gaMa, gMaxTh, autoStudyRate);//退上卷积
        List<Matrix> backList = backAllDownConv(convParameter, errorMatrixList, studyRate, activeFunction, channelNo, kerSize, gaMa, gMaxTh
                , autoStudyRate);//退下卷积
        if (myUNetEncoder != null) {
            sendEncoderError(backList);//给同级编码器发送误差
        }
        if (beforeDecoder != null) {
            beforeDecoder.backErrorMatrix(backList);
        } else {//给上一个编码器发送误差
            encoder.backError(backList);
        }
    }

    protected void sendFeature(long eventID, OutBack outBack, ThreeChannelMatrix featureE,
                               List<Matrix> myFeatures, boolean study, ThreeChannelMatrix backGround) throws Exception {
        if (deep > 1) {
            List<Matrix> encoderMatrixList = myUNetEncoder.getAfterConvMatrix(eventID);//编码器特征
            addFeatures(encoderMatrixList, myFeatures, study);
        }
        List<Matrix> upConvMatrixList = upConvAndPooling(myFeatures, convParameter, channelNo, activeFunction, kerSize, !lastLay);
        if (lastLay) {//最后一层解码器
            toThreeChannelMatrix(upConvMatrixList, featureE, study, outBack, backGround);
        } else {
            afterDecoder.sendFeature(eventID, outBack, featureE, upConvMatrixList, study, backGround);
        }
    }

    private Matrix initUpNervePowerMatrix(Random random) throws Exception {
        int convSize = kerSize * kerSize;
        Matrix nervePowerMatrix = new Matrix(1, convSize);
        for (int j = 0; j < convSize; j++) {
            float power = random.nextFloat() / kerSize;
            nervePowerMatrix.setNub(0, j, power);
        }
        return nervePowerMatrix;
    }

    private void initNervePowerMatrix(Random random, List<Matrix> nervePowerMatrixList, List<Matrix> dymStudyRateList) throws Exception {
        int convSize = kerSize * kerSize;
        Matrix nervePowerMatrix = new Matrix(convSize, 1);
        for (int i = 0; i < convSize; i++) {
            float power = random.nextFloat() / kerSize;
            nervePowerMatrix.setNub(i, 0, power);
        }
        dymStudyRateList.add(new Matrix(convSize, 1));
        nervePowerMatrixList.add(nervePowerMatrix);
    }

    public UNetDecoder getAfterDecoder() {
        return afterDecoder;
    }

    public void setAfterDecoder(UNetDecoder afterDecoder) {
        this.afterDecoder = afterDecoder;
    }

    public UNetDecoder getBeforeDecoder() {
        return beforeDecoder;
    }

    public void setBeforeDecoder(UNetDecoder beforeDecoder) {
        this.beforeDecoder = beforeDecoder;
    }

    public UNetEncoder getEncoder() {
        return encoder;
    }

    public void setEncoder(UNetEncoder encoder) {
        this.encoder = encoder;
    }

    public void setMyUNetEncoder(UNetEncoder myUNetEncoder) {
        this.myUNetEncoder = myUNetEncoder;
    }
}
