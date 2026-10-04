package org.dromara.easyai.td;

import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.yolo.OutBox;

import java.util.List;

/**
 * @author lidapeng
 * @time 2026/10/3 09:11
 */
public class TDBack implements OutBack {
    private float value;

    public float getValue() {
        return value;
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
        value = matrix.getValue(0, 0);
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
