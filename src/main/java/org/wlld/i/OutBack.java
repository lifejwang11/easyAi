package org.wlld.i;

import org.wlld.MatrixTools.Matrix;

/**
 * @author lidapeng
 * @将神经元的输出回调
 * @date 1:07 下午 2019/12/24
 */
public interface OutBack {
    //输出回调
    void getBack(double out, int id, long eventId);
    void getBackMatrix(Matrix matrix,long eventId);
}
