package org.dromara.transFormer;

import org.dromara.config.TfConfig;
import org.dromara.matrixTools.Matrix;
import org.dromara.transFormer.model.CodecBlockModel;
import org.dromara.transFormer.model.TransFormerModel;
import org.dromara.transFormer.nerve.SensoryNerve;

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

    private Matrix addTimeCode(Matrix feature) throws Exception {
        int x = feature.getX();
        int y = feature.getY();
        Matrix matrix = new Matrix(x, y);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                int k = j / 2;
                double wk = 1 / (Math.pow(10000, 2D * k / y));
                double pe;
                if (j % 2 == 0) {//当列数是偶数
                    pe = Math.sin(wk * i);
                } else {//当列数是奇数
                    pe = Math.cos(wk * i);
                }
                double value = feature.getNumber(i, j) + pe;
                matrix.setNub(i, j, value);
            }
        }
        return matrix;
    }

    private Matrix addTimeCodeBySelf(Matrix feature) throws Exception {//添加时间编码
        double timeStep = 1D / maxLength;
        int x = feature.getX();
        int y = feature.getY();
        Matrix matrix = new Matrix(x, y);
        for (int i = 1; i < x; i++) {
            double step = i * timeStep;
            for (int j = 0; j < y; j++) {
                double value = feature.getNumber(i, j) + step;
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
            double value = col.getAVG();
            myFeature.setNub(0, j, value);
        }
        return myFeature;
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
        int multiNumber = tfConfig.getMultiNumber();
        maxLength = tfConfig.getMaxLength();
        selfTimeCode = tfConfig.isSelfTimeCode();
        int featureDimension = tfConfig.getFeatureDimension();
        if (featureDimension % 2 != 0) {
            throw new Exception("TransFormer 词向量维度必须为偶数");
        }
        int allDepth = tfConfig.getAllDepth();
        double studyPoint = tfConfig.getStudyPoint();
        int typeNumber = tfConfig.getTypeNumber();
        boolean showLog = tfConfig.isShowLog();
        int regularModel = tfConfig.getRegularModel();
        double regular = tfConfig.getRegular();
        if (multiNumber > 1 && featureDimension > 0 && allDepth > 0 && typeNumber > 1) {
            for (int i = 0; i < allDepth; i++) {
                CodecBlock encoderBlock = new CodecBlock(multiNumber, featureDimension, studyPoint,
                        i + 1, true, regularModel, regular, maxLength,
                        selfTimeCode, tfConfig.getCoreNumber());
                encoderBlocks.add(encoderBlock);
            }
            CodecBlock lastEnCoderBlock = encoderBlocks.get(encoderBlocks.size() - 1);//最后一层编码器
            for (int i = 0; i < allDepth; i++) {
                CodecBlock decoderBlock = new CodecBlock(multiNumber, featureDimension, studyPoint,
                        i + 2, false, regularModel, regular, maxLength,
                        selfTimeCode, tfConfig.getCoreNumber());
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
                    , selfTimeCode, tfConfig.getCoreNumber());
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
