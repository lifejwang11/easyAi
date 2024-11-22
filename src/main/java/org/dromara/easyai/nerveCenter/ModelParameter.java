package org.dromara.easyai.nerveCenter;


import org.dromara.easyai.nerveEntity.DymNerveStudy;
import org.dromara.easyai.nerveEntity.NerveStudy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @description 学习结果
 * @date 3:33 下午 2020/1/8
 */
public class ModelParameter {
    //神经远模型参数
    private List<List<NerveStudy>> depthNerves = new ArrayList<>();//隐层神经元
    private List<NerveStudy> outNerves = new ArrayList<>();//输出神经元
    private List<List<DymNerveStudy>> dymNerveStudies = new ArrayList<>();//动态神经元隐层

    public List<List<NerveStudy>> getDepthNerves() {
        return depthNerves;
    }

    public void setDepthNerves(List<List<NerveStudy>> depthNerves) {
        this.depthNerves = depthNerves;
    }

    public List<NerveStudy> getOutNerves() {
        return outNerves;
    }

    public void setOutNerves(List<NerveStudy> outNerves) {
        this.outNerves = outNerves;
    }

    public List<List<DymNerveStudy>> getDymNerveStudies() {
        return dymNerveStudies;
    }

    public void setDymNerveStudies(List<List<DymNerveStudy>> dymNerveStudies) {
        this.dymNerveStudies = dymNerveStudies;
    }
}
