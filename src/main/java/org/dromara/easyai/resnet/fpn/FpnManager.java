package org.dromara.easyai.resnet.fpn;

import org.dromara.easyai.batchNerve.BatchNerveConfig;
import org.dromara.easyai.batchNerve.BatchNerveManager;
import org.dromara.easyai.config.FpnConfig;
import org.dromara.easyai.function.ReLu;
import org.dromara.easyai.resnet.ResBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @time 2026/8/27 14:16
 * @des fpn管理器
 */
public class FpnManager {
    private final List<FpnBlock> fpnBlockList = new ArrayList<>();
    private final int typeNumber;
    private final float studyRate;
    private final float gMaxTh;
    private final int deep;
    private final boolean showLog;
    private final int channelNo;
    private final List<ResBlock> resBlockList;
    private final FpnConfig fpnConfig;

    public FpnManager(FpnConfig fpnConfig, List<ResBlock> resBlockList) throws Exception {
        typeNumber = fpnConfig.getTypeNumber();
        studyRate = fpnConfig.getStudyRate();
        gMaxTh = fpnConfig.getgMaxTh();
        deep = fpnConfig.getDeep();
        showLog = fpnConfig.isShowLog();
        channelNo = fpnConfig.getChannelNo();
        this.resBlockList = resBlockList;
        this.fpnConfig = fpnConfig;
        initFpnBlock(fpnConfig.getStartDeep(), fpnConfig.getAllDeep());
    }

    public void insertModel(List<FpnBlockModel> fpnBlockModels) {
        int size = fpnBlockList.size();
        for (int i = 0; i < size; i++) {
            if (fpnBlockModels.size() > i) {
                fpnBlockList.get(i).insertModel(fpnBlockModels.get(i));
            }
        }
    }

    public List<FpnBlockModel> getModel() {
        List<FpnBlockModel> fpnBlockModels = new ArrayList<>();
        for (FpnBlock fpnBlock : fpnBlockList) {
            fpnBlockModels.add(fpnBlock.getModel());
        }
        return fpnBlockModels;
    }

    private void initFpnBlock(int startDeep, int allDeep) throws Exception {
        int size = allDeep - startDeep + 1;
        for (int i = 0; i < size; i++) {
            int deep = startDeep + i;
            boolean first = i == 0;
            FpnBack type = new FpnBack(1);
            FpnBack position = new FpnBack(2);
            BatchNerveManager typeNerve = getTypeManager(deep, type);
            BatchNerveManager positionNerve = getPositionManager(deep, position);
            ResBlock resBlock = resBlockList.get(deep - 1);
            int channel = (int) (channelNo * Math.pow(2, deep - 1));//当前深度通道数
            FpnBlock fpnBlock = new FpnBlock(channel, resBlock, deep, typeNerve, positionNerve, fpnConfig, first);
            type.setFpnBlock(fpnBlock);
            position.setFpnBlock(fpnBlock);
            fpnBlockList.add(fpnBlock);
        }
        resBlockList.get(resBlockList.size() - 1).setFirstFpn(fpnBlockList.get(fpnBlockList.size() - 1));
        connection();
    }

    private void connection() {
        int size = fpnBlockList.size();
        for (int i = size - 1; i > 0; i--) {
            FpnBlock fpnBlock = fpnBlockList.get(i);//深层
            FpnBlock nextFpnBlock = fpnBlockList.get(i - 1);//浅层
            fpnBlock.setSonBlock(nextFpnBlock);
            nextFpnBlock.setFatherBlock(fpnBlock);
        }
    }

    private BatchNerveManager getTypeManager(int myDeep, FpnBack type) throws Exception {
        return new BatchNerveManager(getTypeConfig(myDeep), new ReLu(), type);
    }

    private BatchNerveManager getPositionManager(int myDeep, FpnBack position) throws Exception {
        return new BatchNerveManager(getPositionConfig(myDeep), new ReLu(), position);
    }


    private BatchNerveConfig getTypeConfig(int myDeep) {
        int featureLength = (int) (channelNo * Math.pow(2, myDeep - 1));//当前深度通道数
        BatchNerveConfig typeConfig = new BatchNerveConfig();
        typeConfig.setInputSize(featureLength);
        typeConfig.setHiddenSize(featureLength / 2);
        typeConfig.setOutSize(typeNumber + 1);
        typeConfig.setSoftMax(true);
        typeConfig.setStudyRate(studyRate);
        typeConfig.setAuto(true);
        typeConfig.setGMaxTh(gMaxTh);
        typeConfig.setDeep(deep);
        typeConfig.setShowLog(showLog);
        typeConfig.setRegular(0);
        return typeConfig;
    }

    private BatchNerveConfig getPositionConfig(int myDeep) {
        int featureLength = (int) (channelNo * Math.pow(2, myDeep - 1));//当前深度通道数
        BatchNerveConfig typeConfig = new BatchNerveConfig();
        typeConfig.setInputSize(featureLength);
        typeConfig.setHiddenSize(featureLength / 2);
        typeConfig.setOutSize(5);
        typeConfig.setSoftMax(false);
        typeConfig.setStudyRate(studyRate);
        typeConfig.setAuto(true);
        typeConfig.setGMaxTh(gMaxTh);
        typeConfig.setDeep(deep);
        typeConfig.setShowLog(showLog);
        typeConfig.setRegular(0);
        return typeConfig;
    }

}
