package org.wlld.transFormer.nerve;


import org.wlld.matrixTools.Matrix;
import org.wlld.i.ActiveFunction;
import org.wlld.i.OutBack;

import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * 输出神经元
 * &#064;date  11:25 上午 2019/12/21
 */
public class OutNerve extends Nerve {
    private final SoftMax softMax;

    public OutNerve(int id, double studyPoint, int sensoryNerveNub, int hiddenNerveNub, int outNerveNub,
                    SoftMax softMax, int regularModel, double regular, int coreNumber) throws Exception {
        super(id, "OutNerve", studyPoint, null, sensoryNerveNub,
                hiddenNerveNub, outNerveNub, null, regularModel, regular, coreNumber);
        this.softMax = softMax;
    }

    void getGBySoftMax(Matrix g, long eventId) throws Exception {//接收softMax层回传梯度
        updatePower(eventId, g, null);
    }


    @Override
    protected void toOut(long eventId, Matrix parameter, boolean isStudy, OutBack outBack, List<Integer> E) throws Exception {
        boolean allReady = insertMatrixParameter(eventId, parameter);
        if (allReady) {
            Matrix out = opMatrix(reMatrixFeatures.get(eventId), isStudy);
            reMatrixFeatures.remove(eventId);
            softMax.toOut(eventId, out, isStudy, outBack, E);
        }
    }
}
