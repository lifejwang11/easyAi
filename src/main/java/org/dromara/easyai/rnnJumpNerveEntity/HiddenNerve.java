package org.dromara.easyai.rnnJumpNerveEntity;


import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.i.OutBack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lidapeng
 * 隐层神经元
 * &#064;date  9:30 上午 2019/12/21
 */
public class HiddenNerve extends Nerve {
    private final Map<Long, Float> outMap = new HashMap<>();

    public HiddenNerve(int id, int depth, float studyPoint, boolean init, ActiveFunction activeFunction,
                       int rzType, float lParam, int sensoryNerveNub,
                       int hiddenNerveNub, int outNerveNub, int allDepth, boolean creator, int startDepth) throws Exception {//隐层神经元
        super(id, "HiddenNerve", studyPoint,
                init, activeFunction, rzType, lParam, sensoryNerveNub, hiddenNerveNub,
                outNerveNub, allDepth, creator, startDepth);
        this.depth = depth;
    }

    @Override
    public void input(long eventId, float parameter, boolean isKernelStudy, Map<Integer, Float> E
            , OutBack outBack, Matrix rnnMatrix, int[] storeys, int index, int questionLength) throws Exception {//接收上一层的输入
        boolean allReady = insertParameter(eventId, parameter);
        if (allReady) {//参数齐了，开始计算 sigma - threshold
            float sigma = calculation(eventId);
            float out = activeFunction.function(sigma);//激活函数输出数值
            if (isKernelStudy) {
                outNub = out;
            }
            if (rnnMatrix != null) {//rnn 1改输出值，2查看是否需要转向
                if (!creator || depth < startDepth) {
                    out = out + rnnMatrix.getNumber(depth, getId() - 1);
                } else {
                    out = out + rnnMatrix.getNumber(depth - startDepth + questionLength, getId() - 1);
                }
            }
            if (!isKernelStudy) {
                destroyParameter(eventId);
            }
            sendMessage(eventId, out, isKernelStudy, E, outBack, rnnMatrix, storeys, index, questionLength);
        }
    }

    @Override
    protected void sendAppointTestMessage(long eventId, float parameter, Matrix featureMatrix, OutBack outBack, String myWord) throws Exception {
        boolean allReady = insertParameter(eventId, parameter);//接收测试参数
        if (allReady) {//凑齐参数 发送给输出层
            float sigma = calculation(eventId);
            float out = activeFunction.function(sigma);//激活函数输出数值
            out = out + featureMatrix.getNumber(featureMatrix.getX() - 1, getId() - 1);
            destroyParameter(eventId);
            outMap.put(eventId, out);
            sendRnnTestMessage(eventId, out, featureMatrix, outBack, myWord);
        }
    }

    @Override
    protected void sendMyTestMessage(long eventId, Matrix featureMatrix, OutBack outBack, String word) throws Exception {
        //继续发送
        float out = outMap.get(eventId);
        outMap.remove(eventId);
        sendTestMessage(eventId, out, featureMatrix, outBack, word);
    }

    @Override
    protected void clearData(long eventId) {
        outMap.remove(eventId);
    }

}
