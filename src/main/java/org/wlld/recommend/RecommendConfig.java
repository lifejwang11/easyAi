package org.wlld.recommend;

public class RecommendConfig {
    private double studyTh = 0.5;//进行互相学习的阈值
    private int dimension = 10;//隐层神经元维度

    public double getStudyTh() {
        return studyTh;
    }

    public void setStudyTh(double studyTh) {
        this.studyTh = studyTh;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }
}
