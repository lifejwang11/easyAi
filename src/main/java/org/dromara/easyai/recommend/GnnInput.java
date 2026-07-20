package org.dromara.easyai.recommend;

import org.dromara.easyai.i.OutBack;

import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @time 2026/7/19 21:17
 */
public class GnnInput {
    private final ConnectionTable connectionTable;
    private final GnnLayer gnnLayer;

    public GnnInput(ConnectionTable connectionTable, GnnLayer gnnLayer) {
        this.connectionTable = connectionTable;
        this.gnnLayer = gnnLayer;
    }

    public void createGraph(NodeStudy nodeStudy) {//构建图
        GnnNode gnnNode = nodeStudy.getRootGnnNode();
        if (gnnNode != null) {
            connectionTable.createGraph(gnnNode);
        } else {
            throw new IllegalArgumentException("构建图根节点为空");
        }
    }

    public void study(OutBack outBack, List<NodeStudy> nodeStudies, long eventID, Map<Integer, Float> pd) throws Exception {//训练
        gnnLayer.study(outBack, nodeStudies, eventID, pd);
    }

    public void infer(GnnNode gnnNode, OutBack outBack, long eventID, Map<Integer, Float> pd) throws Exception {//推理
        gnnLayer.infer(gnnNode, outBack, eventID, pd);
    }

}
