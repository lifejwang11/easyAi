package org.dromara.easyai.matrixTools;

import org.dromara.easyai.conv.DymStudy;
import org.dromara.easyai.resnet.entity.NormModel;

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
    private final Matrix bTaDymStudyRate;//偏移系数动态学习率
    private final Matrix powerDymStudyRate;//膨胀系数动态学习率
    private final DymStudy dymStudy;
    private final float studyRate;//全局学习率
    private final MatrixOperation matrixOperation = new MatrixOperation();
    private Map<Integer, Matrix> normMap = new ConcurrentHashMap<>();

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

    public MatrixNorm(int size, float studyRate, float gaMa, float gMaxTh, boolean auTo) throws Exception {
        dymStudy = new DymStudy(gaMa, gMaxTh, auTo);
        bTa = new Matrix(size, size);
        power = new Matrix(size, size);
        bTaDymStudyRate = new Matrix(size, size);
        powerDymStudyRate = new Matrix(size, size);
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
            Matrix myData = normMap.get(m);
            Matrix subPower = matrixOperation.matrixMulPd(errorMatrix, myData, power, false);
            if (allSubPower == null) {
                allSubPower = subPower;
            } else {
                allSubPower = matrixOperation.add(allSubPower, subPower);
            }
            Matrix sub = matrixOperation.matrixMulPd(errorMatrix, myData, power, true);
            int x = sub.getX();
            int y = sub.getY();
            float n = (float) Math.sqrt(x * y);
            float nt = -n / (n - 1);
            Matrix subMatrix = new Matrix(x, y);
            nextErrorMatrixList.add(subMatrix);
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    float subValue = sub.getNumber(i, j);
                    float value = subValue * n + subMatrix.getNumber(i, j);
                    subMatrix.setNub(i, j, value);
                    for (int k = 0; k < x; k++) {
                        for (int l = 0; l < y; l++) {
                            if (k != i || l != j) {
                                float otherValue = subValue * nt + subMatrix.getNumber(k, l);
                                subMatrix.setNub(k, l, otherValue);
                            }
                        }
                    }
                }
            }
        }
        matrixOperation.mathDiv(allSubPower, size);
        Matrix errorPower = dymStudy.getErrorMatrixByStudy(studyRate, powerDymStudyRate, allSubPower).getErrorMatrix();
        power = matrixOperation.add(errorPower, power);
        return nextErrorMatrixList;
    }

    public List<Matrix> backError(List<Matrix> errorMatrixList) throws Exception {
        Matrix avgError = null;
        int size = errorMatrixList.size();
        for (Matrix errorMatrix : errorMatrixList) {
            if (avgError == null) {
                avgError = errorMatrix.copy();
            } else {
                avgError = matrixOperation.add(errorMatrix, avgError);
            }
        }
        matrixOperation.mathDiv(avgError, size);
        Matrix error = dymStudy.getErrorMatrixByStudy(studyRate, bTaDymStudyRate, avgError).getErrorMatrix();
        bTa = matrixOperation.add(error, bTa);//更新bTa
        return back(errorMatrixList);
    }

    public Matrix norm(Matrix matrix, int m) throws Exception {
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
                float value = (matrix.getNumber(i, j) - avg) / sd;
                result.setNub(i, j, value);
            }
        }
        normMap.put(m, result);
        return matrixOperation.add(matrixOperation.mulMatrix(result, power), bTa);
    }
}
