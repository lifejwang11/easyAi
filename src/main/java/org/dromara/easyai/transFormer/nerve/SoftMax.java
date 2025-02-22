package org.dromara.easyai.transFormer.nerve;


import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;

import java.util.ArrayList;
import java.util.List;

public class SoftMax extends Nerve {
    private final List<OutNerve> outNerves;
    private final boolean isShowLog;
    private final MatrixOperation matrixOperation = new MatrixOperation();
    private final float timePunValue;//时间惩罚系数

    public SoftMax(List<OutNerve> outNerves, boolean isShowLog
            , int sensoryNerveNub, int hiddenNerveNub, int outNerveNub, float timePunValue) throws Exception {
        super(0, "softMax", 0, null, sensoryNerveNub, hiddenNerveNub, outNerveNub,
                null, 0, 0, 1);
        this.timePunValue = timePunValue;
        this.outNerves = outNerves;
        this.isShowLog = isShowLog;
    }

    private Matrix getSigmaByColum(Matrix matrix) throws Exception {
        int x = matrix.getX();
        int y = matrix.getY();
        Matrix myMatrix = new Matrix(1, y);
        float pun = 1;
        for (int i = x - 1; i >= 0; i--) {
            for (int j = 0; j < y; j++) {
                float value = myMatrix.getNumber(0, j) + matrix.getNumber(i, j) * pun;
                myMatrix.setNub(0, j, value);
            }
            pun = pun * timePunValue;
        }
        return myMatrix;
    }

    @Override
    protected void toOut(long eventId, Matrix parameter, boolean isStudy, OutBack outBack, List<Integer> E, boolean outAllPro) throws Exception {
        boolean allReady = insertMatrixParameter(eventId, parameter);
        if (allReady) {
            Matrix feature = reMatrixFeatures.get(eventId).getMatrix();//特征
            reMatrixFeatures.remove(eventId);
            int x = feature.getX();
            if (isStudy) {
                // System.out.println("分类数量:" + feature.getY());
                if (E.size() != x) {
                    throw new Exception("期望的序列长度与实际序列不相等！请检查期望E，补充漏掉的序列");
                }
                Matrix allError = null;
                for (int i = 0; i < x; i++) {
                    //Matrix row = feature.getRow(i);
                    Matrix r = feature.getSonOfMatrix(0, 0, i + 1, feature.getY());
                    Matrix row = getSigmaByColum(r);
                    Mes mes = softMax(true, row, false);//输出值
                    int key = E.get(i);
                    if (isShowLog) {
                        System.out.println("softMax==" + key + ",out==" + mes.poi + ",nerveId==" + mes.typeID);
                    }
                    Matrix errors = error(mes, key, i, x);
                    if (i == 0) {
                        allError = errors;
                    } else {
                        allError = matrixOperation.add(allError, errors);
                        //allError = matrixOperation.pushVector(allError, errors, true);
                    }
                }
                int size = outNerves.size();
                for (int i = 0; i < size; i++) {
                    Matrix errorMatrix = allError.getColumn(i);
                    outNerves.get(i).getGBySoftMax(errorMatrix, eventId);
                }
            } else {
                if (outBack != null) {
                    Matrix row = getSigmaByColum(feature);
                    Mes mes = softMax(false, row, outAllPro);//输出值
                    outBack.getBack(mes.poi, mes.typeID, eventId);
                    outBack.getBackMatrix(row, 1, eventId);
                    if (outAllPro) {
                        outBack.getSoftMaxBack(eventId, mes.softMax);
                    }
                } else {
                    throw new Exception("not find outBack");
                }
            }
        }
    }

    private Matrix error(Mes mes, int key, int featureIndex, int allSize) throws Exception {
        int t = key - 1;
        List<Float> softMax = mes.softMax;
        Matrix matrix = new Matrix(allSize, softMax.size());
        for (int i = 0; i < softMax.size(); i++) {
            float self = softMax.get(i);
            float myError;
            if (i != t) {
                myError = -self;
            } else {
                myError = 1 - self;
            }
            float pun = 1;
            for (int j = featureIndex; j >= 0; j--) {
                float error = myError * pun;
                matrix.setNub(j, i, error);
                pun = pun * timePunValue;
            }
        }
        return matrix;
    }

    private Mes softMax(boolean isStudy, Matrix matrix, boolean outAllPro) throws Exception {//计算当前输出结果
        float sigma = 0;
        int id = 0;
        float poi = 0;
        Mes mes = new Mes();
        int size = matrix.getY();
        for (int j = 0; j < size; j++) {
            float value = matrix.getNumber(0, j);
            sigma = (float) Math.exp(value) + sigma;
        }
        List<Float> softMax = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            float eSelf = (float) Math.exp(matrix.getNumber(0, i));
            float value = eSelf / sigma;
            if (isStudy || outAllPro) {
                softMax.add(value);
            }
            if (value > poi) {
                poi = value;
                id = i + 1;
            }
        }
        mes.softMax = softMax;
        mes.typeID = id;
        mes.poi = poi;
        return mes;
    }

    static class Mes {
        int typeID;
        float poi;
        List<Float> softMax;
    }
}
