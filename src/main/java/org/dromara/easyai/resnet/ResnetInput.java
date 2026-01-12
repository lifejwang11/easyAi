package org.dromara.easyai.resnet;

import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.resnet.entity.BatchBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @time 2025/4/17 13:04
 * @des restNet输出类
 */
public class ResnetInput {
    private final ResBlock fistBlock;//第一个残差块
    private final int imageSize;//图像大小
    private final int batchSize;

    public ResnetInput(ResBlock fistBlock, int imageSize, int batchSize) {
        this.fistBlock = fistBlock;
        this.imageSize = imageSize;
        this.batchSize = batchSize;
    }

    public void postFeature(List<BatchBody> batchBodies, OutBack outBack, long eventID, boolean outFeature, boolean study) throws Exception {
        if ((study && batchSize == batchBodies.size()) || (!study && batchBodies.size() == 1)) {
            for (BatchBody batchBody : batchBodies) {
                Matrix matrix = batchBody.getFeatureList().get(0);
                int x = matrix.getX();
                int y = matrix.getY();
                if (x != y || x != imageSize) {
                    throw new Exception("输入的图像必须为正方形，且尺寸必须等同于初始化配置的值：" + imageSize);
                }
            }
            fistBlock.sendMatrixList(batchBodies, outBack, study, eventID, outFeature);
        } else {
            throw new Exception("训练批次数量与预设数量不符");
        }
    }

}
