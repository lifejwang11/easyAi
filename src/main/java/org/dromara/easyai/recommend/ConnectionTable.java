package org.dromara.easyai.recommend;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;

import java.util.*;

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
    private final Random random = new Random();
    private final int studyMaxJumpNumber;//训练时每一跳最多聚合邻居的数量
    private final int studyMinJumpNumber;//训练时每一跳最小聚合邻居的数量

    public ConnectionTable(int nodeSize, int featureLength, int studyMaxJumpNumber, int studyMinJumpNumber) {//特征维度，节点数
        if (nodeSize > 1) {
            this.nodeSize = nodeSize;
            typeArray = new int[nodeSize];
            this.studyMaxJumpNumber = studyMaxJumpNumber;
            this.studyMinJumpNumber = studyMinJumpNumber;
            if (studyMaxJumpNumber <= 0) {
                throw new IllegalArgumentException("每一层聚合邻居数量上限必须大于0");
            }
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

    private List<Integer> getSonOfConnect(List<Integer> connectList) {
        List<Integer> sonConnect = new ArrayList<>(connectList);
        int keepNum = random.nextInt(studyMaxJumpNumber) + 1;
        if (keepNum < studyMinJumpNumber) {
            keepNum = studyMinJumpNumber;
        }
        // 原始数量少于要保留的数量，直接返回
        if (sonConnect.size() <= keepNum) {
            return sonConnect;
        }
        // 全局只打乱一次
        Collections.shuffle(sonConnect, random);
        // 直接截取，无需循环删除
        return new ArrayList<>(sonConnect.subList(0, keepNum));
    }

    public List<GnnNode> getRandomSonNodes(int id, int jumpTimes) {
        List<Matrix> featureList = new ArrayList<>();
        featureList.add(featureMatrixMap.get(id));
        GnnNode rootGnnNode = new GnnNode();
        rootGnnNode.setId(id);
        rootGnnNode.setJumpTimes(0);
        rootGnnNode.setFeatureList(featureList);
        List<GnnNode> gnnNodes = new ArrayList<>();
        gnnNodes.add(rootGnnNode);
        insertNodes(jumpTimes, gnnNodes);
        return gnnNodes;
    }

    private void insertNodes(int jumpTimes, List<GnnNode> gnnNodes) {
        for (GnnNode gnnNode : gnnNodes) {
            int times = insertSonNodes(gnnNode);
            if (times < jumpTimes) {
                List<GnnNode> sonList = gnnNode.getNodeList();
                insertNodes(jumpTimes, sonList);
            }
        }
    }

    private int insertSonNodes(GnnNode node) {
        int id = node.getId();
        List<Integer> connectionList = connectMap.get(id);
        if (connectionList != null) {
            List<Integer> sonList = getSonOfConnect(connectionList);
            List<GnnNode> sons = new ArrayList<>();
            int times = node.getJumpTimes() + 1;
            for (int i : sonList) {
                List<Matrix> featureList = new ArrayList<>();
                featureList.add(featureMatrixMap.get(i));
                GnnNode gnnNode = new GnnNode();
                gnnNode.setId(i);
                gnnNode.setFeatureList(featureList);
                gnnNode.setJumpTimes(times);
                sons.add(gnnNode);
            }
            node.setNodeList(sons);
            return times;
        } else {
            throw new IllegalArgumentException("训练时不可以出现单根的情况");
        }
    }


    public Matrix getConnectOut(int index, Map<Integer, GnnPower> powerMap, List<GnnNode> sonList, int deep) throws Exception {//获取连通矩阵
        List<Integer> connectionList = connectMap.get(index);
        int connectionSize = connectionList.size();
        double myDu = 1 / Math.sqrt(connectionSize);
        Matrix sigMa = null;
        for (GnnNode gnnNode : sonList) {
            int j = gnnNode.getId();
            Matrix feature = gnnNode.getFeatureList().get(deep);//邻居的特征
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
