package org.dromara.easyai.recommend;

import org.dromara.easyai.conv.DymStudy;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.recommend.model.ConnectModel;
import org.dromara.easyai.recommend.model.ConnectionModel;
import org.dromara.easyai.recommend.model.FeatureModel;

import java.util.*;

/**
 * @author lidapeng
 * @time 2026/7/11 10:18
 * @des 离散特征表
 */
public class ConnectionTable {
    private final MatrixOperation matrixOperation = new MatrixOperation();
    private final Map<Integer, List<Integer>> connectMap = new HashMap<>();//需保存模型
    private final Map<Integer, Matrix> featureMatrixMap = new HashMap<>();//离散特征表 需保存模型
    private final Map<Integer, Matrix> dymStudyMap1 = new HashMap<>();
    private final Map<Integer, Matrix> dymStudyMap2 = new HashMap<>();
    private final int nodeSize;//总节点数量
    private final int[] typeArray;//类别数组 需要保存模型

    public ConnectionTable(int nodeSize, int featureLength) {//特征维度，节点数
        if (nodeSize > 1) {
            this.nodeSize = nodeSize;
            typeArray = new int[nodeSize];
            for (int i = 0; i < nodeSize; i++) {
                Matrix featureMatrix = new Matrix(1, featureLength);
                Matrix dymMatrix1 = new Matrix(1, featureLength);
                Matrix dymMatrix2 = new Matrix(1, featureLength);
                featureMatrix.randomInit(featureLength);
                featureMatrixMap.put(i, featureMatrix);
                dymStudyMap1.put(i, dymMatrix1);
                dymStudyMap2.put(i, dymMatrix2);
            }
        } else {
            throw new IllegalArgumentException("GNN节点数量必须大于1");
        }
    }

    ConnectionModel getModel() {
        ConnectionModel connectionModel = new ConnectionModel();
        List<ConnectModel> connectModels = new ArrayList<>();
        List<FeatureModel> featureModels = new ArrayList<>();
        for (Map.Entry<Integer, List<Integer>> entry : connectMap.entrySet()) {
            ConnectModel connectModel = new ConnectModel();
            int key = entry.getKey();
            List<Integer> sonList = entry.getValue();
            connectModel.setId(key);
            connectModel.setSonList(sonList);
            connectModels.add(connectModel);
        }
        for (Map.Entry<Integer, Matrix> entry : featureMatrixMap.entrySet()) {
            FeatureModel featureModel = new FeatureModel();
            featureModel.setId(entry.getKey());
            featureModel.setMatrixModel(entry.getValue().getMatrixModel());
            featureModels.add(featureModel);
        }
        connectionModel.setConnectModels(connectModels);
        connectionModel.setFeatureModels(featureModels);
        connectionModel.setTypeArray(typeArray);
        return connectionModel;
    }

    void insertModel(ConnectionModel connectionModel) {
        List<ConnectModel> connectModels = connectionModel.getConnectModels();
        List<FeatureModel> featureModels = connectionModel.getFeatureModels();
        int[] typesId = connectionModel.getTypeArray();
        for (ConnectModel connectModel : connectModels) {
            int id = connectModel.getId();
            List<Integer> sonList = new ArrayList<>(connectModel.getSonList());
            if (!connectMap.containsKey(id)) {
                connectMap.put(id, sonList);
            } else {
                throw new IllegalArgumentException("连通图出现重复节点id：" + (id + 1));
            }
        }
        if (typesId.length == nodeSize) {
            for (int i = 0; i < nodeSize; i++) {
                int typeId = typesId[i];
                if (typeId != 0) {
                    typeArray[i] = typeId;
                } else {
                    throw new IllegalArgumentException("模型节点类别数组元素值不可以有0，请检查模型文件是否损坏。");
                }
            }
        } else {
            throw new IllegalArgumentException("模型节点类别数组元素数量与初始化节点数量不匹配");
        }
        for (Map.Entry<Integer, Matrix> entry : featureMatrixMap.entrySet()) {
            int key = entry.getKey();
            FeatureModel featureModel = getFeatureById(featureModels, key);
            if (featureModel != null) {
                entry.getValue().insertMatrixModel(featureModel.getMatrixModel());
            } else {
                throw new IllegalArgumentException("模型中缺少离散id:" + (key + 1) + ",的特征值");
            }
        }
    }

