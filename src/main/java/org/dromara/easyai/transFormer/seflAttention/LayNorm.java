package org.dromara.easyai.transFormer.seflAttention;

import org.dromara.easyai.conv.DymStudy;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixList;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.transFormer.CodecBlock;
import org.dromara.easyai.transFormer.FirstDecoderBlock;
import org.dromara.easyai.transFormer.model.LayNormModel;
import org.dromara.easyai.transFormer.nerve.HiddenNerve;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class LayNorm {//残差与归一化
    private MultiSelfAttention multiSelfAttention;//多头
    private final CodecBlock myEncoderBlock;
    private final int featureDimension;//特征维度
    private List<HiddenNerve> hiddenNerves;//第一层隐层
    private final int type;//类别层模型需要保存
    private final Map<Long, MatrixList> reMatrixMap = new ConcurrentHashMap<>();
    private final FirstDecoderBlock firstDecoderBlock;
    private Matrix bTa;//模型需要保存
    private Matrix power;//模型需要保存
    private final Map<Integer, NormErrorBody> normErrorBodyMap = new HashMap<>();
    private final float study;//学习率
    private Matrix myFinalError;//从FNN传来的总误差
    private int number;//记录fnn传来的误差次数
    private final MatrixOperation matrixOperation;
    private final DymStudy dymStudy;
    private int updateTimes = 0;
    private final Matrix ps1;
    private final Matrix ps2;
    private final Matrix bs1;
    private final Matrix bs2;
    private final boolean encoder;
    private final int depth;

    public LayNormModel getModel() throws Exception {
        LayNormModel layNormModel = new LayNormModel();
        layNormModel.setbTa(bTa.getMatrix());
        layNormModel.setPower(power.getMatrix());
        return layNormModel;
    }

    public void insertModel(LayNormModel layNormModel) throws Exception {
        insertPower(layNormModel.getPower(), power);
        insertPower(layNormModel.getbTa(), bTa);
    }

    private void insertPower(float[][] modelPower, Matrix power) throws Exception {
        for (int i = 0; i < power.getX(); i++) {
            for (int j = 0; j < power.getY(); j++) {
                power.setNub(i, j, modelPower[i][j]);
            }
        }
    }

    public LayNorm(int type, int featureDimension, CodecBlock myEncoderBlock, FirstDecoderBlock firstDecoderBlock
            , float study, int coreNumber, boolean encoder, int depth, DymStudy dymStudy) throws Exception {
        this.study = study;
        this.dymStudy = dymStudy;
        this.myEncoderBlock = myEncoderBlock;
        this.encoder = encoder;
        this.depth = depth;
        this.type = type;
        this.featureDimension = featureDimension;
        this.firstDecoderBlock = firstDecoderBlock;
        matrixOperation = new MatrixOperation(coreNumber);
        bTa = new Matrix(1, featureDimension);
        bs1 = new Matrix(1, featureDimension);
        bs2 = new Matrix(1, featureDimension);
        power = new Matrix(featureDimension, featureDimension);
        ps1 = new Matrix(featureDimension, featureDimension);
        ps2 = new Matrix(featureDimension, featureDimension);
        Random random = new Random();
        float sh = 1;
        if (!encoder && depth == 1) {
            sh = featureDimension * featureDimension;
        }
        for (int i = 0; i < featureDimension; i++) {
            float value = random.nextFloat() / sh;
            bTa.setNub(0, i, value);
        }
        for (int i = 0; i < featureDimension; i++) {
            for (int j = 0; j < featureDimension; j++) {
                float value = random.nextFloat() / sh;
                power.setNub(i, j, value);
            }
        }
    }

    private Matrix back(Matrix errorMatrix, int index) throws Exception {
        NormErrorBody normErrorBody = normErrorBodyMap.get(index);
        Matrix myData = normErrorBody.getOutMatrix();//归一化后输出矩阵
        Matrix insertMatrix = normErrorBody.getInsertMatrix();//归一化前输入矩阵
        float avg = normErrorBody.getAvg();//平均值
        float sd = normErrorBody.getSd() + 0.0000001f;//标准差
        Matrix subPower = matrixOperation.matrixMulPd(errorMatrix, myData, power, false);
        //做一下层梯度裁剪
        Matrix sub = matrixOperation.matrixMulPd(errorMatrix, myData, power, true);
        int n = sub.getY();
        subPower = dymStudy.getErrorMatrixByStudy(study, ps1, ps2, subPower, updateTimes);
        power = matrixOperation.add(subPower, power);
        float var = sd * sd * n;
        float sigmaG = sub.getSigma() / (n * sd);//所有分量梯度的和 / n *标准差
        float allZk = 0;
        for (int j = 0; j < n; j++) {
            float zk = sub.getValue(0, j) * myData.getValue(0, j);
            allZk = allZk + zk;
        }
        Matrix subMatrix = new Matrix(1, n);
        for (int i = 0; i < n; i++) {
            float subValue = sub.getValue(0, i) / sd;
            float t = (insertMatrix.getValue(0, i) - avg) / var;
            float error = subValue - sigmaG - t * allZk;
            subMatrix.setValue(0, i, error);
        }
        return subMatrix;
    }

    public void backErrorFromFNN(Matrix errorMatrix, long eventID, Matrix allError) throws Exception {//从fnn传下来的误差类别为1
        number++;
        if (myFinalError == null) {
            myFinalError = errorMatrix;
        } else {
            myFinalError = matrixOperation.add(myFinalError, errorMatrix);
        }
        if (number == featureDimension) {
            number = 0;
            Matrix error = myFinalError.getSonOfMatrix(0, 0, myFinalError.getX(), myFinalError.getY() - 1);
            myFinalError = null;
            Matrix myError = matrixOperation.add(error, allError);//残差误差与FNN回传误差相加
            backErrorFromLine(myError, eventID);
        }
    }

    public void backLastError(Matrix errorMatrix) throws Exception {//作为编码器最后一层接收误差
        if (myFinalError == null) {
            myFinalError = errorMatrix;
        } else {
            myFinalError = matrixOperation.add(myFinalError, errorMatrix);
        }
    }

    public void encoderBackStart(long eventID) throws Exception {
        Matrix error = myFinalError.copy();
        myFinalError = null;
        backErrorFromLine(error, eventID);
    }

    public void backErrorFromLine(Matrix errorMatrix, long eventID) throws Exception {//从线性层后传，类别为2
        updateTimes++;
        errorMatrix = dymStudy.getClipMatrix(errorMatrix, true);
        int x = errorMatrix.getX();
        MatrixList errorMatrixList = null;
        for (int i = 0; i < x; i++) {
            Matrix error = errorMatrix.getRow(i);
            Matrix btError = dymStudy.getErrorMatrixByStudy(study, bs1, bs2, error, updateTimes);
            bTa = matrixOperation.add(btError, bTa);//更新bTa
            Matrix myRowError = back(error, i);
            if (i == 0) {
                errorMatrixList = new MatrixList(myRowError, true);
            } else {
                errorMatrixList.add(myRowError);
            }
        }
        Matrix myError = errorMatrixList.getMatrix();
        //本模块误差处理完毕继续向后传播 2返回到fnn,1返回注意力
        if (type == 2) {//返回fnn
            int size = hiddenNerves.size();
            for (int i = 0; i < size; i++) {
                hiddenNerves.get(i).receiveErrorMatrix(myError.getColumn(i), eventID, myError);
            }
        } else {//误差返回注意力
            multiSelfAttention.backError(myError, eventID);
        }

    }

    public void addNorm(Matrix feature, Matrix outMatrix, long eventID, boolean isStudy
            , OutBack outBack, List<Integer> E, Matrix encoderFeature, boolean outAllPro) throws Exception {//残差及归一化
        Matrix myMatrix = matrixOperation.add(feature, outMatrix);//残差相加
        Matrix out = layNorm(myMatrix, isStudy);
        if (type == 1) {
            if (myEncoderBlock != null) {
                sendHiddenParameter(out, eventID, isStudy, outBack, E, encoderFeature, outAllPro);//发送线性第一层
            } else if (firstDecoderBlock != null) {//解码器第一层//输出
                firstDecoderBlock.sendOutputMatrix(eventID, out, isStudy, outBack, E, outAllPro);
            }
        } else {//输出矩阵
            myEncoderBlock.sendOutputMatrix(eventID, out, isStudy, outBack, E, encoderFeature, outAllPro);
        }
    }

    public void addNormFromNerve(long eventID, boolean isStudy, Matrix parameter, Matrix allFeature,
                                 OutBack outBack, List<Integer> E, Matrix encoderFeature, boolean outAllPro) throws Exception {
        MatrixList matrixFeature;
        if (reMatrixMap.containsKey(eventID)) {
            matrixFeature = reMatrixMap.get(eventID);
            matrixFeature.add(parameter);
        } else {
            matrixFeature = new MatrixList(parameter, false);
            reMatrixMap.put(eventID, matrixFeature);
        }
        if (matrixFeature.getY() == featureDimension) {//执行残差
            reMatrixMap.remove(eventID);
            addNorm(matrixFeature.getMatrix(), allFeature, eventID, isStudy, outBack, E, encoderFeature, outAllPro);
        }
    }

    private void sendHiddenParameter(Matrix feature, long eventId, boolean isStudy
            , OutBack outBack, List<Integer> E, Matrix encoderFeature, boolean outAllPro) throws Exception {//hiddenNerves
        for (HiddenNerve hiddenNerve : hiddenNerves) {
            hiddenNerve.receive(feature, eventId, isStudy, outBack, E, encoderFeature, outAllPro);
        }
    }

    private Matrix norm(Matrix row, boolean isStudy, int index) throws Exception {
        Matrix result = new Matrix(1, row.getY());
        float avg = row.getAVG();//平均值
        float sd = matrixOperation.getSdByMatrix(row, avg, 0.0000001f);//标准差
        for (int i = 0; i < row.getY(); i++) {
            float value = (row.getValue(0, i) - avg) / sd;
            result.setValue(0, i, value);
        }
        if (isStudy) {
            NormErrorBody normErrorBody = new NormErrorBody();
            normErrorBody.setInsertMatrix(row);
            normErrorBody.setAvg(avg);
            normErrorBody.setSd(sd);
            normErrorBody.setOutMatrix(result);
            normErrorBodyMap.put(index, normErrorBody);
        }
        return result;
    }

    private Matrix layNorm(Matrix feature, boolean isStudy) throws Exception {//进行归一化
        int x = feature.getX();
        MatrixList outMatrixList = null;
        if (isStudy) {
            normErrorBodyMap.clear();
        }
        for (int i = 0; i < x; i++) {
            Matrix normData = norm(feature.getRow(i), isStudy, i);//back时候需要
            Matrix want = matrixOperation.add(matrixOperation.mulMatrix(normData, power), bTa);
            if (i == 0) {
                outMatrixList = new MatrixList(want, true);
            } else {
                outMatrixList.add(want);
            }
        }
        return outMatrixList.getMatrix();
    }

    public void setHiddenNerves(List<HiddenNerve> hiddenNerves) {
        this.hiddenNerves = hiddenNerves;
    }

    public void setMultiSelfAttention(MultiSelfAttention multiSelfAttention) {
        this.multiSelfAttention = multiSelfAttention;
    }

}
