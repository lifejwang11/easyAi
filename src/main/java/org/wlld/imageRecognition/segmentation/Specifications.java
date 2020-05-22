package org.wlld.imageRecognition.segmentation;

public class Specifications {
    private double minWidth;
    private double minHeight;
    private double maxWidth;
    private double maxHeight;

    public double getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(double minWidth) {
        this.minWidth = minWidth;
    }

    public double getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(double minHeight) {
        this.minHeight = minHeight;
    }

    public double getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(double maxWidth) {
        this.maxWidth = maxWidth;
    }

    public double getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(double maxHeight) {
        this.maxHeight = maxHeight;
    }
}
