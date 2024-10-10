package org.wlld.nerveEntity;

import org.wlld.matrixTools.Matrix;
import org.wlld.i.OutBack;

import java.util.List;
import java.util.Map;

/**
 * 感知神经元输入层
 *
 * @author lidapeng
 * @date 9:29 上午 2019/12/21
 */
public class SensoryNerve extends Nerve {

    public SensoryNerve(int id, int upNub) throws Exception {
        super(id, upNub, "SensoryNerve", 0, 0.1, false,
                null, false, 0, 0, 0, 0, 0, 0, 0
                , 1);
    }

    /**
     * @param eventId   唯一的事件id
     * @param parameter 输入点的数据
     * @param isStudy   是否是学习 (学习状态没有输出)
     * @param E         标注
     * @param outBack   回调结果
     * @throws Exception
     */
    public void postMessage(long eventId, double parameter, boolean isStudy, Map<Integer, Double> E
            , OutBack outBack) throws Exception {//感知神经元输出

        sendMessage(eventId, parameter, isStudy, E, outBack);
    }

    /**
     * @param eventId       唯一的事件id
     * @param parameter     特征矩阵
     * @param isKernelStudy 是否是学习 (学习状态没有输出)
     * @param E             标注
     * @param outBack       回调结果
     * @param needMatrix    需要矩阵输出
     * @throws Exception
     */
    public void postMatrixMessage(long eventId, Matrix parameter, boolean isKernelStudy
            , Map<Integer, Double> E, OutBack outBack, boolean needMatrix) throws Exception {
        sendMatrix(eventId, parameter, isKernelStudy, E, outBack, needMatrix);
    }

    @Override
    public void connect(List<Nerve> nerveList) {//连接第一层隐层神经元
        super.connect(nerveList);
    }
}
