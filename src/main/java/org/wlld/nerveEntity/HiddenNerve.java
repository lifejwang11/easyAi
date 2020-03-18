package org.wlld.nerveEntity;

import org.wlld.MatrixTools.Matrix;
import org.wlld.i.ActiveFunction;
import org.wlld.i.OutBack;
import org.wlld.imageRecognition.border.Border;

import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * 隐层神经元
 * @date 9:30 上午 2019/12/21
 */
public class HiddenNerve extends Nerve {
    private int depth;//所处深度

    public HiddenNerve(int id, int depth, int upNub, int downNub, double studyPoint,
                       boolean init, ActiveFunction activeFunction, boolean isDynamic, boolean isAccurate) throws Exception {//隐层神经元
        super(id, upNub, "HiddenNerve", downNub, studyPoint, init, activeFunction, isDynamic, isAccurate);
        this.depth = depth;
    }

    @Override
    public void input(long eventId, double parameter, boolean isKernelStudy, Map<Integer, Double> E
            , OutBack outBack) throws Exception {//接收上一层的输入
        // logger.debug("name:{},myId:{},depth:{},eventId:{},parameter:{}--getInput", name, getId(), depth, eventId, parameter);
        boolean allReady = insertParameter(eventId, parameter);
        if (allReady) {//参数齐了，开始计算 sigma - threshold
            double sigma = calculation(eventId);
            double out = activeFunction.function(sigma);//激活函数输出数值
            if (isKernelStudy) {
                outNub = out;
            } else {
                destoryParameter(eventId);
            }
            sendMessage(eventId, out, isKernelStudy, E, outBack);
        }
    }

    @Override
    protected void inputMatrix(long eventId, Matrix matrix, boolean isStudy
            , int E, OutBack outBack) throws Exception {
        Matrix myMatrix = dynamicNerve(matrix, eventId, isStudy);//处理过的矩阵
        sendMatrix(eventId, myMatrix, isStudy, E, outBack);
    }
}
