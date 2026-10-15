package org.dromara.easyai.yolo;

import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.i.OutBack;

import java.util.List;

public class YoloTypeBack implements OutBack {
    private float out = -1;
    private int id = 0;
    private float distX;
    private float distY;
    private float width;
    private float height;
    private float trust;
    private List<Float> softMax;

    public float getDistX() {
        return distX;
    }

    public float getDistY() {
        return distY;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getTrust() {
        return trust;
    }

    public void setOut(float out) {
        this.out = out;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getOut() {
        return out;
    }

    public List<Float> getSoftMax() {
        return softMax;
    }

    public int getId() {
        return id;
    }

    public void clear() {
        out = -1;
        id = 0;
    }

    @Override
    public void getBack(float out, int id, long eventId) {
        if (out > this.out) {
            this.out = out;
            this.id = id;
        }
    }

    @Override
    public void outBackBox(List<OutBox> myOutBox, long eventId, int deep) {

    }

    @Override
    public void getStudyLog(float e, float out, int nerveId) {

    }

    @Override
    public void getSoftMaxBack(long eventId, List<Float> softMax) {
        this.softMax = softMax;
    }


    @Override
    public void backWord(String word, long eventId) {

    }

    @Override
    public void getBackMatrix(Matrix matrix, int id, long eventId) {
        distX = matrix.getValue(0, 0);
        distY = matrix.getValue(0, 1);
        width = matrix.getValue(0, 2);
        height = matrix.getValue(0, 3);
        trust = matrix.getValue(0, 4);
    }

    @Override
    public void getBackMatrixList(List<Matrix> matrix, long eventId) {

    }

    @Override
    public void getWordVector(int id, float w) {

    }

    @Override
    public void getBackThreeChannelMatrix(ThreeChannelMatrix picture) {

    }
}
