package org.dromara.easyai.conv;

import org.dromara.easyai.batchNerve.BatchNerveModel;
import org.dromara.easyai.conv.dcn.DConv;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.nerveEntity.ConvParameter;
import org.dromara.easyai.nerveEntity.ConvSize;
import org.dromara.easyai.resnet.entity.BatchBody;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @time 2025/2/26 10:52
 * @des 卷积运算超类
 */
public abstract class ConvCount {
    private final MatrixOperation matrixOperation = new MatrixOperation();
    private final DConv dConv;

    public ConvCount(DConv dConv) {
        this.dConv = dConv;
    }

    public void insertDConvModel(BatchNerveModel model) {
        if (dConv != null) {
            if (model != null) {
                dConv.insertModel(model);
            } else {
                throw new IllegalArgumentException("可变形卷积模型为空");
            }
        }
    }

    public BatchNerveModel getDConvModel() {
        if (dConv != null) {
            return dConv.getModel();
        }
        return null;
    }

    protected int getConvMyDep(int xSize, int ySize, int kernLen, int minFeatureValue, int step) {
        int xDeep = getConvDeep(xSize, kernLen, minFeatureValue, step);
        int yDeep = getConvDeep(ySize, kernLen, minFeatureValue, step);
        return Math.min(xDeep, yDeep);
    }

    private int getConvDeep(int size, int kernLen, int minFeatureValue, int step) {//获取卷积层深度
        int x = size;
        int deep = 0;//深度
        do {
            x = (x - (kernLen - step)) / step;
            if (step == 1) {
                x = x / 2 + x % 2;
            }
            deep++;
        } while (x > minFeatureValue);
        return deep - 1;
    }

