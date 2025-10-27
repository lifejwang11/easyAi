package org.dromara.easyai.entity;

/**
 * @author lidapeng
 * @time 2025/10/27 16:02
 * @des 混高参数模型
 */
public class ClusterModel {
    private Double a;
    private Float[] avgMatrix;
    private Float[] varMatrix;
    private int key;

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public Double getA() {
        return a;
    }

    public void setA(Double a) {
        this.a = a;
    }

    public Float[] getAvgMatrix() {
        return avgMatrix;
    }

    public void setAvgMatrix(Float[] avgMatrix) {
        this.avgMatrix = avgMatrix;
    }

    public Float[] getVarMatrix() {
        return varMatrix;
    }

    public void setVarMatrix(Float[] varMatrix) {
        this.varMatrix = varMatrix;
    }
}
