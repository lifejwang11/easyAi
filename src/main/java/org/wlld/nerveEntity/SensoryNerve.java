package org.wlld.nerveEntity;

import java.util.List;

/**
 * @author lidapeng
 * 感知神经元输入层
 * @date 9:29 上午 2019/12/21
 */
public class SensoryNerve extends Nerve {

    public SensoryNerve(int id, int upNub) {
        super(id, upNub, "SensoryNerve", 0, 0.1, false);
    }

    public void postMessage(long eventId, double parameter, boolean isStudy, double E) throws Exception {//感知神经元输出
        sendMessage(eventId, parameter, isStudy, E);
    }

    @Override
    public void connect(List<Nerve> nerveList) {//连接第一层隐层神经元
        super.connect(nerveList);
    }
}
