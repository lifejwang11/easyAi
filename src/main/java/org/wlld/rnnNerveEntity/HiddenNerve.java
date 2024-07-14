package org.wlld.rnnNerveEntity;

import org.wlld.matrixTools.Matrix;
import org.wlld.i.ActiveFunction;
import org.wlld.i.OutBack;

import java.util.Map;

/**
 * @author lidapeng
 * 隐层神经元
 * @date 9:30 上午 2019/12/21
 */
public class HiddenNerve extends Nerve {
    private int depth;//所处深度

    public HiddenNerve(int id, int depth, int upNub, int downNub, double studyPoint,
                       boolean init, ActiveFunction activeFunction, boolean isDynamic, int rzType, double lParam
            , int step, int kernLen, int rnnOutNumber) throws Exception {//隐层神经元
        super(id, upNub, "HiddenNerve", downNub, studyPoint,
                init, activeFunction, isDynamic, rzType, lParam, step, kernLen, rnnOutNumber);
        this.depth = depth;
    }

    @Override
    public void input(long eventId, double parameter, boolean isKernelStudy, Map<Integer, Double> E
            , OutBack outBack, boolean isEmbedding, Matrix rnnMatrix) throws Exception {//接收上一层的输入
        boolean allReady = insertParameter(eventId, parameter);
        if (allReady) {//参数齐了，开始计算 sigma - threshold
            if (isEmbedding) {
                outBack.getWordVector(getId(), getWOne(eventId));
                destoryParameter(eventId);
            } else {
                double sigma = calculation(eventId);
                double out = activeFunction.function(sigma);//激活函数输出数值
                if (rnnMatrix != null) {//rnn 1改输出值，2查看是否需要转向
                    out = out + rnnMatrix.getNumber(depth, getId() - 1);
                }
                if (isKernelStudy) {
                    outNub = out;
                } else {
                    destoryParameter(eventId);
                }
                if (rnnMatrix != null && rnnMatrix.getX() == depth + 1) {//转向输出
                    sendRnnMessage(eventId, out, isKernelStudy, E, outBack, false, rnnMatrix);
                } else {
                    sendMessage(eventId, out, isKernelStudy, E, outBack, false, rnnMatrix);
                }
            }
        }
    }

    @Override
    protected void inputMatrix(long eventId, Matrix matrix, boolean isStudy
            , int E, OutBack outBack) throws Exception {
        Matrix myMatrix = conv(matrix);//处理过的矩阵
        sendMatrix(eventId, myMatrix, isStudy, E, outBack);
    }
}
