package org.dromara.easyai.transFormer;

import org.dromara.easyai.config.TfConfig;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.transFormer.model.CodecBlockModel;
import org.dromara.easyai.transFormer.model.TransFormerModel;
import org.dromara.easyai.transFormer.nerve.SensoryNerve;

import java.util.ArrayList;
import java.util.List;

public class TransFormerManager {
    private final SensoryNerve sensoryNerve;//感知神经元
    private final List<CodecBlock> encoderBlocks = new ArrayList<>();//编码器模块
    private final List<CodecBlock> decoderBlocks = new ArrayList<>();//解码器模块
    private final FirstDecoderBlock firstDecoderBlock;//第一个解码器模块
    private final LineBlock lineBlock;//线性分类层
    private final int maxLength;
    private final boolean selfTimeCode;//使用自增时间序列编码
    private final TransWordVector transWordVector;//内置词向量

    public TransWordVector getTransWordVector() {
        return transWordVector;
    }

    public SensoryNerve getSensoryNerve() {
        return sensoryNerve;
    }

    public TransFormerModel getModel() throws Exception {
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

    private Matrix addTimeCode(Matrix feature) throws Exception {
        int x = feature.getX();
        int y = feature.getY();
        Matrix matrix = new Matrix(x, y);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                int k = j / 2;
                float wk = 1 / ((float) Math.pow(10000, 2D * k / y));
                float pe;
                if (j % 2 == 0) {//当列数是偶数
                    pe = (float) Math.sin(wk * i);
                } else {//当列数是奇数
                    pe = (float) Math.cos(wk * i);
                }
                float value = feature.getNumber(i, j) + (float) pe;
                matrix.setNub(i, j, value);
            }
        }
        return matrix;
    }

    private Matrix addTimeCodeBySelf(Matrix feature) throws Exception {//添加时间编码
        float timeStep = 1F / maxLength;
        int x = feature.getX();
        int y = feature.getY();
        Matrix matrix = new Matrix(x, y);
        for (int i = 1; i < x; i++) {
            float step = i * timeStep;
            for (int j = 0; j < y; j++) {
                float value = feature.getNumber(i, j) + step;
                matrix.setNub(i, j, value);
            }
        }
        return matrix;
    }

    public Matrix getStartMatrix(Matrix feature) throws Exception {
        Matrix matrix;
        if (selfTimeCode) {
            matrix = addTimeCodeBySelf(feature);
        } else {
            matrix = addTimeCode(feature);
        }
        Matrix myFeature = new Matrix(1, matrix.getY());
        for (int j = 0; j < matrix.getY(); j++) {
            Matrix col = matrix.getColumn(j);
            float value = col.getAVG();
            myFeature.setNub(0, j, value);
        }
        return myFeature;
    }

    public void insertModel(TransFormerModel transFormerModel) throws Exception {
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

    /**
     * 初始化神经元参数
     *
     * @param tfConfig 配置参数
     * @throws Exception 如果参数错误则抛异常
     */
    public TransFormerManager(TfConfig tfConfig, List<String> sentenceList) throws Exception {
        int multiNumber = tfConfig.getMultiNumber();
        maxLength = tfConfig.getMaxLength();
        selfTimeCode = tfConfig.isSelfTimeCode();
        transWordVector = new TransWordVector(tfConfig);
        int typeNumber = transWordVector.init(sentenceList);
        int featureDimension = tfConfig.getFeatureDimension();
        if (featureDimension % 2 != 0) {
            throw new Exception("TransFormer 词向量维度必须为偶数");
        }
        int allDepth = tfConfig.getAllDepth();
        float studyPoint = tfConfig.getStudyPoint();
        boolean showLog = tfConfig.isShowLog();
        int regularModel = tfConfig.getRegularModel();
        float regular = tfConfig.getRegular();
        if (multiNumber > 1 && featureDimension > 0 && allDepth > 0 && typeNumber > 1) {
            for (int i = 0; i < allDepth; i++) {
                CodecBlock encoderBlock = new CodecBlock(multiNumber, featureDimension, studyPoint,
                        i + 1, true, regularModel, regular, maxLength,
                        selfTimeCode, tfConfig.getCoreNumber(), transWordVector);
                encoderBlocks.add(encoderBlock);
            }
            CodecBlock lastEnCoderBlock = encoderBlocks.get(encoderBlocks.size() - 1);//最后一层编码器
            for (int i = 0; i < allDepth; i++) {
                CodecBlock decoderBlock = new CodecBlock(multiNumber, featureDimension, studyPoint,
                        i + 2, false, regularModel, regular, maxLength,
                        selfTimeCode, tfConfig.getCoreNumber(), transWordVector);
                decoderBlock.setLastEncoderBlock(lastEnCoderBlock);//放入最优一层编码器
                decoderBlocks.add(decoderBlock);
            }
            CodecBlock lastDecoderBlock = decoderBlocks.get(decoderBlocks.size() - 1);
            connectCodecBlock(encoderBlocks);
            connectCodecBlock(decoderBlocks);
            lineBlock = new LineBlock(typeNumber, featureDimension, studyPoint, lastDecoderBlock, showLog, regularModel
                    , regular, tfConfig.getCoreNumber(), tfConfig.getTimePunValue());
            lastDecoderBlock.setLineBlock(lineBlock);
            firstDecoderBlock = new FirstDecoderBlock(multiNumber, featureDimension, studyPoint, decoderBlocks.get(0), maxLength
                    , selfTimeCode, tfConfig.getCoreNumber(), transWordVector);
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
