package org.wlld.nerveEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author lidapeng
 * 输出神经元
 * @date 11:25 上午 2019/12/21
 */
public class OutNerve extends Nerve {
    static final Logger logger = LogManager.getLogger(OutNerve.class);

    public OutNerve(int id, int upNub) {
        super(id, upNub, "OutNerve");
    }

    @Override
    public void input(long eventId, double parameter, boolean isStudy) {
        logger.debug("Nerve:{},eventId:{},parameter:{}--getInput", name, eventId, parameter);
        boolean allReady = insertParameter(eventId, parameter);
        if (allReady) {//参数齐了，开始计算 sigma - threshold
            double sigma = calculation(eventId);
            double out = activeFunction.sigmoid(sigma);
            if (isStudy) {
                outNub = out;
            }
            logger.debug("myId:{},outPut:{}------END", getId(), out);
        }
    }
}
