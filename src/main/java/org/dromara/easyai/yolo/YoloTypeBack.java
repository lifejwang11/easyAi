package org.dromara.easyai.yolo;

import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.i.OutBack;

import java.util.List;

public class YoloTypeBack implements OutBack {
    private float out = -1;
    private int id = 0;


    public float getOut() {
        return out;
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
    public void getSoftMaxBack(long eventId, List<Float> softMax) {

    }


    @Override
    public void backWord(String word, long eventId) {

    }

    @Override
    public void getBackMatrix(Matrix matrix, int id, long eventId) {

    }

    @Override
    public void getWordVector(int id, float w) {

    }

    @Override
    public void getBackThreeChannelMatrix(ThreeChannelMatrix picture) {

    }
}
