package org.dromara.easyai.batchNerve;

import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @time 2026/1/6 15:10
 * @des 批量训练 softMax
 */
public class SoftMaxByQBlock {
    private final QBlock fatherBlock;//向后模块
    private final MatrixOperation matrixOperation = new MatrixOperation();
    private final boolean showLog;

    public SoftMaxByQBlock(QBlock fatherBlock, boolean showLog) {
        this.fatherBlock = fatherBlock;
        this.showLog = showLog;
    }

    private void normMax(Matrix feature) throws Exception {
        int x = feature.getX();
        int y = feature.getY();
        float max = feature.getMaxValue();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = feature.getNumber(i, j);
                feature.setNub(i, j, value - max);
            }
        }
    }

    public void postMessage(List<FeatureBody> featureBodies, boolean study, OutBack outBack, long eventId
            , Map<Integer, Float> pd) throws Exception {
        List<Matrix> errorBodies = new ArrayList<>();
        for (FeatureBody featureBody : featureBodies) {
            Matrix feature = featureBody.getFeature();
            normMax(feature);
            Mes mes = softMax(feature);
            if (study) {//训练
                int key = 0;
                Map<Integer, Float> E = featureBody.getE();
                for (Map.Entry<Integer, Float> entry : E.entrySet()) {
                    if (entry.getValue() > 0.9) {
                        key = entry.getKey();
                        break;
                    }
                }
                boolean errorPD = false;//误差惩罚
                float pdRate = 1;
                if (pd != null && pd.containsKey(key) && key != mes.typeID) {
                    pdRate = pd.get(key);
                    errorPD = true;
                }
                if (showLog) {
                    if (outBack != null) {
                        outBack.getStudyLog(key, mes.poi, mes.typeID);
                    } else {
                        System.out.println("softMax==" + key + ",out==" + mes.poi + ",nerveId==" + mes.typeID);
                    }
                }
                Matrix errors = error(mes, key);
                if (errorPD) {
                    matrixOperation.mathMul(errors, pdRate);
                }
                errorBodies.add(errors);
            } else {//输出
                if (outBack != null) {
                    outBack.getBack(mes.poi, mes.typeID, eventId);
                    outBack.getSoftMaxBack(eventId, mes.softMax);
                    break;
                } else {
                    throw new Exception("not find outBack");
                }
            }
        }
        if (study) {
            fatherBlock.backError(errorBodies);
        }
    }

    private Matrix error(Mes mes, int key) throws Exception {
        int t = key - 1;
        List<Float> softMax = mes.softMax;
        Matrix errorMatrix = new Matrix(1, softMax.size());
        for (int i = 0; i < softMax.size(); i++) {
            float self = softMax.get(i);
            float myError;
            if (i != t) {
                myError = -self;
            } else {
                myError = 1 - self;
            }
            errorMatrix.setNub(0, i, myError);
        }
        return errorMatrix;
    }

    private Mes softMax(Matrix matrix) throws Exception {//计算当前输出结果
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
            softMax.add(value);
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
