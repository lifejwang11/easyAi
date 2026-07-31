package org.dromara.easyai.matrixTools;

import org.dromara.easyai.conv.DymStudy;
import org.dromara.easyai.resnet.entity.NormModel;
import org.dromara.easyai.transFormer.seflAttention.NormErrorBody;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lidapeng
 * @time 2025/4/11 15:27
 * @des 矩阵批量归一化
 */
public class MatrixNorm {
    private Matrix bTa;//偏移值
    private Matrix power;//膨胀系数矩阵
    private final Matrix bTaDymStudyRate;//一阶偏移系数动态学习率
    private final Matrix powerDymStudyRate;//一阶膨胀系数动态学习率
    private final Matrix bTaDymStudyRate2;//二阶偏移系数动态学习率
    private final Matrix powerDymStudyRate2;//二阶膨胀系数动态学习率
    private final DymStudy dymStudy;
    private final float studyRate;//全局学习率
    private final MatrixOperation matrixOperation = new MatrixOperation();
    private final Map<Integer, NormErrorBody> normMap = new ConcurrentHashMap<>();
    private int times = 0;//迭代次数

    public NormModel getModel() {
        NormModel normModel = new NormModel();
        normModel.setBtaParameter(bTa.getMatrixModel());
        normModel.setPowerParameter(power.getMatrixModel());
        return normModel;
    }

    public void insertModel(NormModel normModel) {
        bTa.insertMatrixModel(normModel.getBtaParameter());
        power.insertMatrixModel(normModel.getPowerParameter());
    }

    public MatrixNorm(int size, float studyRate, DymStudy dymStudy) throws Exception {
        this.dymStudy = dymStudy;
        bTa = new Matrix(size, size);
        power = new Matrix(size, size);
        bTaDymStudyRate = new Matrix(size, size);
        powerDymStudyRate = new Matrix(size, size);
        bTaDymStudyRate2 = new Matrix(size, size);
        powerDymStudyRate2 = new Matrix(size, size);
        this.studyRate = studyRate;
        initPower(power);
        initPower(bTa);
    }

    private void initPower(Matrix matrix) throws Exception {
        int size = matrix.getX();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix.setNub(i, j, random.nextFloat() / size);
            }
        }
    }

    private List<Matrix> back(List<Matrix> errorMatrixList) throws Exception {
        int size = errorMatrixList.size();
        Matrix allSubPower = null;
        List<Matrix> nextErrorMatrixList = new ArrayList<>();
        for (int m = 0; m < size; m++) {//遍历没每张图片的误差
            Matrix errorMatrix = errorMatrixList.get(m);
            NormErrorBody normErrorBody = normMap.get(m);
            Matrix myData = normErrorBody.getOutMatrix();
            Matrix insertMatrix = normErrorBody.getInsertMatrix();//归一化前输入矩阵
            float avg = normErrorBody.getAvg();//平均值
            float sd = normErrorBody.getSd() + 0.0000001f;//标准差
            Matrix subPower = matrixOperation.matrixMulPd(errorMatrix, myData, power, false);
            if (allSubPower == null) {
                allSubPower = subPower;
            } else {
                allSubPower = matrixOperation.add(allSubPower, subPower);
            }
            //输出梯度
            Matrix sub = matrixOperation.matrixMulPd(errorMatrix, myData, power, true);
            int x = sub.getX();
            int y = sub.getY();
            float n = x * y;
            float var = sd * sd * n;
            float sigmaG = sub.getSigma() / (n * sd);//所有分量梯度的和 / n *标准差
            float allZk = 0;
            for (int k = 0; k < x; k++) {
                for (int l = 0; l < y; l++) {
                    float zk = sub.getValue(k, l) * myData.getValue(k, l);
                    allZk = allZk + zk;
                }
            }

            Matrix subMatrix = new Matrix(x, y);
            nextErrorMatrixList.add(subMatrix);
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    float subValue = sub.getValue(i, j) / sd;
                    float t = (insertMatrix.getValue(i, j) - avg) / var;
                    float error = subValue - sigmaG - t * allZk;
                    subMatrix.setValue(i, j, error);
                }
            }
        }
        float mySize = 1f / size;
        matrixOperation.mathMul(allSubPower, mySize);
        Matrix errorPower = dymStudy.getErrorMatrixByStudy(studyRate, powerDymStudyRate, powerDymStudyRate2, allSubPower, times);
        power = matrixOperation.add(errorPower, power);
        return nextErrorMatrixList;
    }

    public List<Matrix> backError(List<Matrix> errorMatrixList) throws Exception {
        times++;
        Matrix avgError = null;
        int size = errorMatrixList.size();
        for (Matrix errorMatrix : errorMatrixList) {
            if (avgError == null) {
                avgError = errorMatrix.copy();
            } else {
                avgError = matrixOperation.add(errorMatrix, avgError);
            }
        }
        float mySize = 1f / size;
        matrixOperation.mathMul(avgError, mySize);
        Matrix error = dymStudy.getErrorMatrixByStudy(studyRate, bTaDymStudyRate, bTaDymStudyRate2, avgError, times);
        bTa = matrixOperation.add(error, bTa);//更新bTa
        return back(errorMatrixList);
    }

    public Matrix norm(Matrix matrix, int m, boolean isStudy) throws Exception {
        int x = matrix.getX();
        int y = matrix.getY();
        if (x != y) {
            throw new Exception("必须是方阵才能进行全矩阵的归一化");
        }
        Matrix result = new Matrix(x, y);
        float avg = matrix.getAVG();//平均值
        float sd = matrixOperation.getSdByMatrix(matrix, avg, 0.0000001f);//标准差
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = (matrix.getValue(i, j) - avg) / sd;
                result.setValue(i, j, value);
            }
        }
        if (isStudy) {
            NormErrorBody normErrorBody = new NormErrorBody();
            normErrorBody.setInsertMatrix(matrix);
            normErrorBody.setSd(sd);
            normErrorBody.setAvg(avg);
            normErrorBody.setOutMatrix(result);
            normMap.put(m, normErrorBody);
        }
        return matrixOperation.add(matrixOperation.mulMatrix(result, power), bTa);
    }
}
