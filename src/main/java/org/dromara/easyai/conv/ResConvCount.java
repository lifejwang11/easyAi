package org.dromara.easyai.conv;

import org.dromara.easyai.function.ReLu;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixNorm;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.resnet.entity.BackParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @time 2025/4/11 13:05
 * @des resnet卷积运算超类
 */
public abstract class ResConvCount {
    private final MatrixOperation matrixOperation = new MatrixOperation();


    protected int getConvDeep(int size, int minFeatureValue) {//获取卷积层深度
        int x = size;
        int step = 2;
        int deep = 0;//深度
        do {
            x = x + x % 2;
            x = x / step;
            if (deep == 0) {
                x = x + x % 2;
                x = x / step;
            }
            deep++;
        } while (x > minFeatureValue);
        return deep - 1;
    }

    protected int getFeatureSize(int deep, int size, boolean seven) {
        int x = size;
        int step = 2;
        for (int i = 0; i < deep; i++) {
            x = x + x % step;
            x = x / step;
            if (i == 0) {
                x = x + x % step;
                x = x / step;
            }
        }
        if (seven && deep == 1) {
            x = x * 2;
        }
        return x;
    }

    protected boolean fill(int deep, int size, boolean seven) {
        int x = size;
        int step = 2;
        boolean fill = false;
        for (int i = 0; i < deep; i++) {
            if (i == deep - 1) {//最后一层开始进行判断
                if (x % step > 0) {
                    fill = true;
                }
                if (seven || deep > 1) {
                    break;
                }
            }
            x = x + x % step;
            x = x / step;
            if (i == 0) {
                if (deep == 1) {
                    if (x % step > 0) {
                        fill = true;
                    }
                    break;
                }
                x = x + x % step;
                x = x / step;
            }
        }
        return fill;
    }

    protected List<Matrix> manyOneConv(List<Matrix> feature, List<List<Float>> oneConvPower) throws Exception {
        List<Matrix> result = new ArrayList<>();
        for (List<Float> convPower : oneConvPower) {
            result.add(oneConv(feature, convPower));
        }
        return result;
    }

    protected Matrix oneConv(List<Matrix> feature, List<Float> oneConvPower) throws Exception {//单卷积降维
        int size = oneConvPower.size();
        Matrix sigmaMatrix = null;//所有通道加权求和
        for (int i = 0; i < size; i++) {
            Matrix featureMatrix = feature.get(i);
            int xSize = featureMatrix.getX() / 2;
            Matrix scaleFeature = featureMatrix.scale(true, xSize);
            float power = oneConvPower.get(i);
            Matrix pMatrix = matrixOperation.mathMulBySelf(scaleFeature, power);//通道加权
            if (i == 0) {
                sigmaMatrix = pMatrix;
            } else {
                sigmaMatrix = matrixOperation.add(sigmaMatrix, pMatrix);
            }
        }
        return sigmaMatrix;
    }

    //不是第一层
    protected List<Matrix> downConvMany2(List<Matrix> featureList, List<Matrix> powerMatrixList, boolean study, BackParameter backParameter,
                                         List<MatrixNorm> matrixNormList, List<Matrix> resFeatureList, List<List<Float>> oneConvPower) throws Exception {//多维度卷积运算
        //featureList resFeatureList特征默认进来就是偶数了
        int size = powerMatrixList.size();//卷积通道数
        int featureSize = featureList.size();//特征通道数
        if (size != featureSize) {
            throw new Exception("卷积通道数与特征通道数不一致");
        }
        List<ConvResult> convResults = new ArrayList<>();
        ReLu reLu = new ReLu();
        List<Matrix> outMatrixList = new ArrayList<>();
        if (study) {
            backParameter.setConvResults(convResults);
            backParameter.setOutMatrixList(outMatrixList);
        }
        int matrixSize = 0;
        if (resFeatureList != null && resFeatureList.size() != size) {//要通过1v1卷积层进行升维
            if (oneConvPower == null) {
                throw new Exception("1v1 卷积核空了");
            }
            resFeatureList = manyOneConv(resFeatureList, oneConvPower);
        }
        if (resFeatureList != null && resFeatureList.size() != size) {
            throw new Exception("残差与卷积核维度不相等");
        }
        for (int i = 0; i < size; i++) {
            Matrix powerMatrix = powerMatrixList.get(i);//卷积
            MatrixNorm matrixNorm = matrixNormList.get(i);
            Matrix featureMatrix = padding(featureList.get(i));
            if (i == 0) {
                matrixSize = featureMatrix.getX();
                backParameter.setIm2clSize(matrixSize);
            }
            ConvResult convResult = downConv(featureMatrix, 3, powerMatrix, 1);
            convResults.add(convResult);
            Matrix outMatrix = convResult.getResultMatrix();
            int sub = 3 - 1;
            int mySize = matrixSize - sub;//线性变换后矩阵的行数 （图片长度-（核长-步长））/步长
            //位置重新摆正
            outMatrix = rePosition(outMatrix, mySize);
            //批量归一化 处理
            Matrix normMatrix = matrixNorm.norm(outMatrix);
            // 这个地方要决定是否有跳层
            if (resFeatureList != null) {//与残差相加
                Matrix resFeatureMatrix = resFeatureList.get(i);
                normMatrix = matrixOperation.add(resFeatureMatrix, normMatrix);
            }
            // ReLu
            Matrix myOutMatrix = reluMatrix(normMatrix, reLu);
            outMatrixList.add(myOutMatrix);
        }
        return outMatrixList;
    }

