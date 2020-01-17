package org.wlld.imageRecognition;


import org.wlld.MatrixTools.Matrix;
import org.wlld.config.Kernel;
import org.wlld.config.StudyPattern;
import org.wlld.nerveEntity.SensoryNerve;
import org.wlld.tools.ArithUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Operation {//进行计算
    private TempleConfig templeConfig;//配置初始化参数模板
    private Convolution convolution = new Convolution();

    public Operation(TempleConfig templeConfig) {
        this.templeConfig = templeConfig;
    }

    public List<Double> convolution(Matrix matrix) throws Exception {
        //进行卷积
        int maxNub = 0;
        if (templeConfig.getRow() >= templeConfig.getColumn()) {
            maxNub = templeConfig.getRow();
        } else {
            maxNub = templeConfig.getColumn();
        }
        Matrix matrix1 = convolution.getFeatures(matrix, maxNub);
        return sub(matrix1);
    }

    //模板学习
    public void study(Matrix matrix, Map<Integer, Double> tagging) throws Exception {
        if (templeConfig.getStudyPattern() == StudyPattern.Speed_Pattern) {
            List<Double> list = convolution(matrix);
            intoNerve(1, list, templeConfig.getSensoryNerves(), true, tagging);
        } else {
            throw new Exception("pattern is wrong");
        }
    }

    //卷积核学习
    public void learning(Matrix matrix, Map<Integer, Double> tagging, boolean isNerveStudy) throws Exception {
        if (templeConfig.getStudyPattern() == StudyPattern.Accuracy_Pattern) {
            boolean isKernelStudy = true;
            if (isNerveStudy) {
                isKernelStudy = false;
            }
            intoNerve2(1, matrix, templeConfig.getConvolutionNerveManager().getSensoryNerves(),
                    isKernelStudy, isNerveStudy, tagging);
        } else {
            throw new Exception("pattern is wrong");
        }
    }

    //图像视觉 speed 模式
    public void look(Matrix matrix, long eventId) throws Exception {
        if (templeConfig.getStudyPattern() == StudyPattern.Speed_Pattern) {
            List<Double> list = convolution(matrix);
            intoNerve(eventId, list, templeConfig.getSensoryNerves(), false, null);
        } else if (templeConfig.getStudyPattern() == StudyPattern.Accuracy_Pattern) {
            see(matrix, eventId);
        }
    }

    //图像视觉 Accuracy 模式
    private void see(Matrix matrix, long eventId) throws Exception {
        if (templeConfig.getStudyPattern() == StudyPattern.Accuracy_Pattern) {
            intoNerve2(eventId, matrix, templeConfig.getConvolutionNerveManager().getSensoryNerves(),
                    false, false, null);
        } else {
            throw new Exception("pattern is wrong");
        }
    }

    private List<Double> sub(Matrix matrix) throws Exception {//
        List<Double> list = new ArrayList<>();
        int x = matrix.getX() - 1;
        int y = matrix.getY() - 1;
        for (int i = 0; i < templeConfig.getRow(); i++) {
            for (int j = 0; j < templeConfig.getColumn(); j++) {
                if (i > x || j > y) {
                    list.add(0.0);
                } else {
                    //归一化处理
                    double nub = ArithUtil.div(matrix.getNumber(i, j), 10000000);
                    list.add(nub);
                }
            }
        }
        return list;
    }

    private void intoNerve(long eventId, List<Double> featurList, List<SensoryNerve> sensoryNerveList
            , boolean isStudy, Map<Integer, Double> map) throws Exception {
        for (int i = 0; i < sensoryNerveList.size(); i++) {
            sensoryNerveList.get(i).postMessage(eventId, featurList.get(i), isStudy, map);
        }
    }

    private void intoNerve2(long eventId, Matrix featur, List<SensoryNerve> sensoryNerveList
            , boolean isKernelStudy, boolean isNerveStudy, Map<Integer, Double> E) throws Exception {
        for (int i = 0; i < sensoryNerveList.size(); i++) {
            sensoryNerveList.get(i).postMatrixMessage(eventId, featur, isKernelStudy, isNerveStudy, E);
        }
    }
}