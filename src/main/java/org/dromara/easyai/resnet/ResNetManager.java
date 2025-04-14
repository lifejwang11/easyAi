package org.dromara.easyai.resnet;

import org.dromara.easyai.config.ResNetConfig;
import org.dromara.easyai.conv.ResConvCount;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.nerveCenter.NerveManager;

/**
 * @author lidapeng
 * @time 2025/4/11 10:51
 * @des resnet管理器
 */
public class ResNetManager extends ResConvCount {
    private final NerveManager nerveManager;
    private final ResNetConnectionLine resNetConnectionLine = new ResNetConnectionLine();
    private final float studyRate;//全局学习率

    public ResNetManager(ResNetConfig resNetConfig, ActiveFunction activeFunction) throws Exception {
        int deep = getConvDeep(resNetConfig.getSize(), resNetConfig.getMinFeatureSize());
        int lastSize = getFeatureSize(deep, resNetConfig.getSize(), false);
        int channelNo = resNetConfig.getChannelNo();//通道数
        studyRate = resNetConfig.getStudyRate();
        if (deep < 2) {
            throw new Exception("图像太小了，不能用resnet进行训练");
        }
        int featureLength = (int) (channelNo * Math.pow(2, deep - 1));//卷积层输出特征大小
        nerveManager = new NerveManager(featureLength, resNetConfig.getHiddenNerveNumber(), resNetConfig.getTypeNumber(), resNetConfig.getHiddenDeep()
                , activeFunction, studyRate, resNetConfig.getRegularModel(), resNetConfig.getRegular(), 0);
        nerveManager.init(true, resNetConfig.isShowLog(), resNetConfig.isSoftMax(), resNetConnectionLine);

    }
}