    //第一层
    protected List<Matrix> downConvMany(List<Matrix> featureList, List<Matrix> powerMatrixList, int kerSize, boolean study
            , BackParameter backParameter, List<MatrixNorm> matrixNormList) throws Exception {//多维度卷积运算
        int size = powerMatrixList.size();//卷积通道数
        int featureSize = featureList.size();//特征通道数
        List<List<ConvResult>> convResultList = new ArrayList<>();
        ReLu reLu = new ReLu();
        List<Matrix> outMatrixList = new ArrayList<>();
        if (study) {
            backParameter.setConvResultList(convResultList);
            backParameter.setOutMatrixList(outMatrixList);
        }
        int matrixSize = 0;
        List<Matrix> myFeatureList = new ArrayList<>();
        for (int j = 0; j < featureSize; j++) {//进行padding操作
            Matrix featureMatrix;
            //这个地方决定是否要padding
            featureMatrix = padding2(featureList.get(j), kerSize - 2);
            if (j == 0) {
                matrixSize = featureMatrix.getX();
                backParameter.setIm2clSize(matrixSize);
            }
            myFeatureList.add(featureMatrix);
        }
        for (int i = 0; i < size; i++) {
            Matrix powerMatrix = powerMatrixList.get(i);//卷积
            MatrixNorm matrixNorm = matrixNormList.get(i);
            Matrix addMatrix = null;
            List<ConvResult> convResults = new ArrayList<>();
            convResultList.add(convResults);
            for (int j = 0; j < featureSize; j++) {
                Matrix featureMatrix = myFeatureList.get(j);
                ConvResult convResult = downConv(featureMatrix, kerSize, powerMatrix, 2);
                convResults.add(convResult);
                Matrix outMatrix = convResult.getResultMatrix();
                if (addMatrix == null) {
                    addMatrix = outMatrix.copy();
                } else {
                    addMatrix = matrixOperation.add(addMatrix, outMatrix);
                }
            }
            int sub = kerSize - 2;
            int mySize = (matrixSize - sub) / 2;//线性变换后矩阵的行数 （图片长度-（核长-步长））/步长
            //位置重新摆正
            addMatrix = rePosition(addMatrix, mySize);
            //批量归一化 处理
            Matrix normMatrix = matrixNorm.norm(addMatrix);
            // ReLu
            Matrix outMatrix = reluMatrix(normMatrix, reLu);
            outMatrixList.add(outMatrix);
        }
        return outMatrixList;
    }

    protected void ResBlockError(List<Matrix> errorMatrixList, BackParameter backParameter, List<MatrixNorm> matrixNormList
            , List<Matrix> powerMatrixList, float studyRate, int kernSize) throws Exception {
        ReLu reLu = new ReLu();
        int size = errorMatrixList.size();
        List<Matrix> outMatrixList = backParameter.getOutMatrixList();
        int im2calSize = backParameter.getIm2clSize();
        List<List<ConvResult>> convResultList = backParameter.getConvResultList();
        List<Matrix> resErrorMatrixList = new ArrayList<>();//残差误差
        for (int i = 0; i < size; i++) {
            Matrix errorMatrix = errorMatrixList.get(i);
            Matrix outMatrix = outMatrixList.get(i);
            MatrixNorm matrixNorm = matrixNormList.get(i);
            Matrix powerMatrix = powerMatrixList.get(i);
            List<ConvResult> convResults = convResultList.get(i);
            Matrix errorRelu = backActive(errorMatrix, outMatrix, reLu);//脱掉第一层激活函数
            resErrorMatrixList.add(errorRelu);
            Matrix normError = matrixNorm.backError(errorRelu);//脱掉归一化误差

            // backDownConv(normError, )
        }
    }

