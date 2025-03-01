package org.dromara.easyai.nerveEntity;

import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.i.OutBack;

import java.util.List;
import java.util.Map;

/**
 * 感知神经元输入层
 *
 * @author lidapeng
 * @date 9:29 上午 2019/12/21
 */
public class SensoryNerve extends Nerve {

    public SensoryNerve(int id, int upNub, int channelNo) throws Exception {
        super(id, upNub, "SensoryNerve", 0, 0.1f, false,
                null, false, 0, 0, 0, 0, 0, 0
                , 1, 0, channelNo);
    }

    /**
     * @param eventId   唯一的事件id
     * @param parameter 输入点的数据
     * @param isStudy   是否是学习 (学习状态没有输出)
     * @param E         标注
     * @param outBack   回调结果
     * @throws Exception
     */
    public void postMessage(long eventId, float parameter, boolean isStudy, Map<Integer, Float> E
            , OutBack outBack) throws Exception {//感知神经元输出

        sendMessage(eventId, parameter, isStudy, E, outBack);
    }


    /**
     * @param eventId       唯一的事件id
     * @param parameter     特征图像
     * @param isKernelStudy 是否是学习 (学习状态没有输出)
     * @param E             标注
     * @param outBack       回调结果
     * @param needMatrix    需要矩阵输出
     * @throws Exception
     */
    public void postThreeChannelMatrix(long eventId, ThreeChannelMatrix parameter, boolean isKernelStudy
            , Map<Integer, Float> E, OutBack outBack, boolean needMatrix) throws Exception {
        if (channelNo != 3) {
            throw new Exception("发送三通道矩阵特征，通道数必须设置为3");
        }
        sendThreeChannelMatrix(eventId, parameter, isKernelStudy, E, outBack, needMatrix);
    }

    /**
     * @param eventId       唯一的事件id
     * @param parameter     多通道特征图像
     * @param isKernelStudy 是否是学习 (学习状态没有输出)
     * @param E             标注
     * @param outBack       回调结果
     * @param needMatrix    需要矩阵输出
     * @throws Exception
     */
    public void postMatrixList(long eventId, List<Matrix> parameter, boolean isKernelStudy
            , Map<Integer, Float> E, OutBack outBack, boolean needMatrix) throws Exception {
        if (channelNo != parameter.size()) {
            throw new Exception("设置通道数与实际通道数不符");
        }
        sendListMatrix(eventId, parameter, isKernelStudy, E, outBack, needMatrix);
    }

    @Override
    public void connect(List<Nerve> nerveList) {//连接第一层隐层神经元
        super.connect(nerveList);
    }

    @Override
    public void connectSonOnly(Nerve nerve) {
        super.connectSonOnly(nerve);
    }
}
