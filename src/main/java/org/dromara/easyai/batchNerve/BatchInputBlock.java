package org.dromara.easyai.batchNerve;

import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;

import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @time 2026/1/7 15:31
 * @des 输入模块
 */
public class BatchInputBlock {
    private final QBlock firstBlock;
    private final int inputSize;

    public BatchInputBlock(QBlock firstBlock, int inputSize) {
        this.firstBlock = firstBlock;
        this.inputSize = inputSize;
    }

    public void postMessage(List<FeatureBody> featureBodies, boolean study, OutBack outBack, long eventID
            , Map<Integer, Float> pd) throws Exception {
        Matrix matrix = featureBodies.get(0).getFeature();
        if (matrix.getX() == 1 && matrix.getY() == inputSize) {
            firstBlock.postMassage(featureBodies, study, outBack, eventID, pd);
        } else {
            throw new Exception("特征矩阵的行数必须为1 且 列数要等于配置类中预设值：" + inputSize);
        }
    }
}
