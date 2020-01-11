package org.wlld.nerveEntity;

import org.wlld.MatrixTools.Matrix;
import org.wlld.i.ActiveFunction;

import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * 隐层神经元
 * @date 9:30 上午 2019/12/21
 */
public class HiddenNerve extends Nerve {
    private int depth;//所处深度
    //static final Logger logger = LogManager.getLogger(HiddenNerve.class);

    public HiddenNerve(int id, int depth, int upNub, int downNub, double studyPoint,
                       boolean init, ActiveFunction activeFunction, boolean isMatrix) {//隐层神经元
        super(id, upNub, "HiddenNerve", downNub, studyPoint, init, activeFunction, isMatrix);
        this.depth = depth;
    }

    @Override
    public void input(long eventId, double parameter, boolean isStudy, Map<Integer, Double> E) throws Exception {//接收上一层的输入
        // logger.debug("name:{},myId:{},depth:{},eventId:{},parameter:{}--getInput", name, getId(), depth, eventId, parameter);
        boolean allReady = insertParameter(eventId, parameter);
        if (allReady) {//参数齐了，开始计算 sigma - threshold
            //  logger.debug("depth:{},myID:{}--startCalculation", depth, getId());
            double sigma = calculation(eventId);
            double out = activeFunction.function(sigma);//激活函数输出数值
            if (isStudy) {
                outNub = out;
            } else {
                //System.out.println("sigma:" + sigma);
                destoryParameter(eventId);
            }
            //  logger.debug("depth:{},myID:{},outPut:{}", depth, getId(), out);
            sendMessage(eventId, out, isStudy, E);
        }
        // sendMessage();
    }

    @Override
    protected void inputMatrix(long eventId, Matrix parameter, boolean isStudy
            , double E) throws Exception {
        //先将输入矩阵进行卷积运算
        if (isStudy) {//初始化求和参数
            initFeatures(eventId);
        }
        Matrix matrix = convolution(parameter, eventId, isStudy);//这个神经元卷积结束
        if (isStudy) {//求和
            outNub = calculation(eventId);
        }
        sendMatrixMessage(eventId, matrix, isStudy, E);//将矩阵发到下一层神经元
    }
}
