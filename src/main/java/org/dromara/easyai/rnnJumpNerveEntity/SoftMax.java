package org.dromara.easyai.rnnJumpNerveEntity;


import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.config.RZ;
import org.dromara.easyai.i.OutBack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SoftMax extends Nerve {
    private final List<OutNerve> outNerves;
    private final boolean isShowLog;
    private NerveCenter nerveCenter;//该输出层对应的神经中枢

    public SoftMax(boolean isDynamic, List<OutNerve> outNerves, boolean isShowLog
            , int sensoryNerveNub, int hiddenNerveNub, int outNerveNub, int allDepth) throws Exception {
        super(0, "softMax", 0, false, null, isDynamic
                , RZ.NOT_RZ, 0, 0, 0, sensoryNerveNub, hiddenNerveNub, outNerveNub, allDepth
                , false, 0);
        this.outNerves = outNerves;
        this.isShowLog = isShowLog;
    }

    public void setNerveCenter(NerveCenter nerveCenter) {
        this.nerveCenter = nerveCenter;
    }

    @Override
    protected void sendAppointSoftMax(long eventId, float parameter, Matrix featureMatrix, OutBack outBack, String myWord) throws Exception {
        boolean allReady = insertParameter(eventId, parameter);
        if (allReady) {
            Mes mes = softMax(eventId, false);//输出值
            destroyParameter(eventId);
            nerveCenter.backType(eventId, mes.poi, mes.typeID, featureMatrix, outBack, myWord);
        }
    }

    @Override
    protected void input(long eventId, float parameter, boolean isStudy, Map<Integer, Float> E, OutBack outBack
            , Matrix rnnMatrix, int[] storeys, int index, int questionLength) throws Exception {
        boolean allReady = insertParameter(eventId, parameter);
        if (allReady) {
            Mes mes = softMax(eventId, isStudy);//输出值
            int key = 0;
            if (isStudy) {//学习
                for (Map.Entry<Integer, Float> entry : E.entrySet()) {
                    if (entry.getValue() > 0.9) {
                        key = entry.getKey();
                        break;
                    }
                }
                if (isShowLog) {
                    System.out.println("softMax==" + key + ",out==" + mes.poi + ",nerveId==" + mes.typeID);
                }
                List<Float> errors = error(mes, key);
                features.remove(eventId); //清空当前上层输入参数参数
                int size = outNerves.size();
                for (int i = 0; i < size; i++) {
                    outNerves.get(i).getGBySoftMax(errors.get(i), eventId, storeys, index);
                }
            } else {//输出
                destroyParameter(eventId);
                if (outBack != null) {
                    outBack.getBack(mes.poi, mes.typeID, eventId);
                    outBack.getSoftMaxBack(eventId,mes.softMax);
                } else {
                    throw new Exception("not find outBack");
                }
            }
        }
    }

    private List<Float> error(Mes mes, int key) {
        int t = key - 1;
        List<Float> softMax = mes.softMax;
        List<Float> error = new ArrayList<>();
        for (int i = 0; i < softMax.size(); i++) {
            float self = softMax.get(i);
            float myError;
            if (i != t) {
                myError = -self;
            } else {
                myError = 1 - self;
            }
            error.add(myError);
        }
        return error;
    }

    private Mes softMax(long eventId, boolean isStudy) {//计算当前输出结果
        float sigma = 0;
        int id = 0;
        float poi = 0;
        Mes mes = new Mes();
        List<Float> featuresList = features.get(eventId);
        for (float value : featuresList) {
            sigma = (float)Math.exp(value) + sigma;
        }
        List<Float> softMax = new ArrayList<>();
        for (int i = 0; i < featuresList.size(); i++) {
            float eSelf = (float)Math.exp(featuresList.get(i));
            float value = eSelf / sigma;
            softMax.add(value);
            if (value > poi) {
                poi = value;
                id = i + 1;
            }
        }
        mes.softMax = softMax;
        mes.typeID = id;
        mes.poi = poi;
        return mes;
    }

    static class Mes {
        int typeID;
        float poi;
        List<Float> softMax;
    }
}
