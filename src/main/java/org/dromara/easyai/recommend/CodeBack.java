package org.dromara.easyai.recommend;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.i.OutBack;

import java.util.List;

public class CodeBack implements OutBack {
    private double[] myFeature;

    public double[] getMyFeature() {
        return myFeature;
    }

    public void setMyFeature(double[] myFeature) {
        this.myFeature = myFeature;
    }

    @Override
    public void getBack(double out, int id, long eventId) {
        myFeature[id - 1] = out;
    }

    @Override
    public void getSoftMaxBack(long eventId, List<Double> softMax) {

    }


    @Override
    public void backWord(String word, long eventId) {

    }


    @Override
    public void getBackMatrix(Matrix matrix, int id, long eventId) {

    }

    @Override
    public void getWordVector(int id, double w) {

    }
}
