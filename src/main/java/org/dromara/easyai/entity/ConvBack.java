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
public class ConvBack implements OutBack {
    private Matrix matrix;

    public Matrix getMatrix() {
        return matrix;
    }

    @Override
    public void getBack(float out, int id, long eventId) {
    }

    @Override
    public void getSoftMaxBack(long eventId, List<Float> softMax) {

    }


    @Override
    public void backWord(String word, long eventId) {

    }


    @Override
    public void getBackMatrix(Matrix matrix, int id, long eventId) {
        this.matrix = matrix;
    }

    @Override
    public void getWordVector(int id, float w) {

    }
}
