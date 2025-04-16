package org.dromara.easyai.resnet.entity;

import org.dromara.easyai.conv.ConvResult;
import org.dromara.easyai.matrixTools.Matrix;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @time 2025/4/12 09:40
 * @des 待训练学习参数实体
 */
public class BackParameter {
    private List<List<ConvResult>> convResultList;//记录卷积核与每个通道的计算结果与参数矩阵
    private List<ConvResult> convResults;//普通通道参数
    private List<Matrix> outMatrixList;//卷积最终输出矩阵
    private List<Matrix> scaleMatrixList;//池化后的矩阵
    private int im2clSize;//im2col变化前的X大小

    public List<Matrix> getScaleMatrixList() {
        return scaleMatrixList;
    }

    public void setScaleMatrixList(List<Matrix> scaleMatrixList) {
        this.scaleMatrixList = scaleMatrixList;
    }

    public List<ConvResult> getConvResults() {
        return convResults;
    }

    public void setConvResults(List<ConvResult> convResults) {
        this.convResults = convResults;
    }

    public int getIm2clSize() {
        return im2clSize;
    }

    public void setIm2clSize(int im2clSize) {
        this.im2clSize = im2clSize;
    }

    public List<Matrix> getOutMatrixList() {
        return outMatrixList;
    }

    public void setOutMatrixList(List<Matrix> outMatrixList) {
        this.outMatrixList = outMatrixList;
    }

    public List<List<ConvResult>> getConvResultList() {
        return convResultList;
    }

    public void setConvResultList(List<List<ConvResult>> convResultList) {
        this.convResultList = convResultList;
    }
}
