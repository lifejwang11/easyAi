package org.dromara.easyai.resnet.fpn;

import org.dromara.easyai.batchNerve.BatchNerveModel;

import java.util.List;

/**
 * @author lidapeng
 * @time 2026/9/1 15:48
 * @des fpn模块 模型
 */
public class FpnBlockModel {
    private Float[] downConvPowerList;//下卷积权重
    private BatchNerveModel typeBatchNerveModel;//分类头模型
    private BatchNerveModel positionBatchNerveModel;//位置头模型
    private List<List<Float>> oneConvListModel;//1v1卷积核权重

    public Float[] getDownConvPowerList() {
        return downConvPowerList;
    }

    public void setDownConvPowerList(Float[] downConvPowerList) {
        this.downConvPowerList = downConvPowerList;
    }

    public BatchNerveModel getTypeBatchNerveModel() {
        return typeBatchNerveModel;
    }

    public void setTypeBatchNerveModel(BatchNerveModel typeBatchNerveModel) {
        this.typeBatchNerveModel = typeBatchNerveModel;
    }

    public BatchNerveModel getPositionBatchNerveModel() {
        return positionBatchNerveModel;
    }

    public void setPositionBatchNerveModel(BatchNerveModel positionBatchNerveModel) {
        this.positionBatchNerveModel = positionBatchNerveModel;
    }

    public List<List<Float>> getOneConvListModel() {
        return oneConvListModel;
    }

    public void setOneConvListModel(List<List<Float>> oneConvListModel) {
        this.oneConvListModel = oneConvListModel;
    }
}
