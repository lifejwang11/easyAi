package org.dromara.easyai.recommend;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @time 2026/7/11 10:18
 * @des 离散特征表
 */
public class ConnectionTable {
    private final MatrixOperation matrixOperation = new MatrixOperation();
    private final Map<Integer, List<Integer>> connectMap = new HashMap<>();
    private final Map<Integer, Matrix> featureMatrixMap = new HashMap<>();//离散特征表
    private final int nodeSize;//总节点数量
    private final int[] typeArray;//类别数组

    public ConnectionTable(int nodeSize, int featureLength) {//特征维度，节点数
        if (nodeSize > 1) {
            this.nodeSize = nodeSize;
            typeArray = new int[nodeSize];
            for (int i = 0; i < nodeSize; i++) {
                Matrix featureMatrix = new Matrix(1, featureLength);
                featureMatrix.randomInit(featureLength);
                featureMatrixMap.put(i, featureMatrix);
            }
        } else {
            throw new IllegalArgumentException("GNN节点数量必须大于1");
        }
    }

    public Map<Integer, Matrix> getFeatureMatrixMap() {
        return featureMatrixMap;
    }

    private void writeNode(int nodeID, int sonID) {
        int index = nodeID - 1;
        int sonIndex = sonID - 1;
        if (connectMap.containsKey(index)) {
            List<Integer> nodeList = connectMap.get(index);
            if (!nodeList.contains(sonIndex)) {
                nodeList.add(sonIndex);
            }
        } else {
            List<Integer> nodeList = new ArrayList<>();
            nodeList.add(sonIndex);
            connectMap.put(index, nodeList);
        }
    }

    public Map<Integer, List<Integer>> getConnectMap() {
        return connectMap;
    }

    public void createGraph(GnnNode rootNode) {//构建图
        int rootID = rootNode.getId();
        checkID(rootID, 0);
        insertType(rootID, rootNode.getTypeID());
        List<GnnNode> nodeLists = rootNode.getNodeList();
        if (nodeLists != null && !nodeLists.isEmpty()) {
            connectionMap(rootID, nodeLists);
        } else {
            throw new IllegalArgumentException("不可以输入无子节点的根节点样本");
        }
    }

    private void insertType(int id, int nodeType) {
        if (nodeType != 0) {
            int type = typeArray[id - 1];
            if (type == 0) {
                typeArray[id - 1] = nodeType;
            } else if (type != nodeType) {
                throw new IllegalArgumentException("相同的节点ID不可以存在两个不同的节点类别");
            }
        } else {
            throw new IllegalArgumentException("节点类别ID不能使用0");
        }
    }

    private void connectionMap(int fatherID, List<GnnNode> nodeLists) {
        for (GnnNode gnnNode : nodeLists) {
            int id = gnnNode.getId();
            checkID(id, fatherID);
            writeNode(fatherID, id);
            writeNode(id, fatherID);
            insertType(id, gnnNode.getTypeID());
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


    public Matrix getConnectOut(int index, Map<Integer, Matrix> featureMatrixMap, Map<Integer, GnnPower> powerMap) throws Exception {//获取连通矩阵
        List<Integer> connectionList = connectMap.get(index);
        int connectionSize = connectionList.size();
        double myDu = 1 / Math.sqrt(connectionSize);
        Matrix sigMa = null;
        for (int j : connectionList) {
            Matrix feature = featureMatrixMap.get(j);//邻居的特征
            int type = getNodeType(j);
            if (powerMap.containsKey(type)) {
                GnnPower gnnPower = powerMap.get(type);
                double du = (1 / Math.sqrt(connectMap.get(j).size())) * myDu;
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
        return sigMa;
    }
}
