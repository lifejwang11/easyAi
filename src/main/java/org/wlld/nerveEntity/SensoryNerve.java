package org.wlld.nerveEntity;

import org.wlld.MatrixTools.Matrix;
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
                null, false, false, 0, 0);
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

    public void postMatrixMessage(long eventId, Matrix parameter, boolean isKernelStudy
            , int E, OutBack outBack) throws Exception {
        sendMatrix(eventId, parameter, isKernelStudy, E, outBack);
    }

    @Override
    public void connect(List<Nerve> nerveList) {//连接第一层隐层神经元
        super.connect(nerveList);
    }
}
