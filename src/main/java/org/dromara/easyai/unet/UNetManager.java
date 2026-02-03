package org.dromara.easyai.unet;


import org.dromara.easyai.config.UNetConfig;
import org.dromara.easyai.conv.ConvCount;
import org.dromara.easyai.function.ReLu;
import org.dromara.easyai.function.Tanh;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.nerveEntity.ConvParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @time 2025/2/25 20:19
 * @des UNet网络管理器
 */
public class UNetManager extends ConvCount {
    private final List<UNetEncoder> encoderList = new ArrayList<>();
    private final List<UNetDecoder> decoderList = new ArrayList<>();
    private final int kernLen;
    private final int channelNo;
    private final int deep;
    private final float studyRate;
    private final float oneStudyRate;//1v1卷积权重学习率
    private UNetInput input;//输入类
    private final float gMaxTh;
    private final float layGMaxTh;

    public UNetInput getInput() {
        return input;
    }

    public UNetManager(UNetConfig uNetConfig) throws Exception {
        int xSize = uNetConfig.getXSize();
        int ySize = uNetConfig.getYSize();
        gMaxTh = uNetConfig.getGMaxTh();
        layGMaxTh = uNetConfig.getLayGMaxTh();
        int minFeatureValue = uNetConfig.getMinFeatureValue();
        this.kernLen = uNetConfig.getKerSize();
        this.channelNo = uNetConfig.getChannelNo();
        this.studyRate = uNetConfig.getStudyRate();
        this.oneStudyRate = uNetConfig.getStudyRate();
        this.deep = getConvMyDep(xSize, ySize, kernLen, minFeatureValue);//编码器深度深度
        if (deep > 1) {
            initEncoder(xSize, ySize);//初始化编码器
            initDecoder(uNetConfig.isCutting(), uNetConfig.getCutTh());
            connectionCoder();
        } else {
            throw new Exception("minFeatureValue 设置的值太大了");
        }
    }

