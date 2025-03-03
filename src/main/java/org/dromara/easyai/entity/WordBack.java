package org.dromara.easyai.entity;


import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.i.OutBack;

import java.util.List;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class WordBack implements OutBack {
    private int id;
    private float out = -2;

    public void clear() {
        out = -2;
        id = 0;
    }

    public float getOut() {
        return out;
    }

    public int getId() {
        return id;
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
