package org.dromara.easyai.nerveEntity;

import org.dromara.easyai.matrixTools.Matrix;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @time 2025/2/27 20:21
 * @des 卷积参数保存
 */
public class ConvParameter {
    private final List<Matrix> nerveMatrixList = new ArrayList<>();//权重矩阵
    private final List<ConvSize> convSizeList = new ArrayList<>();
    private final List<Matrix> im2colMatrixList = new ArrayList<>();
    private final List<Matrix> outMatrixList = new ArrayList<>();
    private List<Float> oneConvPower;//1*1卷积核
    private List<Matrix> featureMatrixList;//所有通道特征矩阵集合
    private int outX;
    private int outY;

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

    public List<Float> getOneConvPower() {
        return oneConvPower;
    }

    public void setOneConvPower(List<Float> oneConvPower) {
        this.oneConvPower = oneConvPower;
    }

    public List<Matrix> getFeatureMatrixList() {
        return featureMatrixList;
    }

    public void setFeatureMatrixList(List<Matrix> featureMatrixList) {
        this.featureMatrixList = featureMatrixList;
    }
}
