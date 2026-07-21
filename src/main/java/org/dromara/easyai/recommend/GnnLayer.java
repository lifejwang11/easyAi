package org.dromara.easyai.recommend;

import org.dromara.easyai.batchNerve.BatchNerveManager;
import org.dromara.easyai.batchNerve.FeatureBody;
import org.dromara.easyai.config.GnnConfig;
import org.dromara.easyai.conv.DymStudy;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.recommend.model.GnnLayerModel;
import org.dromara.easyai.recommend.model.NodeModel;

import java.util.*;

/**
 * @author lidapeng
 * @time 2026/7/11 08:58
 * @des gnn层
 */
public class GnnLayer {
    private final Map<Integer, GnnPower> powerMap = new HashMap<>();//权重矩阵 需保存模型
    private final MatrixOperation matrixOperation = new MatrixOperation();
    private final ConnectionTable connectionTable;
    private final ActiveFunction activeFunction;
    private final DymStudy dymStudy;
    private GnnLayer sonLayer;
    private GnnLayer fatherLayer;
    private final int jumpTimes;//跳跃数
    private final BatchNerveManager batchNerveManager;//需保存模型
    private final int deep;//所处深度
    private int updateTimes = 0;
    private final float studyRate;//全局学习率
    private List<NodeStudy> studyNodeStudies;
    private final boolean layerGCut;//是否需要层梯度裁剪

    public GnnLayer(GnnConfig gnnConfig, ActiveFunction activeFunction, ConnectionTable connectionTable
            , BatchNerveManager batchNerveManager, int deep) {//特征维度 类别数量
        dymStudy = new DymStudy(gnnConfig.getgMaxTh(), gnnConfig.isAuto(), gnnConfig.getLayGMaxTh());
        this.studyRate = gnnConfig.getStudy();
        this.activeFunction = activeFunction;
        this.deep = deep;
        layerGCut = gnnConfig.isLayerGCut();
        this.jumpTimes = gnnConfig.getJumpTimes();
        int gnnTypeNumber = gnnConfig.getGnnTypeNumber();
        //特征维度
        int featureLength = gnnConfig.getFeatureLength();
        this.connectionTable = connectionTable;
        this.batchNerveManager = batchNerveManager;
        if (gnnTypeNumber > 0) {
            for (int i = 0; i < gnnTypeNumber; i++) {
                GnnPower gnnPower = initGnnPower(featureLength, gnnConfig.getOtherValue());
                powerMap.put(i + 1, gnnPower);
            }
        } else {
            throw new IllegalArgumentException("节点类别数必须大于0");
        }
    }

    GnnLayerModel getModel() {
        GnnLayerModel gnnLayerModel = new GnnLayerModel();
        List<NodeModel> nodeModels = new ArrayList<>();
        for (Map.Entry<Integer, GnnPower> entry : powerMap.entrySet()) {
            GnnPower gnnPower = entry.getValue();
            NodeModel nodeModel = new NodeModel();
            nodeModel.setTypeId(entry.getKey());
            nodeModel.setArf(gnnPower.getArf());
            nodeModel.setSelfPower(gnnPower.getSelfPower().getMatrixModel());
            nodeModel.setOtherPower(gnnPower.getOtherPower().getMatrixModel());
            nodeModel.setBais(gnnPower.getBais().getMatrixModel());
            nodeModels.add(nodeModel);
        }
        gnnLayerModel.setPowerModelList(nodeModels);
        return gnnLayerModel;
    }

    void insertModel(GnnLayerModel gnnLayerModel) {
        List<NodeModel> nodeModels = gnnLayerModel.getPowerModelList();
        for (Map.Entry<Integer, GnnPower> entry : powerMap.entrySet()) {
            NodeModel nodeModel = getNodeModelById(nodeModels, entry.getKey());
            if (nodeModel != null) {
                GnnPower gnnPower = entry.getValue();
                gnnPower.getSelfPower().insertMatrixModel(nodeModel.getSelfPower());
                gnnPower.getOtherPower().insertMatrixModel(nodeModel.getOtherPower());
                gnnPower.getBais().insertMatrixModel(nodeModel.getBais());
                gnnPower.setArf(nodeModel.getArf());
            } else {
                throw new IllegalArgumentException("类型ID缺失，请检查模型是否损坏：" + entry.getKey());
            }
        }
    }

