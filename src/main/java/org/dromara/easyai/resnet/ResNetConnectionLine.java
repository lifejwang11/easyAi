package org.dromara.easyai.resnet;

import org.dromara.easyai.i.CustomEncoding;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.resnet.entity.BatchBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @time 2025/4/11 11:17
 */
public class ResNetConnectionLine implements CustomEncoding {
    private ResBlock lastBlock;//最后一层残差块
    private int lastSize;//最后一层的特征大小

    public void setLastBlock(ResBlock lastBlock, int lastSize) {
        this.lastBlock = lastBlock;
        this.lastSize = lastSize;
    }

    private void fill(Matrix feature, float value) throws Exception {
        int x = feature.getX();
        int y = feature.getY();
        float myValue = value / (x * y);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                feature.setNub(i, j, myValue);
            }
        }
    }

    @Override
    public void backError(Map<Integer, Float> wg, int id) throws Exception {

    }

    @Override
    public void backErrorList(List<Matrix> nextErrorMatrixList) throws Exception {
        List<BatchBody> batchBodies = new ArrayList<>();
        for (Matrix matrix : nextErrorMatrixList) {//遍历每一张图的特征
            BatchBody batchBody = new BatchBody();
            batchBodies.add(batchBody);
            int size = matrix.getY();//通道数
            List<Matrix> errorMatrixList = new ArrayList<>();//每个通道的误差矩阵集合
            batchBody.setFeatureList(errorMatrixList);
            for (int j = 0; j < size; j++) {//遍历所有通道
                float error = matrix.getNumber(0, j);//该通道误差
                Matrix feature = new Matrix(lastSize, lastSize);//通道的误差矩阵
                fill(feature, error);
                errorMatrixList.add(feature);
            }
        }
        lastBlock.backError(batchBodies);
    }
}
