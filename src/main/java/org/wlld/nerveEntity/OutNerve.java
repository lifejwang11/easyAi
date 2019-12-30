package org.wlld.nerveEntity;

import org.wlld.i.OutBack;
import org.wlld.tools.ArithUtil;

import java.util.Map;

/**
 * @author lidapeng
 * 输出神经元
 * @date 11:25 上午 2019/12/21
 */
public class OutNerve extends Nerve {
    // static final Logger logger = LogManager.getLogger(OutNerve.class);
    private OutBack outBack;
    private long trainNub = 0;//训练次数
    private double allE;//训练累计EK

    public OutNerve(int id, int upNub, int downNub, double studyPoint, boolean init) {
        super(id, upNub, "OutNerve", downNub, studyPoint, init);
    }

    public void setOutBack(OutBack outBack) {
        this.outBack = outBack;
    }

    @Override
    public void input(long eventId, double parameter, boolean isStudy, Map<Integer, Double> E) throws Exception {
        // logger.debug("Nerve:{},eventId:{},parameter:{}--getInput", name, eventId, parameter);
        boolean allReady = insertParameter(eventId, parameter);
        if (allReady) {//参数齐了，开始计算 sigma - threshold
            double sigma = calculation(eventId);
            double out = activeFunction.sigmoid(sigma);
            // logger.debug("myId:{},outPut:{}------END", getId(), out);
            if (isStudy) {//输出结果并进行BP调整权重及阈值
                trainNub++;//训练次数增加
                outNub = out;
                this.E = E.get(getId());
                gradient = outGradient();//当前梯度变化
                //调整权重 修改阈值 并进行反向传播
                updatePower(eventId);
            } else {//获取最后输出
                //System.out.println("当前阈值" + threshold);
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
        //ArithUtil.sub(E, outNub) 求这个的累计平均值
        allE = ArithUtil.add(Math.abs(ArithUtil.sub(E, outNub)), allE);
        double avg = ArithUtil.div(allE, trainNub);
        return ArithUtil.mul(activeFunction.sigmoidG(outNub), avg);
    }
}
