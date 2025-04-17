package org.dromara.easyai.resnet;

import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;

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

    public ResnetInput(ResBlock fistBlock, int imageSize) {
        this.fistBlock = fistBlock;
        this.imageSize = imageSize;
    }

    public void postThreeChannelMatrix(ThreeChannelMatrix threeChannelMatrix, boolean study, Map<Integer, Float> E, OutBack outBack
            , long eventID) throws Exception {
        int x = threeChannelMatrix.getX();
        int y = threeChannelMatrix.getY();
        if (x == y && x == imageSize) {
            List<Matrix> matrixList = new ArrayList<>();
            matrixList.add(threeChannelMatrix.getMatrixR());
            matrixList.add(threeChannelMatrix.getMatrixG());
            matrixList.add(threeChannelMatrix.getMatrixB());
            fistBlock.sendMatrixList(matrixList, outBack, study, E, eventID);
        } else {
            throw new Exception("输入的图像必须为正方形，且尺寸必须等同于初始化配置的值：" + imageSize);
        }
    }

}
