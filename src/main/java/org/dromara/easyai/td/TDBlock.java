package org.dromara.easyai.td;

import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.recommend.GnnInput;
import org.dromara.easyai.recommend.GnnManager;
import org.dromara.easyai.recommend.GnnNode;
import org.dromara.easyai.recommend.NodeStudy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @time 2026/10/2 15:15
 * @des 时序差分模块
 */
public class TDBlock {
    private final float valueRate;//价值更新系数
    private final float discount;//贴现系数
    private final GnnManager gnnManager;//gnn管理器
    private final int jumpTimes;

    TDBlock(float valueRate, float discount, GnnManager gnnManager) throws IllegalAccessException {
        if (valueRate > 0 && valueRate < 1 && discount > 0 && discount < 1) {
            this.valueRate = valueRate;
            this.discount = discount;
            this.gnnManager = gnnManager;
            jumpTimes = gnnManager.getGnnConfig().getJumpTimes();
        } else {
            throw new IllegalAccessException("valueRate 与 discount 都必须大于0 小于1");
        }
    }

    private void computeValue(List<TDState> tdBodyList) throws Exception {
        int size = tdBodyList.size();
        insertValue(tdBodyList);
        if (size > 1) {
            for (int i = size - 2; i >= 0; i--) {
                TDState tdBody = tdBodyList.get(i);
                TDState nextTdBody = tdBodyList.get(i + 1);
                float tdError;
                if (i == size - 2) {//倒数第二个 终止态
                    tdError = nextTdBody.getPrize() - tdBody.getValue();
                } else {
                    tdError = nextTdBody.getPrize() + nextTdBody.getValue() * discount - tdBody.getValue();
                }
                float value = tdBody.getValue() + valueRate * tdError;
                tdBody.setValue(value);
            }
        } else {
            throw new IllegalAccessException("一条推理链路不能只有一个终止态");
        }
    }

    private void insertValue(List<TDState> tdBodyList) throws Exception {
        TDBack tdBack = new TDBack();
        GnnInput gnnInput = gnnManager.getGnnInput();
        int size = tdBodyList.size();
        for (int i = 0; i < size - 1; i++) {
            TDState tdBody = tdBodyList.get(i);
            GnnNode gnnNode = getNode(tdBody, false);
            gnnInput.infer(gnnNode, tdBack, 1L, null);
            tdBody.setValue(tdBack.getValue());
        }
        if (!tdBodyList.get(size - 1).isFinish()) {
            throw new IllegalAccessException("链路的最后要设置终止态");
        }
    }

    NodeStudy getNodeStudy(List<TDState> win) {
        int winSize = win.size();
        NodeStudy nodeStudy = new NodeStudy();
        Map<Integer, Float> e = new HashMap<>();
        nodeStudy.setE(e);
        GnnNode root = getNode(win.get(0), true);
        nodeStudy.setRootGnnNode(root);
        if (winSize > 1) {
            GnnNode father = root;
            for (int j = 1; j < winSize; j++) {
                TDState tdBody = win.get(j);
                father = getSonGnnNode(father, tdBody);
                if (j == winSize - 1) {
                    e.put(1, tdBody.getValue());
                }
            }
        } else {
            e.put(1, win.get(0).getValue());
        }
        return nodeStudy;
    }

    void getNodeStateList(List<TDState> nodeStates, long eventID) throws Exception {
        for (TDState nodeState : nodeStates) {
            float value = getNodeState(nodeState.getId(), eventID);
            nodeState.setValue(value);
        }
    }

    float getNodeState(int id, long eventID) throws Exception {
        GnnInput gnnInput = gnnManager.getGnnInput();
        TDBack tdBack = new TDBack();
        GnnNode root = new GnnNode();
        root.setTypeID(1);
        root.setId(id);
        gnnInput.infer(root, tdBack, eventID, null);
        return tdBack.getValue();
    }

    void infer(List<TDState> tdBodyList, OutBack outBack, long eventID) throws Exception {//链式推理
        GnnInput gnnInput = gnnManager.getGnnInput();
        if (tdBodyList.size() <= jumpTimes) {
            NodeStudy nodeStudy = getNodeStudy(tdBodyList);
            GnnNode root = nodeStudy.getRootGnnNode();
            gnnInput.infer(root, outBack, eventID, null);
        } else {
            throw new IllegalAccessException("推理连接长度不可超过设定跳数");
        }
    }

    void studyMessage(List<TDState> tdBodyList, OutBack outBack) throws Exception {
        computeValue(tdBodyList);
        int size = tdBodyList.size();
        int maxTimes = size - (jumpTimes - 1) - 1;
        GnnInput gnnInput = gnnManager.getGnnInput();
        List<NodeStudy> nodeStudies = new ArrayList<>();
        if (maxTimes > 0) {
            for (int i = 0; i < maxTimes; i++) {
                List<TDState> win = tdBodyList.subList(i, i + jumpTimes);//滑窗
                NodeStudy nodeStudy = getNodeStudy(win);
                nodeStudies.add(nodeStudy);
            }
        } else {
            List<TDState> win = tdBodyList.subList(0, size - 1);//滑窗
            NodeStudy nodeStudy = getNodeStudy(win);
            nodeStudies.add(nodeStudy);
        }
        gnnInput.study(outBack, nodeStudies, 1, null);
    }

    private GnnNode getSonGnnNode(GnnNode father, TDState tdBody) {
        GnnNode sonNode = getNode(tdBody, true);
        father.getNodeList().add(sonNode);
        return sonNode;
    }

    private GnnNode getNode(TDState tdBody, boolean addSon) {
        GnnNode gnnNode = new GnnNode();
        gnnNode.setTypeID(1);
        if (addSon) {
            gnnNode.setNodeList(new ArrayList<>());
        }
        gnnNode.setId(tdBody.getId());
        return gnnNode;
    }


}
