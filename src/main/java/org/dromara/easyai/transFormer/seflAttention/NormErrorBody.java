package org.dromara.easyai.transFormer.seflAttention;

import org.dromara.easyai.matrixTools.Matrix;

/**
 * @author lidapeng
 * @time 2026/7/31 13:36
 * @des 归一化梯度实体
 */
public class NormErrorBody {
    private Matrix insertMatrix;//输入值
    private Matrix outMatrix;//输出值
    private float avg;//均值
    private float sd;//标准差

    public Matrix getInsertMatrix() {
        return insertMatrix;
    }

    public void setInsertMatrix(Matrix insertMatrix) {
        this.insertMatrix = insertMatrix;
    }

    public Matrix getOutMatrix() {
        return outMatrix;
    }

    public void setOutMatrix(Matrix outMatrix) {
        this.outMatrix = outMatrix;
    }

    public float getAvg() {
        return avg;
    }

    public void setAvg(float avg) {
        this.avg = avg;
    }

    public float getSd() {
        return sd;
    }

    public void setSd(float sd) {
        this.sd = sd;
    }
}
