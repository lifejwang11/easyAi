package org.wlld.nerveEntity;

import org.wlld.matrixTools.Matrix;
import org.wlld.matrixTools.MatrixOperation;
import org.wlld.i.ActiveFunction;
import org.wlld.i.OutBack;

import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * 隐层神经元
 * &#064;date  9:30 上午 2019/12/21
 */
public class HiddenNerve extends Nerve {
    private final boolean isConvFinish;//卷积最后一层

    public HiddenNerve(int id, int depth, int upNub, int downNub, double studyPoint,
                       boolean init, ActiveFunction activeFunction, boolean isDynamic, int rzType, double lParam
            , int step, int kernLen, int matrixX, int matrixY, boolean isConvFinish) throws Exception {//隐层神经元
        super(id, upNub, "HiddenNerve", downNub, studyPoint,
                init, activeFunction, isDynamic, rzType, lParam, step, kernLen, depth, matrixX, matrixY);
        this.isConvFinish = isConvFinish;
    }

    @Override
    public void input(long eventId, double parameter, boolean isKernelStudy, Map<Integer, Double> E
            , OutBack outBack) throws Exception {//接收上一层的输入
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
    protected void inputMatrixFeature(long eventId, List<Double> parameters, boolean isStudy, Map<Integer, Double> E, OutBack imageBack) throws Exception {
        boolean allReady = insertParameters(eventId, parameters);
        if (allReady) {//参数齐了，开始计算 sigma - threshold
            double sigma = calculation(eventId);
            double out = activeFunction.function(sigma);//激活函数输出数值
            if (isStudy) {
                outNub = out;
            } else {
                destoryParameter(eventId);
            }
            sendMessage(eventId, out, isStudy, E, imageBack);
        }
    }

    @Override
    protected void inputMatrix(long eventId, Matrix matrix, boolean isStudy
            , Map<Integer, Double> E, OutBack outBack) throws Exception {
        Matrix myMatrix = conv(matrix);//处理过的矩阵
        if (isConvFinish) {
            sendMatrixList(eventId, MatrixOperation.matrixToList(myMatrix), isStudy, E, outBack);
        } else {
            sendMatrix(eventId, myMatrix, isStudy, E, outBack);
        }
    }
}
