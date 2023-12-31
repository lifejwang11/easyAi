package org.wlld.rnnJumpNerveCenter;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.config.SentenceConfig;
import org.wlld.i.OutBack;
import org.wlld.naturalLanguage.word.WordEmbedding;
import org.wlld.rnnJumpNerveEntity.OutNerve;

import java.util.*;

public class SemanticsNerve {//语义解析
    private WordEmbedding wordEmbedding;//词向量嵌入
    private Matrix powerMatrix;//权重矩阵
    private boolean init = false;//是否进行了初始化
    private final List<OutNerve> myOutNerveList;//本层的输出神经元
    private Matrix matrixE;//期望矩阵
    private double step;//时间序列步长
    private final Map<Long, FeatureBody> featureBodyMap = new HashMap<>();//参数
    private int wordVectorDimension;//特征矩阵维度
    private final double studyPoint;//学习率
    private final boolean regular;//是否需要正则化
    private final double paramL;//正则系数

    public double[][] getModel() {
        return powerMatrix.getMatrix();
    }

    public void insertModel(double[][] power) throws Exception {
        for (int i = 0; i < powerMatrix.getX(); i++) {
            for (int j = 0; j < powerMatrix.getY(); j++) {
                powerMatrix.setNub(i, j, power[i][j]);
            }
        }
    }

    public void setMatrixE(String answer, long eventID) throws Exception {//设置期望矩阵
        Matrix featureMatrix = wordEmbedding.getEmbedding(answer, eventID).getFeatureMatrix();
        int x = featureMatrix.getX();
        int y = featureMatrix.getY();
        matrixE = new Matrix(1, y);
        for (int i = 0; i < x; i++) {
            double h = i * step;//时间编码
            for (int j = 0; j < y; j++) {
                double value = featureMatrix.getNumber(i, j) + h;
                featureMatrix.setNub(i, j, value);
            }
        }
        for (int j = 0; j < y; j++) {//列
            double sigMod = 0;
            for (int i = 0; i < x; i++) {
                sigMod = featureMatrix.getNumber(i, j) + sigMod;
            }
            sigMod = sigMod / x * 0.5;
            matrixE.setNub(0, j, sigMod);
        }
    }

    public SemanticsNerve(List<OutNerve> myOutNerveList, double studyPoint, boolean regular, double paramL) {
        this.myOutNerveList = myOutNerveList;
        this.studyPoint = studyPoint;
        this.regular = regular;
        this.paramL = paramL;
    }

    public void init(SentenceConfig config, WordEmbedding wordEmbedding) throws Exception {
        this.wordEmbedding = wordEmbedding;
        this.step = 1D / config.getMaxAnswerLength();
        wordVectorDimension = config.getWordVectorDimension();
        powerMatrix = new Matrix(wordVectorDimension, wordVectorDimension);
        Random random = new Random();
        for (int i = 0; i < wordVectorDimension; i++) {
            for (int j = 0; j < wordVectorDimension; j++) {
                powerMatrix.setNub(i, j, random.nextDouble());
            }
        }
        init = true;
    }

    private List<Double> updatePowerMatrix(Matrix matrixSub, Matrix featureMatrix) throws Exception {
        Matrix subPowerMatrix = new Matrix(wordVectorDimension, wordVectorDimension);
        double xgmSub = 0;
        List<Double> derFeature = new ArrayList<>();
        for (int j = 0; j < wordVectorDimension; j++) {//遍历权重矩阵的列，求矩阵梯度变化量
            double sub = matrixSub.getNumber(0, j);
            xgmSub = xgmSub + sub;
            for (int i = 0; i < wordVectorDimension; i++) {
                double subPower = featureMatrix.getNumber(0, i) * sub;//权重变化量
                subPowerMatrix.setNub(i, j, subPower);
            }
        }
        for (int i = 0; i < wordVectorDimension; i++) {
            double xgmPowerX = 0;
            for (int j = 0; j < wordVectorDimension; j++) {
                xgmPowerX = xgmPowerX + powerMatrix.getNumber(i, j);//权重
            }
            double subFeature = xgmSub * xgmPowerX;
            derFeature.add(subFeature);
        }
        if (regular) {//正则化
            MatrixOperation.mathMul(powerMatrix, paramL);
        }
        powerMatrix = MatrixOperation.add(powerMatrix, subPowerMatrix);//更新权重矩阵
        return derFeature;
    }

    private void semantics(FeatureBody featureBody, OutBack outBack, long eventID, boolean isStudy, int[] storeys, int index) throws Exception {//语义处理
        Matrix matrix = MatrixOperation.mulMatrix(featureBody.matrix, powerMatrix);
        featureBodyMap.remove(eventID);//移除参数
        if (isStudy) {
            if (matrixE != null) {
                Matrix matrixSub = MatrixOperation.sub(matrixE, matrix);
                MatrixOperation.mathMul(matrixSub, 0.5 * studyPoint);
                List<Double> backError = updatePowerMatrix(matrixSub, featureBody.matrix);//需要回传的误差
                for (int i = 0; i < myOutNerveList.size(); i++) {
                    myOutNerveList.get(i).backMatrixError(backError.get(i), eventID, storeys, index);
                }
            } else {
                throw new Exception("训练时没有传递期望矩阵");
            }
        } else {
            outBack.getBackMatrix(matrix, eventID);
        }
    }

    public void send(int id, double parameter, long eventID, OutBack outBack, boolean isStudy, int[] storeys, int index) throws Exception {
        if (init) {
            FeatureBody featureBody;
            if (featureBodyMap.containsKey(eventID)) {//存在
                featureBody = featureBodyMap.get(eventID);
                featureBody.matrix.setNub(0, id - 1, parameter);
                featureBody.size = featureBody.size + 1;
            } else {
                featureBody = new FeatureBody();
                Matrix matrix = new Matrix(1, wordVectorDimension);
                matrix.setNub(0, id - 1, parameter);
                featureBody.matrix = matrix;
                featureBody.size = 1;
                featureBodyMap.put(eventID, featureBody);
            }
            if (featureBody.size == wordVectorDimension) {
                semantics(featureBody, outBack, eventID, isStudy, storeys, index);
            }
        } else {
            throw new Exception("语义解析还没有进行初始化");
        }
    }

    static class FeatureBody {
        Matrix matrix;
        int size;
    }
}
