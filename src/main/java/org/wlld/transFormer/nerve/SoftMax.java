package org.wlld.transFormer.nerve;


import org.wlld.i.OutBack;
import org.wlld.matrixTools.Matrix;
import org.wlld.matrixTools.MatrixOperation;

import java.util.ArrayList;
import java.util.List;

public class SoftMax extends Nerve {
    private final List<OutNerve> outNerves;
    private final boolean isShowLog;
    private final MatrixOperation matrixOperation = new MatrixOperation();

    public SoftMax(List<OutNerve> outNerves, boolean isShowLog
            , int sensoryNerveNub, int hiddenNerveNub, int outNerveNub) throws Exception {
        super(0, "softMax", 0, null, sensoryNerveNub, hiddenNerveNub, outNerveNub,
                null, 0, 0, 1);
        this.outNerves = outNerves;
        this.isShowLog = isShowLog;
    }

    @Override
    protected void toOut(long eventId, Matrix parameter, boolean isStudy, OutBack outBack, List<Integer> E, boolean outAllPro) throws Exception {
        boolean allReady = insertMatrixParameter(eventId, parameter);
        if (allReady) {
            Matrix feature = reMatrixFeatures.get(eventId);//特征
            reMatrixFeatures.remove(eventId);
            int x = feature.getX();
            if (isStudy) {
                if (E.size() != x) {
                    throw new Exception("期望的序列长度与实际序列不相等！请检查期望E，补充漏掉的序列");
                }
                Matrix allError = null;
                for (int i = 0; i < x; i++) {
                    Matrix row = feature.getRow(i);
                    Mes mes = softMax(true, row, false);//输出值
                    int key = E.get(i);
                    if (isShowLog) {
                        System.out.println("softMax==" + key + ",out==" + mes.poi + ",nerveId==" + mes.typeID);
                    }
                    Matrix errors = error(mes, key);
                    if (i == 0) {
                        allError = errors;
                    } else {
                        allError = matrixOperation.pushVector(allError, errors, true);
                    }
                }
                int size = outNerves.size();
                for (int i = 0; i < size; i++) {
                    Matrix errorMatrix = allError.getColumn(i);
                    outNerves.get(i).getGBySoftMax(errorMatrix, eventId);
                }
            } else {
                if (outBack != null) {
                    Mes mes = softMax(false, feature.getRow(x - 1), outAllPro);//输出值
                    outBack.getBack(mes.poi, mes.typeID, eventId);
                    if (outAllPro) {
                        outBack.getSoftMaxBack(eventId, mes.softMax);
                    }
                } else {
                    throw new Exception("not find outBack");
                }
            }
        }
    }

    private Matrix error(Mes mes, int key) throws Exception {
        int t = key - 1;
        List<Double> softMax = mes.softMax;
        Matrix matrix = new Matrix(1, softMax.size());
        for (int i = 0; i < softMax.size(); i++) {
            double self = softMax.get(i);
            double myError;
            if (i != t) {
                myError = -self;
            } else {
                myError = 1 - self;
            }
            matrix.setNub(0, i, myError);
        }
        return matrix;
    }

    private Mes softMax(boolean isStudy, Matrix matrix, boolean outAllPro) throws Exception {//计算当前输出结果
        double sigma = 0;
        int id = 0;
        double poi = 0;
        Mes mes = new Mes();
        int size = matrix.getY();
        for (int j = 0; j < size; j++) {
            double value = matrix.getNumber(0, j);
            sigma = Math.exp(value) + sigma;
        }
        List<Double> softMax = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            double eSelf = Math.exp(matrix.getNumber(0, i));
            double value = eSelf / sigma;
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
        double poi;
        List<Double> softMax;
    }
}
