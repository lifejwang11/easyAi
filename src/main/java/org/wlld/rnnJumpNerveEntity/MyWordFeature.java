package org.wlld.rnnJumpNerveEntity;


import org.wlld.MatrixTools.Matrix;

import java.util.List;

public class MyWordFeature {
    private Matrix featureMatrix;
    private List<Double> firstFeatureList;

    public Matrix getFeatureMatrix() {
        return featureMatrix;
    }

    public void setFeatureMatrix(Matrix featureMatrix) {
        this.featureMatrix = featureMatrix;
    }

    public List<Double> getFirstFeatureList() {
        return firstFeatureList;
    }

    public void setFirstFeatureList(List<Double> firstFeatureList) {
        this.firstFeatureList = firstFeatureList;
    }
}
