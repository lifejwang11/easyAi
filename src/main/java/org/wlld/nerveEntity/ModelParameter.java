package org.wlld.nerveEntity;

import org.wlld.MatrixTools.Matrix;
import org.wlld.imageRecognition.KMatrix;
import org.wlld.imageRecognition.border.BorderBody;
import org.wlld.imageRecognition.border.Frame;

import java.util.ArrayList;
import java.util.HashMap;
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
    private List<NerveStudy> outNevers = new ArrayList<>();//输出神经元
    private List<DymNerveStudy> dymNerveStudies = new ArrayList<>();//动态神经元隐层
    private DymNerveStudy dymOutNerveStudy = new DymNerveStudy();//动态神经元输出层
    private Map<Integer, BorderBody> borderBodyMap = new HashMap<>();//border特征集合
    private Map<Integer, KMatrix> kMatrixMap = new HashMap<>();//K均值矩阵集合
    private Frame frame;//先验边框

    public Map<Integer, BorderBody> getBorderBodyMap() {
        return borderBodyMap;
    }

    public Map<Integer, KMatrix> getkMatrixMap() {
        return kMatrixMap;
    }

    public void setkMatrixMap(Map<Integer, KMatrix> kMatrixMap) {
        this.kMatrixMap = kMatrixMap;
    }

    public void setBorderBodyMap(Map<Integer, BorderBody> borderBodyMap) {
        this.borderBodyMap = borderBodyMap;
    }

    public Frame getFrame() {
        return frame;
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
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
