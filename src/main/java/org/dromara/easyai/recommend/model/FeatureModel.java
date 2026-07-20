package org.dromara.easyai.recommend.model;


/**
 * @author lidapeng
 * @time 2026/7/20 09:12
 */
public class FeatureModel {
    private int id;
    private Float[] matrixModel;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Float[] getMatrixModel() {
        return matrixModel;
    }

    public void setMatrixModel(Float[] matrixModel) {
        this.matrixModel = matrixModel;
    }
}
