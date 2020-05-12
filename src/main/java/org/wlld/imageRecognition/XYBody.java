package org.wlld.imageRecognition;

import org.wlld.imageRecognition.modelEntity.RegressionBody;

public class XYBody {
    private double[] X;
    private double[] Y;
    private RegressionBody regressionBody;

    public RegressionBody getRegressionBody() {
        return regressionBody;
    }

    public void setRegressionBody(RegressionBody regressionBody) {
        this.regressionBody = regressionBody;
    }

    public double[] getX() {
        return X;
    }

    public void setX(double[] x) {
        X = x;
    }

    public double[] getY() {
        return Y;
    }

    public void setY(double[] y) {
        Y = y;
    }
}
