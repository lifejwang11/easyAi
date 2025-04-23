package org.dromara.easyai.conv;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.nerveEntity.ConvParameter;

import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @time 2025/4/19 09:24
 * @des 学习率处理
 */
public class DymStudy {
    private final float gaMa;//梯度衰减率
    private final float gMaxTh;//梯度最大阈值
    private final boolean auto;//是否自动调整学习率
    private final MatrixOperation matrixOperation = new MatrixOperation();

    public DymStudy(float gaMa, float gMaxTh, boolean auto) {
        this.gaMa = gaMa;
        this.gMaxTh = gMaxTh;
        this.auto = auto;
        if (auto) {
            if (gaMa <= 0 || gaMa >= 1) {
                throw new IllegalArgumentException("gaMa 取值范围是(0,1)");
            }
            if (gMaxTh <= 0) {
                throw new IllegalArgumentException("gMaxTh 必须比0大");
            }
        }
    }

    private float getErrorNotAuto(float studyRate, float error) {
        return error * studyRate;
    }

    public float getNerveStudyError(Map<Integer, Float> dymStudyRate, int key, float g, float studyRate) {
        if (!auto) {
            return getErrorNotAuto(studyRate, g);
        }
        float gc = gCropping(g);
        float s = dymStudyRate.get(key);
        float sNext = gaMa * s + (1 - gaMa) * (float) Math.pow(gc, 2);
        float myStudyRate = studyRate / (float) Math.sqrt(sNext + 0.00000001f);
        dymStudyRate.put(key, sNext);
        return myStudyRate * gc;
    }

    public float getOneValueError(float studyRate, float g, ConvParameter convParameter) {
        if (!auto) {
            return getErrorNotAuto(studyRate, g);
        }
        float gc = gCropping(g);
        float s = convParameter.getStudyRateTh();
        float sNext = gaMa * s + (1 - gaMa) * (float) Math.pow(gc, 2);
        convParameter.setStudyRateTh(sNext);
        float myStudyRate = studyRate / (float) Math.sqrt(sNext + 0.00000001f);
        return myStudyRate * gc;
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

    public float getErrorValueByStudy(float studyRate, List<Float> sList, float g, int t) {
        if (!auto) {
            return getErrorNotAuto(studyRate, g);
        }
        float s = sList.get(t);
        g = gCropping(g);
        float sNext = gaMa * s + (1 - gaMa) * (float) Math.pow(g, 2);
        float myStudyRate = studyRate / (float) Math.sqrt(sNext + 0.00000001f);
        sList.set(t, sNext);
        return g * myStudyRate;
    }

    public Matrix getErrorMatrixByStudy(float studyRate, Matrix sMatrix, Matrix gMatrix) throws Exception {//获取动态学习率
        if (!auto) {
            return matrixOperation.mathMulBySelf(gMatrix, studyRate);
        }
        int x = sMatrix.getX();
        int y = sMatrix.getY();
        Matrix errorMatrix = new Matrix(x, y);
        if (x == gMatrix.getX() && y == gMatrix.getY()) {
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    float s = sMatrix.getNumber(i, j);
                    float g = gCropping(gMatrix.getNumber(i, j));
                    float sNext = gaMa * s + (1 - gaMa) * (float) Math.pow(g, 2);
                    sMatrix.setNub(i, j, sNext);
                    float myStudyRate = studyRate / (float) Math.sqrt(sNext + 0.00000001f);
                    errorMatrix.setNub(i, j, myStudyRate * g);
                }
            }
        }
        return errorMatrix;
    }

}
