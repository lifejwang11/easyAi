package org.dromara.easyai.nerveEntity;

import org.dromara.easyai.batchNerve.BatchNerveModel;

import java.util.List;

/**
 * @author lidapeng
 * @time 2025/2/28 13:23
 */
public class ConvDymNerveStudy {
    private List<DymNerveStudy> dymNerveStudyList;//每一层卷积
    private List<List<Float>> oneConvPower;//1conv参数
    private BatchNerveModel batchNerveModel;//可变形卷积

    public BatchNerveModel getBatchNerveModel() {
        return batchNerveModel;
    }

    public void setBatchNerveModel(BatchNerveModel batchNerveModel) {
        this.batchNerveModel = batchNerveModel;
    }

    public List<List<Float>> getOneConvPower() {
        return oneConvPower;
    }

    public void setOneConvPower(List<List<Float>> oneConvPower) {
        this.oneConvPower = oneConvPower;
    }

    public List<DymNerveStudy> getDymNerveStudyList() {
        return dymNerveStudyList;
    }

    public void setDymNerveStudyList(List<DymNerveStudy> dymNerveStudyList) {
        this.dymNerveStudyList = dymNerveStudyList;
    }
}
