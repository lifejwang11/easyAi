package org.dromara.easyai.conv;

import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.nerveEntity.ConvParameter;
import org.dromara.easyai.nerveEntity.ConvSize;

import java.util.List;

/**
 * @author lidapeng
 * @time 2025/2/26 10:52
 * @des unet运算超类
 */
public abstract class ConvCount {
    private final MatrixOperation matrixOperation = new MatrixOperation();

    private Matrix upPooling(Matrix matrix) throws Exception {//上池化
        int x = matrix.getX();
        int y = matrix.getY();
        Matrix myMatrix = new Matrix(x * 2, y * 2);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = matrix.getNumber(i, j);
                insertMatrixValue(i * 2, j * 2, value, myMatrix);
            }
        }
        return myMatrix;
    }

    private Matrix backUpPooling(Matrix errorMatrix) throws Exception {//退上池化
        int x = errorMatrix.getX();
        int y = errorMatrix.getY();
        Matrix myMatrix = new Matrix(x / 2, y / 2);
        for (int i = 0; i < x - 1; i += 2) {
            for (int j = 0; j < y - 1; j += 2) {
                float sigma = errorMatrix.getNumber(i, j) + errorMatrix.getNumber(i, j + 1) +
                        errorMatrix.getNumber(i + 1, j) + errorMatrix.getNumber(i + 1, j + 1);
                myMatrix.setNub(i / 2, j / 2, sigma);
            }
        }
        return myMatrix;
    }

    private Matrix downPooling(Matrix matrix) throws Exception {//下池化
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

    protected Matrix backDownPooling(Matrix matrix, int outX, int outY) throws Exception {//退下池化
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

    private void backUpConv(Matrix errorMatrix) {//退上卷积

    }

    private ConvResult upConv(Matrix matrix, int kerSize, Matrix nervePowerMatrix, ActiveFunction activeFunction) throws Exception {//进行上采样
        ConvResult convResult = new ConvResult();
        int x = getUpSize(matrix.getX(), kerSize);
        int y = getUpSize(matrix.getY(), kerSize);
        Matrix vector = matrixOperation.matrixToVector(matrix, false);//输入矩阵转列向量 t
        Matrix im2colMatrix = matrixOperation.mulMatrix(vector, nervePowerMatrix);
        Matrix out = matrixOperation.reverseIm2col(im2colMatrix, kerSize, 1, x, y);
        Matrix outMatrix = new Matrix(x, y);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = activeFunction.function(out.getNumber(i, j));
                outMatrix.setNub(i, j, value);
            }
        }
        convResult.setLeftMatrix(vector);
        convResult.setResultMatrix(outMatrix);
        return convResult;
    }

    protected Matrix upConvAndPooling(Matrix matrix, ConvParameter convParameter, int convTimes, ActiveFunction activeFunction
            , int kernLen, boolean pooling) throws Exception {
        Matrix downConvMatrix = downConvAndPooling(matrix, convParameter, convTimes, activeFunction, kernLen, false, -1);
        if (pooling) {
            ConvResult result = upConv(downConvMatrix, kernLen, convParameter.getUpNerveMatrix(), activeFunction);
            convParameter.setUpFeatureMatrix(result.getLeftMatrix());
            return upPooling(result.getResultMatrix());
        }
        return downConvMatrix;
    }

    protected Matrix downConvAndPooling(Matrix matrix, ConvParameter convParameter, int convTimes, ActiveFunction activeFunction
            , int kernLen, boolean pooling, long eventID) throws Exception {
        List<ConvSize> convSizeList = convParameter.getConvSizeList();
        List<Matrix> nerveMatrixList = convParameter.getNerveMatrixList();
        List<Matrix> im2colMatrixList = convParameter.getIm2colMatrixList();
        List<Matrix> outMatrixList = convParameter.getOutMatrixList();
        im2colMatrixList.clear();
        outMatrixList.clear();
        for (int i = 0; i < convTimes; i++) {
            ConvSize convSize = convSizeList.get(i);
            Matrix nerveMatrix = nerveMatrixList.get(i);
            int xInput = matrix.getX();
            int yInput = matrix.getY();
            convSize.setXInput(xInput);
            convSize.setYInput(yInput);
            ConvResult convResult = downConvCount(matrix, activeFunction, kernLen, nerveMatrix);
            im2colMatrixList.add(convResult.getLeftMatrix());
            Matrix myMatrix = convResult.getResultMatrix();
            outMatrixList.add(myMatrix);
            matrix = myMatrix;
        }
        if (eventID >= 0) {
            convParameter.getFeatureMap().put(eventID, matrix);
        }
        convParameter.setOutX(matrix.getX());
        convParameter.setOutY(matrix.getY());
        if (pooling) {
            return downPooling(matrix);//池化
        }
        return matrix;
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

    protected float backDecoderOneConv(Matrix errorMatrix, Matrix featureMatrix, float studyRate) throws Exception {
        int x = featureMatrix.getX();
        int y = featureMatrix.getY();
        float allSub = 0;//1卷积权重误差
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = featureMatrix.getNumber(i, j) * errorMatrix.getNumber(i, j) * studyRate;
                allSub = allSub + value;
            }
        }
        return allSub;
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

    protected Matrix backAllDownConv(ConvParameter convParameter, Matrix errorMatrix, float studyPoint
            , ActiveFunction activeFunction, int convTimes, int kernLen) throws Exception {
        List<Matrix> outMatrixList = convParameter.getOutMatrixList();
        List<Matrix> im2colMatrixList = convParameter.getIm2colMatrixList();
        List<Matrix> nerveMatrixList = convParameter.getNerveMatrixList();
        List<ConvSize> convSizeList = convParameter.getConvSizeList();
        for (int i = convTimes - 1; i >= 0; i--) {
            Matrix outMatrix = outMatrixList.get(i);
            Matrix im2col = im2colMatrixList.get(i);
            Matrix nerveMatrix = nerveMatrixList.get(i);
            ConvSize convSize = convSizeList.get(i);
            int xInput = convSize.getXInput();
            int yInput = convSize.getYInput();
            ConvResult convResult = backDownConv(errorMatrix, outMatrix, activeFunction, im2col,
                    nerveMatrix, studyPoint, kernLen, xInput, yInput);
            nerveMatrixList.set(i, convResult.getNervePowerMatrix());//更新权重
            errorMatrix = convResult.getResultMatrix();
        }
        return errorMatrix;
    }

    private ConvResult backDownConv(Matrix errorMatrix, Matrix outMatrix, ActiveFunction activeFunction
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

    private ConvResult downConvCount(Matrix matrix, ActiveFunction activeFunction, int kerSize
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
