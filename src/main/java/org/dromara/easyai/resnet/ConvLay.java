package org.dromara.easyai.resnet;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixNorm;
import org.dromara.easyai.resnet.entity.BackParameter;

import java.util.List;

/**
 * @author lidapeng
 * @time 2025/4/11 17:18
 * @des 单个卷积层
 */
public class ConvLay {
    private List<Matrix> convPower;//第一层卷积权重
    private List<MatrixNorm> matrixNormList;//归一化层
    private final BackParameter backParameter = new BackParameter();

    public BackParameter getBackParameter() {
        return backParameter;
    }

    public List<Matrix> getConvPower() {
        return convPower;
    }

    public void setConvPower(List<Matrix> convPower) {
        this.convPower = convPower;
    }

    public List<MatrixNorm> getMatrixNormList() {
        return matrixNormList;
    }

    public void setMatrixNormList(List<MatrixNorm> matrixNormList) {
        this.matrixNormList = matrixNormList;
    }
}
