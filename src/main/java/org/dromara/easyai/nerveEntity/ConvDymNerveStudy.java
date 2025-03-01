package org.dromara.easyai.nerveEntity;

import java.util.List;

/**
 * @author lidapeng
 * @time 2025/2/28 13:23
 */
public class ConvDymNerveStudy {
    private List<DymNerveStudy> dymNerveStudyList;//每一层卷积
    private List<Float> oneConvPower;//1conv参数

    public List<Float> getOneConvPower() {
        return oneConvPower;
    }

    public void setOneConvPower(List<Float> oneConvPower) {
        this.oneConvPower = oneConvPower;
    }

    public List<DymNerveStudy> getDymNerveStudyList() {
        return dymNerveStudyList;
    }

    public void setDymNerveStudyList(List<DymNerveStudy> dymNerveStudyList) {
        this.dymNerveStudyList = dymNerveStudyList;
    }
}
