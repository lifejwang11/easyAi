package org.wlld.i;

import org.wlld.MatrixTools.Matrix;

/**
 * 将神经元的输出回调
 * @author lidapeng
 * @date 1:07 下午 2019/12/24
 */
public interface OutBack {
    /**
     * 回调
     * @param out 输出数值
     * @param id 输出神经元ID
     * @param eventId 事件ID
     */
    void getBack(double out, int id, long eventId);
    void getBackMatrix(Matrix matrix, long eventId);
}
