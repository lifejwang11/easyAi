package org.wlld.transFormer.seflAttention;

import org.wlld.i.OutBack;
import org.wlld.matrixTools.Matrix;
import org.wlld.matrixTools.MatrixOperation;
import org.wlld.transFormer.CodecBlock;
import org.wlld.transFormer.FirstDecoderBlock;
import org.wlld.transFormer.model.LayNormModel;
import org.wlld.transFormer.nerve.HiddenNerve;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LayNorm {//残差与归一化
    private MultiSelfAttention multiSelfAttention;//多头
    private final CodecBlock myEncoderBlock;
    private final int featureDimension;//特征维度
    private List<HiddenNerve> hiddenNerves;//第一层隐层
    private final int type;//类别层模型需要保存
    private final Map<Long, Matrix> reMatrixMap = new HashMap<>();
    private final FirstDecoderBlock firstDecoderBlock;
    private Matrix bTa;//模型需要保存
    private Matrix power;//模型需要保存
    private Matrix myNormData;//第一步归一化后的数据
    private final double study;//学习率
    private Matrix myFinalError;//从FNN传来的总误差
    private int number;//记录fnn传来的误差次数
    private final MatrixOperation matrixOperation;

    public LayNormModel getModel() {
        LayNormModel layNormModel = new LayNormModel();
        layNormModel.setbTa(bTa.getMatrix());
        layNormModel.setPower(power.getMatrix());
        return layNormModel;
    }

    public void insertModel(LayNormModel layNormModel) throws Exception {
        insertPower(layNormModel.getPower(), power);
        insertPower(layNormModel.getbTa(), bTa);
    }

    private void insertPower(double[][] modelPower, Matrix power) throws Exception {
        for (int i = 0; i < power.getX(); i++) {
            for (int j = 0; j < power.getY(); j++) {
                power.setNub(i, j, modelPower[i][j]);
            }
        }
    }

    public LayNorm(int type, int featureDimension, CodecBlock myEncoderBlock, FirstDecoderBlock firstDecoderBlock
            , double study, int coreNumber) throws Exception {
        this.study = study;
        this.myEncoderBlock = myEncoderBlock;
        this.type = type;
        this.featureDimension = featureDimension;
        this.firstDecoderBlock = firstDecoderBlock;
        matrixOperation = new MatrixOperation(coreNumber);
        bTa = new Matrix(1, featureDimension);
        power = new Matrix(featureDimension, featureDimension);
        Random random = new Random();
        double sh = Math.sqrt(featureDimension);
        for (int i = 0; i < featureDimension; i++) {
            double value = random.nextDouble() / sh;
            bTa.setNub(0, i, value);
        }
        for (int i = 0; i < featureDimension; i++) {
            for (int j = 0; j < featureDimension; j++) {
                double value = random.nextDouble() / sh;
                power.setNub(i, j, value);
            }
        }
    }

    private Matrix back(Matrix errorMatrix, Matrix myData) throws Exception {
        Matrix subPower = matrixOperation.matrixMulPd(errorMatrix, myData, power, false);
        Matrix sub = matrixOperation.matrixMulPd(errorMatrix, myData, power, true);
        power = matrixOperation.add(subPower, power);
        double n = Math.sqrt(sub.getY());
        double nt = -n / (n - 1);
        Matrix subMatrix = new Matrix(1, sub.getY());
        for (int i = 0; i < sub.getY(); i++) {
            double subValue = sub.getNumber(0, i);
            double value = subValue * n * study + subMatrix.getNumber(0, i);
            subMatrix.setNub(0, i, value);
            for (int j = 0; j < sub.getY(); j++) {
                if (i != j) {
                    double otherValue = subValue * nt * study + subMatrix.getNumber(0, j);
                    subMatrix.setNub(0, j, otherValue);
                }
            }
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
        matrixOperation.mathMul(errorMatrix, study);
        int x = errorMatrix.getX();
        Matrix myError = null;
        for (int i = 0; i < x; i++) {
            Matrix error = errorMatrix.getRow(i);
            Matrix myData = myNormData.getRow(i);
            bTa = matrixOperation.add(error, bTa);//更新bTa
            Matrix myRowError = back(error, myData);
            if (i == 0) {
                myError = myRowError;
            } else {
                myError = matrixOperation.pushVector(myError, myRowError, true);
            }
        }
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
            , OutBack outBack, List<Integer> E, Matrix encoderFeature) throws Exception {//残差及归一化
        Matrix myMatrix = matrixOperation.add(feature, outMatrix);//残差相加
        Matrix out = layNorm(myMatrix, isStudy);
        if (type == 1) {
            if (myEncoderBlock != null) {
                sendHiddenParameter(out, eventID, isStudy, outBack, E, encoderFeature);//发送线性第一层
            } else if (firstDecoderBlock != null) {//解码器第一层//输出
                firstDecoderBlock.sendOutputMatrix(eventID, out, isStudy, outBack, E);
            }
        } else {//输出矩阵
            myEncoderBlock.sendOutputMatrix(eventID, out, isStudy, outBack, E, encoderFeature);
        }
    }

    public void addNormFromNerve(long eventID, boolean isStudy, Matrix parameter, Matrix allFeature,
                                 OutBack outBack, List<Integer> E, Matrix encoderFeature) throws Exception {
        Matrix matrixFeature;
        if (reMatrixMap.containsKey(eventID)) {
            Matrix myFeature = reMatrixMap.get(eventID);
            matrixFeature = matrixOperation.pushVector(myFeature, parameter, false);
        } else {
            matrixFeature = parameter;
        }
        reMatrixMap.put(eventID, matrixFeature);
        if (matrixFeature.getY() == featureDimension) {//执行残差
            reMatrixMap.remove(eventID);
            addNorm(matrixFeature, allFeature, eventID, isStudy, outBack, E, encoderFeature);
        }
    }

    private void sendHiddenParameter(Matrix feature, long eventId, boolean isStudy
            , OutBack outBack, List<Integer> E, Matrix encoderFeature) throws Exception {//hiddenNerves
        for (HiddenNerve hiddenNerve : hiddenNerves) {
            hiddenNerve.receive(feature, eventId, isStudy, outBack, E, encoderFeature);
        }
    }

    private Matrix norm(Matrix row) throws Exception {
        Matrix result = new Matrix(1, row.getY());
        double avg = row.getAVG();//平均值
        double sd = matrixOperation.getSdByMatrix(row, avg, 0.00001);//标准差
        for (int i = 0; i < row.getY(); i++) {
            double value = (row.getNumber(0, i) - avg) / sd;
            result.setNub(0, i, value);
        }
        return result;
    }

    private Matrix layNorm(Matrix feature, boolean isStudy) throws Exception {//进行归一化
        int x = feature.getX();
        Matrix out = null;
        if (isStudy) {
            myNormData = null;
        }
        for (int i = 0; i < x; i++) {
            Matrix normData = norm(feature.getRow(i));//back时候需要
            if (isStudy) {
                if (i == 0) {
                    myNormData = normData;
                } else {
                    myNormData = matrixOperation.pushVector(myNormData, normData, true);
                }
            }
            Matrix want = matrixOperation.add(matrixOperation.mulMatrix(normData, power), bTa);
            if (i == 0) {
                out = want;
            } else {
                out = matrixOperation.pushVector(out, want, true);
            }
        }
        return out;
    }

    public void setHiddenNerves(List<HiddenNerve> hiddenNerves) {
        this.hiddenNerves = hiddenNerves;
    }

    public void setMultiSelfAttention(MultiSelfAttention multiSelfAttention) {
        this.multiSelfAttention = multiSelfAttention;
    }

}
