package org.wlld.rnnJumpNerveEntity;


import org.wlld.MatrixTools.Matrix;
import org.wlld.i.OutBack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 感知神经元输入层
 *
 * @author lidapeng
 * @date 9:29 上午 2019/12/21
 */
public class SensoryNerve extends Nerve {

    public SensoryNerve(int id, int allDepth) throws Exception {
        super(id, "SensoryNerve", 0, false, null, false, 0, 0,
                0, 0, 0, 0, 0, allDepth);
        depth = 0;
    }

    /**
     * @param eventId     唯一的事件id（每个用户线程一个id用来处理线程安全）
     * @param parameter   该输入层的输入参数
     * @param isStudy     是否是学习 (学习状态没有输出)
     * @param E           标注
     * @param outBack     回调结果
     * @param isEmbedding 是否获取word2Vec返回结果(单独为词向量嵌入兼容，若无需则传false)
     * @param rnnMatrix   rnn参数矩阵，矩阵中每一行是每一层的特征向量
     * @param storeys    记录跳层路径的数组，即在rnn中经过的层数，若不在此路径集合内则跳跃
     */
    public void postMessage(long eventId, double parameter, boolean isStudy, Map<Integer, Double> E
            , OutBack outBack, boolean isEmbedding, Matrix rnnMatrix, int[] storeys) throws Exception {//感知神经元输出
        sendMessage(eventId, parameter, isStudy, E, outBack, isEmbedding, rnnMatrix, storeys, 0);
    }

    public void postPowerMessage(long eventId, double parameter, Matrix featureMatrix, OutBack outBack) throws Exception {
        //发送权重网络
        List<Integer> storeys = new ArrayList<>();
        storeys.add(0);
        sendTestMessage(eventId, parameter, depth, featureMatrix, storeys, outBack);
    }

    public void postMatrixMessage(long eventId, Matrix parameter, boolean isKernelStudy
            , int E, OutBack outBack) throws Exception {
        sendMatrix(eventId, parameter, isKernelStudy, E, outBack);
    }

    @Override
    public void connect(int depth, List<Nerve> nerveList) {//连接第一层隐层神经元
        super.connect(depth, nerveList);
    }
}
