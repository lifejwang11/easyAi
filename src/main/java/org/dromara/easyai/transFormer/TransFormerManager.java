package org.dromara.easyai.transFormer;

import org.dromara.easyai.config.TfConfig;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.transFormer.model.CodecBlockModel;
import org.dromara.easyai.transFormer.model.TransFormerModel;
import org.dromara.easyai.transFormer.model.TransWordVectorModel;
import org.dromara.easyai.transFormer.nerve.SensoryNerve;

import java.util.ArrayList;
import java.util.List;

public class TransFormerManager {
    private final List<CodecBlock> encoderBlocks = new ArrayList<>();//编码器模块
    private final List<CodecBlock> decoderBlocks = new ArrayList<>();//解码器模块
    private SensoryNerve sensoryNerve;//感知神经元
    private FirstDecoderBlock firstDecoderBlock;//第一个解码器模块
    private LineBlock lineBlock;//线性分类层
    private TransWordVector transWordVector;//内置词向量

    public TransWordVector getTransWordVector() {
        return transWordVector;
    }

    public SensoryNerve getSensoryNerve() {
        return sensoryNerve;
    }

    public TransFormerModel getModel() throws Exception {
        TransFormerModel transFormerModel = new TransFormerModel();
        transFormerModel.setTransWordVectorModel(transWordVector.getModel());
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


    public void insertModel(TransFormerModel transFormerModel, TfConfig tfConfig) throws Exception {
        init(tfConfig, null, transFormerModel.getTransWordVectorModel());
        List<CodecBlockModel> encoderBlockModels = transFormerModel.getEncoderBlockModels();
        List<CodecBlockModel> decoderBlockModels = transFormerModel.getDecoderBlockModels();
        int minSize = Math.min(encoderBlocks.size(), encoderBlockModels.size());
        for (int i = 0; i < minSize; i++) {
            encoderBlocks.get(i).insertModel(encoderBlockModels.get(i));
            decoderBlocks.get(i).insertModel(decoderBlockModels.get(i));
        }
        firstDecoderBlock.insertModel(transFormerModel.getFirstDecoderBlockModel());
        lineBlock.insertModel(transFormerModel.getLineBlockModel());
    }

    public void init(TfConfig tfConfig, List<String> sentenceList) throws Exception {
        if (transWordVector == null) {
            init(tfConfig, sentenceList, null);
        } else {
            transWordVector.init(sentenceList);
        }
    }

    /**
     * 初始化神经元参数
     *
     * @param tfConfig             配置参数
     * @param sentenceList         样本语句
     * @param transWordVectorModel 词向量模型
     * @throws Exception 如果参数错误则抛异常
     */
    private void init(TfConfig tfConfig, List<String> sentenceList, TransWordVectorModel transWordVectorModel) throws Exception {
        transWordVector = new TransWordVector(tfConfig);
        int typeNumber = tfConfig.getTypeNumber();
        if (transWordVectorModel == null) {
            transWordVector.init(sentenceList);
        } else {
            transWordVector.insertModel(transWordVectorModel);
        }
        if (tfConfig.isNorm()) {
            typeNumber = transWordVector.getWordSize();
        }
        int multiNumber = tfConfig.getMultiNumber();
        int featureDimension = tfConfig.getFeatureDimension();
        if (featureDimension % 2 != 0) {
            throw new Exception("TransFormer 词向量维度必须为偶数");
        }
        int allDepth = tfConfig.getAllDepth();
        float studyPoint = tfConfig.getStudyRate();
        boolean showLog = tfConfig.isShowLog();
        int regularModel = tfConfig.getRegularModel();
        float regular = tfConfig.getRegular();
        if (multiNumber > 1 && featureDimension > 0 && allDepth > 0 && typeNumber > 1) {
            for (int i = 0; i < allDepth; i++) {
                CodecBlock encoderBlock = new CodecBlock(multiNumber, featureDimension, studyPoint,
                        i + 1, true, regularModel, regular, tfConfig.getCoreNumber(), transWordVector);
                encoderBlocks.add(encoderBlock);
            }
            CodecBlock lastEnCoderBlock = encoderBlocks.get(encoderBlocks.size() - 1);//最后一层编码器
            for (int i = 0; i < allDepth; i++) {
                CodecBlock decoderBlock = new CodecBlock(multiNumber, featureDimension, studyPoint,
                        i + 2, false, regularModel, regular, tfConfig.getCoreNumber(), transWordVector);
                decoderBlock.setLastEncoderBlock(lastEnCoderBlock);//放入最优一层编码器
                decoderBlocks.add(decoderBlock);
            }
            CodecBlock lastDecoderBlock = decoderBlocks.get(decoderBlocks.size() - 1);
            connectCodecBlock(encoderBlocks);
            connectCodecBlock(decoderBlocks);
            lineBlock = new LineBlock(typeNumber, featureDimension, studyPoint, lastDecoderBlock, showLog, regularModel
                    , regular, tfConfig.getCoreNumber(), tfConfig.getTimePunValue());
            lastDecoderBlock.setLineBlock(lineBlock);
            firstDecoderBlock = new FirstDecoderBlock(multiNumber, featureDimension, studyPoint, decoderBlocks.get(0),
                    tfConfig.getCoreNumber(), transWordVector);
            firstDecoderBlock.setLastEncoderBlock(lastEnCoderBlock);
            decoderBlocks.get(0).setFirstDecoderBlock(firstDecoderBlock);
            sensoryNerve = new SensoryNerve(encoderBlocks.get(0), firstDecoderBlock, transWordVector);
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
