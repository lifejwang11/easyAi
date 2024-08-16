package org.wlld.transFormer;

import org.wlld.function.ReLu;
import org.wlld.i.OutBack;
import org.wlld.matrixTools.Matrix;
import org.wlld.matrixTools.MatrixOperation;
import org.wlld.transFormer.model.CodecBlockModel;
import org.wlld.transFormer.nerve.HiddenNerve;
import org.wlld.transFormer.nerve.Nerve;
import org.wlld.transFormer.seflAttention.LayNorm;
import org.wlld.transFormer.seflAttention.MultiSelfAttention;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodecBlock {
    private final MultiSelfAttention multiSelfAttention;
    private final LayNorm attentionLayNorm;//注意力层残差归一化
    private final List<HiddenNerve> fistHiddenNerves = new ArrayList<>();//线性层第一层
    private final List<HiddenNerve> secondHiddenNerves = new ArrayList<>();//线性层第二层
    private final LayNorm lineLayNorm;//线性层残差归一化
    ////////////////////////////////////
    private CodecBlock afterEncoderBlock;//后编码模块
    private CodecBlock beforeEncoderBlock;//前编码模块
    private CodecBlock lastEncoderBlock;//最后一层编码器
    private final Map<Long, Matrix> outMatrixMap = new HashMap<>();
    private final boolean encoder;//是否为编码器
    private LineBlock lineBlock;//解码器最后的线性分类器
    private FirstDecoderBlock firstDecoderBlock;//解码器第一层

    public CodecBlockModel getModel() {
        List<double[][]> firstNerveModel = new ArrayList<>();
        List<double[][]> secondNerveModel = new ArrayList<>();
        for (int i = 0; i < fistHiddenNerves.size(); i++) {
            firstNerveModel.add(fistHiddenNerves.get(i).getModel());
            secondNerveModel.add(secondHiddenNerves.get(i).getModel());
        }
        CodecBlockModel codecBlockModel = new CodecBlockModel();
        codecBlockModel.setMultiSelfAttentionModel(multiSelfAttention.getModel());
        codecBlockModel.setAttentionLayNormModel(attentionLayNorm.getModel());
        codecBlockModel.setFistNervesModel(firstNerveModel);
        codecBlockModel.setSecondNervesModel(secondNerveModel);
        codecBlockModel.setLineLayNormModel(lineLayNorm.getModel());
        return codecBlockModel;
    }

    public void insertModel(CodecBlockModel codecBlockModel) throws Exception {
        multiSelfAttention.insertModel(codecBlockModel.getMultiSelfAttentionModel());
        attentionLayNorm.insertModel(codecBlockModel.getAttentionLayNormModel());
        List<double[][]> firstNerveModel = codecBlockModel.getFistNervesModel();
        List<double[][]> secondNerveModel = codecBlockModel.getSecondNervesModel();
        for (int i = 0; i < fistHiddenNerves.size(); i++) {
            fistHiddenNerves.get(i).insertModel(firstNerveModel.get(i));
            secondHiddenNerves.get(i).insertModel(secondNerveModel.get(i));
        }
        lineLayNorm.insertModel(codecBlockModel.getLineLayNormModel());
    }

    public void setFirstDecoderBlock(FirstDecoderBlock firstDecoderBlock) {
        this.firstDecoderBlock = firstDecoderBlock;
    }

    public void setLineBlock(LineBlock lineBlock) {
        this.lineBlock = lineBlock;
    }

    public void setLastEncoderBlock(CodecBlock lastEncoderBlock) {
        this.lastEncoderBlock = lastEncoderBlock;
    }

    public void setAfterEncoderBlock(CodecBlock afterEncoderBlock) {
        this.afterEncoderBlock = afterEncoderBlock;
    }

    public void setBeforeEncoderBlock(CodecBlock beforeEncoderBlock) {
        this.beforeEncoderBlock = beforeEncoderBlock;
    }

    public CodecBlock(int maxLength, int multiNumber, int featureDimension, double studyPoint, int depth,
                      boolean encoder, int regularModel, double regular) throws Exception {//进行初始化
        this.encoder = encoder;
        attentionLayNorm = new LayNorm(1, featureDimension, this, null, studyPoint);
        lineLayNorm = new LayNorm(2, featureDimension, this, null, studyPoint);
        multiSelfAttention = new MultiSelfAttention(multiNumber, studyPoint, depth, featureDimension, maxLength, encoder, this);
        multiSelfAttention.setLayNorm(attentionLayNorm);
        attentionLayNorm.setMultiSelfAttention(multiSelfAttention);
        initLine(featureDimension, studyPoint, regularModel, regular);
        attentionLayNorm.setHiddenNerves(fistHiddenNerves);
        lineLayNorm.setHiddenNerves(secondHiddenNerves);
    }

    public void backError(long eventID, Matrix errorMatrix) throws Exception {//最后线性层返回误差
        lineLayNorm.backErrorFromLine(errorMatrix, eventID);
    }

    public void removeOutMatrix(long eventID) {
        outMatrixMap.remove(eventID);
    }

    public Matrix getOutMatrix(long eventID) {
        return outMatrixMap.get(eventID);
    }

    public void sendOutputMatrix(long eventID, Matrix out, boolean isStudy, OutBack outBack,
                                 List<Integer> E, Matrix encoderFeature) throws Exception {//参数正向出口
        if (beforeEncoderBlock != null) {
            beforeEncoderBlock.sendInputMatrix(eventID, out, isStudy, outBack, E, encoderFeature);
        } else if (encoder) {//编码器走到末尾 保存输出矩阵
            outMatrixMap.put(eventID, out);
        } else {//解码器走到头了 输出线性分类层
            lineBlock.sendParameter(eventID, out, isStudy, outBack, E);
        }
    }

    public void backCodecError(Matrix errorMatrix, long eventID, Matrix allFeature) throws Exception {//本层最终误差返回
        Matrix error = MatrixOperation.add(errorMatrix, allFeature);
        if (afterEncoderBlock != null) {
            afterEncoderBlock.backError(eventID, error);
        } else if (firstDecoderBlock != null) {//将误差反给第一层解码器
            firstDecoderBlock.backError(eventID, error);
        }
    }


    public void backLastEncoderError(Matrix error) throws Exception {//给最后一层编码器返回误差
        lastEncoderBlock.backLastError(error);
    }

    private void backLastError(Matrix error) throws Exception {//最后一层编码器接收error
        lineLayNorm.backLastError(error);
    }

    public void encoderBackStart(long eventID) throws Exception {//给最后一层编码器发送back指令
        lineLayNorm.encoderBackStart(eventID);
    }

    //Encoder 参数正向入口
    public void sendInputMatrix(long eventID, Matrix feature, boolean isStudy, OutBack outBack, List<Integer> E
            , Matrix encoderFeature) throws Exception {
        multiSelfAttention.sendMatrixMessage(eventID, feature, isStudy, outBack, E, encoderFeature);
    }

    private void initLine(int featureDimension, double studyPoint, int regularModel, double regular) throws Exception {
        List<Nerve> firstNerves = new ArrayList<>();
        List<Nerve> secondNerves = new ArrayList<>();
        for (int i = 0; i < featureDimension; i++) {
            HiddenNerve hiddenNerve1 = new HiddenNerve(i + 1, 1, studyPoint, new ReLu(), featureDimension,
                    featureDimension, null, regularModel, regular);
            fistHiddenNerves.add(hiddenNerve1);
            hiddenNerve1.setAfterLayNorm(attentionLayNorm);
            firstNerves.add(hiddenNerve1);
        }
        for (int i = 0; i < featureDimension; i++) {
            HiddenNerve hiddenNerve2 = new HiddenNerve(i + 1, 2, studyPoint, null,
                    featureDimension, 1, null, regularModel, regular);
            hiddenNerve2.setBeforeLayNorm(lineLayNorm);
            secondHiddenNerves.add(hiddenNerve2);
            secondNerves.add(hiddenNerve2);
        }
        for (Nerve hiddenNerve : firstNerves) {
            hiddenNerve.connect(secondNerves);
        }
        for (Nerve hiddenNerve : secondNerves) {
            hiddenNerve.connectFather(firstNerves);
        }
    }

}