    private Matrix upPooling(Matrix matrix) {//上池化
        int x = matrix.getX();
        int y = matrix.getY();
        Matrix myMatrix = new Matrix(x * 2, y * 2);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = matrix.getValue(i, j);
                insertMatrixValue(i * 2, j * 2, value, myMatrix);
            }
        }
        return myMatrix;
    }

    protected List<Matrix> backManyUpPooling(List<Matrix> errorMatrix) {
        List<Matrix> result = new ArrayList<>();
        for (Matrix matrix : errorMatrix) {
            result.add(backUpPooling(matrix));
        }
        return result;
    }

    protected Matrix backUpPooling(Matrix errorMatrix) {//退上池化
        int x = errorMatrix.getX();
        int y = errorMatrix.getY();
        Matrix myMatrix = new Matrix(x / 2, y / 2);
        for (int i = 0; i < x - 1; i += 2) {
            for (int j = 0; j < y - 1; j += 2) {
                float sigma = errorMatrix.getValue(i, j) + errorMatrix.getValue(i, j + 1) +
                        errorMatrix.getValue(i + 1, j) + errorMatrix.getValue(i + 1, j + 1);
                myMatrix.setValue(i / 2, j / 2, sigma);
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

    private void insertMatrixValue(int x, int y, float value, Matrix matrix) {
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
                matrix.setValue(i, j, value);
            }
        }
    }

    protected List<Matrix> backDownPoolingByList(List<Matrix> matrixList, int outX, int outY) {
        List<Matrix> result = new ArrayList<>();
        for (Matrix matrix : matrixList) {
            result.add(backDownPooling(matrix, outX, outY));
        }
        return result;
    }

    protected Matrix backDownPooling(Matrix matrix, int outX, int outY) {//退下池化
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
                float value = matrix.getValue(i, j) / 4;
                insertMatrixValue(i * 2, j * 2, value, myMatrix);
            }
        }
        return myMatrix;
    }

    private int getUpSize(int size, int kerSize) {//获取上采样后应该的尺寸
        return size + kerSize - 1;
    }

    private int backUpSize(int size, int kerSize) {//获取上采样前的尺寸
        return size - kerSize + 1;
    }

    protected List<Matrix> backManyUpConv(List<Matrix> errorMatrixList, int kerSize, ConvParameter convParameter, float studyRate,
                                          ActiveFunction activeFunction, DymStudy dymStudy, int times) throws Exception {
        List<Matrix> result = new ArrayList<>();
        int size = errorMatrixList.size();
        for (int i = 0; i < size; i++) {
            Matrix errorMatrix = errorMatrixList.get(i);
            result.add(backUpConv(errorMatrix, kerSize, convParameter, studyRate, activeFunction, i, dymStudy, times));
        }
        return result;
    }

    protected Matrix backUpConv(Matrix errorMatrix, int kerSize, ConvParameter convParameter, float studyRate, ActiveFunction activeFunction
            , int index, DymStudy dymStudy, int times) throws Exception {//退上卷积
        int myX = errorMatrix.getX();
        int myY = errorMatrix.getY();
        Matrix outMatrix = convParameter.getUpOutMatrixList().get(index);
        for (int i = 0; i < myX; i++) {
            for (int j = 0; j < myY; j++) {
                float value = activeFunction.functionG(outMatrix.getNumber(i, j)) * errorMatrix.getNumber(i, j);
                errorMatrix.setNub(i, j, value);
            }
        }
        int x = backUpSize(myX, kerSize);
        int y = backUpSize(myY, kerSize);
        Matrix upNerveMatrix = convParameter.getUpNerveMatrixList().get(index);//上采样卷积权重
        Matrix upDymStudyRate = convParameter.getUpDymStudyRateList().get(index);
        Matrix upDymStudyRate2 = convParameter.getUpDymStudyRate2List().get(index);
        Matrix vector = convParameter.getUpFeatureMatrixList().get(index);
        Matrix error = matrixOperation.im2col(errorMatrix, kerSize, 1);
        Matrix subNerveMatrix = matrixOperation.matrixMulPd(error, vector, upNerveMatrix, false);
        Matrix errorFeature = matrixOperation.matrixMulPd(error, vector, upNerveMatrix, true);
        subNerveMatrix = dymStudy.getErrorMatrixByStudy(studyRate, upDymStudyRate, upDymStudyRate2, subNerveMatrix, times);
        convParameter.getUpNerveMatrixList().set(index, matrixOperation.add(subNerveMatrix, upNerveMatrix));
        return matrixOperation.vectorToMatrix(errorFeature, x, y);
    }

    protected List<Matrix> backUpConvMany(List<Matrix> errorMatrixList, int kerSize, ConvParameter convParameter, float studyRate,
                                          ActiveFunction activeFunction, DymStudy dymStudy, int times) throws Exception {//退上卷积
        int size = errorMatrixList.size();
        List<Matrix> vectorList = new ArrayList<>();
        List<Matrix> errorList = new ArrayList<>();
        List<Matrix> upNerveMatrixList = new ArrayList<>();
        int x = 0, y = 0;
        Matrix upNerveMatrix = convParameter.getUpNerveMatrixList().get(0);//上采样卷积权重
        for (int k = 0; k < size; k++) {
            upNerveMatrixList.add(upNerveMatrix);
            Matrix errorMatrix = errorMatrixList.get(k);
            int myX = errorMatrix.getX();
            int myY = errorMatrix.getY();
            Matrix outMatrix = convParameter.getUpOutMatrixList().get(k);
            for (int i = 0; i < myX; i++) {
                for (int j = 0; j < myY; j++) {
                    float value = activeFunction.functionG(outMatrix.getValue(i, j)) * errorMatrix.getValue(i, j);
                    errorMatrix.setValue(i, j, value);
                }
            }
            x = backUpSize(myX, kerSize);
            y = backUpSize(myY, kerSize);
            Matrix vector = convParameter.getUpFeatureMatrixList().get(k);
            Matrix error = matrixOperation.im2col(errorMatrix, kerSize, 1);
            vectorList.add(vector);
            errorList.add(error);
        }
        Matrix upDymStudyRate = convParameter.getUpDymStudyRateList().get(0);
        Matrix upDymStudyRate2 = convParameter.getUpDymStudyRate2List().get(0);
        List<Matrix> subNerveMatrixList = matrixOperation.matrixMulPdByList(errorList, vectorList, upNerveMatrixList, false);
        List<Matrix> errorFeatureList = matrixOperation.matrixMulPdByList(errorList, vectorList, upNerveMatrixList, true);
        Matrix subNerveMatrix = null;
        for (int i = 0; i < size; i++) {
            Matrix sub = subNerveMatrixList.get(i);
            if (subNerveMatrix == null) {
                subNerveMatrix = sub;
            } else {
                subNerveMatrix = matrixOperation.add(subNerveMatrix, sub);
            }
        }
        matrixOperation.mathMul(subNerveMatrix, 1f / size);
        subNerveMatrix = dymStudy.getErrorMatrixByStudy(studyRate, upDymStudyRate, upDymStudyRate2, subNerveMatrix, times);
        convParameter.getUpNerveMatrixList().set(0, matrixOperation.add(subNerveMatrix, upNerveMatrix));
        for (int i = 0; i < size; i++) {
            Matrix errorFeature = matrixOperation.vectorToMatrix(errorFeatureList.get(i), x, y);
            Matrix result = dymStudy.getClipMatrix(errorFeature, true);
            errorFeatureList.set(i, result);
        }
        return errorFeatureList;
    }

    private ConvResult upConv(List<Matrix> matrixList, int kerSize, List<Matrix> nervePowerMatrixList, ActiveFunction activeFunction, int channelNo) throws Exception {//进行上采样
        ConvResult convResult = new ConvResult();
        int x = getUpSize(matrixList.get(0).getX(), kerSize);
        int y = getUpSize(matrixList.get(0).getY(), kerSize);
        List<Matrix> vectorList = new ArrayList<>();
        List<Matrix> resultList = new ArrayList<>();
        convResult.setLeftMatrixList(vectorList);
        convResult.setResultMatrixList(resultList);
        for (int k = 0; k < channelNo; k++) {
            Matrix matrix = matrixList.get(k);
            Matrix nervePowerMatrix = nervePowerMatrixList.get(k);
            Matrix vector = matrixOperation.matrixToVector(matrix, false);//输入矩阵转列向量 t
            Matrix im2colMatrix = matrixOperation.mulMatrix(vector, nervePowerMatrix);
            Matrix out = matrixOperation.reverseIm2col(im2colMatrix, kerSize, 1, x, y);
            Matrix outMatrix = new Matrix(x, y);
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    float value = activeFunction.function(out.getValue(i, j));
                    outMatrix.setValue(i, j, value);
                }
            }
            vectorList.add(vector);
            resultList.add(outMatrix);
        }
        return convResult;
    }

    private ConvResult upConvMany(List<BatchBody> batchBodies, Matrix nervePower, ActiveFunction activeFunction, int number) throws Exception {//进行上采样
        List<Matrix> matrixList = new ArrayList<>();
        int channelNo = batchBodies.size() * number;
        for (BatchBody batchBody : batchBodies) {
            matrixList.addAll(batchBody.getFeatureList());
        }
        ConvResult convResult = new ConvResult();
        List<Matrix> nervePowerMatrixList = new ArrayList<>();
        int x = getUpSize(matrixList.get(0).getX(), 3);
        int y = getUpSize(matrixList.get(0).getY(), 3);
        List<Matrix> vectorList = new ArrayList<>();
        List<Matrix> resultList = new ArrayList<>();
        convResult.setLeftMatrixList(vectorList);
        convResult.setResultMatrixList(resultList);
        for (int k = 0; k < channelNo; k++) {
            Matrix matrix = matrixList.get(k);
            Matrix vector = matrixOperation.matrixToVector(matrix, false);//输入矩阵转列向量 t
            vectorList.add(vector);
            nervePowerMatrixList.add(nervePower);
        }
        List<Matrix> maxList = matrixOperation.mulMatrixList(vectorList, nervePowerMatrixList);
        for (int k = 0; k < channelNo; k++) {
            Matrix im2colMatrix = maxList.get(k);
            Matrix out = matrixOperation.reverseIm2col(im2colMatrix, 3, 1, x, y);
            Matrix outMatrix = new Matrix(x, y);
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    float value = activeFunction.function(out.getValue(i, j));
                    outMatrix.setValue(i, j, value);
                }
            }
            resultList.add(outMatrix);
        }
        return convResult;
    }

    protected List<Matrix> upConvAndPooling(List<Matrix> matrixList, ConvParameter convParameter, int channelNo, ActiveFunction activeFunction
            , int kernLen, boolean pooling, boolean study) throws Exception {
        List<Matrix> downConvMatrixList = downConvAndPooling(matrixList, convParameter,
                channelNo, activeFunction, kernLen, false, -1, 1, study);
        if (pooling) {
            ConvResult result = upConv(downConvMatrixList, kernLen, convParameter.getUpNerveMatrixList(), activeFunction, channelNo);
            List<Matrix> resultMatrixList = result.getResultMatrixList();
            convParameter.setUpOutMatrixList(result.getResultMatrixList());
            convParameter.setUpFeatureMatrixList(result.getLeftMatrixList());
            List<Matrix> upPoolingMatrixList = new ArrayList<>();
            for (Matrix matrix : resultMatrixList) {
                upPoolingMatrixList.add(upPooling(matrix));
            }
            return upPoolingMatrixList;
        }
        return downConvMatrixList;
    }

    public void upConvAndPoolingMany(List<BatchBody> batchBodies, ConvParameter convParameter, ActiveFunction activeFunction
            , boolean study) throws Exception {
        int batchSize = batchBodies.size();
        int channelNo = batchBodies.get(0).getFeatureList().size();//通道数
        ConvResult result = upConvMany(batchBodies, convParameter.getUpNerveMatrixList().get(0),
                activeFunction, channelNo);
        List<Matrix> resultMatrixList = result.getResultMatrixList();
        convParameter.setUpOutMatrixList(result.getResultMatrixList());
        convParameter.setUpFeatureMatrixList(result.getLeftMatrixList());
        List<Matrix> upPoolingMatrixList = new ArrayList<>();
        for (Matrix matrix : resultMatrixList) {
            upPoolingMatrixList.add(upPooling(matrix));
        }
        List<List<Float>> oneConvPower = convParameter.getOneConvPower();
        if (channelNo != oneConvPower.get(0).size()) {
            throw new IllegalAccessException("通道数与卷积维度不一致，特征通道数:" + channelNo +
                    ",1v1卷积维度" + oneConvPower.get(0).size());
        }
        List<List<Matrix>> upChannelFeatureList = null;
        if (study) {
            upChannelFeatureList = convParameter.getUpChannelFeatureList();
            upChannelFeatureList.clear();
        }
        for (int i = 0; i < batchSize; i++) {// 遍历图片数量
            BatchBody batchBody = batchBodies.get(i);
            int startIndex = i * channelNo;
            int endIndex = startIndex + channelNo;
            List<Matrix> channelFeature = upPoolingMatrixList.subList(startIndex, endIndex);//上采样后所有通道的特征
            if (study) {
                upChannelFeatureList.add(channelFeature);
            }
            List<Matrix> features = manyOneConv(channelFeature, oneConvPower);
            batchBody.setFeatureList(features);
        }
    }

    protected List<Matrix> downConvAndPooling(List<Matrix> matrixList, ConvParameter convParameter, int channelNo, ActiveFunction activeFunction
            , int kernLen, boolean pooling, long eventID, int step, boolean study) throws Exception {
        List<ConvSize> convSizeList = convParameter.getConvSizeList();
        List<Matrix> nerveMatrixList = convParameter.getNerveMatrixList();
        List<Matrix> im2colMatrixList = convParameter.getIm2colMatrixList();
        List<Matrix> outMatrixList = convParameter.getOutMatrixList();
        im2colMatrixList.clear();
        outMatrixList.clear();
        List<Matrix> resultMatrixList = new ArrayList<>();
        for (int i = 0; i < channelNo; i++) {
            Matrix matrix = matrixList.get(i);
            ConvSize convSize = convSizeList.get(i);
            Matrix nerveMatrix = nerveMatrixList.get(i);
            int xInput = matrix.getX();
            int yInput = matrix.getY();
            convSize.setXInput(xInput);
            convSize.setYInput(yInput);
            ConvResult convResult = downConvCount(matrix, activeFunction, kernLen, nerveMatrix, step, study, eventID);
            im2colMatrixList.add(convResult.getLeftMatrix());
            Matrix myMatrix = convResult.getResultMatrix();//卷积后的矩阵
            outMatrixList.add(myMatrix);
            resultMatrixList.add(myMatrix);
        }
        if (eventID >= 0) {
            convParameter.getFeatureMap().put(eventID, resultMatrixList);
        }
        convParameter.setOutX(resultMatrixList.get(0).getX());
        convParameter.setOutY(resultMatrixList.get(0).getY());
        if (pooling && step == 1) {
            List<Matrix> poolMatrixList = new ArrayList<>();
            for (Matrix matrix : resultMatrixList) {
                poolMatrixList.add(downPooling(matrix));
            }
            return poolMatrixList;//池化
        }
        return resultMatrixList;
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

    protected void backOneConvByList(List<Matrix> errorMatrixList, List<Matrix> matrixList, List<List<Float>> oneConvPower,
                                     float studyRate, List<List<Float>> oneDymStudyRateList,
                                     List<List<Float>> oneDymStudyRate2List, DymStudy dymStudy, int times) throws Exception {
        int size = errorMatrixList.size();
        if (size == oneConvPower.size()) {
            for (int i = 0; i < size; i++) {
                Matrix errorMatrix = errorMatrixList.get(i);
                List<Float> oneConvPowerList = oneConvPower.get(i);
                List<Float> oneDymStudyRate = oneDymStudyRateList.get(i);
                List<Float> oneDymStudyRate2 = oneDymStudyRate2List.get(i);
                backOneConv(errorMatrix, matrixList, oneConvPowerList, studyRate, oneDymStudyRate,
                        oneDymStudyRate2, dymStudy, times);
            }
        } else {
            throw new Exception("误差矩阵大小与通道数不相符");
        }
    }

    protected void backOneConv(Matrix errorMatrix, List<Matrix> matrixList, List<Float> oneConvPower,
                               float studyRate, List<Float> sList, List<Float> s2List, DymStudy dym, int times) throws Exception {//单卷积降维回传
        int size = oneConvPower.size();
        for (int t = 0; t < size; t++) {
            Matrix myMatrix = matrixList.get(t);
            int x = myMatrix.getX();
            int y = myMatrix.getY();
            float power = oneConvPower.get(t);
            float allSubPower = 0;
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    float subPower = myMatrix.getValue(i, j) * errorMatrix.getValue(i, j);
                    allSubPower = allSubPower + subPower;
                }
            }
            float error = dym.getErrorValueByStudy(studyRate, sList, s2List, allSubPower, t, times);
            power = power + error;
            oneConvPower.set(t, power);
        }
    }

    protected List<Matrix> getBackOneConvPool(List<Matrix> errorMatrixList, float studyRate, DymStudy dymStudy, int times,
                                          ConvParameter convParameter, int channelNo) throws Exception {//一张图片的
        List<List<Matrix>> upChannelFeatureList = convParameter.getUpChannelFeatureList();
        List<List<Float>> oneConvPower = convParameter.getOneConvPower();//外层是输出通道，内层是输入通道
        int size = upChannelFeatureList.size();
        List<Matrix> gNextMatrixList = new ArrayList<>();
        List<Matrix> myOneMatrixError = null;
        for (int i = 0; i < size; i++) {//遍历图片
            int startIndex = i * channelNo;
            int endIndex = startIndex + channelNo;
            List<Matrix> matrixList = upChannelFeatureList.get(i);//输入通道
            List<Matrix> errorMatrixChannel = errorMatrixList.subList(startIndex, endIndex);//输出通道
            List<Matrix> oneAllConvError = new ArrayList<>();//一张图片的1v1卷积核误差
            List<Matrix> gAllMatrixList = null;
            for (int k = 0; k < channelNo; k++) {//遍历输出通道 一个输出通道 对应多个输入通道
                Matrix errorMatrix = errorMatrixChannel.get(k);
                List<Float> onePower = oneConvPower.get(k);
                FpnOneConvError fpnOneConvError = backOneConvMany(errorMatrix, matrixList, onePower);
                Matrix oneConvError = fpnOneConvError.getOneConvError();//该输出通道卷积核误差
                List<Matrix> gMatrixList = fpnOneConvError.getgMatrixList();//该输出通道对应的输入通道误差
                oneAllConvError.add(oneConvError);
                if (gAllMatrixList == null) {
                    gAllMatrixList = gMatrixList;
                } else {
                    gAllMatrixList = matrixOperation.addMatrixList(gAllMatrixList, gMatrixList);
                }
            }
            gNextMatrixList.addAll(gAllMatrixList);
            if (myOneMatrixError == null) {
                myOneMatrixError = oneAllConvError;
            } else {
                myOneMatrixError = matrixOperation.addMatrixList(myOneMatrixError, oneAllConvError);
            }
        }
        matrixOperation.mathMulByList(myOneMatrixError, 1f / size);
        List<List<Float>> ss1 = convParameter.getOneDymStudyRateList();
        List<List<Float>> ss2 = convParameter.getOneDymStudyRate2List();
        for (int k = 0; k < channelNo; k++) {
            Matrix oneError = myOneMatrixError.get(k);
            List<Float> onePowerList = oneConvPower.get(k);
            List<Float> s1 = ss1.get(k);
            List<Float> s2 = ss2.get(k);
            int inSize = oneError.getY();
            for (int t = 0; t < inSize; t++) {
                float error = dymStudy.getErrorValueByStudy(studyRate, s1, s2, oneError.getValue(0, t), t, times);
                float onePower = onePowerList.get(t);
                onePowerList.set(t, error + onePower);
            }
        }
        int allSize = gNextMatrixList.size();
        for (int i = 0; i < allSize; i++) {
            Matrix gMatrix = gNextMatrixList.get(i);
            Matrix errorMatrix = dymStudy.getClipMatrix(gMatrix, true);
            gNextMatrixList.set(i, errorMatrix);
        }
        return backManyUpPooling(gNextMatrixList);
    }

    //一个输出通道，该输出通道对应的输入通道，该输出通道对应的卷积核
    protected FpnOneConvError backOneConvMany(Matrix errorMatrix, List<Matrix> matrixList, List<Float> oneConvPower) throws Exception {//单卷积降维回传
        int size = oneConvPower.size();
        FpnOneConvError fpnOneConvError = new FpnOneConvError();
        List<Matrix> gMatrixList = new ArrayList<>();//当前通道下所有输入特征图的误差
        Matrix oneConvError = new Matrix(1, size);
        fpnOneConvError.setgMatrixList(gMatrixList);
        fpnOneConvError.setOneConvError(oneConvError);
        for (int t = 0; t < size; t++) {//遍历输入通道数
            Matrix myMatrix = matrixList.get(t);
            int x = myMatrix.getX();
            int y = myMatrix.getY();
            float power = oneConvPower.get(t);
            Matrix gMatrix = matrixOperation.mathMulBySelf(errorMatrix, power);
            gMatrixList.add(gMatrix);
            float allSubPower = 0;
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    float subPower = myMatrix.getValue(i, j) * errorMatrix.getValue(i, j);
                    allSubPower = allSubPower + subPower;
                }
            }
            oneConvError.setValue(0, t, allSubPower);
        }
        return fpnOneConvError;
    }

    protected List<Matrix> backAllDownConv(ConvParameter convParameter, List<Matrix> errorMatrixList, float studyPoint
            , ActiveFunction activeFunction, int channelNo, int kernLen, DymStudy dymStudy, int times
            , boolean cutLayG, int step) throws Exception {
        List<Matrix> outMatrixList = convParameter.getOutMatrixList();
        List<Matrix> im2colMatrixList = convParameter.getIm2colMatrixList();
        List<Matrix> nerveMatrixList = convParameter.getNerveMatrixList();
        List<ConvSize> convSizeList = convParameter.getConvSizeList();
        List<Matrix> sMatrixList = convParameter.getDymStudyRateList();
        List<Matrix> sMatrix2List = convParameter.getDymStudyRate2List();
        List<Matrix> resultMatrixList = new ArrayList<>();
        for (int i = 0; i < channelNo; i++) {
            Matrix errorMatrix = errorMatrixList.get(i);
            Matrix outMatrix = outMatrixList.get(i);
            Matrix im2col = im2colMatrixList.get(i);
            Matrix nerveMatrix = nerveMatrixList.get(i);
            ConvSize convSize = convSizeList.get(i);
            Matrix sMatrix = sMatrixList.get(i);
            Matrix s2Matrix = sMatrix2List.get(i);
            int xInput = convSize.getXInput();
            int yInput = convSize.getYInput();
            ConvResult convResult = backDownConv(errorMatrix, outMatrix, activeFunction, im2col,
                    nerveMatrix, studyPoint, kernLen, xInput, yInput, sMatrix, s2Matrix, dymStudy, times, step);
            nerveMatrixList.set(i, convResult.getNervePowerMatrix());//更新权重
            resultMatrixList.add(convResult.getResultMatrix());
        }
        if (cutLayG) {
            for (int i = 0; i < resultMatrixList.size(); i++) {//误差裁剪
                Matrix result = dymStudy.getClipMatrix(resultMatrixList.get(i), true);
                resultMatrixList.set(i, result);
            }
        }
        return resultMatrixList;
    }

    private ConvResult backDownConv(Matrix errorMatrix, Matrix outMatrix, ActiveFunction activeFunction, Matrix im2col, Matrix nerveMatrix,
                                    float studyRate, int kernSize, int xInput, int yInput, Matrix sMatrix, Matrix s2Matrix, DymStudy dymStudy
            , int times, int step) throws Exception {
        //下采样卷积误差反向传播
        ConvResult convResult = new ConvResult();
        int x = errorMatrix.getX();
        int y = errorMatrix.getY();
        Matrix resultError = new Matrix(x * y, 1);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float error = errorMatrix.getValue(i, j);
                float out = outMatrix.getValue(i, j);
                error = error * activeFunction.functionG(out);
                resultError.setValue(y * i + j, 0, error);
            }
        }
        Matrix wSub = matrixOperation.matrixMulPd(resultError, im2col, nerveMatrix, false);
        Matrix im2colSub = matrixOperation.matrixMulPd(resultError, im2col, nerveMatrix, true);
        wSub = dymStudy.getErrorMatrixByStudy(studyRate, sMatrix, s2Matrix, wSub, times);
        nerveMatrix = matrixOperation.add(nerveMatrix, wSub);//调整卷积核
        Matrix gNext;//其余误差
        if (dConv != null) {
            List<Matrix> im2colSubList = new ArrayList<>();
            im2colSubList.add(im2colSub);
            gNext = dConv.backError(im2colSubList).get(0);
        } else {
            gNext = matrixOperation.reverseIm2col(im2colSub, kernSize, step, xInput, yInput);//其余误差
        }
        convResult.setNervePowerMatrix(nerveMatrix);
        convResult.setResultMatrix(gNext);
        return convResult;
    }

    protected ConvResult backDownConvMany(List<Matrix> errorMatrixList, List<Matrix> outMatrixList, ActiveFunction activeFunction,
                                          List<Matrix> im2colList, Matrix nerveMatrix, float studyRate, int kernSize, int xInput, int yInput, Matrix sMatrix, Matrix s2Matrix, DymStudy dymStudy
            , int times, int step) throws Exception {
        //下采样卷积误差反向传播
        ConvResult convResult = new ConvResult();
        int size = errorMatrixList.size();
        List<Matrix> resultErrorList = new ArrayList<>();
        List<Matrix> nerveMatrixList = new ArrayList<>();
        for (int k = 0; k < size; k++) {
            Matrix errorMatrix = errorMatrixList.get(k);
            Matrix outMatrix = outMatrixList.get(k);
            int x = errorMatrix.getX();
            int y = errorMatrix.getY();
            Matrix resultError = new Matrix(x * y, 1);
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    float error = errorMatrix.getValue(i, j);
                    float out = outMatrix.getValue(i, j);
                    error = error * activeFunction.functionG(out);
                    resultError.setValue(y * i + j, 0, error);
                }
            }
            resultErrorList.add(resultError);
            nerveMatrixList.add(nerveMatrix);
        }
        List<Matrix> wSubList = matrixOperation.matrixMulPdByList(resultErrorList, im2colList, nerveMatrixList, false);
        List<Matrix> im2colSubList = matrixOperation.matrixMulPdByList(resultErrorList, im2colList, nerveMatrixList, true);
        Matrix wSub = null;
        for (int k = 0; k < size; k++) {
            Matrix sub = wSubList.get(k);
            if (wSub == null) {
                wSub = sub;
            } else {
                wSub = matrixOperation.add(wSub, sub);
            }
        }
        matrixOperation.mathMul(wSub, 1f / size);
        wSub = dymStudy.getErrorMatrixByStudy(studyRate, sMatrix, s2Matrix, wSub, times);
        nerveMatrix = matrixOperation.add(nerveMatrix, wSub);//调整卷积核
        List<Matrix> gNextList = new ArrayList<>();
        for (int k = 0; k < size; k++) {
            Matrix im2colSub = im2colSubList.get(k);
            Matrix gNext = matrixOperation.reverseIm2col(im2colSub, kernSize, step, xInput, yInput);//其余误差
            gNextList.add(gNext);
        }
        convResult.setNervePowerMatrix(nerveMatrix);
        int gSize = gNextList.size();
        for (int i = 0; i < gSize; i++) {//误差裁剪
            Matrix result = dymStudy.getClipMatrix(gNextList.get(i), true);
            gNextList.set(i, result);
        }
        convResult.setResultMatrixList(gNextList);
        return convResult;
    }

    ConvResult downConvCount(Matrix matrix, ActiveFunction activeFunction, int kerSize
            , Matrix nervePowerMatrix, int step, boolean study, long eventID) throws Exception {//进行下采样卷积运算
        ConvResult convResult = new ConvResult();
        int xInput = matrix.getX();
        int yInput = matrix.getY();
        int sub = kerSize - step;
        int x = (xInput - sub) / step;//线性变换后矩阵的行数 （图片长度-（核长-步长））/步长
        int y = (yInput - sub) / step;//线性变换后矩阵的列数
        Matrix myMatrix = new Matrix(x, y);//线性变化后的矩阵
        Matrix im2col;
        if (dConv != null) {
            if (study) {
                List<Matrix> outMatrixList = new ArrayList<>();
                outMatrixList.add(matrix);
                im2col = dConv.study(outMatrixList, eventID).get(0);
            } else {
                im2col = dConv.movePosition(matrix, eventID);
            }
        } else {
            im2col = matrixOperation.im2col(matrix, kerSize, step);
        }
        convResult.setLeftMatrix(im2col);
        //输出矩阵
        Matrix matrixOut = matrixOperation.mulMatrix(im2col, nervePowerMatrix);
        //输出矩阵重新排序
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float nub = activeFunction.function(matrixOut.getValue(i * y + j, 0));
                myMatrix.setValue(i, j, nub);
            }
        }
        convResult.setResultMatrix(myMatrix);
        return convResult;
    }

    protected ConvResult downConvCountMany(List<Matrix> matrices, ActiveFunction activeFunction, int kerSize
            , Matrix nervePowerMatrix, int step) throws Exception {//进行下采样卷积运算
        ConvResult convResult = new ConvResult();
        List<Matrix> leftMatrixList = new ArrayList<>();
        List<Matrix> resultMatrixList = new ArrayList<>();
        List<Matrix> nervePowerMatrixList = new ArrayList<>();
        int size = matrices.size();
        int sub = kerSize - step;
        for (Matrix matrix : matrices) {
            Matrix im2col = matrixOperation.im2col(matrix, kerSize, step);
            leftMatrixList.add(im2col);
            nervePowerMatrixList.add(nervePowerMatrix);
        }
        convResult.setLeftMatrixList(leftMatrixList);
        List<Matrix> matrixOutList = matrixOperation.mulMatrixList(leftMatrixList, nervePowerMatrixList);
        for (int t = 0; t < size; t++) {
            Matrix matrix = matrices.get(t);
            Matrix matrixOut = matrixOutList.get(t);
            int xInput = matrix.getX();
            int yInput = matrix.getY();
            int x = (xInput - sub) / step;//线性变换后矩阵的行数 （图片长度-（核长-步长））/步长
            int y = (yInput - sub) / step;//线性变换后矩阵的列数
            Matrix myMatrix = new Matrix(x, y);//线性变化后的矩阵
            //输出矩阵重新排序
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    float nub = activeFunction.function(matrixOut.getValue(i * y + j, 0));
                    myMatrix.setValue(i, j, nub);
                }
            }
            resultMatrixList.add(myMatrix);
        }
        convResult.setResultMatrixList(resultMatrixList);
        return convResult;
    }

}
