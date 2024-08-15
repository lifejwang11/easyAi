package org.wlld.rnnNerveEntity;

import org.wlld.matrixTools.Matrix;
import org.wlld.i.ActiveFunction;
import org.wlld.i.OutBack;

import java.util.Map;

/**
 * @author lidapeng
 * 输出神经元
 * @date 11:25 上午 2019/12/21
 */
public class OutNerve extends Nerve {
    private Map<Integer, Matrix> matrixMapE;//主键与期望矩阵的映射
    private boolean isShowLog;
    private boolean isSoftMax;

    public OutNerve(int id, int upNub, int downNub, double studyPoint, boolean init,
                    ActiveFunction activeFunction, boolean isDynamic, boolean isShowLog,
                    int rzType, double lParam, boolean isSoftMax, int step, int kernLen) throws Exception {
        super(id, upNub, "OutNerve", downNub, studyPoint, init,
                activeFunction, isDynamic, rzType, lParam, step, kernLen, 0);
        this.isShowLog = isShowLog;
        this.isSoftMax = isSoftMax;
    }

    void getGBySoftMax(double g, long eventId) throws Exception {//接收softMax层回传梯度
        gradient = g;
        updatePower(eventId);
    }

    public void setMatrixMap(Map<Integer, Matrix> matrixMap) {
        matrixMapE = matrixMap;
    }

    @Override
    public void input(long eventId, double parameter, boolean isStudy, Map<Integer, Double> E
            , OutBack outBack, boolean isEmbedding, Matrix rnnMatrix) throws Exception {
        boolean allReady = insertParameter(eventId, parameter);
        if (allReady) {//参数齐了，开始计算 sigma - threshold
            double sigma = calculation(eventId);
            if (isSoftMax) {
                if (!isStudy) {
                    destoryParameter(eventId);
                }
                sendMessage(eventId, sigma, isStudy, E, outBack, false, rnnMatrix);
            } else {
                double out = activeFunction.function(sigma);
                if (isStudy) {//输出结果并进行BP调整权重及阈值
                    outNub = out;
                    if (E.containsKey(getId())) {
                        this.E = E.get(getId());
                    } else {
                        this.E = 0;
                    }
                    if (isShowLog) {
                        System.out.println("E==" + this.E + ",out==" + out + ",nerveId==" + getId());
                    }
                    gradient = outGradient();//当前梯度变化
                    //调整权重 修改阈值 并进行反向传播
                    updatePower(eventId);
                } else {//获取最后输出
                    destoryParameter(eventId);
                    if (outBack != null) {
                        outBack.getBack(out, getId(), eventId);
                    } else {
                        throw new Exception("not find outBack");
                    }
                }
            }
        }
    }

    @Override
    protected void inputMatrix(long eventId, Matrix matrix, boolean isKernelStudy
            , int E, OutBack outBack) throws Exception {
        Matrix myMatrix = conv(matrix);
        if (isKernelStudy) {//回传
            Matrix matrix1 = matrixMapE.get(E);
            if (isShowLog) {
                System.out.println("E========" + E);
                System.out.println(myMatrix.getString());
            }
            if (matrix1.getX() == myMatrix.getX() && matrix1.getY() == myMatrix.getY()) {
                Matrix g = getGradient(myMatrix, matrix1);
                //System.out.println("error:" + g.getString() + ",hope:" + matrix1.getString());
                backMatrix(g);
            } else {
                throw new Exception("Wrong size setting of image in templateConfig");
            }
        } else {//卷积层输出
            if (outBack != null) {
                outBack.getBackMatrix(myMatrix, getId(), eventId);
            } else {
                throw new Exception("not find outBack");
            }
        }
    }

    private Matrix getGradient(Matrix matrix, Matrix E) throws Exception {
        Matrix matrix1 = new Matrix(matrix.getX(), matrix.getY());
        for (int i = 0; i < E.getX(); i++) {
            for (int j = 0; j < E.getY(); j++) {
                double nub = E.getNumber(i, j) - matrix.getNumber(i, j);
                //ArithUtil.sub(E.getNumber(i, j), matrix.getNumber(i, j));
                matrix1.setNub(i, j, nub);
            }
        }
        return matrix1;
    }

    private double outGradient() {//生成输出层神经元梯度变化
        //上层神经元输入值 * 当前神经元梯度*学习率 =该上层输入的神经元权重变化
        //当前梯度神经元梯度变化 *学习旅 * -1 = 当前神经元阈值变化
        return activeFunction.functionG(outNub) * (E - outNub);
    }
}
