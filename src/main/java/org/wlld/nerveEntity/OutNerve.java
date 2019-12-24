package org.wlld.nerveEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wlld.i.OutBack;
import org.wlld.tools.ArithUtil;

/**
 * @author lidapeng
 * 输出神经元
 * @date 11:25 上午 2019/12/21
 */
public class OutNerve extends Nerve {
    static final Logger logger = LogManager.getLogger(OutNerve.class);
    private OutBack outBack;

    public OutNerve(int id, int upNub, int downNub, double studyPoint, boolean init) {
        super(id, upNub, "OutNerve", downNub, studyPoint, init);
    }

    public void setOutBack(OutBack outBack) {
        this.outBack = outBack;
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
            } else {//获取最后输出
                destoryParameter(eventId);
                if (outBack != null) {
                    outBack.getBack(out, getId(), eventId);
                } else {
                    throw new Exception("not find outBack");
                }
            }
        }
    }

    private double outGradient() {//生成输出层神经元梯度变化
        //上层神经元输入值 * 当前神经元梯度*学习率 =该上层输入的神经元权重变化
        //当前梯度神经元梯度变化 *学习旅 * -1 = 当前神经元阈值变化
        return ArithUtil.mul(activeFunction.sigmoidG(outNub), ArithUtil.sub(E, outNub));
    }
}
