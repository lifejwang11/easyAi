package org.wlld.nerveEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author lidapeng
 * 隐层神经元
 * @date 9:30 上午 2019/12/21
 */
public class HiddenNerve extends Nerve {
    private int depth;//所处深度
    static final Logger logger = LogManager.getLogger(HiddenNerve.class);

    public HiddenNerve(int id, int depth, int upNub) {//隐层神经元
        super(id, upNub);
        this.depth = depth;
    }

    @Override
    public void input(Nerve nerve) {//接收上一层的输入
        int id = nerve.getId();
        double w = dendrites.get(nerve.getId());//当前神经元权重
        double value = nerve.getMessage();
        logger.debug("depth:{},id:{},w:{},value:{}", depth, id, w, value);
        // setMessage(nerve.getMessage());
        // sendMessage();
    }
}
