package org.dromara.easyai.batchNerve;

import org.dromara.easyai.config.RZ;
import org.dromara.easyai.conv.DymStudy;
import org.dromara.easyai.conv.MyStudy;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.i.CustomEncoding;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author lidapeng
 * @time 2025/8/7 08:55
 * @des QLearning 基础运算单元
 */
public class QBlock {
    private final DymStudy dymStudy;
    private final ActiveFunction activeFunction;
    private Matrix powerMatrix;//权重矩阵
    private Matrix bMatrix;//偏移矩阵
    private final Matrix bDymStudyRate;//偏移量动态学习率
    private final Matrix powerDymStudyRate;//权重动态学习率
    private final MatrixOperation matrixOperation = new MatrixOperation();
    private final List<Matrix> inputMatrixList = new ArrayList<>();//输入矩阵
    private final List<Matrix> outputMatrixList = new ArrayList<>();
    private QBlock sonBlock;//向前模块
    private QBlock fatherBlock;//向后模块
    private SoftMaxByQBlock softMaxByQBlock;
    private final float studyRate;
    private final CustomEncoding customEncoding;
    private final boolean showLog;
    private final int regularModel;//正则模式
    private final float regular;//正则系数

    public QBlock(DymStudy dymStudy, int inputSize, int outputSize, ActiveFunction activeFunction
            , float studyRate, CustomEncoding customEncoding, boolean showLog, int regularModel, float regular) throws Exception {
        Random random = new Random();
        this.customEncoding = customEncoding;
        this.showLog = showLog;
        this.studyRate = studyRate;
        this.dymStudy = dymStudy;
        this.activeFunction = activeFunction;
        this.powerMatrix = new Matrix(inputSize, outputSize);
        this.powerDymStudyRate = new Matrix(inputSize, outputSize);
        this.bMatrix = new Matrix(1, outputSize);
        this.bDymStudyRate = new Matrix(1, outputSize);
        this.regularModel = regularModel;
        this.regular = regular;
        initMatrix(powerMatrix, random);
        initMatrix(bMatrix, random);
    }

    public void setSoftMaxByQBlock(SoftMaxByQBlock softMaxByQBlock) {
        this.softMaxByQBlock = softMaxByQBlock;
    }

    public QBlockModel getModel() {
        QBlockModel qBlockModel = new QBlockModel();
        qBlockModel.setBtaModel(bMatrix.getMatrixModel());
        qBlockModel.setPowerModel(powerMatrix.getMatrixModel());
        return qBlockModel;
    }

    public void insertModel(QBlockModel qBlockModel) {
        bMatrix.insertMatrixModel(qBlockModel.getBtaModel());
        powerMatrix.insertMatrixModel(qBlockModel.getPowerModel());
    }

    public void postMassage(List<FeatureBody> featureBodies, boolean study, OutBack outBack, long eventID,
                            Map<Integer, Float> pd) throws Exception {
        calculation(featureBodies, study);
        if (sonBlock != null) {
            sonBlock.postMassage(featureBodies, study, outBack, eventID, pd);
        } else {//走到尽头了
            if (softMaxByQBlock != null) {
                softMaxByQBlock.postMessage(featureBodies, study, outBack, eventID, pd);
            } else {//
                errorOperation(featureBodies, study, outBack, eventID);
            }
        }
    }

    private void errorOperation(List<FeatureBody> featureBodies, boolean study, OutBack outBack, long eventID) throws Exception {
        if (study) {
            List<Matrix> errorBodies = new ArrayList<>();
            for (FeatureBody featureBody : featureBodies) {
                Matrix feature = featureBody.getFeature();
                Map<Integer, Float> EMap = featureBody.getE();
                int y = feature.getY();
                Matrix errors = new Matrix(1, y);
                for (int j = 0; j < y; j++) {
                    if (EMap.containsKey(j + 1)) {
                        float e = EMap.get(j + 1);
                        float out = feature.getNumber(0, j);
                        errors.setNub(0, j, e - out);
                        if (showLog) {
                            if (outBack != null) {
                                outBack.getStudyLog(e, out, j + 1);
                            } else {
                                System.out.println("E==" + e + ",out==" + out + ",nerveId==" + (j + 1));
                            }
                        }
                    } else {
                        throw new Exception("有输出神经元ID 没有分配期望值");
                    }
                }
                errorBodies.add(errors);
            }
            backError(errorBodies);
        } else {
            if (outBack != null) {
                FeatureBody featureBody = featureBodies.get(0);
                Matrix outMatrix = featureBody.getFeature();
                outBack.getBackMatrix(outMatrix, 0, eventID);
            } else {
                throw new Exception("not find outBack");
            }
        }
    }