    private float[] getFValue(Float[] values) {
        float[] fValue = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            fValue[i] = values[i];
        }
        return fValue;
    }

    private Float[] getValue(float[] values) {
        Float[] result = new Float[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i];
        }
        return result;
    }

    public void insertModel(UNetModel uNetModel) throws Exception {
        List<ConvModel> encoderModel = uNetModel.getEncoderModels();
        List<ConvModel> decoderModel = uNetModel.getDecoderModels();
        if (encoderModel.size() != deep) {
            throw new Exception("模型深度不匹配");
        }
        for (int i = 0; i < deep; i++) {
            ConvParameter convParameter = encoderList.get(i).getConvParameter();
            List<Matrix> matrixList = convParameter.getNerveMatrixList();
            ConvModel convModel = encoderModel.get(i);
            List<Float[]> downPowers = convModel.getDownNervePower();
            List<List<Float>> oneNervePower = convModel.getOneNervePowerList();
            convParameter.setOneConvPower(oneNervePower);
            for (int j = 0; j < matrixList.size(); j++) {
                Matrix matrix = matrixList.get(j);
                float[] power = getFValue(downPowers.get(j));
                matrix.setCudaMatrix(power, matrix.getX(), matrix.getY());
            }
        }
        for (int i = 0; i < deep + 1; i++) {
            ConvParameter convParameter = decoderList.get(i).getConvParameter();
            List<Matrix> matrixList = convParameter.getNerveMatrixList();
            ConvModel convModel = decoderModel.get(i);
            List<Float[]> downPowers = convModel.getDownNervePower();
            List<Float[]> upNervePowerModel = convModel.getUpNervePower();
            convParameter.setUpOneConvPower(convModel.getOneNervePower());
            List<Matrix> upNervePowers = convParameter.getUpNerveMatrixList();
            for (int j = 0; j < upNervePowerModel.size(); j++) {
                float[] upPower = getFValue(upNervePowerModel.get(j));
                Matrix upNervePower = upNervePowers.get(j);
                upNervePower.setCudaMatrix(upPower, upNervePower.getX(), upNervePower.getY());
            }
            for (int j = 0; j < matrixList.size(); j++) {
                Matrix matrix = matrixList.get(j);
                float[] power = getFValue(downPowers.get(j));
                matrix.setCudaMatrix(power, matrix.getX(), matrix.getY());
            }
        }
    }

    public UNetModel getModel() {
        UNetModel unetModel = new UNetModel();
        List<ConvModel> encoderModel = new ArrayList<>();
        List<ConvModel> decoderModel = new ArrayList<>();
        unetModel.setEncoderModels(encoderModel);
        unetModel.setDecoderModels(decoderModel);
        for (int i = 0; i < deep; i++) {//遍历每一层
            ConvModel convModel = new ConvModel();
            encoderModel.add(convModel);
            ConvParameter convParameter = encoderList.get(i).getConvParameter();
            List<Float[]> downNervePower = new ArrayList<>();
            convModel.setDownNervePower(downNervePower);
            List<List<Float>> onePowers = convParameter.getOneConvPower();
            if (onePowers != null && !onePowers.isEmpty()) {
                convModel.setOneNervePowerList(onePowers);
            }
            List<Matrix> downNerveMatrix = convParameter.getNerveMatrixList();//下采样卷积权重
            for (Matrix nerveMatrix : downNerveMatrix) {
                Float[] downPower = getValue(nerveMatrix.getCudaMatrix());
                downNervePower.add(downPower);
            }
        }
        for (int i = 0; i < deep + 1; i++) {
            ConvModel convModel = new ConvModel();
            decoderModel.add(convModel);
            ConvParameter convParameter = decoderList.get(i).getConvParameter();
            convModel.setOneNervePower(convParameter.getUpOneConvPower());
            List<Float[]> downNervePower = new ArrayList<>();
            convModel.setDownNervePower(downNervePower);
            List<Matrix> upNerveMatrix = convParameter.getUpNerveMatrixList();
            List<Float[]> upNervePower = new ArrayList<>();
            for (Matrix upMatrix : upNerveMatrix) {
                upNervePower.add(getValue(upMatrix.getCudaMatrix()));
            }
            convModel.setUpNervePower(upNervePower);
            List<Matrix> downNerveMatrix = convParameter.getNerveMatrixList();
            for (Matrix nerveMatrix : downNerveMatrix) {
                Float[] downPower = getValue(nerveMatrix.getCudaMatrix());
                downNervePower.add(downPower);
            }
        }
        return unetModel;
    }

    private void connectionCoder() {//链接编解码器
        UNetEncoder lastUNetEncoder = encoderList.get(deep - 1);//最后一层编码器
        UNetDecoder firstUNetDecoder = decoderList.get(0);//第一层解码器
        lastUNetEncoder.setDecoder(firstUNetDecoder);
        firstUNetDecoder.setEncoder(lastUNetEncoder);
        for (int i = 0; i < deep; i++) {
            UNetEncoder uNetEncoder = encoderList.get(i);
            UNetDecoder uNetDecoder = decoderList.get(deep - i);
            uNetDecoder.setMyUNetEncoder(uNetEncoder);//绑定统计编码器
        }
    }

    private void initDecoder(boolean cutting, float cutTh) throws Exception {
        Cutting myCut = null;
        if (cutting) {
            myCut = new Cutting(cutTh);
        }
        for (int i = 0; i < deep + 1; i++) {
            UNetDecoder uNetDecoder = new UNetDecoder(kernLen, i + 1, channelNo, new Tanh(),
                    i == deep, studyRate, myCut, oneStudyRate, gMaxTh, layGMaxTh);
            decoderList.add(uNetDecoder);
        }
        for (int i = 0; i < deep; i++) {
            UNetDecoder uNetDecoder = decoderList.get(i);
            UNetDecoder nextUNetDecoder = decoderList.get(i + 1);
            uNetDecoder.setAfterDecoder(nextUNetDecoder);
            nextUNetDecoder.setBeforeDecoder(uNetDecoder);
        }
    }

    private void initEncoder(int xSize, int ySize) throws Exception {
        for (int i = 0; i < deep; i++) {
            UNetEncoder uNetEncoder = new UNetEncoder(kernLen, channelNo, i + 1, new ReLu(), studyRate
                    , xSize, ySize, oneStudyRate, gMaxTh, layGMaxTh);
            if (i == 0) {
                input = new UNetInput(uNetEncoder);
            }
            encoderList.add(uNetEncoder);
        }
        for (int i = 0; i < deep - 1; i++) {
            UNetEncoder uNetEncoder = encoderList.get(i);
            UNetEncoder nextUNetEncoder = encoderList.get(i + 1);
            uNetEncoder.setAfterEncoder(nextUNetEncoder);
            nextUNetEncoder.setBeforeEncoder(uNetEncoder);
        }
    }
}
