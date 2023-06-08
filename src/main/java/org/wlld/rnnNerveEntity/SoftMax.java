package org.wlld.rnnNerveEntity;

import org.wlld.MatrixTools.Matrix;
import org.wlld.config.RZ;
import org.wlld.i.OutBack;

import java.util.List;
import java.util.Map;

public class SoftMax extends Nerve {
    private OutNerve outNerve;
    private boolean isShowLog;

    public SoftMax(int id, int upNub, boolean isDynamic, OutNerve outNerve, boolean isShowLog) throws Exception {
        super(id, upNub, "softMax", 0, 0, false, null, isDynamic
                , RZ.NOT_RZ, 0, 0, 0, 0);
        this.outNerve = outNerve;
        this.isShowLog = isShowLog;
    }

    @Override
    protected void input(long eventId, double parameter, boolean isStudy, Map<Integer, Double> E, OutBack outBack, boolean isEmbedding
            , Matrix rnnMatrix) throws Exception {
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
                outNerve.getGBySoftMax(gradient, eventId, getId());
            } else {//输出
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
        double g = outNub;
        if (E == 1) {
            //g = ArithUtil.sub(g, 1);
            g = g - 1;
        }
        return g;
    }

    private double softMax(long eventId) {//计算当前输出结果
        double sigma = 0;
        List<Double> featuresList = features.get(eventId);
        double self = featuresList.get(getId() - 1);
        double eSelf = Math.exp(self);
        for (int i = 0; i < featuresList.size(); i++) {
            double value = featuresList.get(i);
            // sigma = ArithUtil.add(Math.exp(value), sigma);
            sigma = Math.exp(value) + sigma;
        }
        return eSelf / sigma;//ArithUtil.div(eSelf, sigma);
    }
}
