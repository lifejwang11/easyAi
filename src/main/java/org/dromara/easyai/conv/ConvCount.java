package org.dromara.easyai.conv;

import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;

/**
 * @author lidapeng
 * @time 2025/2/26 10:52
 * @des unet运算超类
 */
public abstract class ConvCount {
    private MatrixOperation matrixOperation = new MatrixOperation();

    protected Matrix downPooling(Matrix matrix) throws Exception {//下池化
        int x = matrix.getX();
        int y = matrix.getY();
        Matrix myMatrix = new Matrix(x / 2, y / 2);
        for (int i = 0; i < x - 1; i += 2) {
            for (int j = 0; j < y - 1; j += 2) {
                float sigma = (matrix.getNumber(i, j) + matrix.getNumber(i, j + 1) +
                        matrix.getNumber(i + 1, j) + matrix.getNumber(i + 1, j + 1)) / 4f;
                myMatrix.setNub(i / 2, j / 2, sigma);
            }
        }
        return myMatrix;
    }

    private int getUpSize(int size, int kerSize) {//获取上采样后应该的尺寸
        return size + kerSize - 1;
    }

    protected ConvResult upConv(Matrix matrix, int kerSize, Matrix nervePowerMatrix) throws Exception {//进行上采样
        ConvResult convResult = new ConvResult();
        int x = getUpSize(matrix.getX(), kerSize);
        int y = getUpSize(matrix.getY(), kerSize);
        Matrix vector = matrixOperation.matrixToVector(matrix, false);//输入矩阵转列向量
        Matrix im2colMatrix = matrixOperation.mulMatrix(vector, nervePowerMatrix);
        Matrix out = matrixOperation.reverseIm2col(im2colMatrix, kerSize, 1, x, y);
        convResult.setLeftMatrix(vector);
        convResult.setResultMatrix(out);
        return convResult;
    }

    protected ConvResult backDownConv(Matrix errorMatrix, Matrix outMatrix, ActiveFunction activeFunction
            , Matrix im2col, Matrix nerveMatrix, float studyRate, int kernSize, int xInput, int yInput) throws Exception {//下采样卷积误差反向传播
        ConvResult convResult = new ConvResult();
        int x = errorMatrix.getX();
        int y = errorMatrix.getY();
        Matrix resultError = new Matrix(x * y, 1);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float error = errorMatrix.getNumber(i, j);
                float out = outMatrix.getNumber(i, j);
                error = error * activeFunction.functionG(out);
                resultError.setNub(y * i + j, 0, error);
            }
        }
        Matrix wSub = matrixOperation.matrixMulPd(resultError, im2col, nerveMatrix, false);
        Matrix im2colSub = matrixOperation.matrixMulPd(resultError, im2col, nerveMatrix, true);
        matrixOperation.mathMul(wSub, studyRate);
        nerveMatrix = matrixOperation.add(nerveMatrix, wSub);//调整卷积核
        Matrix gNext = matrixOperation.reverseIm2col(im2colSub, kernSize, 1, xInput, yInput);//其余误差
        convResult.setNervePowerMatrix(nerveMatrix);
        convResult.setResultMatrix(gNext);
        return convResult;
    }

    protected ConvResult downConv(Matrix matrix, ActiveFunction activeFunction, int kerSize
            , Matrix nervePowerMatrix) throws Exception {//进行下采样卷积运算
        ConvResult convResult = new ConvResult();
        int xInput = matrix.getX();
        int yInput = matrix.getY();
        int sub = kerSize - 1;
        int x = xInput - sub;//线性变换后矩阵的行数 （图片长度-（核长-步长））/步长
        int y = yInput - sub;//线性变换后矩阵的列数
        Matrix myMatrix = new Matrix(x, y);//线性变化后的矩阵
        Matrix im2col = matrixOperation.im2col(matrix, kerSize, 1);
        convResult.setLeftMatrix(im2col);
        //输出矩阵
        Matrix matrixOut = matrixOperation.mulMatrix(im2col, nervePowerMatrix);
        //输出矩阵重新排序
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float nub = activeFunction.function(matrixOut.getNumber(i * y + j, 0));
                myMatrix.setNub(i, j, nub);
            }
        }
        convResult.setResultMatrix(myMatrix);
        return convResult;
    }

}
