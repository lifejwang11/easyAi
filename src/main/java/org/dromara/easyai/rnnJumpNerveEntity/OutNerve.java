package org.dromara.easyai.rnnJumpNerveEntity;


import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.i.OutBack;

import java.util.Map;

/**
 * @author lidapeng
 * 输出神经元
 * &#064;date  11:25 上午 2019/12/21
 */
public class OutNerve extends Nerve {
    private final boolean isShowLog;
    private final boolean isSoftMax;

    public OutNerve(int id, float studyPoint, boolean init,
                    ActiveFunction activeFunction, boolean isShowLog,
                    int rzType, float lParam, boolean isSoftMax, int sensoryNerveNub, int hiddenNerveNub,
                    int outNerveNub, int allDepth) throws Exception {
        super(id, "OutNerve", studyPoint, init, activeFunction, rzType, lParam,
                sensoryNerveNub, hiddenNerveNub, outNerveNub, allDepth, false, 0);
        this.isShowLog = isShowLog;
        this.isSoftMax = isSoftMax;
    }


    void getGBySoftMax(float g, long eventId, int[] storeys, int index) throws Exception {//接收softMax层回传梯度
        gradient = g;
        updatePower(eventId, storeys, index);
    }

    @Override
    protected void sendAppointTestMessage(long eventId, float parameter, Matrix featureMatrix, OutBack outBack, String myWord) throws Exception {
        //计算出结果返回给对应的层的神经中枢
        boolean allReady = insertParameter(eventId, parameter);
        if (allReady) {//所有参数集齐
            float sigma = calculation(eventId);
            destroyParameter(eventId);
            sendSoftMaxBack(eventId, sigma, featureMatrix, outBack, myWord);
        }
    }

    @Override
    public void input(long eventId, float parameter, boolean isStudy, Map<Integer, Float> E
            , OutBack outBack, Matrix rnnMatrix, int[] storeys, int index, int questionLength) throws Exception {
        boolean allReady = insertParameter(eventId, parameter);
        if (allReady) {//参数齐了，开始计算 sigma - threshold
            float sigma = calculation(eventId);
            if (isSoftMax) {
                if (!isStudy) {
                    destroyParameter(eventId);
                }
                sendSoftMax(eventId, sigma, isStudy, E, outBack, rnnMatrix, storeys, index);
            } else {
                float out = activeFunction.function(sigma);
                if (isStudy) {//输出结果并进行BP调整权重及阈值
                    outNub = out;
                    if (E.containsKey(getId())) {
                        this.E = E.get(getId());
                    } else {
                        this.E = 0;
                    }
                    if (isShowLog) {
                        System.out.println("E==" + this.E + ",out==" + out + ",nerveId==" + getId());
                    }
                    gradient = outGradient();//当前梯度变化
                    //调整权重 修改阈值 并进行反向传播
                    updatePower(eventId, storeys, index);
                } else {//获取最后输出
                    destroyParameter(eventId);
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
