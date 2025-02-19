package org.dromara.easyai.i;


import org.dromara.easyai.matrixTools.Matrix;

import java.util.List;

/**
 * 将神经元的输出回调
 *
 * @author lidapeng
 * &#064;date  1:07 下午 2019/12/24
 */
public interface OutBack {
    /**
     * 回调
     *
     * @param out     输出数值
     * @param id      输出神经元ID
     * @param eventId 事件ID
     */
    void getBack(float out, int id, long eventId);

    /**
     * 多分类回调
     *
     * @param eventId 事件ID
     * @param softMax 概率集合
     */
    void getSoftMaxBack(long eventId, List<Float> softMax);

    /**
     * 回调
     *
     * @param word    输出语句
     * @param eventId 事件ID
     */
    void backWord(String word, long eventId);

    /**
     * 特征矩阵回调
     *
     * @param matrix  输出矩阵
     * @param eventId 事件ID
     * @param id      通道id
     */
    void getBackMatrix(Matrix matrix, int id, long eventId);

    /**
     * 回调词向量
     *
     * @param id 当前词向量id
     */
    void getWordVector(int id, float w);

}
