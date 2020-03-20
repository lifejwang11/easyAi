package org.wlld.nerveEntity;

import org.wlld.MatrixTools.Matrix;
import org.wlld.i.ActiveFunction;
import org.wlld.i.OutBack;
import org.wlld.imageRecognition.border.Border;
import org.wlld.nerveCenter.NerveManager;
import org.wlld.tools.ArithUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * 输出神经元
 * @date 11:25 上午 2019/12/21
 */
public class OutNerve extends Nerve {
    private Map<Integer, Matrix> matrixMapE;//主键与期望矩阵的映射
    private boolean isShowLog;

    public OutNerve(int id, int upNub, int downNub, double studyPoint, boolean init,
                    ActiveFunction activeFunction, boolean isDynamic, boolean isAccurate
            , boolean isShowLog) throws Exception {
        super(id, upNub, "OutNerve", downNub, studyPoint, init, activeFunction, isDynamic, isAccurate);
        this.isShowLog = isShowLog;
    }


    public void setMatrixMap(Map<Integer, Matrix> matrixMap) {
        matrixMapE = matrixMap;
    }


    @Override
    public void input(long eventId, double parameter, boolean isStudy, Map<Integer, Double> E
            , OutBack outBack) throws Exception {
        boolean allReady = insertParameter(eventId, parameter);
        if (allReady) {//参数齐了，开始计算 sigma - threshold
            double sigma = calculation(eventId);
            double out = activeFunction.function(sigma);
            if (isStudy) {//输出结果并进行BP调整权重及阈值
                outNub = out;
                if (E.containsKey(getId())) {
                    this.E = E.get(getId());
                } else {
                    this.E = -1;
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

    @Override
    protected void inputMatrix(long eventId, Matrix matrix, boolean isKernelStudy
            , int E, OutBack outBack) throws Exception {
        Matrix myMatrix = dynamicNerve(matrix, eventId, isKernelStudy);
        if (isKernelStudy) {//回传
            Matrix matrix1 = matrixMapE.get(E);
//            if (isShowLog) {
//                System.out.println("E================" + E);
//                System.out.println(myMatrix.getString());
//            }
            if (matrix1.getX() <= myMatrix.getX() && matrix1.getY() <= myMatrix.getY()) {
                double g = getGradient(myMatrix, matrix1);
                backMatrix(g, eventId);
            } else {
                throw new Exception("Wrong size setting of image in templateConfig");
            }
        } else {//卷积层输出
            if (outBack != null) {
                outBack.getBackMatrix(myMatrix, eventId);
            } else {
                throw new Exception("not find outBack");
            }
        }
    }

    private double getGradient(Matrix matrix, Matrix E) throws Exception {
        double all = 0;
        int allNub = 0;
        for (int i = 0; i < E.getX(); i++) {
            for (int j = 0; j < E.getY(); j++) {
                allNub++;
                double nub = ArithUtil.sub(E.getNumber(i, j), matrix.getNumber(i, j));
                all = ArithUtil.add(all, nub);
            }
        }
        return ArithUtil.div(all, allNub);
    }

    private double outGradient() {//生成输出层神经元梯度变化
        //上层神经元输入值 * 当前神经元梯度*学习率 =该上层输入的神经元权重变化
        //当前梯度神经元梯度变化 *学习旅 * -1 = 当前神经元阈值变化
        return ArithUtil.mul(activeFunction.functionG(outNub), ArithUtil.sub(E, outNub));
    }
}
