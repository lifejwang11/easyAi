package org.dromara.easyai.rnnNerveCenter;


import org.dromara.easyai.rnnNerveEntity.DymNerveStudy;
import org.dromara.easyai.rnnNerveEntity.NerveStudy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @description 学习结果
 * @date 3:33 下午 2020/1/8
 */
public class ModelParameter {
    //神经远模型参数
    private List<List<NerveStudy>> depthNerves = new ArrayList<>();//隐层神经元
    private List<NerveStudy> outNerves = new ArrayList<>();//输出神经元
    private List<RnnOutNerveStudy> rnnOutNerveStudies = new ArrayList<>();//rnn多级输出神经元
    private List<DymNerveStudy> dymNerveStudies = new ArrayList<>();//动态神经元隐层
    private DymNerveStudy dymOutNerveStudy = new DymNerveStudy();//动态神经元输出层
    private Map<Integer, Double> foodS;//干食品类别对应的积分 需激活注入

    public List<RnnOutNerveStudy> getRnnOutNerveStudies() {
        return rnnOutNerveStudies;
    }

    public void setRnnOutNerveStudies(List<RnnOutNerveStudy> rnnOutNerveStudies) {
        this.rnnOutNerveStudies = rnnOutNerveStudies;
    }

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

    public Map<Integer, Double> getFoodS() {
        return foodS;
    }

    public void setFoodS(Map<Integer, Double> foodS) {
        this.foodS = foodS;
    }
}
