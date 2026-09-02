package org.dromara.easyai.yolo;

import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lidapeng
 * @time 2026/9/2 16:52
 */
public class FpnOut implements OutBack {
    private final Map<Integer, List<OutBox>> outMap = new ConcurrentHashMap<>();

    public void clear() {
        outMap.clear();
    }

    public Map<Integer, List<OutBox>> getOutMap() {
        return outMap;
    }

    @Override
    public void getBack(float out, int id, long eventId) {

    }

    @Override
    public void outBackBox(List<OutBox> myOutBox, long eventId, int deep) {
        outMap.put(deep, myOutBox);
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

    }

    @Override
    public void getWordVector(int id, float w) {

    }

    @Override
    public void getBackThreeChannelMatrix(ThreeChannelMatrix picture) {

    }
}
