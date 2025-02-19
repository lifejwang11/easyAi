package org.dromara.easyai.transFormer.nerve;


import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.i.OutBack;

import java.util.List;

/**
 * @author lidapeng
 * 输出神经元
 * &#064;date  11:25 上午 2019/12/21
 */
public class OutNerve extends Nerve {
    private final SoftMax softMax;

    public OutNerve(int id, float studyPoint, int sensoryNerveNub, int hiddenNerveNub, int outNerveNub,
                    SoftMax softMax, int regularModel, float regular, int coreNumber) throws Exception {
        super(id, "OutNerve", studyPoint, null, sensoryNerveNub,
                hiddenNerveNub, outNerveNub, null, regularModel, regular, coreNumber);
        this.softMax = softMax;
    }

    void getGBySoftMax(Matrix g, long eventId) throws Exception {//接收softMax层回传梯度
        updatePower(eventId, g, null);
    }


    @Override
    protected void toOut(long eventId, Matrix parameter, boolean isStudy, OutBack outBack, List<Integer> E, boolean outAllPro) throws Exception {
        boolean allReady = insertMatrixParameter(eventId, parameter);
        if (allReady) {
            Matrix out = opMatrix(reMatrixFeatures.get(eventId), isStudy);
            reMatrixFeatures.remove(eventId);
            softMax.toOut(eventId, out, isStudy, outBack, E, outAllPro);
        }
    }
}