    private Matrix getRegularMatrix(Matrix pMatrix, Matrix studyRateMatrix) throws Exception {//获取正则矩阵
        int x = pMatrix.getX();
        int y = pMatrix.getY();
        Matrix reMatrix = new Matrix(x, y);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = pMatrix.getNumber(i, j);
                float studyRate = this.studyRate;
                if (studyRateMatrix != null) {
                    studyRate = studyRateMatrix.getNumber(i, j);
                }
                float re = 0;
                if (regularModel == RZ.L2) {//L2正则
                    re = regular * -value * studyRate;
                } else {//L1正则
                    if (value > 0) {
                        re = -regular * studyRate;
                    } else if (value < 0) {
                        re = regular * studyRate;
                    }
                }
                reMatrix.setNub(i, j, re);
            }
        }
        return reMatrix;
    }

    public void backError(List<Matrix> errorBodies) throws Exception {//返回误差
        List<Matrix> activeGMatrixList = new ArrayList<>();//脱了激活函数的梯度矩阵集合
        List<Matrix> powerGMatrixList = new ArrayList<>();//权重梯度矩阵集合
        List<Matrix> nextErrorMatrixList = new ArrayList<>();//下一层权重误差
        Matrix avgGActiveMatrix = null;
        Matrix avgGPowerMatrix = null;
        int size = errorBodies.size();
        for (int i = 0; i < size; i++) {
            Matrix errors = errorBodies.get(i);
            Matrix outMatrix = outputMatrixList.get(i);
            Matrix featureMatrix = inputMatrixList.get(i);
            Matrix gMatrix = backActiveMatrix(outMatrix, errors);//脱掉激活函数
            Matrix gPowerMatrix = matrixOperation.matrixMulPd(gMatrix, featureMatrix, powerMatrix, false);
            Matrix nextErrorMatrix = matrixOperation.matrixMulPd(gMatrix, featureMatrix, powerMatrix, true);
            activeGMatrixList.add(gMatrix);
            powerGMatrixList.add(gPowerMatrix);
            nextErrorMatrixList.add(nextErrorMatrix);
        }
        for (int i = 0; i < size; i++) {
            Matrix gActiveMatrix = activeGMatrixList.get(i);//更新偏移量
            Matrix gPowerMatrix = powerGMatrixList.get(i);//更新权重
            if (avgGActiveMatrix == null) {
                avgGActiveMatrix = gActiveMatrix;
                avgGPowerMatrix = gPowerMatrix;
            } else {
                avgGActiveMatrix = matrixOperation.add(avgGActiveMatrix, gActiveMatrix);
                avgGPowerMatrix = matrixOperation.add(avgGPowerMatrix, gPowerMatrix);
            }
        }
        matrixOperation.mathDiv(avgGActiveMatrix, size);
        matrixOperation.mathDiv(avgGPowerMatrix, size);
        MyStudy btErrorStudy = dymStudy.getErrorMatrixByStudy(studyRate, bDymStudyRate, avgGActiveMatrix);
        MyStudy powerErrorStudy = dymStudy.getErrorMatrixByStudy(studyRate, powerDymStudyRate, avgGPowerMatrix);
        Matrix btErrorMatrix = btErrorStudy.getErrorMatrix();
        Matrix powerErrorMatrix = powerErrorStudy.getErrorMatrix();
        bMatrix = matrixOperation.add(bMatrix, btErrorMatrix);
        powerMatrix = matrixOperation.add(powerMatrix, powerErrorMatrix);
        if (regularModel != RZ.NOT_RZ) {//有正则
            //这里处理正则
            Matrix btStudyMatrix = btErrorStudy.getStudyRateMatrix();
            Matrix powerStudyMatrix = powerErrorStudy.getStudyRateMatrix();
            Matrix reBtMatrix = getRegularMatrix(bMatrix, btStudyMatrix);
            Matrix rePowerMatrix = getRegularMatrix(powerMatrix, powerStudyMatrix);
            bMatrix = matrixOperation.add(bMatrix, reBtMatrix);
            powerMatrix = matrixOperation.add(powerMatrix, rePowerMatrix);
        }
        if (fatherBlock != null) {
            fatherBlock.backError(nextErrorMatrixList);
        } else if (customEncoding != null) {//误差回传
            customEncoding.backErrorList(nextErrorMatrixList);
        }
    }

    private Matrix backActiveMatrix(Matrix outMatrix, Matrix errors) throws Exception {
        int size = errors.getY();
        Matrix errorMatrix = new Matrix(outMatrix.getX(), outMatrix.getY());
        for (int j = 0; j < size; j++) {
            float error = errors.getNumber(0, j) * activeFunction.functionG(outMatrix.getNumber(0, j));
            errorMatrix.setNub(0, j, error);
        }
        return errorMatrix;
    }

    private void calculation(List<FeatureBody> featureBodies, boolean study) throws Exception {
        if (study) {
            inputMatrixList.clear();
            outputMatrixList.clear();
        }
        for (FeatureBody featureBody : featureBodies) {
            Matrix featureMatrix = featureBody.getFeature();
            Matrix result = matrixOperation.mulMatrix(featureMatrix, powerMatrix);
            if (study) {
                inputMatrixList.add(featureMatrix.copy());
            }
            Matrix outMatrix = matrixOperation.add(result, bMatrix);
            int x = outMatrix.getX();
            int y = outMatrix.getY();
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    float value = activeFunction.function(outMatrix.getNumber(i, j));
                    outMatrix.setNub(i, j, value);
                }
            }
            if (study) {
                outputMatrixList.add(outMatrix.copy());
            }
            featureBody.setFeature(outMatrix);
        }
    }

    private void initMatrix(Matrix matrix, Random random) throws Exception {//初始化矩阵
        int x = matrix.getX();
        int y = matrix.getY();
        float sh = (float) Math.sqrt(x);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                matrix.setNub(i, j, random.nextFloat() / sh);
            }
        }
    }

    public void setSonBlock(QBlock sonBlock) {
        this.sonBlock = sonBlock;
    }

    public void setFatherBlock(QBlock fatherBlock) {
        this.fatherBlock = fatherBlock;
    }
}
