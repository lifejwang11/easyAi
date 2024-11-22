package org.dromara.easyai.rnnNerveCenter;

public class RandomNerveBody {//随机神经网络体
    private NerveManager nerveManager;
    private int[] featureIndexes;//特征下标集合
    private int key;//该随机神经网络体主键

    public NerveManager getNerveManager() {
        return nerveManager;
    }

    public void setNerveManager(NerveManager nerveManager) {
        this.nerveManager = nerveManager;
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
