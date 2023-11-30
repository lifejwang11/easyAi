package org.wlld.rnnJumpNerveEntity;


import org.wlld.MatrixTools.Matrix;
import org.wlld.config.RZ;
import org.wlld.i.OutBack;

import java.util.List;
import java.util.Map;

public class SoftMax extends Nerve {
    private final OutNerve outNerve;
    private final boolean isShowLog;

    public SoftMax(int id, boolean isDynamic, OutNerve outNerve, boolean isShowLog
            , int sensoryNerveNub, int hiddenNerveNub, int outNerveNub, int allDepth) throws Exception {
        super(id, "softMax", 0, false, null, isDynamic
                , RZ.NOT_RZ, 0, 0, 0, sensoryNerveNub, hiddenNerveNub, outNerveNub, allDepth);
        this.outNerve = outNerve;
        this.isShowLog = isShowLog;
    }

    @Override
    protected void input(long eventId, double parameter, boolean isStudy, Map<Integer, Double> E, OutBack outBack, boolean isEmbedding
            , Matrix rnnMatrix, int[] storeys, int index) throws Exception {
        boolean allReady = insertParameter(eventId, parameter);
        if (allReady) {
            double out = softMax(eventId);//输出值
            if (isStudy) {//学习
                outNub = out;
                if (E.containsKey(getId())) {
                    this.E = E.get(getId());
                } else {
                    this.E = 0;
                }
                if (isShowLog) {
                    System.out.println("softMax==" + this.E + ",out==" + out + ",nerveId==" + getId());
                }
                gradient = -outGradient();//当前梯度变化 把梯度返回
                features.remove(eventId); //清空当前上层输入参数参数
                outNerve.getGBySoftMax(gradient, eventId, storeys, index);
            } else {//输出
                destroyParameter(eventId);
                if (outBack != null) {
                    outBack.getBack(out, getId(), eventId);
                } else {
                    throw new Exception("not find outBack");
                }
            }
        }
    }


    private double outGradient() {//生成输出层神经元梯度变化
        double g = outNub;
        if (E == 1) {
            g = g - 1;
        }
        return g;
    }

    private double softMax(long eventId) {//计算当前输出结果
        double sigma = 0;
        List<Double> featuresList = features.get(eventId);
        double self = featuresList.get(getId() - 1);
        double eSelf = Math.exp(self);
        for (double value : featuresList) {
            sigma = Math.exp(value) + sigma;
        }
        return eSelf / sigma;
    }
}
