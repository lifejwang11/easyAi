package org.dromara.easyai.rnnNerveEntity;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.i.OutBack;

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
        super(id, upNub, "SensoryNerve", 0, 0.1f, false,
                null, 0, 0, 0);
    }

    /**
     * @param eventId   唯一的事件id
     * @param parameter 输入点的数据
     * @param isStudy   是否是学习 (学习状态没有输出)
     * @param E         标注
     * @param outBack   回调结果
     * @throws Exception
     */
    public void postMessage(long eventId, float parameter, boolean isStudy, Map<Integer, Float> E
            , OutBack outBack, boolean isEmbedding, Matrix rnnMatrix) throws Exception {//感知神经元输出

        sendMessage(eventId, parameter, isStudy, E, outBack, isEmbedding, rnnMatrix);
    }

    @Override
    public void connect(List<Nerve> nerveList) {//连接第一层隐层神经元
        super.connect(nerveList);
    }
}
