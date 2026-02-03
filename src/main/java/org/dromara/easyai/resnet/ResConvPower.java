package org.dromara.easyai.resnet;


import org.dromara.easyai.resnet.entity.DoubleResConvModel;

import java.util.List;

/**
 * @author lidapeng
 * @time 2025/4/11 13:48
 * @des 残差块卷积权重
 */
public class ResConvPower {
    private ConvLay firstConvPower;//第一层卷积权重
    private ConvLay secondConvPower;//第二层卷积权重
    private List<List<Float>> oneConvPower;//1v1卷积层 需要作为模型取出
    private List<List<Float>> dymStudyRateList;//1*1一阶动态学习率
    private List<List<Float>> dymStudyRate2List;//1*1二阶动态学习率

    public List<List<Float>> getDymStudyRate2List() {
        return dymStudyRate2List;
    }

    public void setDymStudyRate2List(List<List<Float>> dymStudyRate2List) {
        this.dymStudyRate2List = dymStudyRate2List;
    }

    public List<List<Float>> getDymStudyRateList() {
        return dymStudyRateList;
    }

    public void setDymStudyRateList(List<List<Float>> dymStudyRateList) {
        this.dymStudyRateList = dymStudyRateList;
    }

    public DoubleResConvModel getModel() {
        DoubleResConvModel model = new DoubleResConvModel();
        model.setFirstResConvModel(firstConvPower.getModel());
        model.setSecondResConvModel(secondConvPower.getModel());
        model.setOneConvPowerModel(oneConvPower);
        return model;
    }

    public void insertModel(DoubleResConvModel model) {
        firstConvPower.insertModel(model.getFirstResConvModel());
        secondConvPower.insertModel(model.getSecondResConvModel());
        oneConvPower = model.getOneConvPowerModel();
    }

    public ConvLay getFirstConvPower() {
        return firstConvPower;
    }

    public void setFirstConvPower(ConvLay firstConvPower) {
        this.firstConvPower = firstConvPower;
    }

    public ConvLay getSecondConvPower() {
        return secondConvPower;
    }

    public void setSecondConvPower(ConvLay secondConvPower) {
        this.secondConvPower = secondConvPower;
    }

    public List<List<Float>> getOneConvPower() {
        return oneConvPower;
    }

    public void setOneConvPower(List<List<Float>> oneConvPower) {
        this.oneConvPower = oneConvPower;
    }
}
