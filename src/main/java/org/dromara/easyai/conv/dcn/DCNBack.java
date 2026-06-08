package org.dromara.easyai.conv.dcn;

import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;

import java.util.List;

/**
 * @author lidapeng
 * @time 2026/6/3 11:44
 */
public class DCNBack implements OutBack {
    private List<Matrix> matrixList;

    public List<Matrix> getMatrixList() {
        return matrixList;
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
    public void getBackMatrixList(List<Matrix> matrix, long eventId) {
        matrixList = matrix;
    }

    @Override
    public void getWordVector(int id, float w) {

    }

    @Override
    public void getBackThreeChannelMatrix(ThreeChannelMatrix picture) {

    }
}
