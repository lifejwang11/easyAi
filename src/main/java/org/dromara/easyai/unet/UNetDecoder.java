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
    private final Cutting cutting;//输出语义切割图像

    public UNetDecoder(int kerSize, int deep, int convTimes, ActiveFunction activeFunction
            , boolean lastLay, float studyRate, Cutting cutting) throws Exception {
        this.cutting = cutting;
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
                float value = (myFeature.getNumber(i, j) + encoderValue) / 2;
                myFeature.setNub(i, j, value);
            }
        }
    }

    private void toThreeChannelMatrix(Matrix feature, ThreeChannelMatrix featureE, boolean study, OutBack outBack
            , ThreeChannelMatrix backGround) throws Exception {
        int x = feature.getX();
        int y = feature.getY();
        //System.out.println("x:" + x + " y:" + y + ",feature:" + feature.getAVG());
        if (study) {//训练
            ThreeChannelMatrix sfe = featureE.scale(true, y);//缩放
            ThreeChannelMatrix fe = fillColor(sfe, x, y);//补0
            if (fe == null) {
                fe = sfe;
            }
            Matrix he = fe.CalculateAvgGrayscale();
            Matrix errorMatrix = matrixOperation.sub(he, feature);//总误差
            backLastError(errorMatrix);
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
                    value = error.getNumber(i, j) / 2;
                }
                encoderError.setNub(i, j, value);
            }
        }
        myUNetEncoder.setDecodeErrorMatrix(encoderError);
    }

    protected void backErrorMatrix(Matrix errorMatrix) throws Exception {//接收解码器误差
        //退上池化，退上卷积 退下卷积 并返回编码器误差
        Matrix error = backUpPooling(errorMatrix);//退上池化
        Matrix error2 = backUpConv(error, kerSize, convParameter, studyRate, activeFunction);//退上卷积
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
                               Matrix myFeature, boolean study, ThreeChannelMatrix backGround) throws Exception {
        if (deep > 1) {
            Matrix encoderMatrix = myUNetEncoder.getAfterConvMatrix(eventID);//编码器特征
            addFeature(encoderMatrix, myFeature, study);
        }
        //System.out.println("deep==" + deep + ",解码器运算前:" + myFeature.getAVG());
        Matrix upConvMatrix = upConvAndPooling(myFeature, convParameter, convTimes, activeFunction, kerSize, !lastLay);
        //System.out.println("deep==" + deep + ",解码器运算后:" + upConvMatrix.getAVG());
        if (lastLay) {//最后一层解码器
            toThreeChannelMatrix(upConvMatrix, featureE, study, outBack, backGround);
        } else {
            afterDecoder.sendFeature(eventID, outBack, featureE, upConvMatrix, study, backGround);
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
