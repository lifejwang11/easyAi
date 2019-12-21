package org.wlld.nerveEntity;

import java.util.List;

/**
 * @author lidapeng
 * 感知神经元输入层
 * @date 9:29 上午 2019/12/21
 */
public class SensoryNerve extends Nerve {

    public SensoryNerve(int id, int upNub) {
        super(id, upNub);
    }

    @Override
    public void setMessage(double message) {
        super.setMessage(message);
    }

    public boolean postMessage(double message) {//感知神经元输出
        setMessage(message);
        return sendMessage();
    }

    @Override
    public void connect(List<Nerve> nerveList) {//连接第一层隐层神经元
        super.connect(nerveList);
    }
}
