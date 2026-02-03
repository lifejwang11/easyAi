package org.dromara.easyai.conv;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.nerveEntity.ConvParameter;

import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @time 2025/4/19 09:24
 * @des 学习率处理 AdamW
 */
public class DymStudy {
    private final float gMaxTh;//梯度最大阈值
    private final float layGMaxTh;//层梯度最大阈值
    private final boolean auto;//是否自动调整学习率
    private final MatrixOperation matrixOperation = new MatrixOperation();

    public DymStudy(float gMaxTh, boolean auto, float layGMaxTh) {
        this.gMaxTh = gMaxTh;
        this.auto = auto;
        this.layGMaxTh = layGMaxTh;
        if (auto) {
            if (gMaxTh <= 0) {
                throw new IllegalArgumentException("gMaxTh 必须比0大");
            }
        }
    }

    private float getErrorNotAuto(float studyRate, float error) {
        return error * studyRate;
    }

    public float getNerveStudyError(Map<Integer, Float> dymStudyRate, Map<Integer, Float> dymStudyRate2,
                                    int key, float g, float studyRate, int times) {
        float gc = gCropping(g);
        if (!auto) {
            return getErrorNotAuto(studyRate, gc);
        }
        float s1 = dymStudyRate.get(key);
        float s2 = dymStudyRate2.get(key);
        float[] result = getAdamW(s1, s2, gc, times, studyRate);
        dymStudyRate.put(key, result[0]);
        dymStudyRate2.put(key, result[1]);
        return result[2];
    }

    public float getOneValueError(float studyRate, float g, ConvParameter convParameter, int times) {
        float gc = gCropping(g);
        if (!auto) {
            return getErrorNotAuto(studyRate, gc);
        }
        float s1 = convParameter.getStudyRateTh();
        float s2 = convParameter.getStudyRateTh2();
        float[] result = getAdamW(s1, s2, gc, times, studyRate);
        convParameter.setStudyRateTh(result[0]);
        convParameter.setStudyRateTh2(result[1]);
        return result[2];
    }

    private float gCropping(float g) {//梯度裁剪
        if (Math.abs(g) > gMaxTh) {
            if (g < 0) {
                return -1 * gMaxTh;
            } else {
                return 1 * gMaxTh;
            }
        }
        return g;
    }

    public float getErrorValueByStudy(float studyRate, List<Float> sList, List<Float> s2List, float g, int t, int times) {
        g = gCropping(g);
        if (!auto) {
            return getErrorNotAuto(studyRate, g);
        }
        float s1 = sList.get(t);
        float s2 = s2List.get(t);
        float[] result = getAdamW(s1, s2, g, times, studyRate);
        sList.set(t, result[0]);
        s2List.set(t, result[1]);
        return result[2];
    }

    private float[] getAdamW(float s1, float s2, float g, int times, float studyRate) {//做adamW运算
        //梯度衰减率 一阶矩衰减系数
        float gaMa = 0.9f;
        float sNext1 = gaMa * s1 + (1 - gaMa) * g;//更新一阶矩
        //二阶矩
        float gaMaSecond = 0.999f;
        float sNext2 = gaMaSecond * s2 + (1 - gaMaSecond) * (float) Math.pow(g, 2);//更新二阶矩
        double upNext1 = sNext1 / (1 - Math.pow(gaMa, times));//一阶矩修正
        double upNext2 = sNext2 / (1 - Math.pow(gaMaSecond, times));//二阶矩修正
        double error = (upNext1 / (Math.sqrt(upNext2) + 0.00000001)) * studyRate;
        return new float[]{sNext1, sNext2, (float) error};
    }

    public Matrix getClipMatrix(Matrix gMatrix, boolean layNorm) throws Exception {
        float norm = matrixOperation.getNorm(gMatrix);
        Matrix gcMatrix;
        float myGMaxTh = gMaxTh;
        if (layNorm) {
            myGMaxTh = layGMaxTh;
        }
        if (norm > myGMaxTh) {
            float s = myGMaxTh / norm;
            gcMatrix = matrixOperation.mathMulBySelf(gMatrix, s);
        } else {
            gcMatrix = gMatrix;
        }
        return gcMatrix;
    }

    public Matrix getErrorMatrixByStudy(float studyRate, Matrix s1Matrix, Matrix s2Matrix, Matrix gMatrix, int times) throws Exception {//获取动态学习率
        Matrix gcMatrix = getClipMatrix(gMatrix, false);
        if (!auto) {
            return matrixOperation.mathMulBySelf(gcMatrix, studyRate);
        }
        int x = s1Matrix.getX();
        int y = s1Matrix.getY();
        Matrix errorMatrix = new Matrix(x, y);
        if (x == gcMatrix.getX() && y == gcMatrix.getY()) {
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    float s1 = s1Matrix.getNumber(i, j);//一阶矩
                    float s2 = s2Matrix.getNumber(i, j);//二阶矩
                    float g = gcMatrix.getNumber(i, j);//梯度
                    float[] result = getAdamW(s1, s2, g, times, studyRate);
                    s1Matrix.setNub(i, j, result[0]);
                    s2Matrix.setNub(i, j, result[1]);
                    errorMatrix.setNub(i, j, result[2]);
                }
            }
        }
        return errorMatrix;
    }

    public void regMatrix(Matrix power, float regRate, float studyRate) throws Exception {//正则化权重矩阵
        int x = power.getX();
        int y = power.getY();
        float rate = regRate * studyRate;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = power.getNumber(i, j);
                if (value != 0) {
                    float regValue = value - value * rate;
                    power.setNub(i, j, regValue);
                }
            }
        }
    }
}
