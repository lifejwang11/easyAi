package org.dromara.easyai.entity;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.i.OutBack;

import java.util.ArrayList;
import java.util.List;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class WordMatrix implements OutBack {
    private final float[] vector;

    public WordMatrix(int size) {
        vector = new float[size];
    }

    public Matrix getVector() throws Exception {
        Matrix matrix = new Matrix(1, vector.length);
        for (int i = 0; i < vector.length; i++) {
            matrix.setNub(0, i, vector[i]);
        }
        return matrix;
    }

    public List<Float> getList() {
        List<Float> list = new ArrayList<>();
        for (float v : vector) {
            list.add(v);
        }
        return list;
    }

    @Override
    public void getBack(float out, int id, long eventId) {

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

    }

    @Override
    public void getWordVector(int id, float w) {
        vector[id - 1] = w;
    }

    @Override
    public void getBackThreeChannelMatrix(ThreeChannelMatrix picture) {

    }
}
