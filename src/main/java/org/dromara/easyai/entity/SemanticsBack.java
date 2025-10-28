package org.dromara.easyai.entity;


import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.i.OutBack;

import java.util.List;

public class SemanticsBack implements OutBack {
    private Matrix matrix;
    private String word;

    public String getWord() {
        return word;
    }

    public Matrix getMatrix() {
        return matrix;
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
        this.word = word;
    }

    @Override
    public void getBackMatrix(Matrix matrix, int id, long eventId) {
        this.matrix = matrix;
    }

    @Override
    public void getWordVector(int id, float w) {

    }

    @Override
    public void getBackThreeChannelMatrix(ThreeChannelMatrix picture) {

    }
}
