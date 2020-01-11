package org.wlld.nerveEntity;

import org.wlld.MatrixTools.Matrix;

import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * 感知神经元输入层
 * @date 9:29 上午 2019/12/21
 */
public class SensoryNerve extends Nerve {

    public SensoryNerve(int id, int upNub) {
        super(id, upNub, "SensoryNerve", 0, 0.1, false, null, false);
    }

    public void postMessage(long eventId, double parameter, boolean isStudy, Map<Integer, Double> E) throws Exception {//感知神经元输出

        sendMessage(eventId, parameter, isStudy, E);
    }

    public void postMatrixMessage(long enevtId, Matrix parameter, boolean isStudy, double E) throws Exception {
        sendMatrixMessage(enevtId, parameter, isStudy, E);
    }

    @Override
    public void connect(List<Nerve> nerveList) {//连接第一层隐层神经元
        super.connect(nerveList);
    }
}
