package org.dromara.easyai.tsd;

import org.dromara.easyai.conv.DymStudy;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;

import java.util.Map;
import java.util.Random;

/**
 * @author lidapeng
 * @time 2025/8/7 08:55
 * @des QLearning 基础运算单元
 */
public class QBlock {
    private final DymStudy dymStudy;
    private final ActiveFunction activeFunction;
    private Matrix powerMatrix;//权重矩阵
    private Matrix bMatrix;//偏移矩阵
    private Matrix bDymStudyRate;//偏移量动态学习率
    private Matrix powerDymStudyRate;//权重动态学习率
    private MatrixOperation matrixOperation = new MatrixOperation();
    private Matrix inputMatrix;
    private Matrix outputMatrix;
    private QBlock sonBlock;//向前模块
    private QBlock outBlock;//输出模块
    private QBlock fatherBlock;//向后模块
    private int deep;//深度

    public QBlock(DymStudy dymStudy, int inputSize, int outputSize, ActiveFunction activeFunction, int deep) throws Exception {
        Random random = new Random();
        this.deep = deep;
        this.dymStudy = dymStudy;
        this.activeFunction = activeFunction;
        this.powerMatrix = new Matrix(inputSize, outputSize);
        this.powerDymStudyRate = new Matrix(inputSize, outputSize);
        this.bMatrix = new Matrix(1, outputSize);
        this.bDymStudyRate = new Matrix(1, outputSize);
        initMatrix(powerMatrix, random);
        initMatrix(bMatrix, random);
    }

    private Matrix calculation(Matrix featureMatrix, boolean study, Map<Integer, Float> E, OutBack outBack, Matrix wordMatrix) throws Exception {
        Matrix result = matrixOperation.mulMatrix(featureMatrix, powerMatrix);
        if (study) {
            inputMatrix = result;
        }
        Matrix outMatrix = matrixOperation.add(result, bMatrix);
        int x = outMatrix.getX();
        int y = outMatrix.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = activeFunction.function(outMatrix.getNumber(i, j));
                outMatrix.setNub(i, j, value);
            }
        }
        if (study) {
            outputMatrix = outMatrix;
        }
        Matrix res;
        if (deep < wordMatrix.getX()) {
            Matrix word = wordMatrix.getRow(deep);
            res = matrixOperation.add(outMatrix, word);
        } else {
            res = outMatrix;
        }
        return res;
    }

    private void initMatrix(Matrix matrix, Random random) throws Exception {//初始化矩阵
        int x = matrix.getX();
        int y = matrix.getY();
        float sh = (float) Math.sqrt(x);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                matrix.setNub(i, j, random.nextFloat() / sh);
            }
        }
    }

    public void setSonBlock(QBlock sonBlock) {
        this.sonBlock = sonBlock;
    }

    public void setOutBlock(QBlock outBlock) {
        this.outBlock = outBlock;
    }

    public void setFatherBlock(QBlock fatherBlock) {
        this.fatherBlock = fatherBlock;
    }
}
