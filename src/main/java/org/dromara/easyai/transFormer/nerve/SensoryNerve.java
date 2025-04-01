package org.dromara.easyai.transFormer.nerve;


import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.transFormer.CodecBlock;
import org.dromara.easyai.transFormer.FirstDecoderBlock;
import org.dromara.easyai.transFormer.TransWordVector;

import java.util.List;

/**
 * 感知神经元输入层
 *
 * @author lidapeng
 * @date 9:29 上午 2019/12/21
 */
public class SensoryNerve {
    private final CodecBlock firstEncoderBlock;
    private final FirstDecoderBlock firstDecoderBlock;
    private final TransWordVector transWordVector;

    public SensoryNerve(CodecBlock firstEncoderBlock, FirstDecoderBlock firstDecoderBlock
            , TransWordVector transWordVector) throws Exception {
        this.firstEncoderBlock = firstEncoderBlock;
        this.firstDecoderBlock = firstDecoderBlock;
        this.transWordVector = transWordVector;
    }

    /**
     * @param eventId          唯一的事件id
     * @param encoderParameter 编码器输入特征
     * @param decoderParameter 解码器输入特征
     * @param isStudy          是否是学习 (学习状态没有输出)
     * @param E                标注
     * @param outBack          回调结果
     */
    public void postMessage(long eventId, Matrix encoderParameter, Matrix decoderParameter, boolean isStudy, List<Integer> E
            , OutBack outBack, boolean outAllPro) throws Exception {//感知神经元输入
        firstEncoderBlock.sendInputMatrix(eventId, encoderParameter, isStudy, outBack, E, null, outAllPro);
        firstDecoderBlock.sendInputMatrix(eventId, decoderParameter, isStudy, outBack, E, outAllPro);
    }

    /**
     * @param eventId         唯一的事件id
     * @param encoderSentence 编码器输入语句
     * @param decoderSentence 解码器输入语句
     * @param isStudy         是否是学习 (学习状态没有输出)
     * @param outBack         回调结果
     */
    public void postSentence(long eventId, String encoderSentence, String decoderSentence, boolean isStudy, OutBack outBack) throws Exception {
        Matrix encoderFeature = transWordVector.getWordVector(encoderSentence, false, isStudy);//编码器特征
        Matrix decoderFeature = transWordVector.getWordVector(decoderSentence, true, isStudy);//解码器特征
        List<Integer> E = transWordVector.getE(decoderSentence);
        postMessage(eventId, encoderFeature, decoderFeature, isStudy, E, outBack, false);
    }

}
