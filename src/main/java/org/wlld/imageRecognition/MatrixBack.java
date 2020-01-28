package org.wlld.imageRecognition;

import org.wlld.MatrixTools.Matrix;
import org.wlld.i.OutBack;

/**
 * @author lidapeng
 * @description
 * @date 11:07 上午 2020/1/28
 */
public class MatrixBack implements OutBack {
    private Matrix matrix;
    private long eventId;

    public Matrix getMatrix() {
        return matrix;
    }

    public long getEventId() {
        return eventId;
    }

    @Override
    public void getBack(double out, int id, long eventId) {

    }

    @Override
    public void getBackMatrix(Matrix matrix, long eventId) {
        this.matrix = matrix;
        this.eventId = eventId;
    }
}