    private NodeModel getNodeModelById(List<NodeModel> nodeModels, int id) {
        NodeModel nodeModel = null;
        for (NodeModel myNodeModel : nodeModels) {
            if (myNodeModel.getTypeId() == id) {
                nodeModel = myNodeModel;
                break;
            }
        }
        return nodeModel;
    }

    void infer(GnnNode gnnNode, OutBack outBack, long eventID, Map<Integer, Float> pd) throws Exception {//进行推理
        List<GnnNode> gnnNodes = connectionTable.getFeatureList(gnnNode, jumpTimes);
        aggNode(gnnNodes);//聚合本层节点
        if (sonLayer != null) {//还有下一层
            sonLayer.nextInfer(outBack, gnnNodes, eventID, pd);
        } else {//进入线性层
            toBatchNerve(outBack, gnnNodes, eventID, pd);
        }
    }

    void nextInfer(OutBack outBack, List<GnnNode> gnnNodes, long eventID, Map<Integer, Float> pd) throws Exception {
        aggNode(gnnNodes);//聚合本层节点
        if (sonLayer != null) {//还有下一层
            sonLayer.nextInfer(outBack, gnnNodes, eventID, pd);
        } else {//进入线性层
            toBatchNerve(outBack, gnnNodes, eventID, pd);
        }
    }

    //开始训练
    void study(OutBack outBack, List<NodeStudy> nodeStudies, long eventID, Map<Integer, Float> pd) throws Exception {
        updateTimes++;
        for (NodeStudy nodeStudy : nodeStudies) {
            GnnNode rootGnn = nodeStudy.getRootGnnNode();
            if (rootGnn != null) {
                List<GnnNode> gnnFeatures = connectionTable.getRandomSonNodes(rootGnn, jumpTimes);//构建子图
                aggNode(gnnFeatures);//聚合本层节点
                nodeStudy.setGnnFeatures(gnnFeatures);
            } else {
                throw new IllegalArgumentException("训练时有根节点为空");
            }
        }
        this.studyNodeStudies = nodeStudies;
        if (sonLayer != null) {//还有下一层
            sonLayer.nextStudy(outBack, nodeStudies, eventID, pd);
        } else {//进入线性层
            toStudyBatchNerve(outBack, nodeStudies, eventID, pd);
        }
    }


    void nextStudy(OutBack outBack, List<NodeStudy> nodeStudies, long eventID, Map<Integer, Float> pd) throws Exception {
        updateTimes++;
        for (NodeStudy nodeStudy : nodeStudies) {
            List<GnnNode> gnnFeatures = nodeStudy.getGnnFeatures();
            aggNode(gnnFeatures);//聚合本层节点
        }
        this.studyNodeStudies = nodeStudies;
        if (sonLayer != null) {//还有下一层
            sonLayer.nextStudy(outBack, nodeStudies, eventID, pd);
        } else {//进入线性层
            toStudyBatchNerve(outBack, nodeStudies, eventID, pd);
        }
    }

