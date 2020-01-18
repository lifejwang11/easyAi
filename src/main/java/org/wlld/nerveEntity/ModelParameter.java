package org.wlld.nerveEntity;

import org.wlld.MatrixTools.Matrix;

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
    private List<NerveStudy> outNevers = new ArrayList<>();//输出神经元
    private List<DymNerveStudy> dymNerveStudies = new ArrayList<>();//动态神经元隐层
    private DymNerveStudy dymOutNerveStudy = new DymNerveStudy();//动态神经元输出层

    public List<DymNerveStudy> getDymNerveStudies() {
        return dymNerveStudies;
    }

    public void setDymNerveStudies(List<DymNerveStudy> dymNerveStudies) {
        this.dymNerveStudies = dymNerveStudies;
    }

    public DymNerveStudy getDymOutNerveStudy() {
        return dymOutNerveStudy;
    }

    public void setDymOutNerveStudy(DymNerveStudy dymOutNerveStudy) {
        this.dymOutNerveStudy = dymOutNerveStudy;
    }

    public List<List<NerveStudy>> getDepthNerves() {
        return depthNerves;
    }

    public void setDepthNerves(List<List<NerveStudy>> depthNerves) {
        this.depthNerves = depthNerves;
    }

    public List<NerveStudy> getOutNevers() {
        return outNevers;
    }

    public void setOutNevers(List<NerveStudy> outNevers) {
        this.outNevers = outNevers;
    }
}
