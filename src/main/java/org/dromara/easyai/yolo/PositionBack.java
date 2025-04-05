package org.dromara.easyai.yolo;

import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.i.OutBack;

import java.util.List;

public class PositionBack implements OutBack {
    private float distX;
    private float distY;
    private float width;
    private float height;
    private float trust;
    private Matrix featureMatrix;

    public Matrix getFeatureMatrix() {
        return featureMatrix;
    }

    public float getTrust() {
        return trust;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getDistX() {
        return distX;
    }

    public float getDistY() {
        return distY;
    }

    @Override
    public void getBack(float out, int id, long eventId) {
        switch (id) {
            case 1:
                distX = out;
                break;
            case 2:
                distY = out;
                break;
            case 3:
                width = out;
                break;
            case 4:
                height = out;
                break;
            case 5:
                trust = out;
                break;
        }
    }

    @Override
    public void getSoftMaxBack(long eventId, List<Float> softMax) {

    }


    @Override
    public void backWord(String word, long eventId) {

    }

    @Override
    public void getBackMatrix(Matrix matrix, int id, long eventId) {
        featureMatrix = matrix;
    }

    @Override
    public void getWordVector(int id, float w) {

    }

    @Override
    public void getBackThreeChannelMatrix(ThreeChannelMatrix picture) {

    }
}
