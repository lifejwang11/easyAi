package org.dromara.easyai.matrixTools;

import org.dromara.easyai.resnet.entity.NormModel;

import java.util.Random;

/**
 * @author lidapeng
 * @time 2025/4/11 15:27
 * @des 矩阵批量归一化
 */
public class MatrixNorm {
    private Matrix bTa;//偏移值
    private Matrix power;//膨胀系数矩阵
    private final float studyRate;//全局学习率
    private final MatrixOperation matrixOperation = new MatrixOperation();
    private Matrix norm;

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

    public MatrixNorm(int size, float studyRate) throws Exception {
        bTa = new Matrix(size, size);
        power = new Matrix(size, size);
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

    private Matrix back(Matrix errorMatrix, Matrix myData) throws Exception {
        Matrix subPower = matrixOperation.matrixMulPd(errorMatrix, myData, power, false);
        Matrix sub = matrixOperation.matrixMulPd(errorMatrix, myData, power, true);
        int x = sub.getX();
        int y = sub.getY();
        power = matrixOperation.add(subPower, power);
        float n = (float) Math.sqrt(x * y);
        float nt = -n / (n - 1);
        Matrix subMatrix = new Matrix(x, y);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float subValue = sub.getNumber(i, j) * studyRate;
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
        return subMatrix;
    }

    public Matrix backError(Matrix errorMatrix) throws Exception {
        Matrix error = matrixOperation.mathMulBySelf(errorMatrix, studyRate);
        bTa = matrixOperation.add(error, bTa);//更新bTa
        return back(error, norm);
    }

    public Matrix norm(Matrix matrix) throws Exception {
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
        norm = result;
        return matrixOperation.add(matrixOperation.mulMatrix(result, power), bTa);
    }
}
