package org.dromara.easyai.rnnJumpNerveEntity;


import org.dromara.easyai.matrixTools.Matrix;

import java.util.List;

public class MyWordFeature {
    private Matrix featureMatrix;
    private List<Float> firstFeatureList;

    public Matrix getFeatureMatrix() {
        return featureMatrix;
    }

    public void setFeatureMatrix(Matrix featureMatrix) {
        this.featureMatrix = featureMatrix;
    }

    public List<Float> getFirstFeatureList() {
        return firstFeatureList;
    }

    public void setFirstFeatureList(List<Float> firstFeatureList) {
        this.firstFeatureList = firstFeatureList;
    }
}
