package org.dromara.easyai.conv;

import org.dromara.easyai.function.ReLu;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixNorm;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.resnet.entity.BackParameter;
import org.dromara.easyai.resnet.entity.ResnetError;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @time 2025/4/11 13:05
 * @des resnet卷积运算超类
 */
public abstract class ResConvCount {
    private final MatrixOperation matrixOperation = new MatrixOperation();
    private final DymStudy dymStudy = new DymStudy();

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
            if (deep == 1 && seven) {
                return x;
            }
            if (i == 0) {
                x = x + x % step;
                x = x / step;
            }
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

    protected List<Matrix> backDownPoolingByList(List<Matrix> matrixList) throws Exception {
        List<Matrix> result = new ArrayList<>();
        for (Matrix matrix : matrixList) {
            result.add(backDownPooling(matrix));
        }
        return result;
    }

    private Matrix backDownPooling(Matrix matrix) throws Exception {//退下池化
        int x = matrix.getX();
        int y = matrix.getY();
        Matrix myMatrix = new Matrix(x * 2, y * 2);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = matrix.getNumber(i, j) / 4;
                insertMatrixValue(i * 2, j * 2, value, myMatrix);
            }
        }
        return myMatrix;
    }

    private void insertMatrixValue(int x, int y, float value, Matrix matrix) throws Exception {
        int xSize = x + 2;
        int ySize = y + 2;
        for (int i = x; i < xSize; i++) {
            for (int j = y; j < ySize; j++) {
                matrix.setNub(i, j, value);
            }
        }
    }


    protected List<Matrix> backOneConvByList(List<Matrix> errorMatrixList, List<Matrix> matrixList, List<List<Float>> oneConvPower, float studyRate
            , List<List<Float>> dymStudyList) throws Exception {
        int size = errorMatrixList.size();//addMatrixList
        if (size == oneConvPower.size()) {
            List<Matrix> allErrorMatrixList = null;
            for (int i = 0; i < size; i++) {
                Matrix errorMatrix = errorMatrixList.get(i);
                List<Float> oneConvPowerList = oneConvPower.get(i);
                List<Float> dymStudy = dymStudyList.get(i);
                List<Matrix> nextErrorMatrixList = backOneConv(errorMatrix, matrixList, oneConvPowerList, studyRate, dymStudy);
                if (i == 0) {
                    allErrorMatrixList = nextErrorMatrixList;
                } else {
                    allErrorMatrixList = matrixOperation.addMatrixList(allErrorMatrixList, nextErrorMatrixList);
                }
            }
            //退下池化
            for (int i = 0; i < allErrorMatrixList.size(); i++) {
                Matrix errorMatrix = allErrorMatrixList.get(i);
                allErrorMatrixList.set(i, backDownPooling(errorMatrix));
            }
            return allErrorMatrixList;
        } else {
            throw new Exception("误差矩阵大小与通道数不相符");
        }

    }

    protected List<Matrix> backOneConv(Matrix errorMatrix, List<Matrix> matrixList, List<Float> oneConvPower, float studyRate
            , List<Float> sList) throws Exception {//单卷积降维回传
        int size = oneConvPower.size();
        List<Matrix> nextErrorMatrixList = new ArrayList<>();
        float gaMa = 0.9f;
        for (int t = 0; t < size; t++) {
            Matrix myMatrix = matrixList.get(t);
            int x = myMatrix.getX();
            int y = myMatrix.getY();
            Matrix errorMyMatrix = new Matrix(x, y);
            nextErrorMatrixList.add(errorMyMatrix);
            float power = oneConvPower.get(t);
            float s = sList.get(t);
            float allSubPower = 0;
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    float error = errorMatrix.getNumber(i, j);
                    float subPower = myMatrix.getNumber(i, j) * error;
                    float subG = power * error;
                    allSubPower = allSubPower + subPower;
                    errorMyMatrix.setNub(i, j, subG);
                }
            }
            float sNext = gaMa * s + (1 - gaMa) * (float) Math.pow(allSubPower, 2);
            float myStudyRate = studyRate / (float) Math.sqrt(sNext + 0.00000001f);
            sList.set(t, sNext);
            power = power + allSubPower * myStudyRate;
            oneConvPower.set(t, power);
        }
        return nextErrorMatrixList;
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
            //对残差进行池化后保存
            List<Matrix> poolingMatrixList = new ArrayList<>();
            if (study) {
                backParameter.setScaleMatrixList(poolingMatrixList);
            }
            for (Matrix matrix : resFeatureList) {
                poolingMatrixList.add(downPooling(matrix));
            }
            resFeatureList = manyOneConv(poolingMatrixList, oneConvPower);
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

    protected List<Matrix> ResBlockError(List<Matrix> errorMatrixList, BackParameter backParameter, List<MatrixNorm> matrixNormList
            , List<Matrix> powerMatrixList, float studyRate, int kernSize, List<Matrix> resErrorMatrix
            , List<Matrix> sMatrixList) throws Exception {
        ReLu reLu = new ReLu();
        int size = errorMatrixList.size();
        List<Matrix> outMatrixList = backParameter.getOutMatrixList();
        List<List<ConvResult>> convResultList = backParameter.getConvResultList();
        int im2calSize = backParameter.getIm2clSize();
        List<Matrix> nextAllErrorMatrixList = null;//要回传的误差
        for (int i = 0; i < size; i++) {
            Matrix errorMatrix = errorMatrixList.get(i);
            Matrix outMatrix = outMatrixList.get(i);
            MatrixNorm matrixNorm = matrixNormList.get(i);
            List<ConvResult> convResults = convResultList.get(i);
            Matrix powerMatrix = powerMatrixList.get(i);//卷积核
            Matrix sMatrix = sMatrixList.get(i);
            Matrix errorRelu = backActive(errorMatrix, outMatrix, reLu);//脱掉第一层激活函数
            Matrix normError = matrixNorm.backError(errorRelu);//脱掉归一化误差
            List<Matrix> nextErrorMatrixList = new ArrayList<>();
            Matrix nerveAllError = null;//权重总误差
            for (ConvResult convResult : convResults) {
                ConvResult myConvResult = backDownConv(normError, convResult.getLeftMatrix(), powerMatrix, studyRate, kernSize,
                        im2calSize, 2, sMatrix);
                Matrix subPower = myConvResult.getNervePowerMatrix();
                Matrix nextErrorMatrix = myConvResult.getResultMatrix();
                nextErrorMatrixList.add(unPadding2(nextErrorMatrix, kernSize - 2));
                if (nerveAllError == null) {
                    nerveAllError = subPower;
                } else {
                    nerveAllError = matrixOperation.add(nerveAllError, subPower);
                }
            }
            powerMatrixList.set(i, matrixOperation.add(powerMatrix, nerveAllError));//更新权重
            if (nextAllErrorMatrixList == null) {
                nextAllErrorMatrixList = nextErrorMatrixList;
            } else {
                nextAllErrorMatrixList = matrixOperation.addMatrixList(nextAllErrorMatrixList, nextErrorMatrixList);
            }
        }
        if (resErrorMatrix != null) {
            return matrixOperation.addMatrixList(nextAllErrorMatrixList, resErrorMatrix);
        }
        return nextAllErrorMatrixList;
    }

    protected ResnetError ResBlockError2(List<Matrix> errorMatrixList, BackParameter backParameter, List<MatrixNorm> matrixNormList
            , List<Matrix> powerMatrixList, float studyRate, boolean resError, List<List<Float>> oneConvPower, List<Matrix> resErrorMatrix
            , List<Matrix> sMatrixList, List<List<Float>> dymStudyRateList) throws Exception {
        ResnetError resnetError = new ResnetError();
        ReLu reLu = new ReLu();
        int size = errorMatrixList.size();
        List<Matrix> outMatrixList = backParameter.getOutMatrixList();
        int im2calSize = backParameter.getIm2clSize();
        List<ConvResult> convResultList = backParameter.getConvResults();
        List<Matrix> resErrorMatrixList = new ArrayList<>();//残差误差
        List<Matrix> nextErrorMatrixList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Matrix errorMatrix = errorMatrixList.get(i);
            Matrix outMatrix = outMatrixList.get(i);
            MatrixNorm matrixNorm = matrixNormList.get(i);
            Matrix powerMatrix = powerMatrixList.get(i);//卷积核
            ConvResult convResult = convResultList.get(i);
            Matrix sMatrix = sMatrixList.get(i);
            Matrix errorRelu = backActive(errorMatrix, outMatrix, reLu);//脱掉第一层激活函数
            if (resError) {
                resErrorMatrixList.add(errorRelu);
            }
            Matrix normError = matrixNorm.backError(errorRelu);//脱掉归一化误差
            ConvResult myConvResult = backDownConv(normError, convResult.getLeftMatrix(), powerMatrix, studyRate, 3, im2calSize, 1
                    , sMatrix);
            powerMatrix = matrixOperation.add(powerMatrix, myConvResult.getNervePowerMatrix());
            powerMatrixList.set(i, powerMatrix);//更新卷积核
            Matrix nextErrorMatrix = unPadding(myConvResult.getResultMatrix());
            nextErrorMatrixList.add(nextErrorMatrix);
        }
        if (resError && oneConvPower != null) {
            resErrorMatrixList = backOneConvByList(resErrorMatrixList, backParameter.getScaleMatrixList(), oneConvPower, studyRate, dymStudyRateList);
        } else if (!resError) {//残差与误差求和
            nextErrorMatrixList = matrixOperation.addMatrixList(nextErrorMatrixList, resErrorMatrix);
        }
        resnetError.setNextErrorMatrixList(nextErrorMatrixList);
        resnetError.setResErrorMatrixList(resErrorMatrixList);
        return resnetError;
    }

    private ConvResult backDownConv(Matrix errorMatrix, Matrix im2col, Matrix nerveMatrix, float studyRate, int kernSize, int im2colSize
            , int step, Matrix sMatrix) throws Exception {
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
        wSub = dymStudy.getErrorMatrixByStudy(studyRate, sMatrix, wSub);
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

    protected Matrix unPadding2(Matrix matrix, int paddingSize) {
        int x = matrix.getX() - paddingSize;
        int y = matrix.getY() - paddingSize;
        return matrix.getSonOfMatrix(0, 0, x, y);
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

    private Matrix unPadding(Matrix matrix) {
        int x = matrix.getX();
        int y = matrix.getY();
        return matrix.getSonOfMatrix(1, 1, x - 2, y - 2);
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
