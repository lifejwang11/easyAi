package org.wlld.rnnNerveCenter;

public class RandomModelParameter {
    private ModelParameter modelParameter;//模型
    private int[] featureIndexes;//特征下标集合
    private int key;//该随机神经网络体主键

    public ModelParameter getModelParameter() {
        return modelParameter;
    }

    public void setModelParameter(ModelParameter modelParameter) {
        this.modelParameter = modelParameter;
    }

    public int[] getFeatureIndexes() {
        return featureIndexes;
    }

    public void setFeatureIndexes(int[] featureIndexes) {
        this.featureIndexes = featureIndexes;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}
