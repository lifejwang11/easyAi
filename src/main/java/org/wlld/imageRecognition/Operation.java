package org.wlld.imageRecognition;


import org.wlld.MatrixTools.Matrix;
import org.wlld.config.Kernel;
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
        List<Double> list = convolution(matrix);
        intoNerve(1, list, templeConfig.getSensoryNerves(), true, tagging);
    }

    //图像视觉
    public void look(Matrix matrix, long eventId) throws Exception {
        List<Double> list = convolution(matrix);
        intoNerve(eventId, list, templeConfig.getSensoryNerves(), false, null);
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
}