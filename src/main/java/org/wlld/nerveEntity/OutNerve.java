package org.wlld.nerveEntity;

import org.wlld.MatrixTools.Matrix;
import org.wlld.i.ActiveFunction;
import org.wlld.i.OutBack;
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
    private OutBack outBack;
    private NerveManager nerveManager;
    private Map<Integer, Matrix> matrixMapE;//主键与期望矩阵的映射
    private Matrix matrixF;

    public OutNerve(int id, int upNub, int downNub, double studyPoint, boolean init,
                    ActiveFunction activeFunction, boolean isDynamic) throws Exception {
        super(id, upNub, "OutNerve", downNub, studyPoint, init, activeFunction, isDynamic);
    }

    public void setNerveManager(NerveManager nerveManager) {
        this.nerveManager = nerveManager;
    }

    public void setMatrixMap(Map<Integer, Matrix> matrixMap) {
        matrixMapE = matrixMap;
    }

    public void setOutBack(OutBack outBack) {
        this.outBack = outBack;
    }

    @Override
    public void input(long eventId, double parameter, boolean isStudy, Map<Integer, Double> E) throws Exception {
        boolean allReady = insertParameter(eventId, parameter);
        if (allReady) {//参数齐了，开始计算 sigma - threshold
            double sigma = calculation(eventId);
            double out = activeFunction.function(sigma);
            if (isStudy) {//输出结果并进行BP调整权重及阈值
                outNub = out;
                this.E = E.get(getId());
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
    protected void inputMartix(long eventId, Matrix matrix, boolean isKernelStudy, boolean isNerveStudy, Map<Integer, Double> E) throws Exception {
        Matrix myMatrix = dynamicNerve(matrix, eventId, isKernelStudy);
        if (matrixF == null) {
            matrixF = new Matrix(myMatrix.getX(), myMatrix.getY());
        }
        if (isKernelStudy) {//回传
            // System.out.println(myMatrix.getString());
            for (Map.Entry<Integer, Double> entry : E.entrySet()) {
                double g;
                if (entry.getValue() > 0.5) {//正模板
                    Matrix matrix1 = matrixMapE.get(entry.getKey());
                    g = getGradient(myMatrix, matrix1);
                } else {
                    g = getGradient(myMatrix, matrixF);
                }
                backMatrix(g, eventId);
            }//所有训练集的卷积核训练结束,才需要再次训练全连接层
        } else {//输出到全连接层
            List<Double> featurList = getFeaturList(myMatrix);
            intoNerve(eventId, featurList, isNerveStudy, E);
        }
    }

    private List<Double> getFeaturList(Matrix matrix) throws Exception {//
        List<Double> list = new ArrayList<>();
        for (int i = 0; i < matrix.getX(); i++) {
            for (int j = 0; j < matrix.getY(); j++) {
                double nub = ArithUtil.div(matrix.getNumber(i, j), 10);
                list.add(nub);
            }
        }
        return list;
    }

    private void intoNerve(long eventId, List<Double> featurList, boolean isStudy, Map<Integer, Double> map) throws Exception {
        List<SensoryNerve> sensoryNerveList = nerveManager.getSensoryNerves();
        for (int i = 0; i < sensoryNerveList.size(); i++) {
            sensoryNerveList.get(i).postMessage(eventId, featurList.get(i), isStudy, map);
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
