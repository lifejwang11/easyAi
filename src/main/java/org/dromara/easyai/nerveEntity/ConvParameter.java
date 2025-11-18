package org.dromara.easyai.nerveEntity;

import org.dromara.easyai.matrixTools.Matrix;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lidapeng
 * @time 2025/2/27 20:21
 * @des 卷积参数保存
 */
public class ConvParameter {
    private final List<Matrix> nerveMatrixList = new ArrayList<>();//下采样卷积权重矩阵 需取出模型
    private final List<Matrix> dymStudyRateList = new ArrayList<>();//动态学习率
    private final List<ConvSize> convSizeList = new ArrayList<>();
    private final List<Matrix> im2colMatrixList = new ArrayList<>();
    private final List<Matrix> outMatrixList = new ArrayList<>();
    private List<List<Float>> oneConvPower;//1*1卷积核 需取出模型
    private List<List<Float>> oneDymStudyRateList;//
    private List<Float> upOneDymStudyRateList;
    private List<Float> upOneConvPower;//上采样降维卷积核 需要取出模型
    private List<Matrix> featureMatrixList;//所有通道特征矩阵集合
    private final List<Matrix> upDymStudyRateList = new ArrayList<>();
    private final List<Matrix> upNerveMatrixList = new ArrayList<>();//上采样卷积权重  需要取出模型
    private List<Matrix> upFeatureMatrixList;//上采样输入特征
    private List<Matrix> upOutMatrixList;//上卷积输出矩阵集合
    private final Map<Long, List<Matrix>> featureMap = new ConcurrentHashMap<>();
    private int outX;
    private int outY;
    private int encoderX;
    private int encoderY;
    private float studyRateTh = 0;//记录线性模型动态偏移值学习率

    public float getStudyRateTh() {
        return studyRateTh;
    }

    public void setStudyRateTh(float studyRateTh) {
        this.studyRateTh = studyRateTh;
    }

    public List<Matrix> getUpDymStudyRateList() {
        return upDymStudyRateList;
    }

    public List<Float> getUpOneDymStudyRateList() {
        return upOneDymStudyRateList;
    }

    public void setUpOneDymStudyRateList(List<Float> upOneDymStudyRateList) {
        this.upOneDymStudyRateList = upOneDymStudyRateList;
    }

    public List<List<Float>> getOneDymStudyRateList() {
        return oneDymStudyRateList;
    }

    public void setOneDymStudyRateList(List<List<Float>> oneDymStudyRateList) {
        this.oneDymStudyRateList = oneDymStudyRateList;
    }

    public List<Matrix> getDymStudyRateList() {
        return dymStudyRateList;
    }

    public List<Matrix> getUpFeatureMatrixList() {
        return upFeatureMatrixList;
    }

    public void setUpFeatureMatrixList(List<Matrix> upFeatureMatrixList) {
        this.upFeatureMatrixList = upFeatureMatrixList;
    }

    public List<Matrix> getUpOutMatrixList() {
        return upOutMatrixList;
    }

    public void setUpOutMatrixList(List<Matrix> upOutMatrixList) {
        this.upOutMatrixList = upOutMatrixList;
    }

    public List<Float> getUpOneConvPower() {
        return upOneConvPower;
    }

    public void setUpOneConvPower(List<Float> upOneConvPower) {
        this.upOneConvPower = upOneConvPower;
    }


    public int getEncoderX() {
        return encoderX;
    }

    public void setEncoderX(int encoderX) {
        this.encoderX = encoderX;
    }

    public int getEncoderY() {
        return encoderY;
    }

    public void setEncoderY(int encoderY) {
        this.encoderY = encoderY;
    }

    public Map<Long, List<Matrix>> getFeatureMap() {
        return featureMap;
    }

    public List<Matrix> getUpNerveMatrixList() {
        return upNerveMatrixList;
    }

    public int getOutX() {
        return outX;
    }

    public void setOutX(int outX) {
        this.outX = outX;
    }

    public int getOutY() {
        return outY;
    }

    public void setOutY(int outY) {
        this.outY = outY;
    }

    public List<Matrix> getNerveMatrixList() {
        return nerveMatrixList;
    }

    public List<ConvSize> getConvSizeList() {
        return convSizeList;
    }

    public List<Matrix> getIm2colMatrixList() {
        return im2colMatrixList;
    }

    public List<Matrix> getOutMatrixList() {
        return outMatrixList;
    }

    public List<List<Float>> getOneConvPower() {
        return oneConvPower;
    }

    public void setOneConvPower(List<List<Float>> oneConvPower) {
        this.oneConvPower = oneConvPower;
    }

    public List<Matrix> getFeatureMatrixList() {
        return featureMatrixList;
    }

    public void setFeatureMatrixList(List<Matrix> featureMatrixList) {
        this.featureMatrixList = featureMatrixList;
    }
}
