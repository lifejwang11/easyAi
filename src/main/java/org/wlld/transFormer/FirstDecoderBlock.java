package org.wlld.transFormer;

import org.wlld.i.OutBack;
import org.wlld.matrixTools.Matrix;
import org.wlld.transFormer.seflAttention.LayNorm;
import org.wlld.transFormer.seflAttention.MultiSelfAttention;

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

    public FirstDecoderBlock(int maxLength, int multiNumber, int featureDimension, double studyPoint
            , CodecBlock codecBlock) throws Exception {//进行初始化
        //注意力层残差归一化
        attentionLayNorm = new LayNorm(1, featureDimension, null, this, studyPoint);
        multiSelfAttention = new MultiSelfAttention(multiNumber, studyPoint, 1, featureDimension, maxLength, false, null);
        multiSelfAttention.setLayNorm(attentionLayNorm);
        attentionLayNorm.setMultiSelfAttention(multiSelfAttention);
        this.codecBlock = codecBlock;
    }

    public void backError(long eventID, Matrix error) throws Exception {
        attentionLayNorm.backErrorFromLine(error, eventID);
        lastEncoderBlock.encoderBackStart(eventID);
    }

    public void sendOutputMatrix(long eventID, Matrix out, boolean isStudy, OutBack outBack, List<Integer> E) throws Exception {
        Matrix c = lastEncoderBlock.getOutMatrix(eventID);
        lastEncoderBlock.removeOutMatrix(eventID);
        codecBlock.sendInputMatrix(eventID, out, isStudy, outBack, E, c);
    }

    //Decoder 参数正向入口
    public void sendInputMatrix(long eventID, Matrix feature, boolean isStudy, OutBack outBack, List<Integer> E) throws Exception {
        multiSelfAttention.sendMatrixMessage(eventID, feature, isStudy, outBack, E, null);
    }
}
