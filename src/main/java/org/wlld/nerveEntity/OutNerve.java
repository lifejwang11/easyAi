package org.wlld.nerveEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wlld.tools.ArithUtil;

import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * 输出神经元
 * @date 11:25 上午 2019/12/21
 */
public class OutNerve extends Nerve {
    static final Logger logger = LogManager.getLogger(OutNerve.class);

    public OutNerve(int id, int upNub, int downNub, boolean init) {
        super(id, upNub, "OutNerve", downNub, init);
    }

    @Override
    public void input(long eventId, double parameter, boolean isStudy, double E) throws Exception {
        logger.debug("Nerve:{},eventId:{},parameter:{}--getInput", name, eventId, parameter);
        boolean allReady = insertParameter(eventId, parameter);
        if (allReady) {//参数齐了，开始计算 sigma - threshold
            double sigma = calculation(eventId);
            double out = activeFunction.sigmoid(sigma);
            logger.debug("myId:{},outPut:{}------END", getId(), out);
            if (isStudy) {//输出结果并进行BP调整权重及阈值
                outNub = out;
                this.E = E;
                gradient = outGradient();//当前梯度变化
                //调整权重 修改阈值 并进行反向传播
                updatePower(eventId);
            } else {
                destoryParameter(eventId);
            }
        }
    }

    private double outGradient() {//生成输出层神经元梯度变化
        //上层神经元输入值 * 当前神经元梯度*学习率 =该上层输入的神经元权重变化
        //当前梯度神经元梯度变化 *学习旅 * -1 = 当前神经元阈值变化
        return ArithUtil.mul(activeFunction.sigmoidG(outNub), ArithUtil.sub(E, outNub));
    }
}
