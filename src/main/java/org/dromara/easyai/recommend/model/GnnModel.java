package org.dromara.easyai.recommend.model;

import org.dromara.easyai.batchNerve.BatchNerveModel;

import java.util.List;

/**
 * @author lidapeng
 * @time 2026/7/20 10:00
 * @des gnn模型
 */
public class GnnModel {
    private List<GnnLayerModel> gnnLayerModelList;//聚合层model
    private ConnectionModel connectionModel;//离散图表及连通性model
    private BatchNerveModel batchNerveModel;//线性层model

    public List<GnnLayerModel> getGnnLayerModelList() {
        return gnnLayerModelList;
    }

    public void setGnnLayerModelList(List<GnnLayerModel> gnnLayerModelList) {
        this.gnnLayerModelList = gnnLayerModelList;
    }

    public ConnectionModel getConnectionModel() {
        return connectionModel;
    }

    public void setConnectionModel(ConnectionModel connectionModel) {
        this.connectionModel = connectionModel;
    }

    public BatchNerveModel getBatchNerveModel() {
        return batchNerveModel;
    }

    public void setBatchNerveModel(BatchNerveModel batchNerveModel) {
        this.batchNerveModel = batchNerveModel;
    }
}
