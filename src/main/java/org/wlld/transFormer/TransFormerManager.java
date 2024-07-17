package org.wlld.transFormer;

import org.wlld.config.TfConfig;
import org.wlld.transFormer.model.CodecBlockModel;
import org.wlld.transFormer.model.TransFormerModel;
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

    public TransFormerModel getModel() {
        TransFormerModel transFormerModel = new TransFormerModel();
        List<CodecBlockModel> encoderBlockModels = new ArrayList<>();
        List<CodecBlockModel> decoderBlockModels = new ArrayList<>();
        for (int i = 0; i < encoderBlocks.size(); i++) {
            encoderBlockModels.add(encoderBlocks.get(i).getModel());
            decoderBlockModels.add(decoderBlocks.get(i).getModel());
        }
        transFormerModel.setEncoderBlockModels(encoderBlockModels);
        transFormerModel.setDecoderBlockModels(decoderBlockModels);
        transFormerModel.setFirstDecoderBlockModel(firstDecoderBlock.getModel());
        transFormerModel.setLineBlockModel(lineBlock.getModel());
        return transFormerModel;
    }

    public void insertModel(TransFormerModel transFormerModel) throws Exception {
        List<CodecBlockModel> encoderBlockModels = transFormerModel.getEncoderBlockModels();
        List<CodecBlockModel> decoderBlockModels = transFormerModel.getDecoderBlockModels();
        for (int i = 0; i < encoderBlocks.size(); i++) {
            encoderBlocks.get(i).insertModel(encoderBlockModels.get(i));
            decoderBlocks.get(i).insertModel(decoderBlockModels.get(i));
        }
        firstDecoderBlock.insertModel(transFormerModel.getFirstDecoderBlockModel());
        lineBlock.insertModel(transFormerModel.getLineBlockModel());
    }

    /**
     * 初始化神经元参数
     *
     * @param tfConfig 配置参数
     * @throws Exception 如果参数错误则抛异常
     */
    public TransFormerManager(TfConfig tfConfig) throws Exception {
        int maxLength = tfConfig.getMaxLength();
        int multiNumber = tfConfig.getMultiNumber();
        int featureDimension = tfConfig.getFeatureDimension();
        int allDepth = tfConfig.getAllDepth();
        double studyPoint = tfConfig.getStudyPoint();
        int typeNumber = tfConfig.getTypeNumber();
        boolean showLog = tfConfig.isShowLog();
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
            throw new Exception("param is null,typeNumber:" + typeNumber + ",featureDimension:" + featureDimension);
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
