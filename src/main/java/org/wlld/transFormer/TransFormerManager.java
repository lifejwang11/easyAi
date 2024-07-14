package org.wlld.transFormer;

import org.wlld.naturalLanguage.word.WordEmbedding;
import org.wlld.transFormer.nerve.SensoryNerve;

import java.util.ArrayList;
import java.util.List;

public class TransFormerManager {
    private final SensoryNerve sensoryNerve;//感知神经元
    private final List<CodecBlock> encoderBlocks = new ArrayList<>();//编码器模块
    private final List<CodecBlock> decoderBlocks = new ArrayList<>();//解码器模块
    private final FirstDecoderBlock firstDecoderBlock;//第一个解码器模块
    private final LineBlock lineBlock;//线性分类层

    public SensoryNerve getSensoryNerve() {
        return sensoryNerve;
    }

    /**
     * 初始化神经元参数
     *
     * @param featureDimension 特征维度
     * @param allDepth         网络深度
     * @param maxLength        序列最大长度
     * @throws Exception 如果参数错误则抛异常
     */
    public TransFormerManager(int maxLength, int multiNumber, int featureDimension, int allDepth, double studyPoint,
                              int typeNumber, boolean showLog) throws Exception {
        if (multiNumber > 1 && featureDimension > 0 && allDepth > 0 && typeNumber > 1) {
            for (int i = 0; i < allDepth; i++) {
                CodecBlock encoderBlock = new CodecBlock(maxLength, multiNumber, featureDimension, studyPoint, i + 1, true);
                encoderBlocks.add(encoderBlock);
            }
            CodecBlock lastEnCoderBlock = encoderBlocks.get(encoderBlocks.size() - 1);//最后一层编码器
            for (int i = 0; i < allDepth; i++) {
                CodecBlock decoderBlock = new CodecBlock(maxLength, multiNumber, featureDimension, studyPoint, i + 2, false);
                decoderBlock.setLastEncoderBlock(lastEnCoderBlock);//放入最优一层编码器
                decoderBlocks.add(decoderBlock);
            }
            CodecBlock lastDecoderBlock = decoderBlocks.get(decoderBlocks.size() - 1);
            connectCodecBlock(encoderBlocks);
            connectCodecBlock(decoderBlocks);
            lineBlock = new LineBlock(typeNumber, featureDimension, studyPoint, lastDecoderBlock, showLog);
            lastDecoderBlock.setLineBlock(lineBlock);
            firstDecoderBlock = new FirstDecoderBlock(maxLength, multiNumber, featureDimension, studyPoint, decoderBlocks.get(0));
            firstDecoderBlock.setLastEncoderBlock(lastEnCoderBlock);
            decoderBlocks.get(0).setFirstDecoderBlock(firstDecoderBlock);
            sensoryNerve = new SensoryNerve(encoderBlocks.get(0), firstDecoderBlock);
        } else {
            throw new Exception("param is null");
        }
    }

    private void connectCodecBlock(List<CodecBlock> codecBlocks) {
        int size = codecBlocks.size();
        for (int i = 0; i < size - 1; i++) {
            CodecBlock encoderBlock = codecBlocks.get(i);
            CodecBlock beforeBlock = codecBlocks.get(i + 1);
            encoderBlock.setBeforeEncoderBlock(beforeBlock);
            beforeBlock.setAfterEncoderBlock(encoderBlock);
        }
    }

}
