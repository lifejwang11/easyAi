package org.wlld.nerveEntity;

import org.wlld.imageRecognition.border.Frame;
import org.wlld.imageRecognition.modelEntity.KBorder;
import org.wlld.imageRecognition.modelEntity.LvqModel;

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
    private List<NerveStudy> outNerves = new ArrayList<>();//输出神经元
    private List<DymNerveStudy> dymNerveStudies = new ArrayList<>();//动态神经元隐层
    private DymNerveStudy dymOutNerveStudy = new DymNerveStudy();//动态神经元输出层
    private Map<Integer, KBorder> borderMap = new HashMap<>();//边框距离模型
    private LvqModel lvqModel;//LVQ模型
    private Map<Integer, List<Double>> matrixK = new HashMap<>();//均值特征向量
    private Map<Integer, List<List<Double>>> knnVector;//Knn模型
    private Frame frame;//先验边框
    private double dnnAvg;//

    public Map<Integer, List<List<Double>>> getKnnVector() {
        return knnVector;
    }

    public void setKnnVector(Map<Integer, List<List<Double>>> knnVector) {
        this.knnVector = knnVector;
    }

    public double getDnnAvg() {
        return dnnAvg;
    }

    public void setDnnAvg(double dnnAvg) {
        this.dnnAvg = dnnAvg;
    }

    public Map<Integer, List<Double>> getMatrixK() {
        return matrixK;
    }

    public void setMatrixK(Map<Integer, List<Double>> matrixK) {
        this.matrixK = matrixK;
    }

    public Frame getFrame() {
        return frame;
    }

    public Map<Integer, KBorder> getBorderMap() {
        return borderMap;
    }

    public void setBorderMap(Map<Integer, KBorder> borderMap) {
        this.borderMap = borderMap;
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

    public List<NerveStudy> getOutNerves() {
        return outNerves;
    }

    public void setOutNerves(List<NerveStudy> outNerves) {
        this.outNerves = outNerves;
    }

    public LvqModel getLvqModel() {
        return lvqModel;
    }

    public void setLvqModel(LvqModel lvqModel) {
        this.lvqModel = lvqModel;
    }
}
