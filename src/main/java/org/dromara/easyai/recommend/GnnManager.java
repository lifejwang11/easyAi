package org.dromara.easyai.recommend;

import org.dromara.easyai.batchNerve.BatchNerveConfig;
import org.dromara.easyai.batchNerve.BatchNerveManager;
import org.dromara.easyai.config.GnnConfig;
import org.dromara.easyai.function.ReLu;
import org.dromara.easyai.i.ActiveFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @time 2026/7/15 14:15
 * @des gnn管理器
 */
public class GnnManager {
    private final ConnectionTable connectionTable;//特征离散表及聚合关系
    private final BatchNerveManager batchNerveManager;
    private final List<GnnLayer> gnnLayerList = new ArrayList<>();

    public GnnManager(GnnConfig gnnConfig, ActiveFunction activeFunction) throws Exception {
        int nodeSize = gnnConfig.getNodeSize();//节点数量
        int featureLength = gnnConfig.getFeatureLength();//特征维度
        int jumpTimes = gnnConfig.getJumpTimes();
        initGnnLayer(gnnConfig);
        GnnBack gnnBack = new GnnBack(gnnLayerList.get(jumpTimes - 1));
        connectionTable = new ConnectionTable(nodeSize, featureLength);
        batchNerveManager = new BatchNerveManager(getBathNerveConfig(gnnConfig), activeFunction, gnnBack);
    }

    public ConnectionTable getConnectionTable() {
        return connectionTable;
    }

    private void initGnnLayer(GnnConfig gnnConfig) {
        int jumpTimes = gnnConfig.getJumpTimes();
        for (int i = 0; i < jumpTimes; i++) {
            GnnLayer gnnLayer;
            if (i == jumpTimes - 1) {
                gnnLayer = new GnnLayer(gnnConfig, new ReLu(), connectionTable, batchNerveManager, i + 1);
            } else {
                gnnLayer = new GnnLayer(gnnConfig, new ReLu(), connectionTable, null, i + 1);
            }
            gnnLayerList.add(gnnLayer);
        }
        for (int i = 0; i < jumpTimes - 1; i++) {
            GnnLayer gnnLayer = gnnLayerList.get(i);
            GnnLayer nextGnnLayer = gnnLayerList.get(i + 1);
            gnnLayer.connectSonLayer(nextGnnLayer);
            nextGnnLayer.connectFatherLayer(gnnLayer);
        }
    }

    private BatchNerveConfig getBathNerveConfig(GnnConfig gnnConfig) {
        BatchNerveConfig batchNerveConfig = new BatchNerveConfig();
        batchNerveConfig.setInputSize(gnnConfig.getFeatureLength() * 2);
        batchNerveConfig.setHiddenSize(gnnConfig.getFeatureLength());
        batchNerveConfig.setOutSize(gnnConfig.getOutNumber());
        batchNerveConfig.setSoftMax(gnnConfig.isSoftMax());
        batchNerveConfig.setStudyRate(gnnConfig.getStudy());
        batchNerveConfig.setAuto(gnnConfig.isAuto());
        batchNerveConfig.setGMaxTh(gnnConfig.getgMaxTh());
        batchNerveConfig.setDeep(gnnConfig.getDeep());
        batchNerveConfig.setShowLog(gnnConfig.isShowLog());
        batchNerveConfig.setRegularModel(gnnConfig.getRegularModel());
        batchNerveConfig.setRegular(gnnConfig.getRegular());
        return batchNerveConfig;
    }
}
