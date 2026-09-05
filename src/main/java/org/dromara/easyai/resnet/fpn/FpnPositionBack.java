package org.dromara.easyai.resnet.fpn;

import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.yolo.OutBox;

import java.util.List;

/**
 * @author lidapeng
 * @time 2026/9/5 14:52
 */
public class FpnPositionBack implements OutBack {
    private float distX;
    private float distY;
    private float width;
    private float height;
    private float trust;

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

    @Override
    public void getBack(float out, int id, long eventId) {

    }

    @Override
    public void outBackBox(List<OutBox> myOutBox, long eventId, int deep) {

    }

    @Override
    public void getStudyLog(float e, float out, int nerveId) {

    }

    @Override
    public void getSoftMaxBack(long eventId, List<Float> softMax) {

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
