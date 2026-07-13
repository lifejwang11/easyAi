package org.dromara.easyai.recommend;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;

import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @time 2026/7/11 10:18
 * @des 离散特征表
 */
public class ConnectionTable {
    private final MatrixOperation matrixOperation = new MatrixOperation();
    private final Matrix connectMatrix;//图结构 该结构需要保存(该结构可能非常大)
    private final int nodeSize;//总节点数量
    private boolean finishCreate;//是否完成图构建
    private final Matrix dMatrix;//度矩阵
    private final int[] typeArray;//类别数组

    public ConnectionTable(int nodeSize) {//特征维度，节点数
        if (nodeSize > 1) {
            finishCreate = false;
            this.nodeSize = nodeSize;
            connectMatrix = new Matrix(nodeSize, nodeSize);
            dMatrix = new Matrix(1, nodeSize);
            typeArray = new int[nodeSize];
        } else {
            throw new IllegalArgumentException("GNN节点数量必须大于1");
        }
    }


    public void createGraph(GnnNode rootNode) {//构建图
        if (finishCreate) {
            throw new IllegalArgumentException("执行init方法后，不可以再构建图");
        }
        int rootID = rootNode.getId();
        checkID(rootID, 0);
        typeArray[rootID - 1] = rootNode.getTypeID();
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
            checkID(id, fatherID);
            if (connectMatrix.getValue(fatherID - 1, id - 1) < 0.5f) {
                typeArray[id - 1] = gnnNode.getTypeID();
                connectMatrix.setValue(fatherID - 1, id - 1, 1);
                connectMatrix.setValue(id - 1, fatherID - 1, 1);
            }
            List<GnnNode> sonNodeList = gnnNode.getNodeList();
            if (sonNodeList != null && !sonNodeList.isEmpty()) {
                connectionMap(id, sonNodeList);
            }
        }
    }

    private void checkID(int id, int fatherID) {
        if (id > nodeSize) {
            throw new IllegalArgumentException("注入样本离散id与初始化离散id数量不匹配");
        } else if (id < 1) {
            throw new IllegalArgumentException("离散id值不可小于1");
        }
        if (fatherID == id) {
            throw new IllegalArgumentException("父级节点不可以与子节点使用同一个ID");
        }
    }

    public int getNodeType(int index) {
        return typeArray[index];
    }

    public Matrix getConnectMatrix() {
        return connectMatrix;
    }

    public Matrix getConnectOut(int index, Matrix featureMatrix, Map<Integer, GnnPower> powerMap) throws Exception {//获取连通矩阵
        double myDu = 1 / Math.sqrt(dMatrix.getValue(0, index));
        Matrix sigMa = null;
        for (int j = 0; j < nodeSize; j++) {
            if (j != index) {
                if (connectMatrix.getValue(index, j) > 0.5) {//有连通
                    Matrix feature = featureMatrix.getRow(j);//邻居的特征
                    int type = getNodeType(j);
                    if (powerMap.containsKey(type)) {
                        GnnPower gnnPower = powerMap.get(type);
                        double du = (1 / Math.sqrt(dMatrix.getValue(0, j))) * myDu;
                        float arf = (float) (gnnPower.getArf() * du);
                        Matrix otherPower = gnnPower.getOtherPower();
                        Matrix wf = matrixOperation.mulMatrix(feature, otherPower);
                        matrixOperation.mathMul(wf, arf);
                        if (sigMa == null) {
                            sigMa = wf;
                        } else {
                            sigMa = matrixOperation.add(sigMa, wf);
                        }
                    } else {
                        throw new IllegalArgumentException("出现不在配置参数列表里的节点类别");
                    }
                }
            }
        }
        return sigMa;
    }
}
