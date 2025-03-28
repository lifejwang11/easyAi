package org.dromara.easyai.transFormer.nerve;


import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.transFormer.LineBlock;

import java.util.List;

/**
 * @author lidapeng
 * 隐层神经元
 * &#064;date  9:30 上午 2019/12/21
 */
public class HiddenNerve extends Nerve {

    public HiddenNerve(int id, int depth, float studyPoint, ActiveFunction activeFunction, int sensoryNerveNub,
                       int outNerveNub, LineBlock lineBlock, int regularModel, float regular
            , int coreNumber) throws Exception {//隐层神经元
        super(id, "HiddenNerve", studyPoint, activeFunction, sensoryNerveNub, 0,
                outNerveNub, lineBlock, regularModel, regular, coreNumber);
        this.depth = depth;
    }

    public void receiveErrorMatrix(Matrix g, long eventId, Matrix allError) throws Exception {//
        updatePower(eventId, g, allError);
    }

    public void receive(Matrix feature, long eventId, boolean isStudy, OutBack outBack,
                        List<Integer> E, Matrix encoderFeature, boolean outAllPro) throws Exception {//接收上一个残差层传过来得参数
        Matrix out = opMatrix(feature, isStudy);
        sendMessage(eventId, out, isStudy, feature, outBack, E, encoderFeature, outAllPro);
    }

    @Override
    protected void input(long eventId, Matrix parameter, boolean isStudy, Matrix allFeature, OutBack outBack,
                         List<Integer> E, Matrix encoderFeature, boolean outAllPro) throws Exception {//第二层收到参数
        boolean allReady = insertMatrixParameter(eventId, parameter);
        if (allReady) {//参数齐了，开始计算
            Matrix out = opMatrix(reMatrixFeatures.get(eventId).getMatrix(), isStudy);
            reMatrixFeatures.remove(eventId);
            beforeLayNorm.addNormFromNerve(eventId, isStudy, out, allFeature, outBack, E, encoderFeature, outAllPro);
        }
    }

    public void postMessage(long eventId, Matrix feature, boolean isStudy, OutBack outBack, List<Integer> E, boolean outAllPro) throws Exception {
        Matrix out = opMatrix(feature, isStudy);
        sendOutMessage(eventId, out, isStudy, outBack, E, outAllPro);
    }
}
