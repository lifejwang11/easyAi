package org.dromara.easyai.conv;

import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;

import java.util.List;

/**
 * @author lidapeng
 * @time 2025/2/26 10:52
 * @des unet运算超类
 */
public abstract class ConvCount {
    private final MatrixOperation matrixOperation = new MatrixOperation();

    protected Matrix downPooling(Matrix matrix) throws Exception {//下池化
        int x = matrix.getX();
        int y = matrix.getY();
        int xt = 0;
        int yt = 0;
        if (x % 2 == 1) {//奇数
            xt = 1;
        }
        if (y % 2 == 1) {
            yt = 1;
        }
        Matrix myMatrix = new Matrix(x / 2 + xt, y / 2 + yt);
        for (int i = 0; i < x - 1; i += 2) {
            for (int j = 0; j < y - 1; j += 2) {
                float sigma = (matrix.getNumber(i, j) + matrix.getNumber(i, j + 1) +
                        matrix.getNumber(i + 1, j) + matrix.getNumber(i + 1, j + 1)) / 4f;
                myMatrix.setNub(i / 2, j / 2, sigma);
            }
        }
        return myMatrix;
    }

    private void insertMatrixValue(int x, int y, float value, Matrix matrix) throws Exception {
        int xSize = x + 2;
        int ySize = y + 2;
        if (xSize > matrix.getX()) {
            xSize--;
        }
        if (ySize > matrix.getY()) {
            ySize--;
        }
        for (int i = x; i < xSize; i++) {
            for (int j = y; j < ySize; j++) {
                matrix.setNub(i, j, value);
            }
        }
    }

    protected Matrix backDownPooling(Matrix matrix, int outX, int outY) throws Exception {
        int x = matrix.getX();
        int y = matrix.getY();
        int xt = 0;
        int yt = 0;
        if (outX % 2 == 1) {
            xt = 1;
        }
        if (outY % 2 == 1) {
            yt = 1;
        }
        Matrix myMatrix = new Matrix(x * 2 - xt, y * 2 - yt);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = matrix.getNumber(i, j) / 4;
                insertMatrixValue(i * 2, j * 2, value, myMatrix);
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

    protected Matrix oneConv(List<Matrix> feature, List<Float> oneConvPower) throws Exception {//单卷积降维
        int size = oneConvPower.size();
        Matrix sigmaMatrix = null;//所有通道加权求和
        for (int i = 0; i < size; i++) {
            Matrix featureMatrix = feature.get(i);
            float power = oneConvPower.get(i);
            Matrix pMatrix = matrixOperation.mathMulBySelf(featureMatrix, power);//通道加权
            if (i == 0) {
                sigmaMatrix = pMatrix;
            } else {
                sigmaMatrix = matrixOperation.add(sigmaMatrix, pMatrix);
            }
        }
        return sigmaMatrix;
    }

    protected void backOneConv(Matrix errorMatrix, List<Matrix> matrixList, List<Float> oneConvPower, float studyRate) throws Exception {//单卷积降维回传
        int size = oneConvPower.size();
        for (int t = 0; t < size; t++) {
            Matrix myMatrix = matrixList.get(t);
            int x = myMatrix.getX();
            int y = myMatrix.getY();
            float power = oneConvPower.get(t);
            float allSubPower = 0;
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    float subPower = myMatrix.getNumber(i, j) * errorMatrix.getNumber(i, j) * studyRate;
                    allSubPower = allSubPower + subPower;
                }
            }
            power = power + allSubPower;
            oneConvPower.set(t, power);
        }
    }

    protected ConvResult backDownConv(Matrix errorMatrix, Matrix outMatrix, ActiveFunction activeFunction
            , Matrix im2col, Matrix nerveMatrix, float studyRate, int kernSize, int xInput, int yInput) throws Exception {
        //下采样卷积误差反向传播
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
