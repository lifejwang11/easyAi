package org.dromara.easyai.conv;

import org.dromara.easyai.matrixTools.Matrix;

import java.util.List;

/**
 * @author lidapeng
 * @time 2026/8/31 17:04
 */
public class FpnOneConvError {
    private List<Matrix> gMatrixList;//当前通道下所有输入特征图的误差
    private Matrix oneConvError;//当前通道的1v1卷积层总误差

    public List<Matrix> getgMatrixList() {
        return gMatrixList;
    }

    public void setgMatrixList(List<Matrix> gMatrixList) {
        this.gMatrixList = gMatrixList;
    }

    public Matrix getOneConvError() {
        return oneConvError;
    }

    public void setOneConvError(Matrix oneConvError) {
        this.oneConvError = oneConvError;
    }
}
