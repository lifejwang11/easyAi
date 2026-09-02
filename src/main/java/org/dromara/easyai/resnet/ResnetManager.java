package org.dromara.easyai.resnet;

import org.dromara.easyai.batchNerve.BatchInputBlock;
import org.dromara.easyai.batchNerve.BatchNerveConfig;
import org.dromara.easyai.batchNerve.BatchNerveManager;
import org.dromara.easyai.config.FpnConfig;
import org.dromara.easyai.config.RZ;
import org.dromara.easyai.config.ResnetConfig;
import org.dromara.easyai.conv.ResConvCount;
import org.dromara.easyai.conv.dcn.DConv;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.nerveCenter.NerveManager;
import org.dromara.easyai.nerveEntity.SensoryNerve;
import org.dromara.easyai.resnet.entity.ResBlockModel;
import org.dromara.easyai.resnet.entity.ResnetModel;
import org.dromara.easyai.resnet.fpn.FpnManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @time 2025/4/11 10:51
 * @des resnet管理器
 */
public class ResnetManager extends ResConvCount {
    private BatchNerveManager batchNerveManager;
    private final List<ResBlock> resBlockList = new ArrayList<>();//残差集合
    private ResnetInput restNetInput;
    private int deep;//深度
    private int featureLength;//最后一层通道数
    private int lastSize;//最后一层卷积层大小
    private FpnManager fpnManager;

    public int getDeep() {
        return deep;
    }

    public int getFeatureLength() {
        return featureLength;
    }

    public int getLastSize() {
        return lastSize;
    }

    public ResnetInput getRestNetInput() {
        return restNetInput;
    }

    public ResnetModel getModel() throws Exception {
        ResnetModel resnetModel = new ResnetModel();
        if (fpnManager != null) {
            resnetModel.setFpnBlockModels(fpnManager.getModel());
        }
        List<ResBlockModel> resBlockModelList = new ArrayList<>();
        resnetModel.setResBlockModelList(resBlockModelList);
        for (ResBlock resBlock : resBlockList) {
            resBlockModelList.add(resBlock.getModel());
        }
        resnetModel.setParameter(batchNerveManager.getModel());
        return resnetModel;
    }

    public void insertModel(ResnetModel resnetModel) {
        if (fpnManager != null) {
            fpnManager.insertModel(resnetModel.getFpnBlockModels());
        }
        List<ResBlockModel> resBlockModelList = resnetModel.getResBlockModelList();
        int size = resBlockList.size();
        for (int i = 0; i < size; i++) {
            resBlockList.get(i).insertModel(resBlockModelList.get(i));
        }
        batchNerveManager.insertModel(resnetModel.getParameter());
    }

    private int getFeatureSize(int deep, int size) {
        int x = size;
        int step = 2;
        for (int i = 0; i < deep; i++) {
            x = x + x % step;
            x = x / step;
            if (i == 0) {
                x = x + x % step;
                x = x / step;
            }
        }
        return x;
    }

    /**
     * 计算每个stage输出特征图尺寸，用于FPN尺寸对齐
     * 规则：
     * 1. 7*7卷积固定开启：7*7(stride2) + maxPool(stride2)，产出stage0输出
     * 2. 3*3 stride=1，padding补两排0，尺寸不变，不参与尺寸运算
     * 3. stageCount：全部stage总个数（包含stage0），外部传入，输入图像可变，下采样层数不固定
     * 4. 每一次stride=2下采样，actionDeep递增，传给fill，不再传固定全局值
     *
     * @param inputSize  原始正方形输入图像尺寸
     * @param stageCount 全部stage总个数（含stage0），必须 >=1
     * @return List<Integer> 每个stage输出尺寸，list.size == stageCount
     */
    public List<Integer> calcStageOutputSizes(int inputSize, int stageCount) {
        List<Integer> outputSizes = new ArrayList<>();
        int current = inputSize;
        final int step = 2;
        int actionDeep = 0; // 记录这是第几次stride‑2下采样动作

        // stage0：7*7(stride2) 第1次下采样动作
        actionDeep++;
        boolean needPad7x7 = fill(actionDeep, current, true);
        current = needPad7x7 ? (current + step - 1) / step : current / step;

        // maxPool(stride2) 第2次下采样动作，完成stage0
        boolean needPadPool = fill(actionDeep, current, false);
        current = needPadPool ? (current + step - 1) / step : current / step;
        outputSizes.add(current);

        // 剩下 stageCount‑1 个残差下采样stage，每一轮一次stride‑2
        if (stageCount > 1) {
            for (int s = 1; s < stageCount; s++) {
                actionDeep++;
                boolean needPad = fill(actionDeep, current, false);
                current = needPad ? (current + step - 1) / step : current / step;
                outputSizes.add(current);
            }
        }
        return outputSizes;
    }


    public ResnetManager(ResnetConfig resNetConfig, ActiveFunction activeFunction) throws Exception {
        init(resNetConfig, activeFunction);
    }

