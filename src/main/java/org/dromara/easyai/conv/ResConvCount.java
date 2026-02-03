package org.dromara.easyai.conv;

import org.dromara.easyai.function.ReLu;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixNorm;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.resnet.entity.BackParameter;
import org.dromara.easyai.resnet.entity.BatchBody;
import org.dromara.easyai.resnet.entity.ResnetError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    protected List<List<Matrix>> backOneConvErrorMatrix(List<Matrix> errorMatrixList, List<List<Matrix>> resMatrixList, List<Float> oneConvPower,
                                                        float studyRate, List<Float> dymStudyList,
                                                        List<Float> dymStudy2List, DymStudy dym, int times) throws Exception {
        int size = errorMatrixList.size();//该通道的所有图像的残差误差
        int ds = oneConvPower.size();//1*1卷积核纬度
        Map<Integer, Float> dMap = new HashMap<>();//主键是1*1卷积核纬度 值是平均梯度矩阵
        List<List<Matrix>> channelNextErrorMatrixList = new ArrayList<>();//所有图像在该通道下的下一层残差误差
        for (int a = 0; a < size; a++) {//遍历该通道的所有图像特征
            Matrix errorMatrix = errorMatrixList.get(a);//该通道每个图像的残差误差
            List<Matrix> matrixList = resMatrixList.get(a);//该通道每个图像的残差入参特征矩阵集合
            List<Matrix> nextErrorMatrixList = new ArrayList<>();//该通道下一层所有纬度的误差矩阵集合
            channelNextErrorMatrixList.add(nextErrorMatrixList);
            for (int b = 0; b < ds; b++) {//遍历每个1*1卷积核的纬度
                float power = oneConvPower.get(b);//1*1卷积核每个纬度的权重系数
                Matrix myMatrix = matrixList.get(b);//1*1卷积核每个纬度对应的上一层输出的残差
                int x = myMatrix.getX();
                int y = myMatrix.getY();
                Matrix errorMyMatrix = new Matrix(x, y);//当前纬度的下一层残差误差
                nextErrorMatrixList.add(errorMyMatrix);
                float allSubPower = 0;
                for (int i = 0; i < x; i++) {
                    for (int j = 0; j < y; j++) {
                        float error = errorMatrix.getNumber(i, j);
                        float gPower = myMatrix.getNumber(i, j) * error;
                        float subG = power * error;
                        allSubPower = allSubPower + gPower;
                        errorMyMatrix.setNub(i, j, subG);
                    }
                }
                if (dMap.containsKey(b)) {
                    dMap.put(b, dMap.get(b) + allSubPower);
                } else {
                    dMap.put(b, allSubPower);
                }
            }
        }
        for (Map.Entry<Integer, Float> entry : dMap.entrySet()) {
            int key = entry.getKey();
            float gPower = entry.getValue() / size;//平均梯度
            float power = oneConvPower.get(key);//1*1卷积核每个纬度的权重系数
            float error = dym.getErrorValueByStudy(studyRate, dymStudyList, dymStudy2List, gPower, key, times);
            power = power + error;
            oneConvPower.set(key, power);
        }
        return channelNextErrorMatrixList;
    }

    //不是第一层
    protected void downConvMany2(List<BatchBody> batchBodies, List<Matrix> powerMatrixList, boolean study, List<BackParameter> backParameterList,
                                 List<MatrixNorm> matrixNormList, List<BatchBody> resBatchBody, List<List<Float>> oneConvPower) throws Exception {//多维度卷积运算
        //featureList resFeatureList特征默认进来就是偶数了
        List<Matrix> firstMulMatrixList = new ArrayList<>();
        List<Matrix> secondMulMatrixList = new ArrayList<>();
        List<List<ConvResult>> allConvResultList = new ArrayList<>();
        List<List<Matrix>> allOutMatrixList = new ArrayList<>();
        List<List<Matrix>> allResFeatureList = new ArrayList<>();
        ReLu reLu = new ReLu();
        int size = powerMatrixList.size();//卷积通道数
        int matrixSize = 0;
        for (int m = 0; m < batchBodies.size(); m++) {
            BatchBody batchBody = batchBodies.get(m);
            List<Matrix> featureList = batchBody.getFeatureList();
            BackParameter backParameter = backParameterList.get(m);
            int featureSize = featureList.size();//特征通道数
            if (size != featureSize) {
                throw new Exception("卷积通道数与特征通道数不一致");
            }
            List<ConvResult> convResults = new ArrayList<>();
            List<Matrix> outMatrixList = new ArrayList<>();
            allConvResultList.add(convResults);
            allOutMatrixList.add(outMatrixList);
            if (study) {
                backParameter.setConvResults(convResults);
                backParameter.setOutMatrixList(outMatrixList);
            }
            List<Matrix> resFeatureList = null;
            if (resBatchBody != null) {
                resFeatureList = resBatchBody.get(m).getFeatureList();
            }
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
                if (resFeatureList.size() != size) {
                    throw new Exception("残差与卷积核维度不相等");
                }
            }
            if (resFeatureList != null) {
                allResFeatureList.add(resFeatureList);
            }
            for (int i = 0; i < size; i++) {
                ConvResult convResult = new ConvResult();
                Matrix powerMatrix = powerMatrixList.get(i);//卷积
                Matrix featureMatrix = padding(featureList.get(i));
                if (i == 0) {
                    matrixSize = featureMatrix.getX();
                    backParameter.setIm2clSize(matrixSize);
                }
                Matrix im2col = matrixOperation.im2col(featureMatrix, 3, 1);
                convResult.setLeftMatrix(im2col);
                firstMulMatrixList.add(im2col);
                secondMulMatrixList.add(powerMatrix);
                convResults.add(convResult);
            }
        }
        List<Matrix> resultMatrixList = matrixOperation.mulMatrixList(firstMulMatrixList, secondMulMatrixList);
        for (int m = 0; m < batchBodies.size(); m++) {
            int startIndex = m * size;
            int endIndex = startIndex + size;
            List<Matrix> result = resultMatrixList.subList(startIndex, endIndex);
            BatchBody batchBody = batchBodies.get(m);
            List<ConvResult> convResults = allConvResultList.get(m);
            List<Matrix> outMatrixList = allOutMatrixList.get(m);
            List<Matrix> resFeatureList = null;
            if (!allResFeatureList.isEmpty()) {
                resFeatureList = allResFeatureList.get(m);
            }
            for (int i = 0; i < size; i++) {
                MatrixNorm matrixNorm = matrixNormList.get(i);
                ConvResult convResult = convResults.get(i);
                Matrix outMatrix = result.get(i);
                convResult.setResultMatrix(outMatrix);
                int sub = 3 - 1;
                int mySize = matrixSize - sub;//线性变换后矩阵的行数 （图片长度-（核长-步长））/步长
                //位置重新摆正
                Matrix outMatrixRe = rePosition(outMatrix, mySize);
                //批量归一化 处理
                Matrix normMatrix = matrixNorm.norm(outMatrixRe, m);
                // 这个地方要决定是否有跳层
                if (resFeatureList != null) {//与残差相加
                    Matrix resFeatureMatrix = resFeatureList.get(i);
                    normMatrix = matrixOperation.add(resFeatureMatrix, normMatrix);
                }
                // ReLu
                Matrix myOutMatrix = reluMatrix(normMatrix, reLu);
                outMatrixList.add(myOutMatrix);
            }
            batchBody.setFeatureList(outMatrixList);
        }
    }

    //第一层
    protected void downConvMany(List<BatchBody> batchBodies, List<Matrix> powerMatrixList, int kerSize, boolean study
            , List<BackParameter> backParameterList, List<MatrixNorm> matrixNormList) throws Exception {//多维度卷积运算
        int batchSize = batchBodies.size();
        int size = powerMatrixList.size();//卷积通道数
        ReLu reLu = new ReLu();
        int matrixSize = 0;
        List<Matrix> firstMulMatrix = new ArrayList<>();
        List<Matrix> secondMulMatrix = new ArrayList<>();
        List<List<Matrix>> allFeatureList = new ArrayList<>();
        int featureSize = 0;
        for (int m = 0; m < batchSize; m++) {
            BatchBody batchBody = batchBodies.get(m);
            List<Matrix> featureList = batchBody.getFeatureList();
            BackParameter backParameter = backParameterList.get(m);
            featureSize = featureList.size();//特征通道数
            List<Matrix> myFeatureList = new ArrayList<>();
            allFeatureList.add(myFeatureList);
            for (int j = 0; j < featureSize; j++) {//进行padding操作
                Matrix featureMatrix;
                //这个地方决定是否要padding
                featureMatrix = padding2(featureList.get(j), kerSize - 2);
                if (j == 0) {
                    matrixSize = featureMatrix.getX();
                    backParameter.setIm2clSize(matrixSize);
                }
                Matrix im2col = matrixOperation.im2col(featureMatrix, kerSize, 2);
                //进行变换
                myFeatureList.add(im2col);
            }
            for (int i = 0; i < size; i++) {
                Matrix powerMatrix = powerMatrixList.get(i);//卷积
                for (int j = 0; j < featureSize; j++) {
                    Matrix featureMatrix = myFeatureList.get(j);
                    firstMulMatrix.add(featureMatrix);
                    secondMulMatrix.add(powerMatrix);
                }
            }
        }
        List<Matrix> resultMatrixList = matrixOperation.mulMatrixList(firstMulMatrix, secondMulMatrix);
        for (int m = 0; m < batchSize; m++) {
            List<Matrix> myFeatureList = allFeatureList.get(m);
            int startIndex = m * size * featureSize;
            int endIndex = startIndex + size * featureSize;
            List<Matrix> result = resultMatrixList.subList(startIndex, endIndex);
            BatchBody batchBody = batchBodies.get(m);
            BackParameter backParameter = backParameterList.get(m);
            List<List<ConvResult>> convResultList = new ArrayList<>();
            List<Matrix> outMatrixList = new ArrayList<>();
            if (study) {
                backParameter.setConvResultList(convResultList);
                backParameter.setOutMatrixList(outMatrixList);
            }
            for (int i = 0; i < size; i++) {
                MatrixNorm matrixNorm = matrixNormList.get(i);
                Matrix addMatrix = null;
                List<ConvResult> convResults = new ArrayList<>();
                convResultList.add(convResults);
                for (int j = 0; j < featureSize; j++) {
                    Matrix featureMatrix = myFeatureList.get(j);
                    Matrix outMatrix = result.get(i * featureSize + j);
                    ConvResult convResult = new ConvResult();
                    convResult.setLeftMatrix(featureMatrix);
                    convResult.setResultMatrix(outMatrix);
                    convResults.add(convResult);
                    if (addMatrix == null) {
                        addMatrix = outMatrix;
                    } else {
                        addMatrix = matrixOperation.add(addMatrix, outMatrix);
                    }
                }
                int sub = kerSize - 2;
                int mySize = (matrixSize - sub) / 2;//线性变换后矩阵的行数 （图片长度-（核长-步长））/步长
                //位置重新摆正
                addMatrix = rePosition(addMatrix, mySize);
                //批量归一化 处理
                Matrix normMatrix = matrixNorm.norm(addMatrix, m);
                // ReLu
                Matrix outMatrix = reluMatrix(normMatrix, reLu);
                outMatrixList.add(outMatrix);
            }
            batchBody.setFeatureList(outMatrixList);
        }
    }

    private void reColMatrix(List<Matrix> gResultMatrixList, int kernSize, int im2calSize) throws Exception {
        for (int i = 0; i < gResultMatrixList.size(); i++) {
            Matrix gErrorMatrix = gResultMatrixList.get(i);//上一层误差矩阵 ture
            Matrix nextErrorMatrix = matrixOperation.reverseIm2col(gErrorMatrix, kernSize, 2, im2calSize, im2calSize);//其余误差
            Matrix paddingErrorMatrix = unPadding2(nextErrorMatrix, kernSize - 2);
            gResultMatrixList.set(i, paddingErrorMatrix);
        }
    }

    protected List<BatchBody> ResBlockError(List<BatchBody> batchBodies, List<BackParameter> backParameterList,
                                            List<MatrixNorm> matrixNormList, List<Matrix> powerMatrixList,
                                            float studyRate, int kernSize, List<Matrix> sMatrixList, List<Matrix> s2MatrixList,
                                            DymStudy dymStudy, int times, boolean rz, float rzRate) throws Exception {
        ReLu reLu = new ReLu();
        Map<Integer, List<Matrix>> reluMatrixMap = new HashMap<>();//所有脱掉Relu 的图片的误差 主键是通道id，值是所有图片该通道的误差矩阵集合
        List<Matrix> nextAllErrorMatrixList = null;//要回传的误差
        List<Matrix> gFirstMulMatrix = new ArrayList<>();
        List<Matrix> gSecondMulMatrix = new ArrayList<>();
        List<Matrix> pFirstMulMatrix = new ArrayList<>();
        List<Matrix> pSecondMulMatrix = new ArrayList<>();
        int channelSize = 0;
        int im2calSize = 0;
        int pictureSize = batchBodies.size();
        for (int m = 0; m < pictureSize; m++) {
            BatchBody batchBody = batchBodies.get(m);
            List<Matrix> errorMatrixList = batchBody.getFeatureList();
            BackParameter backParameter = backParameterList.get(m);
            channelSize = errorMatrixList.size();
            List<Matrix> outMatrixList = backParameter.getOutMatrixList();
            im2calSize = backParameter.getIm2clSize();
            for (int i = 0; i < channelSize; i++) {//遍历当前层卷积通道数
                Matrix errorMatrix = errorMatrixList.get(i);
                Matrix outMatrix = outMatrixList.get(i);
                Matrix errorRelu = backActive(errorMatrix, outMatrix, reLu);//脱掉第一层激活函数
                if (reluMatrixMap.containsKey(i)) {
                    reluMatrixMap.get(i).add(errorRelu);
                } else {
                    List<Matrix> reluMatrixList = new ArrayList<>();
                    reluMatrixList.add(errorRelu);
                    reluMatrixMap.put(i, reluMatrixList);
                }
            }
        }
        int convResultSize = 0;
        for (int i = 0; i < channelSize; i++) {
            Matrix powerMatrix = powerMatrixList.get(i);//卷积核
            Matrix tpPowerMatrix = matrixOperation.transPosition(powerMatrix);//权重的转制矩阵
            List<Matrix> errorReluList = reluMatrixMap.get(i);
            MatrixNorm matrixNorm = matrixNormList.get(i);
            List<Matrix> normError = matrixNorm.backError(errorReluList);//脱掉归一化误差
            errorMatrixToVectorByList(normError);//误差向量
            gFirstMulMatrix.addAll(normError);
            for (int m = 0; m < pictureSize; m++) {
                BackParameter backParameter = backParameterList.get(m);
                List<List<ConvResult>> convResultList = backParameter.getConvResultList();
                List<ConvResult> convResults = convResultList.get(i);
                convResultSize = convResults.size();
                gSecondMulMatrix.add(tpPowerMatrix);//true
                Matrix errorVector = normError.get(m);
                for (ConvResult convResult : convResults) {
                    Matrix im2col = convResult.getLeftMatrix();
                    Matrix tpIm2col = matrixOperation.transPosition(im2col);
                    pFirstMulMatrix.add(tpIm2col);
                    pSecondMulMatrix.add(errorVector);//false
                }
            }
        }
        List<Matrix> gResultMatrixList = matrixOperation.mulMatrixList(gFirstMulMatrix, gSecondMulMatrix);
        List<Matrix> pResultMatrixList = matrixOperation.mulMatrixList(pFirstMulMatrix, pSecondMulMatrix);
        reColMatrix(gResultMatrixList, kernSize, im2calSize);
        for (int i = 0; i < channelSize; i++) {
            int gStartIndex = i * pictureSize;
            int gEndIndex = gStartIndex + pictureSize;
            int pStartIndex = gStartIndex * convResultSize;
            int pEndIndex = pStartIndex + pictureSize * convResultSize;
            Matrix sMatrix = sMatrixList.get(i);
            Matrix s2Matrix = s2MatrixList.get(i);
            Matrix powerMatrix = powerMatrixList.get(i);//卷积核
            List<Matrix> gErrorMatrixList = gResultMatrixList.subList(gStartIndex, gEndIndex);//每个通道的所有图的误差
            List<Matrix> pErrorMatrixList = pResultMatrixList.subList(pStartIndex, pEndIndex);//每个通道所有图所有权重波动矩阵
            Matrix channelSubPower = null;
            for (int m = 0; m < pictureSize; m++) {//遍历所有图
                int picStartIndex = m * convResultSize;
                int picEndIndex = picStartIndex + convResultSize;
                Matrix nerveAllError = null;//一张图的权重总误差
                List<Matrix> pErrors = pErrorMatrixList.subList(picStartIndex, picEndIndex);
                for (Matrix wSubMatrix : pErrors) {
                    if (nerveAllError == null) {
                        nerveAllError = wSubMatrix;
                    } else {
                        nerveAllError = matrixOperation.add(nerveAllError, wSubMatrix);
                    }
                }
                if (channelSubPower == null) {
                    channelSubPower = nerveAllError;
                } else {
                    channelSubPower = matrixOperation.add(channelSubPower, nerveAllError);
                }
            }
            matrixOperation.mathDiv(channelSubPower, pictureSize);
            Matrix subPower = dymStudy.getErrorMatrixByStudy(studyRate, sMatrix, s2Matrix, channelSubPower, times);
            powerMatrix = matrixOperation.add(powerMatrix, subPower);
            if (rz) {
                dymStudy.regMatrix(powerMatrix, rzRate, studyRate);
            }
            powerMatrixList.set(i, powerMatrix);//更新权重
            if (nextAllErrorMatrixList == null) {
                nextAllErrorMatrixList = gErrorMatrixList;
            } else {
                nextAllErrorMatrixList = matrixOperation.addMatrixList(nextAllErrorMatrixList, gErrorMatrixList);
            }
        }
        List<BatchBody> nextBatchBodies = new ArrayList<>();
        for (int m = 0; m < batchBodies.size(); m++) {
            BatchBody batchBody = batchBodies.get(m);
            ResnetError resnetError = batchBody.getResnetError();
            BatchBody myBatchBody = new BatchBody();
            nextBatchBodies.add(myBatchBody);
            List<Matrix> errorMatrixList = new ArrayList<>();
            myBatchBody.setFeatureList(errorMatrixList);
            Matrix errorMatrix = nextAllErrorMatrixList.get(m);
            for (int i = 0; i < convResultSize; i++) {
                errorMatrixList.add(errorMatrix.copy());
            }
            if (resnetError != null && resnetError.getResErrorMatrixList() != null) {
                List<Matrix> resErrorMatrix = resnetError.getResErrorMatrixList();
                errorMatrixList = matrixOperation.addMatrixList(errorMatrixList, resErrorMatrix);
                myBatchBody.setFeatureList(errorMatrixList);
            }
        }
        clipErrorMatrixList(nextBatchBodies, dymStudy);
        return nextBatchBodies;
    }

    protected List<BatchBody> ResBlockError2(List<BatchBody> batchBodies, List<BackParameter> backParameterList, List<MatrixNorm> matrixNormList
            , List<Matrix> powerMatrixList, float studyRate, boolean resError, List<List<Float>> oneConvPower
            , List<Matrix> sMatrixList, List<Matrix> s2MatrixList, List<List<Float>> dymStudyRateList,
                                             List<List<Float>> dymStudyRate2List, DymStudy dymStudy,
                                             int times, boolean rz, float rzRate) throws Exception {
        ReLu reLu = new ReLu();
        Map<Integer, List<Matrix>> reluMatrixMap = new HashMap<>();//所有脱掉Relu 的图片的误差 主键是通道id，值是所有图片该通道的误差矩阵集合
        Map<Integer, List<Matrix>> tpIm2colMatrixMap = new HashMap<>();//所有左乘矩阵 主键是通道id，值是所有图片该通道的左乘矩阵集合
        int channelSize = 0;
        int im2calSize = 0;
        List<Matrix> gFirstMulMatrix = new ArrayList<>();
        List<Matrix> gSecondMulMatrix = new ArrayList<>();
        List<Matrix> pFirstMulMatrix = new ArrayList<>();
        List<Matrix> pSecondMulMatrix = new ArrayList<>();
        List<ResnetError> resnetErrors = new ArrayList<>();
        for (int m = 0; m < batchBodies.size(); m++) {
            BatchBody batchBody = batchBodies.get(m);
            List<Matrix> errorMatrixList = batchBody.getFeatureList();
            BackParameter backParameter = backParameterList.get(m);
            ResnetError resnetError = new ResnetError();//误差集合
            resnetErrors.add(resnetError);
            channelSize = errorMatrixList.size();//卷积通道数
            List<Matrix> outMatrixList = backParameter.getOutMatrixList();
            im2calSize = backParameter.getIm2clSize();
            List<ConvResult> convResultList = backParameter.getConvResults();
            List<Matrix> resErrorMatrixList = new ArrayList<>();//残差误差
            for (int i = 0; i < channelSize; i++) {
                Matrix errorMatrix = errorMatrixList.get(i);//每一层通道的误差
                Matrix outMatrix = outMatrixList.get(i);//每一层通道的输出矩阵
                ConvResult convResult = convResultList.get(i);//每一层通道的参数
                Matrix errorRelu = backActive(errorMatrix, outMatrix, reLu);//脱掉第一层激活函数
                Matrix im2col = convResult.getLeftMatrix();
                Matrix tpIm2col = matrixOperation.transPosition(im2col);
                if (reluMatrixMap.containsKey(i)) {
                    reluMatrixMap.get(i).add(errorRelu);
                    tpIm2colMatrixMap.get(i).add(tpIm2col);
                } else {
                    List<Matrix> reluMatrixList = new ArrayList<>();
                    List<Matrix> im2colMatrixList = new ArrayList<>();
                    reluMatrixList.add(errorRelu);
                    im2colMatrixList.add(tpIm2col);
                    reluMatrixMap.put(i, reluMatrixList);
                    tpIm2colMatrixMap.put(i, im2colMatrixList);
                }
                if (resError) {
                    resErrorMatrixList.add(errorRelu);
                }
            }
            resnetError.setResErrorMatrixList(resErrorMatrixList);
        }
        int errorSize = 0;
        for (int i = 0; i < channelSize; i++) {//遍历卷积通道
            List<Matrix> matrixList = reluMatrixMap.get(i);//该通道所有特征误差
            List<Matrix> tpIm2colMatrixList = tpIm2colMatrixMap.get(i);//该通道下所有图片的左乘矩阵集合
            MatrixNorm matrixNorm = matrixNormList.get(i);//每一层通道的批量归一化层
            Matrix powerMatrix = powerMatrixList.get(i);//卷积核
            Matrix tpPowerMatrix = matrixOperation.transPosition(powerMatrix);//卷积核权重的转制矩阵
            List<Matrix> normErrorList = matrixNorm.backError(matrixList);//脱掉归一化误差 返回一个通道所有图片的误差矩阵
            errorMatrixToVectorByList(normErrorList);//误差集合转列向量
            gFirstMulMatrix.addAll(normErrorList);
            errorSize = normErrorList.size();
            for (int k = 0; k < errorSize; k++) {
                gSecondMulMatrix.add(tpPowerMatrix);//true 特征的变化量
            }
            pFirstMulMatrix.addAll(tpIm2colMatrixList);
            pSecondMulMatrix.addAll(normErrorList);//false 权重变化量
        }
        List<Matrix> gResultMatrixList = matrixOperation.mulMatrixList(gFirstMulMatrix, gSecondMulMatrix);//特征变化量
        List<Matrix> pResultMatrixList = matrixOperation.mulMatrixList(pFirstMulMatrix, pSecondMulMatrix);//权重变化量
        List<BatchBody> nextBatchBodies = new ArrayList<>();
        List<List<Matrix>> resMatrixList = new ArrayList<>();
        for (int m = 0; m < batchBodies.size(); m++) {
            BackParameter backParameter = backParameterList.get(m);
            resMatrixList.add(backParameter.getScaleMatrixList());
            BatchBody batchBody = new BatchBody();
            nextBatchBodies.add(batchBody);
        }
        if (resError && oneConvPower != null) {
            Map<Integer, List<Matrix>> resErrorByID = new HashMap<>();//主键是图片id
            for (int i = 0; i < channelSize; i++) {
                List<Matrix> errorMatrixList = reluMatrixMap.get(i);//该通道所有特征误差
                List<List<Matrix>> resNext = backOneConvErrorMatrix(errorMatrixList, resMatrixList, oneConvPower.get(i), studyRate,
                        dymStudyRateList.get(i), dymStudyRate2List.get(i), dymStudy, times);//所有图像在该通道下的下一层残差误差
                for (int m = 0; m < nextBatchBodies.size(); m++) {//遍历该通道下的所有图像的 下一层残差误差
                    List<Matrix> matrixList = resNext.get(m);//当前通道下，每个图片的残差误差
                    if (resErrorByID.containsKey(m)) {
                        resErrorByID.put(m, matrixOperation.addMatrixList(resErrorByID.get(m), matrixList));
                    } else {
                        resErrorByID.put(m, matrixList);
                    }
                }
            }
            for (int m = 0; m < nextBatchBodies.size(); m++) {
                ResnetError resnetError = resnetErrors.get(m);
                List<Matrix> allErrorMatrixList = resErrorByID.get(m);
                //退下池化
                for (int i = 0; i < allErrorMatrixList.size(); i++) {
                    Matrix errorMatrix = allErrorMatrixList.get(i);
                    allErrorMatrixList.set(i, backDownPooling(errorMatrix));
                }
                resnetError.setResErrorMatrixList(allErrorMatrixList);
            }
        }
        for (int i = 0; i < channelSize; i++) {
            int startIndex = errorSize * i;
            int finishIndex = startIndex + errorSize;
            Matrix powerMatrix = powerMatrixList.get(i);//卷积核
            List<Matrix> gErrorMatrixList = gResultMatrixList.subList(startIndex, finishIndex);//特征变化量集合 ture
            List<Matrix> wSubMatrixList = pResultMatrixList.subList(startIndex, finishIndex);//权重变化量集合
            Matrix wSubMatrix = getAvgMatrixByList(wSubMatrixList);
            List<Matrix> nextErrorMatrixList = reverIm2colByMatrixList(gErrorMatrixList, im2calSize);//下一层的误差
            Matrix sMatrix = sMatrixList.get(i);
            Matrix s2Matrix = s2MatrixList.get(i);
            Matrix subPower = dymStudy.getErrorMatrixByStudy(studyRate, sMatrix, s2Matrix, wSubMatrix, times);
            powerMatrix = matrixOperation.add(powerMatrix, subPower);//更新卷积核
            if (rz) {//正则化处理
                dymStudy.regMatrix(powerMatrix, rzRate, studyRate);
            }
            powerMatrixList.set(i, powerMatrix);//更新卷积核
            for (int m = 0; m < nextBatchBodies.size(); m++) {//遍历所有图片
                BatchBody nextBatchBody = nextBatchBodies.get(m);
                if (resError && nextBatchBody.getResnetError() == null) {//带残差
                    ResnetError resnetError = resnetErrors.get(m);
                    nextBatchBody.setResnetError(resnetError);
                }
                nextBatchBody.getFeatureList().add(nextErrorMatrixList.get(m));
            }
        }
        if (!resError) {//加残差
            for (int m = 0; m < batchBodies.size(); m++) {
                BatchBody batchBody = batchBodies.get(m);
                BatchBody nextBatchBody = nextBatchBodies.get(m);
                List<Matrix> nextErrorMatrixList = nextBatchBody.getFeatureList();
                List<Matrix> resErrorMatrixList = batchBody.getResnetError().getResErrorMatrixList();
                nextErrorMatrixList = matrixOperation.addMatrixList(nextErrorMatrixList, resErrorMatrixList);
                nextBatchBody.setFeatureList(nextErrorMatrixList);
            }
        }
        clipErrorMatrixList(nextBatchBodies, dymStudy);//损失裁剪
        return nextBatchBodies;
    }

    private void clipErrorMatrixList(List<BatchBody> nextBatchBodies, DymStudy dymStudy) throws Exception {
        for (BatchBody nextBatchBody : nextBatchBodies) {//损失裁剪
            List<Matrix> nextErrorMatrixList = nextBatchBody.getFeatureList();
            int nextErrorSize = nextErrorMatrixList.size();
            for (int i = 0; i < nextErrorSize; i++) {
                Matrix gcMatrix = dymStudy.getClipMatrix(nextErrorMatrixList.get(i), true);
                nextErrorMatrixList.set(i, gcMatrix);
            }
        }
    }

    private Matrix getAvgMatrixByList(List<Matrix> wSubMatrixList) throws Exception {
        Matrix avgMatrix = null;
        int size = wSubMatrixList.size();
        for (Matrix matrix : wSubMatrixList) {
            if (avgMatrix == null) {
                avgMatrix = matrix;
            } else {
                avgMatrix = matrixOperation.add(avgMatrix, matrix);
            }
        }
        matrixOperation.mathDiv(avgMatrix, size);
        return avgMatrix;
    }

    private List<Matrix> reverIm2colByMatrixList(List<Matrix> gErrorMatrixList, int im2calSize) throws Exception {
        List<Matrix> nextErrorMatrixList = new ArrayList<>();
        for (Matrix gErrorMatrix : gErrorMatrixList) {
            Matrix nextErrorMatrix = matrixOperation.reverseIm2col(gErrorMatrix, 3, 1,
                    im2calSize, im2calSize);//其余误差
            nextErrorMatrixList.add(unPadding(nextErrorMatrix));
        }
        return nextErrorMatrixList;
    }

    private void errorMatrixToVectorByList(List<Matrix> normErrorList) throws Exception {
        int size = normErrorList.size();
        for (int i = 0; i < size; i++) {
            Matrix normError = errorMatrixToVector(normErrorList.get(i));
            normErrorList.set(i, normError);
        }
    }

    private Matrix errorMatrixToVector(Matrix errorMatrix) throws Exception {//将误差矩阵变成列向量
        int x = errorMatrix.getX();
        int y = errorMatrix.getY();
        Matrix resultError = new Matrix(x * y, 1);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float error = errorMatrix.getNumber(i, j);
                resultError.setNub(y * i + j, 0, error);
            }
        }
        return resultError;
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
}
