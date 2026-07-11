package org.dromara.easyai.recommend;

import org.dromara.easyai.matrixTools.Matrix;

import java.util.List;

/**
 * @author lidapeng
 * @time 2026/7/11 10:18
 * @des 离散特征表
 */
public class DiscreteTable {
    private final Matrix featureMatrix;
    private final Matrix connectMatrix;//图结构 该结构需要保存(该结构可能非常大)
    private final int nodeSize;//总节点数量
    private boolean finishCreate;//是否完成图构建
    private final Matrix dMatrix;//度矩阵

    public DiscreteTable(int featureLength, int nodeSize) {//特征维度，节点数
        if (nodeSize > 1) {
            finishCreate = false;
            this.nodeSize = nodeSize;
            connectMatrix = new Matrix(nodeSize, nodeSize);
            featureMatrix = new Matrix(nodeSize, featureLength);
            dMatrix = new Matrix(1, nodeSize);
            featureMatrix.randomInit(featureLength);
        } else {
            throw new IllegalArgumentException("GNN节点数量必须大于1");
        }
    }

    public void createGraph(GnnNode rootNode) {//构建图
        if (finishCreate) {
            throw new IllegalArgumentException("执行init方法后，不可以再构建图");
        }
        int rootID = rootNode.getId();
        checkID(rootID);
        List<GnnNode> nodeLists = rootNode.getNodeList();
        if (nodeLists != null && !nodeLists.isEmpty()) {
            connectionMap(rootID, nodeLists);
        }
    }

    public void init() {//完成构建图后执行初始化
        int x = connectMatrix.getX();
        int y = connectMatrix.getY();
        for (int i = 0; i < x; i++) {
            int d = 0;
            for (int j = 0; j < y; j++) {
                if (connectMatrix.getValue(i, j) > 0.5f) {
                    d++;
                }
            }
            dMatrix.setValue(0, i, d);
        }
        finishCreate = true;
    }

    private void connectionMap(int fatherID, List<GnnNode> nodeLists) {
        for (GnnNode gnnNode : nodeLists) {
            int id = gnnNode.getId();
            checkID(id);
            if (connectMatrix.getValue(fatherID - 1, id - 1) < 0.5f) {
                connectMatrix.setValue(fatherID - 1, id - 1, 1);
                connectMatrix.setValue(id - 1, fatherID - 1, 1);
            }
            List<GnnNode> sonNodeList = gnnNode.getNodeList();
            if (sonNodeList != null && !sonNodeList.isEmpty()) {
                connectionMap(id, sonNodeList);
            }
        }
    }

    private void checkID(int id) {
        if (id > nodeSize) {
            throw new IllegalArgumentException("注入样本离散id与初始化离散id数量不匹配");
        } else if (id < 1) {
            throw new IllegalArgumentException("离散id值不可小于1");
        }
    }

    public void getConnectMatrix(int id) {//获取连通矩阵
        Matrix connect = connectMatrix.getRow(id - 1);

    }

    public Matrix getFeature(int nodeID) {
        return featureMatrix.getRow(nodeID - 1);
    }
}
