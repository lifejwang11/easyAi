package org.dromara.easyai.nerveEntity;

import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.i.OutBack;

import java.util.Map;

/**
 * @author lidapeng
 * 输出神经元
 * @date 11:25 上午 2019/12/21
 */
public class OutNerve extends Nerve {
    private final boolean isShowLog;
    private final boolean isSoftMax;

    public OutNerve(int id, int upNub, int downNub, float studyPoint, boolean init,
                    ActiveFunction activeFunction, boolean isDynamic, boolean isShowLog,
                    int rzType, float lParam, boolean isSoftMax, int kernLen, int coreNumber, float gaMa, float gMaxTh, boolean auTo
            , float GRate) throws Exception {
        super(id, upNub, "OutNerve", downNub, studyPoint, init,
                activeFunction, isDynamic, rzType, lParam, kernLen, 0, 0, 0
                , coreNumber, 0, 0, false, null, gaMa, gMaxTh, auTo, GRate);
        this.isShowLog = isShowLog;
        this.isSoftMax = isSoftMax;
    }

    void getGBySoftMax(float g, long eventId) throws Exception {//接收softMax层回传梯度
        gradient = g;
        updatePower(eventId);
    }


    @Override
    public void input(long eventId, float parameter, boolean isStudy, Map<Integer, Float> E
            , OutBack outBack) throws Exception {
        boolean allReady = insertParameter(eventId, parameter);
        if (allReady) {//参数齐了，开始计算 sigma - threshold
            float sigma = calculation(eventId);
            if (isSoftMax) {
                if (!isStudy) {
                    destoryParameter(eventId);
                }
                sendMessage(eventId, sigma, isStudy, E, outBack);
            } else {
                float out = activeFunction.function(sigma);
                if (isStudy) {//输出结果并进行BP调整权重及阈值
                    outNub = out;
                    if (E.containsKey(getId())) {
                        this.E = E.get(getId());
                    } else {
                        this.E = -1;
                    }
                    if (isShowLog) {
                        System.out.println("E==" + this.E + ",out==" + out + ",nerveId==" + getId());
                    }
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
    }

    private float outGradient() {//生成输出层神经元梯度变化
        //上层神经元输入值 * 当前神经元梯度*学习率 =该上层输入的神经元权重变化
        //当前梯度神经元梯度变化 *学习旅 * -1 = 当前神经元阈值变化
        return activeFunction.functionG(outNub) * (E - outNub);
    }
}
