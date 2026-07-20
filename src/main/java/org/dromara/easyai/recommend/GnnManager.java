package org.dromara.easyai.recommend;

import org.dromara.easyai.batchNerve.BatchNerveConfig;
import org.dromara.easyai.batchNerve.BatchNerveManager;
import org.dromara.easyai.config.GnnConfig;
import org.dromara.easyai.function.ReLu;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.recommend.model.GnnLayerModel;
import org.dromara.easyai.recommend.model.GnnModel;

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
    private final GnnInput gnnInput;

    public GnnManager(GnnConfig gnnConfig, ActiveFunction activeFunction) throws Exception {
        int nodeSize = gnnConfig.getNodeSize();//节点数量
        int featureLength = gnnConfig.getFeatureLength();//特征维度
        int jumpTimes = gnnConfig.getJumpTimes();
        GnnBack gnnBack = new GnnBack();
        connectionTable = new ConnectionTable(nodeSize, featureLength, gnnConfig.getStudyMaxJumpNumber()
                , gnnConfig.getStudyMinJumpNumber());
        batchNerveManager = new BatchNerveManager(getBathNerveConfig(gnnConfig), activeFunction, gnnBack);
        initGnnLayer(gnnConfig);
        gnnBack.setGnnLayer(gnnLayerList.get(jumpTimes - 1));
        gnnInput = new GnnInput(connectionTable, gnnLayerList.get(0));
    }

    public GnnModel getModel() {
        GnnModel gnnModel = new GnnModel();
        List<GnnLayerModel> gnnLayerModelList = new ArrayList<>();
        for (GnnLayer gnnLayer : gnnLayerList) {
            GnnLayerModel gnnLayerModel = gnnLayer.getModel();
            gnnLayerModelList.add(gnnLayerModel);
        }
        gnnModel.setConnectionModel(connectionTable.getModel());
        gnnModel.setBatchNerveModel(batchNerveManager.getModel());
        gnnModel.setGnnLayerModelList(gnnLayerModelList);
        return gnnModel;
    }

    public void insertModel(GnnModel gnnModel) {
        List<GnnLayerModel> gnnLayerModelList = gnnModel.getGnnLayerModelList();
        for (int i = 0; i < gnnLayerList.size(); i++) {
            GnnLayer gnnLayer = gnnLayerList.get(i);
            GnnLayerModel gnnLayerModel = gnnLayerModelList.get(i);
            if (gnnLayerModel != null) {
                gnnLayer.insertModel(gnnLayerModel);
            } else {
                System.out.println("警告！模型缺失聚合层:" + (i + 1) + ",的数据，请自行斟酌。");
            }
        }
        connectionTable.insertModel(gnnModel.getConnectionModel());
        batchNerveManager.insertModel(gnnModel.getBatchNerveModel());
    }

    public GnnInput getGnnInput() {
        return gnnInput;
    }

    private void initGnnLayer(GnnConfig gnnConfig) {
        int jumpTimes = gnnConfig.getJumpTimes();
        for (int i = 0; i < jumpTimes; i++) {
            GnnLayer gnnLayer;
            if (i == jumpTimes - 1) {
                gnnLayer = new GnnLayer(gnnConfig, new ReLu(), connectionTable, batchNerveManager, i);
            } else {
                gnnLayer = new GnnLayer(gnnConfig, new ReLu(), connectionTable, null, i);
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
        batchNerveConfig.setInputSize(gnnConfig.getFeatureLength());
        batchNerveConfig.setHiddenSize(gnnConfig.getFeatureLength() / 2);
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
