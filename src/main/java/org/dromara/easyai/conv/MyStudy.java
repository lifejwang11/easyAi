package org.dromara.easyai.conv;

import org.dromara.easyai.matrixTools.Matrix;

/**
 * @author lidapeng
 * @time 2025/9/18 14:09
 */
public class MyStudy {
    private float myStudyRate;
    private float error;
    private Matrix errorMatrix;
    private Matrix studyRateMatrix;

    public Matrix getErrorMatrix() {
        return errorMatrix;
    }

    public void setErrorMatrix(Matrix errorMatrix) {
        this.errorMatrix = errorMatrix;
    }

    public Matrix getStudyRateMatrix() {
        return studyRateMatrix;
    }

    public void setStudyRateMatrix(Matrix studyRateMatrix) {
        this.studyRateMatrix = studyRateMatrix;
    }

    public float getMyStudyRate() {
        return myStudyRate;
    }

    public void setMyStudyRate(float myStudyRate) {
        this.myStudyRate = myStudyRate;
    }

    public float getError() {
        return error;
    }

    public void setError(float error) {
        this.error = error;
    }
}