    private FeatureModel getFeatureById(List<FeatureModel> featureModels, int id) {
        FeatureModel myFeatureModel = null;
        for (FeatureModel featureModel : featureModels) {
            if (featureModel.getId() == id) {
                myFeatureModel = featureModel;
                break;
            }
        }
        return myFeatureModel;
    }

    void updateFeatureMap(int id, Matrix error, DymStudy dymStudy, float studyRate, int times) throws Exception {
        Matrix s1 = dymStudyMap1.get(id);
        Matrix s2 = dymStudyMap2.get(id);
        Matrix table = featureMatrixMap.get(id);
        Matrix subTable = dymStudy.getErrorMatrixByStudy(studyRate, s1, s2, error, times);
        Matrix nextTable = matrixOperation.add(table, subTable);
        featureMatrixMap.put(id, nextTable);
    }


    private void writeNode(int nodeID, int sonID) {
        if (connectMap.containsKey(nodeID)) {
            List<Integer> nodeList = connectMap.get(nodeID);
            if (!nodeList.contains(sonID)) {
                nodeList.add(sonID);
            }
        } else {
            List<Integer> nodeList = new ArrayList<>();
            nodeList.add(sonID);
            connectMap.put(nodeID, nodeList);
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
            int type = typeArray[id];
            if (type == 0) {
                typeArray[id] = nodeType;
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
        if (id >= nodeSize) {
            throw new IllegalArgumentException("注入样本离散id与初始化离散id数量不匹配");
        } else if (id < 0) {
            throw new IllegalArgumentException("离散id值不可小于0");
        }
        if (fatherID == id) {
            throw new IllegalArgumentException("父级节点不可以与子节点使用同一个ID");
        }
    }

    int getNodeType(int index) {
        if (index >= typeArray.length) {
            throw new IllegalArgumentException("出现没有在构建图中出现的节点id:" + (index + 1));
        }
        return typeArray[index];
    }

    List<GnnNode> getFeatureList(GnnNode gnnNode, int jumpsTimes) {
        List<Matrix> featureList = new ArrayList<>();
        int id = gnnNode.getId();
        Matrix feature = featureMatrixMap.get(id).copy();
        if (feature == null) {
            throw new IllegalArgumentException("根节点的id模型中不存在:" + gnnNode.getId());
        }
        featureList.add(feature);
        gnnNode.setJumpTimes(0);
        gnnNode.setFeatureList(featureList);
        List<GnnNode> gnnNodeList = new ArrayList<>();
        gnnNodeList.add(gnnNode);
        insertFeature(gnnNodeList, jumpsTimes);
        return gnnNodeList;
    }

    private void insertFeature(List<GnnNode> gnnNodeList, int jumpsTimes) {
        for (GnnNode gnnNode : gnnNodeList) {
            int jump = gnnNode.getJumpTimes();
            if (jump < jumpsTimes) {
                insertTableFeature(gnnNode);
                List<GnnNode> sonList = gnnNode.getNodeList();
                insertFeature(sonList, jumpsTimes);
            }
        }
    }

    private void insertTableFeature(GnnNode gnnNode) {
        List<GnnNode> nodeList = gnnNode.getNodeList();
        int jump = gnnNode.getJumpTimes();
        int fatherId = gnnNode.getId();
        if (nodeList != null && !nodeList.isEmpty()) {
            for (GnnNode sonNode : nodeList) {
                List<Matrix> featureList = new ArrayList<>();
                int id = sonNode.getId();
                Matrix feature = featureMatrixMap.get(id).copy();
                if (feature == null) {
                    throw new IllegalArgumentException("出现模型中不存在的节点ID:" + sonNode.getId());
                }
                featureList.add(feature);
                sonNode.setJumpTimes(jump + 1);
                sonNode.setFeatureList(featureList);
            }
        } else {
            nodeList = new ArrayList<>();
            List<Matrix> featureList = new ArrayList<>();
            GnnNode sonNode = new GnnNode();
            Matrix feature = gnnNode.getFeatureList().get(0).copy();
            featureList.add(feature);
            sonNode.setJumpTimes(jump + 1);
            sonNode.setId(fatherId);
            sonNode.setFeatureList(featureList);
            nodeList.add(sonNode);
            gnnNode.setNodeList(nodeList);
        }
    }

    List<GnnNode> getRandomSonNodes(GnnNode rootGnn, int jumpTimes) {
        int id = rootGnn.getId();
        List<Matrix> featureList = new ArrayList<>();
        featureList.add(featureMatrixMap.get(id).copy());
        rootGnn.setJumpTimes(0);
        rootGnn.setFeatureList(featureList);
        List<GnnNode> gnnNodes = new ArrayList<>();
        gnnNodes.add(rootGnn);
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
        List<GnnNode> nodeList = node.getNodeList();
        int times = node.getJumpTimes() + 1;
        if (nodeList != null && !nodeList.isEmpty()) {
            for (GnnNode gnnNode : nodeList) {
                int i = gnnNode.getId();
                List<Matrix> featureList = new ArrayList<>();
                featureList.add(featureMatrixMap.get(i).copy());
                gnnNode.setFeatureList(featureList);
                gnnNode.setJumpTimes(times);
            }
        } else {//连接自身
            nodeList = new ArrayList<>();
            List<Matrix> featureList = new ArrayList<>();
            featureList.add(node.getFeatureList().get(0).copy());
            GnnNode nextNode = new GnnNode();
            nextNode.setId(id);
            nextNode.setFeatureList(featureList);
            nextNode.setJumpTimes(times);
            nodeList.add(nextNode);
            node.setNodeList(nodeList);
        }
        return times;
    }


    Matrix getConnectOut(int index, Map<Integer, GnnPower> powerMap, List<GnnNode> sonList, int featIndex) throws Exception {
        List<Integer> connectionList = connectMap.get(index);
        if (connectionList == null) {
            throw new IllegalArgumentException("出现不在图构建中的节点id:" + (index + 1));
        }
        int connectionSize = connectionList.size();
        double myDu = 1 / Math.sqrt(connectionSize);
        Matrix sigMa = null;
        for (GnnNode gnnNode : sonList) {
            int j = gnnNode.getId();
            Matrix feature = getMatrixFeature(featIndex, gnnNode);
            int type = getNodeType(j);
            if (powerMap.containsKey(type)) {
                GnnPower gnnPower = powerMap.get(type);
                List<Integer> connection = connectMap.get(j);
                if (connection == null) {
                    throw new IllegalArgumentException("出现不在图构建中的节点id:" + (j + 1));
                }
                double du = (1 / Math.sqrt(connection.size())) * myDu;
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
        // 无邻居聚合结果，返回同维度零矩阵，避免上层空指针
        if (sigMa == null) {
            throw new IllegalArgumentException("节点无邻居异常");
        }
        return sigMa;
    }

    private Matrix getMatrixFeature(int featIndex, GnnNode gnnNode) {
        List<Matrix> featList = gnnNode.getFeatureList();
        Matrix feature;
        if (featList.size() > featIndex) {
            feature = featList.get(featIndex);
        } else {
            int fallbackIdx = featIndex - 1;
            if (fallbackIdx < 0) {
                throw new RuntimeException("特征读取下标异常：fallbackIdx=" + fallbackIdx + "，featIndex=" + featIndex + "，子节点不存在可用特征");
            }
            feature = featList.get(fallbackIdx);
        }
        return feature;
    }
}
