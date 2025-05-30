package org.dromara.easyai.resnet;

import org.dromara.easyai.config.ResnetConfig;
import org.dromara.easyai.conv.ResConvCount;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.nerveCenter.NerveManager;
import org.dromara.easyai.nerveEntity.SensoryNerve;
import org.dromara.easyai.resnet.entity.ResBlockModel;
import org.dromara.easyai.resnet.entity.ResnetModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @time 2025/4/11 10:51
 * @des resnet管理器
 */
public class ResnetManager extends ResConvCount {
    private final NerveManager nerveManager;
    private final List<ResBlock> resBlockList = new ArrayList<>();//残差集合
    private final ResnetInput restNetInput;

    public ResnetInput getRestNetInput() {
        return restNetInput;
    }

    public ResnetModel getModel() throws Exception {
        ResnetModel resnetModel = new ResnetModel();
        List<ResBlockModel> resBlockModelList = new ArrayList<>();
        resnetModel.setResBlockModelList(resBlockModelList);
        for (ResBlock resBlock : resBlockList) {
            resBlockModelList.add(resBlock.getModel());
        }
        resnetModel.setParameter(nerveManager.getDnnModel());
        return resnetModel;
    }

    public void insertModel(ResnetModel resnetModel) {
        List<ResBlockModel> resBlockModelList = resnetModel.getResBlockModelList();
        int size = resBlockList.size();
        for (int i = 0; i < size; i++) {
            resBlockList.get(i).insertModel(resBlockModelList.get(i));
        }
        nerveManager.insertDnnModel(resnetModel.getParameter());
    }

    public ResnetManager(ResnetConfig resNetConfig, ActiveFunction activeFunction) throws Exception {
        int deep = getConvDeep(resNetConfig.getSize(), resNetConfig.getMinFeatureSize());//获取深度
        int channelNo = resNetConfig.getChannelNo();//通道数
        int lastSize = getFeatureSize(deep, resNetConfig.getSize(), true);//最后一层特征大小
        //全局学习率
        float studyRate = resNetConfig.getStudyRate();
        if (deep < 2) {
            throw new Exception("图像尺寸太小了，不能用resnet进行训练");
        }
        int featureLength = (int) (channelNo * Math.pow(2, deep - 1));//卷积层输出特征大小
        nerveManager = new NerveManager(featureLength, resNetConfig.getHiddenNerveNumber(), resNetConfig.getTypeNumber(), resNetConfig.getHiddenDeep()
                , activeFunction, studyRate, resNetConfig.getRegularModel(), resNetConfig.getRegular(), 0, resNetConfig.getGaMa()
                , resNetConfig.getGMaxTh(), resNetConfig.isAuto());
        ResNetConnectionLine resNetConnectionLine = new ResNetConnectionLine();
        nerveManager.init(true, resNetConfig.isShowLog(), resNetConfig.isSoftMax(), resNetConnectionLine);
        for (int i = 0; i < deep; i++) {
            List<SensoryNerve> sensoryNerves = null;
            if (i == deep - 1) {
                sensoryNerves = nerveManager.getSensoryNerves();
            }
            ResBlock resBlock = new ResBlock(channelNo, i + 1, studyRate, resNetConfig.getSize(), sensoryNerves, resNetConfig.getGaMa()
                    , resNetConfig.getGMaxTh(), resNetConfig.isAuto());
            resBlockList.add(resBlock);
        }
        restNetInput = new ResnetInput(resBlockList.get(0), resNetConfig.getSize());
        connection();//残差块进行互相连接
        resNetConnectionLine.setLastBlock(resBlockList.get(deep - 1), lastSize, resNetConfig.getHiddenNerveNumber(), featureLength);
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