    private ConvResult backDownConv(Matrix errorMatrix, Matrix im2col, Matrix nerveMatrix, float studyRate, int kernSize, int im2colSize
            , int step) throws Exception {
        //下采样卷积误差反向传播
        ConvResult convResult = new ConvResult();
        int x = errorMatrix.getX();
        int y = errorMatrix.getY();
        Matrix resultError = new Matrix(x * y, 1);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float error = errorMatrix.getNumber(i, j);
                resultError.setNub(y * i + j, 0, error);
            }
        }
        Matrix wSub = matrixOperation.matrixMulPd(resultError, im2col, nerveMatrix, false);
        Matrix im2colSub = matrixOperation.matrixMulPd(resultError, im2col, nerveMatrix, true);
        matrixOperation.mathMul(wSub, studyRate);
        Matrix gNext = matrixOperation.reverseIm2col(im2colSub, kernSize, step, im2colSize, im2colSize);//其余误差
        convResult.setNervePowerMatrix(wSub);
        convResult.setResultMatrix(gNext);
        return convResult;
    }

    private Matrix backActive(Matrix errorMatrix, Matrix outMatrix, ActiveFunction activeFunction) throws Exception {
        //下采样卷积误差反向传播
        int x = errorMatrix.getX();
        int y = errorMatrix.getY();
        Matrix resultError = new Matrix(x, y);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float error = errorMatrix.getNumber(i, j);
                float out = outMatrix.getNumber(i, j);
                error = error * activeFunction.functionG(out);
                resultError.setNub(i, j, error);
            }
        }
        return resultError;
    }

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

    protected Matrix padding2(Matrix matrix, int paddingSize) throws Exception {
        int xSize = matrix.getX();
        int ySize = matrix.getY();
        int x = xSize + paddingSize;
        int y = ySize + paddingSize;
        Matrix outMatrix = new Matrix(x, y);
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                outMatrix.setNub(i, j, matrix.getNumber(i, j));
            }
        }
        return outMatrix;
    }

    private Matrix padding(Matrix matrix) throws Exception {//padding
        int x = matrix.getX() + 2;
        int y = matrix.getY() + 2;
        Matrix outMatrix = new Matrix(x, y);
        for (int i = 1; i < x - 1; i++) {
            for (int j = 1; j < y - 1; j++) {
                outMatrix.setNub(i, j, matrix.getNumber(i - 1, j - 1));
            }
        }
        return outMatrix;
    }

    private Matrix rePosition(Matrix matrixOut, int mySize) throws Exception {//重新摆正位置
        Matrix myMatrix = new Matrix(mySize, mySize);//线性变化后的矩阵
        for (int i = 0; i < mySize; i++) {
            for (int j = 0; j < mySize; j++) {
                float nub = matrixOut.getNumber(i * mySize + j, 0);
                myMatrix.setNub(i, j, nub);
            }
        }
        return myMatrix;
    }

    private Matrix reluMatrix(Matrix matrixOut, ActiveFunction activeFunction) throws Exception {
        int x = matrixOut.getX();
        int y = matrixOut.getY();
        Matrix myMatrix = new Matrix(x, y);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float nub = activeFunction.function(matrixOut.getNumber(i, j));
                myMatrix.setNub(i, j, nub);
            }
        }
        return myMatrix;
    }

    private ConvResult downConv(Matrix matrix, int kerSize, Matrix nervePowerMatrix, int step) throws Exception {//进行下采样卷积运算
        ConvResult convResult = new ConvResult();
        Matrix im2col = matrixOperation.im2col(matrix, kerSize, step);
        convResult.setLeftMatrix(im2col);
        //输出矩阵
        Matrix matrixOut = matrixOperation.mulMatrix(im2col, nervePowerMatrix);//列向量
        convResult.setResultMatrix(matrixOut);
        return convResult;
    }
}
