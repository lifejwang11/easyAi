package org.dromara.easyai.naturalLanguage.languageCreator;

import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.i.OutBack;

import java.util.List;


public class CreatorWord implements OutBack {
    private int id;
    private float maxOut = -2;

    public int getId() {
        return id;
    }

    public float getMaxOut() {
        return maxOut;
    }

    public void clear() {
        maxOut = -2;
    }

    @Override
    public void getBack(float out, int id, long eventId) {
        if (out > maxOut) {
            maxOut = out;
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
