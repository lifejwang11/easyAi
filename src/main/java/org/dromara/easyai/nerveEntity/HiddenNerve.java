package org.dromara.easyai.nerveEntity;

import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.i.CustomEncoding;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixList;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.i.OutBack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * 隐层神经元
 * &#064;date  9:30 上午 2019/12/21
 */
public class HiddenNerve extends Nerve {
    private final boolean isConvFinish;//卷积最后一层
    private final MatrixOperation matrixOperation = new MatrixOperation();

    public HiddenNerve(int id, int depth, int upNub, int downNub, float studyPoint,
                       boolean init, ActiveFunction activeFunction, boolean isDynamic, int rzType, float lParam
            , int kernLen, int matrixX, int matrixY, boolean isConvFinish, int coreNumber, int channelNo, float oneConvStudy, boolean norm
            , CustomEncoding customEncoding, float gaMa, float gMaxTh, boolean auTo) throws Exception {//隐层神经元
        super(id, upNub, "HiddenNerve", downNub, studyPoint,
                init, activeFunction, isDynamic, rzType, lParam, kernLen, depth, matrixX, matrixY
                , coreNumber, channelNo, oneConvStudy, norm, customEncoding, gaMa, gMaxTh, auTo);
        this.isConvFinish = isConvFinish;
    }

    @Override
    public void input(long eventId, float parameter, boolean isKernelStudy, Map<Integer, Float> E
            , OutBack outBack, Map<Integer, Float> pd) throws Exception {//接收上一层的输入
        boolean allReady = insertParameter(eventId, parameter);
        if (allReady) {//参数齐了，开始计算 sigma - threshold
            float sigma = calculation(eventId);
            float out = activeFunction.function(sigma);//激活函数输出数值
            if (isKernelStudy) {
                outNub = out;
            } else {
                destoryParameter(eventId);
            }
            sendMessage(eventId, out, isKernelStudy, E, outBack, pd);
        }
    }

    @Override
    protected void inputMatrixFeature(long eventId, List<Float> parameters, boolean isStudy, Map<Integer, Float> E,
                                      OutBack imageBack, Map<Integer, Float> pd) throws Exception {
        insertParameters(eventId, parameters);
        float sigma = calculation(eventId);
        float out = activeFunction.function(sigma);//激活函数输出数值
        if (isStudy) {
            outNub = out;
        } else {
            destoryParameter(eventId);
        }
        sendMessage(eventId, out, isStudy, E, imageBack, pd);
    }

    @Override
    protected void inputMatrix(long eventId, List<Matrix> matrix, boolean isStudy
            , Map<Integer, Float> E, OutBack outBack, boolean needMatrix, Map<Integer, Float> pd) throws Exception {
        List<Matrix> myMatrix = conv(matrix);//处理过的矩阵
        if (isConvFinish) {
            Matrix ourMatrix;
            if (myMatrix.size() == 1) {
                ourMatrix = myMatrix.get(0);
            } else {
                MatrixList matrixList = new MatrixList(myMatrix.get(0), true, 100);
                for (int i = 1; i < myMatrix.size(); i++) {
                    matrixList.add(myMatrix.get(i));
                }
                ourMatrix = matrixList.getMatrix();
            }
            if (!isStudy && needMatrix) {
                outBack.getBackMatrix(ourMatrix, getId(), eventId);
            }
            sendMatrixList(eventId, matrixOperation.matrixToList(ourMatrix), isStudy, E, outBack, pd);
        } else {
            sendMatrix(eventId, myMatrix, isStudy, E, outBack, needMatrix, pd);
        }
    }

    @Override
    protected void inputThreeChannelMatrix(long eventId, ThreeChannelMatrix picture, boolean isKernelStudy, Map<Integer, Float> E,
                                           OutBack outBack, boolean needMatrix, Map<Integer, Float> pd) throws Exception {
        //接收三通道矩阵
        List<Matrix> matrixList = new ArrayList<>();
        matrixList.add(picture.getMatrixR());
        matrixList.add(picture.getMatrixG());
        matrixList.add(picture.getMatrixB());
        demRedByMatrixList(eventId, matrixList, isKernelStudy, E, outBack, needMatrix, pd);
    }
}
