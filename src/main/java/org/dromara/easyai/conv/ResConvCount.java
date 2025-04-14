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

    protected List<Matrix> downConvMany(List<Matrix> featureList, List<Matrix> powerMatrixList, int kerSize, int step, boolean study
            , BackParameter backParameter, MatrixNorm matrixNorm, List<Matrix> resFeatureList, List<List<Float>> oneConvPower) throws Exception {//多维度卷积运算
        //featureList resFeatureList特征默认进来就是偶数了
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
        if (resFeatureList != null && resFeatureList.size() != size) {//要通过1v1卷积层进行升维
            if (oneConvPower == null) {
                throw new Exception("1v1 卷积核空了");
            }
            resFeatureList = manyOneConv(resFeatureList, oneConvPower);
        }
        if (resFeatureList != null && resFeatureList.size() != size) {
            throw new Exception("残差与卷积核维度不相等");
        }
        List<Matrix> myFeatureList = new ArrayList<>();
        for (int j = 0; j < featureSize; j++) {//进行padding操作
            Matrix featureMatrix;
            //这个地方决定是否要padding
            if (step == 1) {
                featureMatrix = padding(featureList.get(j));
            } else {//步长为2
                featureMatrix = padding2(featureList.get(j), kerSize - step);
            }
            if (j == 0) {
                matrixSize = featureMatrix.getX();
            }
            myFeatureList.add(featureMatrix);
        }
        for (int i = 0; i < size; i++) {
            Matrix powerMatrix = powerMatrixList.get(i);//卷积
            Matrix addMatrix = null;
            List<ConvResult> convResults = new ArrayList<>();
            convResultList.add(convResults);
            for (int j = 0; j < featureSize; j++) {
                Matrix featureMatrix = myFeatureList.get(j);
                ConvResult convResult = downConv(featureMatrix, kerSize, powerMatrix, step);
                convResults.add(convResult);
                Matrix outMatrix = convResult.getResultMatrix();
                if (addMatrix == null) {
                    addMatrix = outMatrix.copy();
                } else {
                    addMatrix = matrixOperation.add(addMatrix, outMatrix);
                }
            }
            int sub = kerSize - step;
            int mySize = (matrixSize - sub) / step;//线性变换后矩阵的行数 （图片长度-（核长-步长））/步长
            //位置重新摆正
            addMatrix = rePosition(addMatrix, mySize);
            //批量归一化 处理
            Matrix normMatrix = matrixNorm.norm(addMatrix);
            // 这个地方要决定是否有跳层
            if (resFeatureList != null) {//与残差相加
                Matrix resFeatureMatrix = resFeatureList.get(i);
                normMatrix = matrixOperation.add(resFeatureMatrix, normMatrix);
            }
            // ReLu
            Matrix outMatrix = reluMatrix(normMatrix, reLu);
            outMatrixList.add(outMatrix);
        }
        return outMatrixList;
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