    void backError(List<Matrix> nextErrorMatrixList) throws Exception {
        int size = nextErrorMatrixList.size();
        Map<Integer, List<Integer>> connectMap = connectionTable.getConnectMap();
        List<Map<Integer, NodeError>> rootErrorList = new ArrayList<>();
        List<Map<Integer, NodeError>> otherErrorList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Map<Integer, NodeError> rootErrorMap = new HashMap<>();
            Map<Integer, NodeError> otherErrorMap = new HashMap<>();
            List<GnnNode> gnnNodes = studyNodeStudies.get(i).getGnnFeatures();
            Matrix error = nextErrorMatrixList.get(i);
            gnnNodes.get(0).setDeepError(error);
            addError(gnnNodes, connectMap, rootErrorMap, otherErrorMap);
            rootErrorList.add(rootErrorMap);
            otherErrorList.add(otherErrorMap);
        }
        updatePower(rootErrorList, true, size);
        updatePower(otherErrorList, false, size);
        if (fatherLayer != null) {
            fatherLayer.backError2(studyNodeStudies);
        } else {
            updateTable(studyNodeStudies);
        }
    }

    private void backError2(List<NodeStudy> studyNodeStudies) throws Exception {
        int size = studyNodeStudies.size();
        Map<Integer, List<Integer>> connectMap = connectionTable.getConnectMap();
        List<Map<Integer, NodeError>> rootErrorList = new ArrayList<>();
        List<Map<Integer, NodeError>> otherErrorList = new ArrayList<>();
        for (NodeStudy studyNodeStudy : studyNodeStudies) {
            Map<Integer, NodeError> rootErrorMap = new HashMap<>();
            Map<Integer, NodeError> otherErrorMap = new HashMap<>();
            List<GnnNode> gnnNodes = studyNodeStudy.getGnnFeatures();
            addError(gnnNodes, connectMap, rootErrorMap, otherErrorMap);
            rootErrorList.add(rootErrorMap);
            otherErrorList.add(otherErrorMap);
        }
        updatePower(rootErrorList, true, size);
        updatePower(otherErrorList, false, size);
        if (fatherLayer != null) {
            fatherLayer.backError2(studyNodeStudies);
        } else {
            updateTable(studyNodeStudies);
        }
    }

    private void updateTable(List<NodeStudy> studyNodeStudies) throws Exception {//更新离散特征表
        Map<Integer, Matrix> tableMap = new HashMap<>();
        for (NodeStudy studyNodeStudy : studyNodeStudies) {
            List<GnnNode> gnnNodes = studyNodeStudy.getGnnFeatures();
            insertTableError(gnnNodes, tableMap);
        }
        for (Map.Entry<Integer, Matrix> entry : tableMap.entrySet()) {
            int key = entry.getKey();
            Matrix error = entry.getValue();
            connectionTable.updateFeatureMap(key, error, dymStudy, studyRate, updateTimes);
        }
    }

    private void insertTableError(List<GnnNode> gnnNodes, Map<Integer, Matrix> tableMap) throws Exception {
        for (GnnNode gnnNode : gnnNodes) {
            int id = gnnNode.getId();
            Matrix error = gnnNode.getDeepError();
            if (tableMap.containsKey(id)) {
                Matrix myError = tableMap.get(id);
                Matrix nextError = matrixOperation.add(myError, error);
                tableMap.put(id, nextError);
            } else {
                tableMap.put(id, error);
            }
            List<GnnNode> sonList = gnnNode.getNodeList();
            if (sonList != null) {
                insertTableError(sonList, tableMap);
            }
        }

    }

    private void addError(List<GnnNode> gnnNodes, Map<Integer, List<Integer>> connectMap, Map<Integer, NodeError> rootErrorMap
            , Map<Integer, NodeError> otherErrorMap) throws Exception {
        for (GnnNode gnnNode : gnnNodes) {
            int jump = gnnNode.getJumpTimes();
            int aggScope = jumpTimes - deep;//2-1
            if (jump >= aggScope) {
                continue;
            }
            Matrix myError = gnnNode.getError();//来自本层误差
            Matrix deepError = gnnNode.getDeepError();//来自深层误差
            if (myError == null) {
                myError = deepError;
            } else {
                myError = matrixOperation.add(myError, deepError);
            }
            List<Matrix> featList = gnnNode.getFeatureList();
            int outFeatIdx = deep + 1;
            // 底层强校验：本层输出必须存在deep+1，缺失直接抛逻辑异常
            if (featList.size() <= outFeatIdx) {
                throw new RuntimeException(String.format(
                        "节点id=%d 反向梯度失败：缺失本层输出特征，deep=%d 目标下标=%d，特征列表长度=%d",
                        gnnNode.getId(), deep, outFeatIdx, featList.size()
                ));
            }
            Matrix outMatrix = featList.get(outFeatIdx);
            // 激活函数求导
            Matrix error = unActiveMatrix(myError, outMatrix);
            // 1. 计算selfPower、bias梯度，分发自身输入梯度
            addRootError(error, rootErrorMap, gnnNode);
            // 2. 计算otherPower梯度，分发邻居子节点梯度
            List<GnnNode> sonNodes = gnnNode.getNodeList();
            if (sonNodes != null && !sonNodes.isEmpty()) {
                addOtherError(error, otherErrorMap, gnnNode, connectMap);
                // 递归向下传递梯度给子节点
                addError(sonNodes, connectMap, rootErrorMap, otherErrorMap);
            }
        }
    }


    private void updatePower(List<Map<Integer, NodeError>> errorList, boolean root, int size) throws Exception {
        Map<Integer, NodeError> errorMap = new HashMap<>();
        float times = 1f / size;
        // 累加批次内所有样本梯度
        for (Map<Integer, NodeError> errorNodeMap : errorList) {
            for (Map.Entry<Integer, NodeError> entry : errorNodeMap.entrySet()) {
                int key = entry.getKey();
                NodeError nodeError = entry.getValue();
                if (errorMap.containsKey(key)) {
                    NodeError allNodeError = errorMap.get(key);
                    Matrix sigmaErrorPower = matrixOperation.add(allNodeError.getErrorPower(), nodeError.getErrorPower());
                    allNodeError.setErrorPower(sigmaErrorPower);
                    // self分支才累加偏置梯度
                    if (root) {
                        Matrix sigmaErrorBais = matrixOperation.add(allNodeError.getErrorBais(), nodeError.getErrorBais());
                        allNodeError.setErrorBais(sigmaErrorBais);
                    }
                } else {
                    errorMap.put(key, nodeError);
                }
            }
        }
        // AdamW梯度更新
        for (Map.Entry<Integer, NodeError> entry : errorMap.entrySet()) {
            int key = entry.getKey();
            NodeError nodeError = entry.getValue();
            GnnPower gnnPower = powerMap.get(key);
            Matrix errorPower = nodeError.getErrorPower();
            Matrix avgErrorPoser = matrixOperation.mathMulBySelf(errorPower, times);
            if (root) {
                Matrix errorBais = nodeError.getErrorBais();
                Matrix avgErrorBais = matrixOperation.mathMulBySelf(errorBais, times);
                Matrix subBais = dymStudy.getErrorMatrixByStudy(studyRate, gnnPower.getDymBais1(), gnnPower.getDymBais2()
                        , avgErrorBais, updateTimes);
                Matrix subSelfPower = dymStudy.getErrorMatrixByStudy(studyRate, gnnPower.getDymSelfPower1(),
                        gnnPower.getDymSelfPower2(), avgErrorPoser, updateTimes);
                Matrix bais = matrixOperation.add(subBais, gnnPower.getBais());
                Matrix selfPower = matrixOperation.add(subSelfPower, gnnPower.getSelfPower());
                gnnPower.setBais(bais);
                gnnPower.setSelfPower(selfPower);
            } else {
                Matrix subOtherPower = dymStudy.getErrorMatrixByStudy(studyRate, gnnPower.getDymOtherPower1(),
                        gnnPower.getDymOtherPower2(), avgErrorPoser, updateTimes);
                Matrix newOther = matrixOperation.add(subOtherPower, gnnPower.getOtherPower());
                gnnPower.setOtherPower(newOther);
            }
        }
    }

    private float getDu(int id1, int id2, Map<Integer, List<Integer>> connectMap) {
        int size1 = connectMap.get(id1).size();
        int size2 = connectMap.get(id2).size();
        double du1 = 1f / Math.sqrt(size1);
        double du2 = 1f / Math.sqrt(size2);
        return (float) (du1 * du2);
    }

    private void addRootError(Matrix error, Map<Integer, NodeError> rootMap, GnnNode gnnNode) throws Exception {
        int id = gnnNode.getId();
        int nodeType = connectionTable.getNodeType(id);
        GnnPower gnnPower = powerMap.get(nodeType);
        List<Matrix> featList = gnnNode.getFeatureList();
        int selfFeatIdx = deep;
        // 底层下标强校验
        if (featList.size() <= selfFeatIdx) {
            throw new RuntimeException(String.format(
                    "节点id=%d 自身梯度计算失败：缺失输入特征deep=%d，列表长度=%d",
                    id, selfFeatIdx, featList.size()
            ));
        }
        Matrix rootFeature = featList.get(selfFeatIdx);
        Matrix powerMatrix = gnnPower.getSelfPower();
        // dLoss/dW_self = feat^T · dLoss/dZ
        Matrix errorPower = matrixOperation.matrixMulPd(error, rootFeature, powerMatrix, false);
        // 向下传递给输入特征的梯度 dLoss/dX = dLoss/dZ · W_self^T
        Matrix nextError = matrixOperation.matrixMulPd(error, rootFeature, powerMatrix, true);
        // 累加当前类型权重、偏置梯度
        if (rootMap.containsKey(nodeType)) {
            NodeError nodeError = rootMap.get(nodeType);
            Matrix myErrorPower = nodeError.getErrorPower();
            Matrix addError = matrixOperation.add(myErrorPower, errorPower);
            nodeError.setErrorPower(addError);
            Matrix bais = nodeError.getErrorBais();
            Matrix addBais = matrixOperation.add(bais, error);
            nodeError.setErrorBais(addBais);
        } else {
            NodeError nodeError = new NodeError();
            nodeError.setErrorPower(errorPower);
            nodeError.setErrorBais(error.copy());
            rootMap.put(nodeType, nodeError);
        }
        // 梯度裁剪只作用于向下传递的特征梯度，不影响权重更新梯度
        if (layerGCut) {
            nextError = dymStudy.getClipMatrix(nextError, true);
        }
        gnnNode.setDeepError(nextError);
    }//主节点误差=下层深度误差

    private void addOtherError(Matrix error, Map<Integer, NodeError> errorMap, GnnNode gnnNode, Map<Integer, List<Integer>> connectMap) throws Exception {
        int rootID = gnnNode.getId();
        List<GnnNode> gnnNodes = gnnNode.getNodeList();
        Map<Integer, Matrix> typeError = new HashMap<>();
        for (GnnNode sonNode : gnnNodes) {
            int id = sonNode.getId();
            int nodeType = connectionTable.getNodeType(id);
            GnnPower gnnPower = powerMap.get(nodeType);
            List<Matrix> sonFeatList = sonNode.getFeatureList();
            int selfFeatIdx = deep;
            // 底层下标强校验
            if (sonFeatList.size() <= selfFeatIdx) {
                throw new RuntimeException(String.format(
                        "邻居节点id=%d 邻域梯度失败：缺失输入deep=%d，列表长度=%d",
                        id, selfFeatIdx, sonFeatList.size()
                ));
            }
            Matrix rootFeature = sonFeatList.get(selfFeatIdx);
            Matrix powerMatrix = gnnPower.getOtherPower();
            // 正向反向du完全对齐，提取统一计算
            float duRaw = getDu(id, rootID, connectMap);
            float du = duRaw * gnnPower.getArf();
            Matrix feature = matrixOperation.mathMulBySelf(rootFeature, du);
            Matrix errorPower = matrixOperation.matrixMulPd(error, feature, powerMatrix, false);
            Matrix power = matrixOperation.mathMulBySelf(powerMatrix, du);
            // 向下传递给邻居节点的梯度
            Matrix nextError = matrixOperation.matrixMulPd(error, rootFeature, power, true);
            if (layerGCut) {
                nextError = dymStudy.getClipMatrix(nextError, true);
            }
            // 累加子节点梯度（多父节点共享子节点时自动叠加）
            sonNode.setError(nextError);
            sonNode.setDeepError(nextError.copy());
            // 按节点类型累加other权重梯度
            if (typeError.containsKey(nodeType)) {
                Matrix myError = typeError.get(nodeType);
                Matrix sigma = matrixOperation.add(myError, errorPower);
                typeError.put(nodeType, sigma);
            } else {
                typeError.put(nodeType, errorPower);
            }
        }
        // 批量写入全局批次梯度map
        for (Map.Entry<Integer, Matrix> myError : typeError.entrySet()) {
            int nodeType = myError.getKey();
            Matrix errorPower = myError.getValue();
            if (errorMap.containsKey(nodeType)) {
                NodeError nodeError = errorMap.get(nodeType);
                Matrix myErrorPower = nodeError.getErrorPower();
                Matrix addError = matrixOperation.add(myErrorPower, errorPower);
                nodeError.setErrorPower(addError);
            } else {
                NodeError nodeError = new NodeError();
                nodeError.setErrorPower(errorPower);
                errorMap.put(nodeType, nodeError);
            }
        }
    }

    private void toBatchNerve(OutBack outBack, List<GnnNode> nodeList, long eventID, Map<Integer, Float> pd) throws Exception {//输入进线性层
        List<FeatureBody> featureBodies = new ArrayList<>();
        GnnNode gnnNode = nodeList.get(0);
        Matrix feature = gnnNode.getFeatureList().get(deep + 1);
        FeatureBody featureBody = new FeatureBody();
        featureBody.setFeature(feature);
        featureBodies.add(featureBody);
        batchNerveManager.getInputBlock().postMessage(featureBodies, false, outBack, eventID, pd);
    }

    private void toStudyBatchNerve(OutBack outBack, List<NodeStudy> nodeStudies, long eventID, Map<Integer, Float> pd) throws Exception {//输入进线性层
        List<FeatureBody> featureBodies = new ArrayList<>();
        for (NodeStudy nodeStudy : nodeStudies) {
            GnnNode gnnNode = nodeStudy.getGnnFeatures().get(0);
            Matrix feature = gnnNode.getFeatureList().get(deep + 1);
            FeatureBody featureBody = new FeatureBody();
            featureBody.setFeature(feature);
            featureBody.setE(nodeStudy.getE());
            featureBodies.add(featureBody);
        }
        batchNerveManager.getInputBlock().postMessage(featureBodies, true, outBack, eventID, pd);
    }


    /**
     * 自底向上冒泡聚合：先递归所有子节点完成本层聚合，再聚合自身
     *
     * @param rootGnnNodes 当前待处理节点集合
     * @throws Exception 矩阵运算异常
     */
    private void aggNode(List<GnnNode> rootGnnNodes) throws Exception {
        int neighborFeatIdx = deep + 1;
        int aggScope = jumpTimes - deep;
        for (GnnNode gnnNode : rootGnnNodes) {
            List<GnnNode> sonList = gnnNode.getNodeList();
            int jump = gnnNode.getJumpTimes();
            if (sonList != null && !sonList.isEmpty() && jump < aggScope) {
                aggNode(sonList);
                Matrix aggFeature = getAggMatrix(gnnNode, sonList, deep, neighborFeatIdx);
                gnnNode.getFeatureList().add(aggFeature);
            }
        }
    }


    private Matrix getAggMatrix(GnnNode rootNode, List<GnnNode> sonList, int selfFeatIdx, int neighborFeatIdx) throws Exception {
        int id = rootNode.getId();
        List<Matrix> selfFeatList = rootNode.getFeatureList();
        // 防护：防止输入特征下标越界
        if (selfFeatList.size() <= selfFeatIdx) {
            throw new RuntimeException("节点id=" + id + " 缺失输入特征，目标下标：" + selfFeatIdx);
        }
        Matrix myFeature = selfFeatList.get(selfFeatIdx);
        int nodeType = connectionTable.getNodeType(id);
        Matrix connectionFeature = connectionTable.getConnectOut(id, powerMap, sonList, neighborFeatIdx);
        if (powerMap.containsKey(nodeType)) {
            GnnPower gnnPower = powerMap.get(nodeType);
            Matrix selfPower = gnnPower.getSelfPower();
            Matrix bais = gnnPower.getBais();
            Matrix selfFeature = matrixOperation.mulMatrix(myFeature, selfPower);
            Matrix out = matrixOperation.addThreeMatrix(selfFeature, connectionFeature, bais);
            activeMatrix(out);
            return out;
        } else {
            throw new IllegalArgumentException("样本中有配置里不存在的节点类型id");
        }
    }

    private void activeMatrix(Matrix out) {
        int x = out.getX();
        int y = out.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = activeFunction.function(out.getValue(i, j));
                out.setValue(i, j, value);
            }
        }
    }

    private Matrix unActiveMatrix(Matrix errorMatrix, Matrix outMatrix) {
        int x = errorMatrix.getX();
        int y = errorMatrix.getY();
        Matrix myError = new Matrix(x, y);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float error = errorMatrix.getValue(i, j);
                float out = outMatrix.getValue(i, j);
                float value = activeFunction.functionG(out) * error;
                myError.setValue(i, j, value);
            }
        }
        return myError;
    }

    private GnnPower initGnnPower(int featureLength, float otherValue) {
        GnnPower gnnPower = new GnnPower();
        Matrix selfPower = new Matrix(featureLength, featureLength);
        Matrix otherPower = new Matrix(featureLength, featureLength);
        Matrix bais = new Matrix(1, featureLength);
        selfPower.randomInit(featureLength);
        otherPower.randomInit(featureLength);
        bais.randomInit(featureLength);
        gnnPower.setArf(otherValue);
        gnnPower.setSelfPower(selfPower);
        gnnPower.setDymSelfPower1(new Matrix(featureLength, featureLength));
        gnnPower.setDymSelfPower2(new Matrix(featureLength, featureLength));
        gnnPower.setOtherPower(otherPower);
        gnnPower.setDymOtherPower1(new Matrix(featureLength, featureLength));
        gnnPower.setDymOtherPower2(new Matrix(featureLength, featureLength));
        gnnPower.setBais(bais);
        gnnPower.setDymBais1(new Matrix(1, featureLength));
        gnnPower.setDymBais2(new Matrix(1, featureLength));
        return gnnPower;
    }


    void connectSonLayer(GnnLayer sonLayer) {
        this.sonLayer = sonLayer;
    }

    void connectFatherLayer(GnnLayer fatherLayer) {
        this.fatherLayer = fatherLayer;
    }
}
