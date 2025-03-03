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
 * @time 2025/3/2 07:55
 * @des unet解码器
 */
public class UNetDecoder extends ConvCount {
    private final ConvParameter convParameter = new ConvParameter();//内存中卷积层模型及临时数据
    private final MatrixOperation matrixOperation = new MatrixOperation();
    private final int kerSize;
    private final int deep;//当前深度
    private final float studyRate;//学习率
    private final int convTimes;//卷积层数
    private final boolean lastLay;//是否为最后一层
    private final ActiveFunction activeFunction;
    private UNetDecoder afterDecoder;//下一个解码器
    private UNetDecoder beforeDecoder;//上一个解码器
    private UNetEncoder encoder;//上一个编码器
    private UNetEncoder myUNetEncoder;//同级编码器
    private final ConvSize convSize = new ConvSize();

    public UNetDecoder(int kerSize, int deep, int convTimes, ActiveFunction activeFunction
            , boolean lastLay, float studyRate) throws Exception {
        this.kerSize = kerSize;
        this.deep = deep;
        this.studyRate = studyRate;
        this.lastLay = lastLay;
        this.convTimes = convTimes;
        this.activeFunction = activeFunction;
        Random random = new Random();
        List<Matrix> nerveMatrixList = convParameter.getNerveMatrixList();
        convParameter.setUpNerveMatrix(initUpNervePowerMatrix(random));
        List<ConvSize> convSizeList = convParameter.getConvSizeList();
        for (int i = 0; i < convTimes; i++) {
            initNervePowerMatrix(random, nerveMatrixList);
            convSizeList.add(new ConvSize());
        }
        if (lastLay) {
            List<Float> oneConvPower = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                oneConvPower.add(random.nextFloat() / 3);
            }
            convParameter.setOneConvPower(oneConvPower);
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
                float value = myFeature.getNumber(i, j) + encoderValue;
                myFeature.setNub(i, j, value);
            }
        }
    }

    private void toThreeChannelMatrix(Matrix feature, ThreeChannelMatrix featureE, boolean study, OutBack outBack) throws Exception {
        int x = feature.getX();
        int y = feature.getY();
        List<Float> oneConvPower = convParameter.getOneConvPower();
        Matrix matrixR = null;
        Matrix matrixG = null;
        Matrix matrixB = null;
        for (int i = 0; i < 3; i++) {
            float value = oneConvPower.get(i);
            if (i == 0) {
                matrixR = matrixOperation.mathMulBySelf(feature, value);
            } else if (i == 1) {
                matrixG = matrixOperation.mathMulBySelf(feature, value);
            } else {
                matrixB = matrixOperation.mathMulBySelf(feature, value);
            }
        }
        if (study) {//训练
            ThreeChannelMatrix sfe = featureE.scale(true, y);//缩放
            ThreeChannelMatrix fe = fillColor(sfe, x, y);//补0
            if (fe == null) {
                fe = sfe;
            }
            Matrix errorR = matrixOperation.sub(fe.getMatrixR(), matrixR);
            Matrix errorG = matrixOperation.sub(fe.getMatrixG(), matrixG);
            Matrix errorB = matrixOperation.sub(fe.getMatrixB(), matrixB);
            Matrix featureSubR = matrixOperation.mathMulBySelf(errorR, oneConvPower.get(0));
            Matrix featureSubG = matrixOperation.mathMulBySelf(errorG, oneConvPower.get(1));
            Matrix featureSubB = matrixOperation.mathMulBySelf(errorB, oneConvPower.get(2));
            Matrix errorMatrix = matrixOperation.addThreeMatrix(featureSubR, featureSubG, featureSubB);//总误差
            float v0 = backDecoderOneConv(errorR, feature, studyRate);
            float v1 = backDecoderOneConv(errorG, feature, studyRate);
            float v2 = backDecoderOneConv(errorB, feature, studyRate);
            oneConvPower.set(0, oneConvPower.get(0) + v0);
            oneConvPower.set(1, oneConvPower.get(1) + v1);
            oneConvPower.set(2, oneConvPower.get(2) + v2);
            backLastError(errorMatrix);
            //误差矩阵开始back
        } else {//输出
            ThreeChannelMatrix threeChannelMatrix = new ThreeChannelMatrix();
            threeChannelMatrix.setX(x);
            threeChannelMatrix.setY(y);
            threeChannelMatrix.setMatrixR(matrixR);
            threeChannelMatrix.setMatrixG(matrixG);
            threeChannelMatrix.setMatrixB(matrixB);
            outBack.getBackThreeChannelMatrix(threeChannelMatrix);
        }
    }

    private void backLastError(Matrix errorMatrix) throws Exception {//最后一层的误差反向传播
        Matrix error = backAllDownConv(convParameter, errorMatrix, studyRate, activeFunction, convTimes, kerSize);
        sendEncoderError(error);//给同级解码器发送误差
        beforeDecoder.backErrorMatrix(error);
    }

    private void sendEncoderError(Matrix error) throws Exception {//给同级解码器发送误差
        Matrix encoderError = new Matrix(convSize.getXInput(), convSize.getYInput());
        int x = convSize.getXInput();
        int y = convSize.getYInput();
        int tx = error.getX();
        int ty = error.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = 0;
                if (i < tx && j < ty) {
                    value = error.getNumber(i, j);
                }
                encoderError.setNub(i, j, value);
            }
        }
        myUNetEncoder.setDecodeErrorMatrix(encoderError);
    }

    protected void backErrorMatrix(Matrix errorMatrix) throws Exception {//接收解码器误差
        //退上池化，退上卷积 退下卷积 并返回编码器误差
        Matrix error = backUpPooling(errorMatrix);//退上池化
        Matrix error2 = backUpConv(error, kerSize, convParameter, studyRate);//退上卷积
        Matrix back = backAllDownConv(convParameter, error2, studyRate, activeFunction, convTimes, kerSize);//退下卷积
        if (myUNetEncoder != null) {
            sendEncoderError(back);//给同级解码器发送误差
        }
        if (beforeDecoder != null) {
            beforeDecoder.backErrorMatrix(back);
        } else {//给上一个编码器发送误差
            encoder.backError(back);
        }
    }

    protected void sendFeature(long eventID, OutBack outBack, ThreeChannelMatrix featureE,
                               Matrix myFeature, boolean study) throws Exception {
        if (deep > 1) {
            Matrix encoderMatrix = myUNetEncoder.getAfterConvMatrix(eventID);//编码器特征
            addFeature(encoderMatrix, myFeature, study);
        }
        Matrix upConvMatrix = upConvAndPooling(myFeature, convParameter, convTimes, activeFunction, kerSize, !lastLay);
        if (lastLay) {//最后一层解码器
            toThreeChannelMatrix(upConvMatrix, featureE, study, outBack);
        } else {
            afterDecoder.sendFeature(eventID, outBack, featureE, upConvMatrix, study);
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

    private void initNervePowerMatrix(Random random, List<Matrix> nervePowerMatrixList) throws Exception {
        int convSize = kerSize * kerSize;
        Matrix nervePowerMatrix = new Matrix(convSize, 1);
        for (int i = 0; i < convSize; i++) {
            float power = random.nextFloat() / kerSize;
            nervePowerMatrix.setNub(i, 0, power);
        }
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
