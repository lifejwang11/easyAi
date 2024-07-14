package org.wlld.rnnJumpNerveEntity;


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

    public SensoryNerve(int id, int allDepth) throws Exception {
        super(id, "SensoryNerve", 0, false, null, false, 0, 0,
                0, 0, 0, 0, 0, allDepth, false, 0);
        depth = 0;
    }

    /**
     * @param eventId   唯一的事件id
     * @param parameter 输入点的数据
     * @param isStudy   是否是学习 (学习状态没有输出)
     * @param E         标注
     * @param outBack   回调结果
     */
    public void postMessage(long eventId, double parameter, boolean isStudy, Map<Integer, Double> E
            , OutBack outBack, Matrix rnnMatrix, int[] storeys, int questionLength) throws Exception {//感知神经元输出
        sendMessage(eventId, parameter, isStudy, E, outBack, rnnMatrix, storeys, 0, questionLength);
    }

//    public void postPowerMessage(long eventId, double parameter, Matrix featureMatrix, OutBack outBack) throws Exception {
//        sendTestMessage(eventId, parameter, featureMatrix, outBack, null);
//    }

    public void postMatrixMessage(long eventId, Matrix parameter, boolean isKernelStudy
            , int E, OutBack outBack) throws Exception {
        sendMatrix(eventId, parameter, isKernelStudy, E, outBack);
    }

    @Override
    public void connect(int depth, List<Nerve> nerveList) {//连接第一层隐层神经元
        super.connect(depth, nerveList);
    }
}
