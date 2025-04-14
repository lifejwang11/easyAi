package org.dromara.easyai.resnet;

import org.dromara.easyai.i.CustomEncoding;
import org.dromara.easyai.matrixTools.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @time 2025/4/11 11:17
 */
public class ResNetConnectionLine implements CustomEncoding {
    private ResBlock lastBlock;//最后一层残差块
    private int lastSize;//最后一层的大小
    private int allTimes;//隐层神经数量
    private int nerveSize;//输入神经元数量
    private int number = 0;
    private final List<Float> errorValues = new ArrayList<>();

    public void setLastBlock(ResBlock lastBlock, int lastSize, int allTimes, int nerveSize) {
        this.lastBlock = lastBlock;
        this.lastSize = lastSize;
        this.allTimes = allTimes;
        this.nerveSize = nerveSize;
    }

    private void addError(Map<Integer, Float> wg) {
        for (int i = 1; i <= nerveSize; i++) {
            float error = wg.get(i);
            if (number == 1) {
                errorValues.add(error);
            } else {
                float value = error + errorValues.get(i - 1);
                errorValues.set(i - 1, value);
            }
        }
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

    private void toError() throws Exception {
        List<Matrix> errorMatrix = new ArrayList<>();
        for (Float errorValue : errorValues) {
            Matrix feature = new Matrix(lastSize, lastSize);
            float error = errorValue;
            fill(feature, error);
            errorMatrix.add(feature);
        }
        errorValues.clear();
        lastBlock.backError(errorMatrix);
    }

    @Override
    public void backError(Map<Integer, Float> wg, int id) throws Exception {
        number++;
        addError(wg);
        if (number == allTimes) {
            number = 0;
            toError();
        }
    }
}