    public ResnetManager(ResnetConfig resNetConfig, FpnConfig fpnConfig, ActiveFunction activeFunction) throws Exception {
        init(resNetConfig, activeFunction);
        fpnConfig.setTypeNumber(resNetConfig.getTypeNumber());
        if (deep < fpnConfig.getStartDeep()) {
            throw new IllegalAccessException("fpn起始层不可以大于resnet总层数");
        }
        fpnConfig.setAllDeep(deep);
        fpnConfig.setStudyRate(resNetConfig.getStudyRate());
        fpnConfig.setgMaxTh(resNetConfig.getGMaxTh());
        fpnConfig.setLayerCutTh(resNetConfig.getLayGMaxTh());
        fpnConfig.setDeep(resNetConfig.getHiddenDeep());
        fpnConfig.setShowLog(resNetConfig.isShowLog());
        fpnConfig.setChannelNo(resNetConfig.getChannelNo());
        fpnConfig.setSize(resNetConfig.getSize());
        fpnManager = new FpnManager(fpnConfig, resBlockList);
    }

    private void init(ResnetConfig resNetConfig, ActiveFunction activeFunction) throws Exception {
        int deep = getConvDeep(resNetConfig.getSize(), resNetConfig.getMinFeatureSize());//获取深度
        int channelNo = resNetConfig.getChannelNo();//通道数
        int lastSize = getFeatureSize(deep, resNetConfig.getSize());//最后一层特征大小
        int dcnDeep = resNetConfig.getDcnDeep();
        //全局学习率
        float studyRate = resNetConfig.getStudyRate();
        if (deep < 1) {
            throw new Exception("图像尺寸太小了，不能用resnet进行训练");
        }
        int featureLength = (int) (channelNo * Math.pow(2, deep - 1));//卷积层输出特征大小
        this.lastSize = lastSize;
        this.deep = deep;
        this.featureLength = featureLength;
        BatchNerveConfig batchNerveConfig = getBatchNerveConfig(resNetConfig, featureLength, studyRate);
        ResNetConnectionLine resNetConnectionLine = new ResNetConnectionLine();
        batchNerveManager = new BatchNerveManager(batchNerveConfig, activeFunction, resNetConnectionLine);
        boolean rz = resNetConfig.getRegularModel() != RZ.NOT_RZ;
        for (int i = 0; i < deep; i++) {
            BatchInputBlock batchInputBlock = null;
            if (i == deep - 1) {
                batchInputBlock = batchNerveManager.getInputBlock();
            }
            boolean dConv = isDCN(i, dcnDeep);
            ResBlock resBlock = new ResBlock(channelNo, i + 1, studyRate, resNetConfig.getSize(), batchInputBlock
                    , resNetConfig.getGMaxTh(), resNetConfig.isAuto(), resNetConfig.getBatchSize(),
                    rz, resNetConfig.getRegular(), resNetConfig.getLayGMaxTh(), dConv, resNetConfig.isFpn());
            resBlockList.add(resBlock);
        }
        restNetInput = new ResnetInput(resBlockList.get(0), resNetConfig.getSize(), resNetConfig.getBatchSize());
        connection();//残差块进行互相连接
        resNetConnectionLine.setLastBlock(resBlockList.get(deep - 1), lastSize);
    }

    private boolean isDCN(int i, int dcnDeep) {
        return dcnDeep > 0 && i >= dcnDeep - 1;
    }

    private BatchNerveConfig getBatchNerveConfig(ResnetConfig resNetConfig, int featureLength, float studyRate) {
        BatchNerveConfig batchNerveConfig = new BatchNerveConfig();
        batchNerveConfig.setInputSize(featureLength);
        batchNerveConfig.setHiddenSize(resNetConfig.getHiddenNerveNumber());
        batchNerveConfig.setOutSize(resNetConfig.getTypeNumber());
        batchNerveConfig.setSoftMax(resNetConfig.isSoftMax());
        batchNerveConfig.setStudyRate(studyRate);
        batchNerveConfig.setAuto(resNetConfig.isAuto());
        batchNerveConfig.setGMaxTh(resNetConfig.getGMaxTh());
        batchNerveConfig.setDeep(resNetConfig.getHiddenDeep());
        batchNerveConfig.setShowLog(resNetConfig.isShowLog());
        batchNerveConfig.setRegularModel(resNetConfig.getRegularModel());
        batchNerveConfig.setRegular(resNetConfig.getRegular());
        return batchNerveConfig;
    }

    private void connection() {//残差块相互连接
        int size = resBlockList.size();
        for (int i = 0; i < size - 1; i++) {
            ResBlock resBlock = resBlockList.get(i);
            ResBlock nextResBlock = resBlockList.get(i + 1);
            resBlock.setSonResBlock(nextResBlock);
            nextResBlock.setFatherResBlock(resBlock);
        }
    }

}
