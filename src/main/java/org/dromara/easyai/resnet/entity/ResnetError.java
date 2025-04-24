package org.dromara.easyai.resnet.entity;

import org.dromara.easyai.matrixTools.Matrix;

import java.util.List;

/**
 * @author lidapeng
 * @time 2025/4/16 11:19
 */
public class ResnetError {
    private List<Matrix> resErrorMatrixList;//残差误差
    private List<Matrix> nextErrorMatrixList;

    public List<Matrix> getResErrorMatrixList() {
        return resErrorMatrixList;
    }

    public void setResErrorMatrixList(List<Matrix> resErrorMatrixList) {
        this.resErrorMatrixList = resErrorMatrixList;
    }

    public List<Matrix> getNextErrorMatrixList() {
        return nextErrorMatrixList;
    }

    public void setNextErrorMatrixList(List<Matrix> nextErrorMatrixList) {
        this.nextErrorMatrixList = nextErrorMatrixList;
    }
}
