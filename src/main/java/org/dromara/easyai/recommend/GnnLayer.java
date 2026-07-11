package org.dromara.easyai.recommend;

import org.dromara.easyai.config.GnnConfig;
import org.dromara.easyai.conv.DymStudy;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.matrixTools.Matrix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author lidapeng
 * @time 2026/7/11 08:58
 * @des gnn层
 */
public class GnnLayer {
    private final Map<Integer, GnnPower> powerMap = new HashMap<>();//权重矩阵
    private final DiscreteTable discreteTable;
    private final ActiveFunction activeFunction;
    private final DymStudy dymStudy;
    private GnnLayer sonLayer;
    private GnnLayer fatherLayer;
    private int featureLength;//特征维度

    public GnnLayer(GnnConfig gnnConfig, ActiveFunction activeFunction, DiscreteTable discreteTable) {//特征维度 类别数量
        dymStudy = new DymStudy(gnnConfig.getgMaxTh(), gnnConfig.isAuto(), gnnConfig.getLayGMaxTh());
        this.activeFunction = activeFunction;
        int gnnTypeNumber = gnnConfig.getGnnTypeNumber();
        featureLength = gnnConfig.getFeatureLength();
        this.discreteTable = discreteTable;
        for (int i = 0; i < gnnTypeNumber; i++) {
            GnnPower gnnPower = initGnnPower(featureLength);
            powerMap.put(i + 1, gnnPower);
        }
    }

    private GnnPower initGnnPower(int featureLength) {
        GnnPower gnnPower = new GnnPower();
        Matrix selfPower = new Matrix(featureLength, featureLength);
        Matrix otherPower = new Matrix(featureLength, featureLength);
        Matrix bais = new Matrix(1, featureLength);
        selfPower.randomInit(featureLength);
        otherPower.randomInit(featureLength);
        bais.randomInit(featureLength);
        gnnPower.setArf(0.5f);
        gnnPower.setSelfPower(selfPower);
        gnnPower.setOtherPower(otherPower);
        gnnPower.setBais(bais);
        return gnnPower;
    }


    private Matrix getFeature(GnnNode gnnNode) {
        Matrix feature;
        if (gnnNode.isDiscreteFeature()) {//离散特征数据
            feature = discreteTable.getFeature(gnnNode.getId());
        } else {
            feature = gnnNode.getFeature();
        }
        return feature;
    }

    public void connectSonLayer(GnnLayer sonLayer) {
        this.sonLayer = sonLayer;
    }

    public void connectFatherLayer(GnnLayer fatherLayer) {
        this.fatherLayer = fatherLayer;
    }
}
