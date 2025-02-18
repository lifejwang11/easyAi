package org.dromara.easyai.recommend;

public class RecommendConfig {
    private float studyTh = 0.5f;//进行互相学习的阈值
    private int dimension = 10;//隐层神经元维度

    public float getStudyTh() {
        return studyTh;
    }

    public void setStudyTh(float studyTh) {
        this.studyTh = studyTh;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }
}
