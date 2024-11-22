package org.dromara.easyai.transFormer;

import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.transFormer.model.FirstDecoderModel;
import org.dromara.easyai.transFormer.seflAttention.LayNorm;
import org.dromara.easyai.transFormer.seflAttention.MultiSelfAttention;

import java.util.List;


public class FirstDecoderBlock {//解码器模块
    private final MultiSelfAttention multiSelfAttention;
    private final LayNorm attentionLayNorm;
    //////////////////
    private final CodecBlock codecBlock;//前方的解码层
    private CodecBlock lastEncoderBlock;//最后一层编码器

    public void setLastEncoderBlock(CodecBlock lastEncoderBlock) {
        this.lastEncoderBlock = lastEncoderBlock;
    }

    public FirstDecoderBlock(int multiNumber, int featureDimension, double studyPoint, CodecBlock codecBlock, int maxLength, boolean selfTimeCode
            , int coreNumber) throws Exception {//进行初始化
        //注意力层残差归一化
        attentionLayNorm = new LayNorm(1, featureDimension, null, this, studyPoint, coreNumber);
        multiSelfAttention = new MultiSelfAttention(multiNumber, studyPoint, 1, featureDimension, false, null, maxLength
                , selfTimeCode, coreNumber);
        multiSelfAttention.setLayNorm(attentionLayNorm);
        attentionLayNorm.setMultiSelfAttention(multiSelfAttention);
        this.codecBlock = codecBlock;
    }

    public FirstDecoderModel getModel() {
        FirstDecoderModel firstDecoderModel = new FirstDecoderModel();
        firstDecoderModel.setMultiSelfAttentionModel(multiSelfAttention.getModel());
        firstDecoderModel.setAttentionLayNormModel(attentionLayNorm.getModel());
        return firstDecoderModel;
    }

    public void insertModel(FirstDecoderModel firstDecoderModel) throws Exception {
        multiSelfAttention.insertModel(firstDecoderModel.getMultiSelfAttentionModel());
        attentionLayNorm.insertModel(firstDecoderModel.getAttentionLayNormModel());
    }

    public void backError(long eventID, Matrix error) throws Exception {
        attentionLayNorm.backErrorFromLine(error, eventID);
        lastEncoderBlock.encoderBackStart(eventID);
    }

    public void sendOutputMatrix(long eventID, Matrix out, boolean isStudy, OutBack outBack, List<Integer> E, boolean outAllPro) throws Exception {
        Matrix c = lastEncoderBlock.getOutMatrix(eventID);
        lastEncoderBlock.removeOutMatrix(eventID);
        codecBlock.sendInputMatrix(eventID, out, isStudy, outBack, E, c, outAllPro);
    }

    //Decoder 参数正向入口
    public void sendInputMatrix(long eventID, Matrix feature, boolean isStudy, OutBack outBack, List<Integer> E, boolean outAllPro) throws Exception {
        multiSelfAttention.sendMatrixMessage(eventID, feature, isStudy, outBack, E, null, outAllPro);
    }
}
