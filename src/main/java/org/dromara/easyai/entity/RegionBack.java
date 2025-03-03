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
public class RegionBack implements OutBack {
    private int id;
    private float point = -2;

    public void setId(int id) {
        this.id = id;
    }

    public void clear() {
        id = 0;
        point = -2;
    }

    public int getId() {
        return id;
    }

    public float getPoint() {
        return point;
    }

    @Override
    public void getBack(float out, int id, long eventId) {
        if (out > point) {
            point = out;
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
