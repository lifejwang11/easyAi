package org.dromara.easyai.td;

import org.dromara.easyai.config.GnnConfig;
import org.dromara.easyai.function.NoActivation;
import org.dromara.easyai.function.Tanh;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.recommend.GnnManager;
import org.dromara.easyai.recommend.NodeStudy;
import org.dromara.easyai.recommend.model.GnnModel;

import java.util.List;

/**
 * @author lidapeng
 * @time 2026/10/3 14:09
 * @des 时序差分管理器
 */
public class TDManager {
    private final GnnManager gnnManager;
    private final TDBlock tdBlock;
    private boolean init = false;//是否完成初始化

    /**
     * 构造函数
     *
     * @param gnnConfig gnn配置类
     * @param valueRate 价值更新系数 0.001-0.0001
     * @param discount  贴现系数 0.9-1
     */
    public TDManager(GnnConfig gnnConfig, float valueRate, float discount, ActiveFunction activeFunction) throws Exception {
        gnnConfig.setGnnTypeNumber(1);
        gnnConfig.setSoftMax(false);
        gnnConfig.setOutNumber(1);
        gnnManager = new GnnManager(gnnConfig, activeFunction);
        tdBlock = new TDBlock(valueRate, discount, gnnManager);
    }

    public void createNode(List<TDState> tdBodyList) throws IllegalAccessException {//初始化进行全图构建
        if (!tdBodyList.get(tdBodyList.size() - 1).isFinish()) {
            throw new IllegalAccessException("每条数据的链路结尾必须为终止状态");
        }
        NodeStudy nodeStudy = tdBlock.getNodeStudy(tdBodyList);
        gnnManager.getGnnInput().createGraph(nodeStudy);
        init = true;
    }

    public void insertValueList(List<TDState> nodeStates, long eventID) throws Exception {//批量注入节点价值
        tdBlock.getNodeStateList(nodeStates, eventID);
    }

    public float getValueByID(int id, long eventID) throws Exception {//根据节点id获取节点价值
        return tdBlock.getNodeState(id, eventID);
    }

    public void infer(List<TDState> tdBodyList, OutBack outBack, long eventID) throws Exception {//链式推理
        tdBlock.infer(tdBodyList, outBack, eventID);
    }

    public void study(List<TDState> tdBodyList, OutBack outBack) throws Exception {
        if (init) {
            tdBlock.studyMessage(tdBodyList, outBack);
        } else {
            throw new IllegalAccessException("训练前必须先构建节点");
        }
    }


    public GnnModel getModel() {
        return gnnManager.getModel();
    }

    public void insertModel(GnnModel gnnModel) {
        init = true;
        gnnManager.insertModel(gnnModel);
    }

}
