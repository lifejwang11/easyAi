package org.dromara.easyai.recommend;

import org.dromara.easyai.batchNerve.BatchNerveManager;
import org.dromara.easyai.batchNerve.FeatureBody;
import org.dromara.easyai.config.GnnConfig;
import org.dromara.easyai.conv.DymStudy;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;

import java.util.*;

/**
 * @author lidapeng
 * @time 2026/7/11 08:58
 * @des gnn层
 */
public class GnnLayer {
    private final Map<Integer, GnnPower> powerMap = new HashMap<>();//权重矩阵
    private final MatrixOperation matrixOperation = new MatrixOperation();
    private final ConnectionTable connectionTable;
    private final ActiveFunction activeFunction;
    private final DymStudy dymStudy;
    private GnnLayer sonLayer;
    private GnnLayer fatherLayer;
    private final int featureLength;//特征维度
    private final int jumpTimes;//跳跃数
    private final BatchNerveManager batchNerveManager;
    private final int deep;//所处深度
    private int updateTimes = 0;
    private final float studyRate;//全局学习率
    private List<NodeStudy> studyNodeStudies;

    public GnnLayer(GnnConfig gnnConfig, ActiveFunction activeFunction, ConnectionTable connectionTable
            , BatchNerveManager batchNerveManager, int deep) {//特征维度 类别数量
        dymStudy = new DymStudy(gnnConfig.getgMaxTh(), gnnConfig.isAuto(), gnnConfig.getLayGMaxTh());
        this.studyRate = gnnConfig.getStudy();
        this.activeFunction = activeFunction;
        this.deep = deep;
        this.jumpTimes = gnnConfig.getJumpTimes();
        int gnnTypeNumber = gnnConfig.getGnnTypeNumber();
        featureLength = gnnConfig.getFeatureLength();
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

    //开始训练
    public void study(OutBack outBack, List<NodeStudy> nodeStudies, boolean study
            , long eventID, Map<Integer, Float> pd) throws Exception {
        updateTimes++;
        for (NodeStudy nodeStudy : nodeStudies) {
            int t = nodeStudy.getRootId() - 1;
            List<GnnNode> gnnFeatures = connectionTable.getRandomSonNodes(t, jumpTimes);
            aggNode(gnnFeatures);//聚合本层节点
            nodeStudy.setGnnFeatures(gnnFeatures);
        }
        this.studyNodeStudies = nodeStudies;
        if (sonLayer != null) {//还有下一层
            sonLayer.nextStudy(outBack, nodeStudies, study, eventID, pd);
        } else {//进入线性层
            toBatchNerve(outBack, nodeStudies, study, eventID, pd);
        }
    }


    public void nextStudy(OutBack outBack, List<NodeStudy> nodeStudies, boolean study
            , long eventID, Map<Integer, Float> pd) throws Exception {
        updateTimes++;
        for (NodeStudy nodeStudy : nodeStudies) {
            List<GnnNode> gnnFeatures = nodeStudy.getGnnFeatures();
            aggNode(gnnFeatures);//聚合本层节点
        }
        this.studyNodeStudies = nodeStudies;
        if (sonLayer != null) {//还有下一层
            sonLayer.nextStudy(outBack, nodeStudies, study, eventID, pd);
        } else {//进入线性层
            toBatchNerve(outBack, nodeStudies, study, eventID, pd);
        }
    }

    public void backError(List<Matrix> nextErrorMatrixList) throws Exception {//误差从线性层回传
        int size = nextErrorMatrixList.size();
        Map<Integer, NodeError> rootErrorMap = new HashMap<>();
        Map<Integer, NodeError> otherErrorMap = new HashMap<>();
        Map<Integer, List<Integer>> connectMap = connectionTable.getConnectMap();
        for (int i = 0; i < size; i++) {
            GnnNode gnnNode = studyNodeStudies.get(i).getGnnFeatures().get(0);
            Matrix outMatrix = gnnNode.getFeatureList().get(deep + 1);
            Matrix rootError = nextErrorMatrixList.get(i);
            Matrix error = unActiveMatrix(rootError, outMatrix);//脱激活函数
            addRootError(error, rootErrorMap, gnnNode);//下一层主节点误差
            addOtherError(error, otherErrorMap, gnnNode, connectMap);
        }
        updatePower(rootErrorMap, true);
        updatePower(otherErrorMap, false);
        if (fatherLayer != null) {//继续将误差向浅层传递

        } else {//已经在第一层了，直接更新离散特征表

        }
    }

    private void backError2() {

    }


    private void updatePower(Map<Integer, NodeError> errorMap, boolean root) throws Exception {
        for (Map.Entry<Integer, NodeError> entry : errorMap.entrySet()) {
            int key = entry.getKey();
            NodeError nodeError = entry.getValue();
            GnnPower gnnPower = powerMap.get(key);
            Matrix errorPower = nodeError.getErrorPower();
            float times = 1f / nodeError.getAddTimes();
            matrixOperation.mathMul(errorPower, times);
            if (root) {
                Matrix errorBais = nodeError.getErrorBais();
                matrixOperation.mathMul(errorBais, times);
                Matrix subBais = dymStudy.getErrorMatrixByStudy(studyRate, gnnPower.getDymBais1(), gnnPower.getDymBais2()
                        , errorBais, updateTimes);
                Matrix subSelfPower = dymStudy.getErrorMatrixByStudy(studyRate, gnnPower.getDymSelfPower1(),
                        gnnPower.getDymSelfPower2(), errorPower, updateTimes);
                Matrix bais = matrixOperation.add(subBais, gnnPower.getBais());
                Matrix selfPower = matrixOperation.add(subSelfPower, gnnPower.getSelfPower());
                gnnPower.setBais(bais);
                gnnPower.setSelfPower(selfPower);
            } else {
                Matrix subOtherPower = dymStudy.getErrorMatrixByStudy(studyRate, gnnPower.getDymOtherPower1(),
                        gnnPower.getDymOtherPower2(), errorPower, updateTimes);
                Matrix otherPower = matrixOperation.add(subOtherPower, gnnPower.getOtherPower());
                gnnPower.setOtherPower(otherPower);
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
        int nodeType = connectionTable.getNodeType(id);//节点类别
        GnnPower gnnPower = powerMap.get(nodeType);//该类别权重
        Matrix rootFeature = gnnNode.getFeatureList().get(deep);//该节点输入特征
        Matrix powerMatrix = gnnPower.getSelfPower();
        Matrix errorPower = matrixOperation.matrixMulPd(error, rootFeature, powerMatrix, false);
        Matrix nextError = matrixOperation.matrixMulPd(error, rootFeature, powerMatrix, true);
        if (rootMap.containsKey(nodeType)) {
            NodeError nodeError = rootMap.get(nodeType);
            Matrix myErrorPower = nodeError.getErrorPower();
            Matrix addError = matrixOperation.add(myErrorPower, errorPower);
            nodeError.setErrorPower(addError);
            Matrix bais = nodeError.getErrorBais();
            Matrix addBais = matrixOperation.add(bais, error);
            nodeError.setErrorBais(addBais);
            nodeError.setAddTimes(nodeError.getAddTimes() + 1);
        } else {
            NodeError nodeError = new NodeError();
            nodeError.setAddTimes(1);
            nodeError.setErrorPower(errorPower);
            nodeError.setErrorBais(error.copy());
            rootMap.put(nodeType, nodeError);
        }
        gnnNode.setError(nextError);
    }

    private void addOtherError(Matrix error, Map<Integer, NodeError> errorMap, GnnNode gnnNode, Map<Integer, List<Integer>> connectMap) throws Exception {
        int rootID = gnnNode.getId();
        List<GnnNode> gnnNodes = gnnNode.getNodeList();
        Map<Integer, Matrix> typeError = new HashMap<>();
        for (GnnNode sonNode : gnnNodes) {
            int id = sonNode.getId();
            int nodeType = connectionTable.getNodeType(id);//节点类别
            GnnPower gnnPower = powerMap.get(nodeType);//该类别权重
            Matrix rootFeature = sonNode.getFeatureList().get(deep);//该节点输入特征
            Matrix powerMatrix = gnnPower.getOtherPower();//邻居节点;
            float du = getDu(id, rootID, connectMap) * gnnPower.getArf();
            Matrix feature = matrixOperation.mathMulBySelf(rootFeature, du);
            Matrix errorPower = matrixOperation.matrixMulPd(error, feature, powerMatrix, false);
            Matrix power = matrixOperation.mathMulBySelf(powerMatrix, du);
            Matrix nextError = matrixOperation.matrixMulPd(error, rootFeature, power, true);
            sonNode.setError(nextError);
            if (typeError.containsKey(nodeType)) {
                Matrix myError = typeError.get(nodeType);
                Matrix sigma = matrixOperation.add(myError, errorPower);
                typeError.put(nodeType, sigma);
            } else {
                typeError.put(nodeType, errorPower);
            }
        }

        for (Map.Entry<Integer, Matrix> myError : typeError.entrySet()) {
            int nodeType = myError.getKey();
            Matrix errorPower = myError.getValue();
            if (errorMap.containsKey(nodeType)) {
                NodeError nodeError = errorMap.get(nodeType);
                Matrix myErrorPower = nodeError.getErrorPower();
                Matrix addError = matrixOperation.add(myErrorPower, errorPower);
                nodeError.setErrorPower(addError);
                nodeError.setAddTimes(nodeError.getAddTimes() + 1);
            } else {
                NodeError nodeError = new NodeError();
                nodeError.setAddTimes(1);
                nodeError.setErrorPower(errorPower);
                errorMap.put(nodeType, nodeError);
            }
        }
    }


    private void toBatchNerve(OutBack outBack, List<NodeStudy> nodeStudies, boolean study
            , long eventID, Map<Integer, Float> pd) throws Exception {//输入进线性层
        List<FeatureBody> featureBodies = new ArrayList<>();
        for (NodeStudy nodeStudy : nodeStudies) {
            GnnNode gnnNode = nodeStudy.getGnnFeatures().get(0);
            Matrix feature = gnnNode.getFeatureList().get(deep + 1);
            FeatureBody featureBody = new FeatureBody();
            featureBody.setFeature(feature);
            featureBody.setE(nodeStudy.getE());
            featureBodies.add(featureBody);
        }
        batchNerveManager.getInputBlock().postMessage(featureBodies, study, outBack, eventID, pd);
    }


    private void aggNode(List<GnnNode> rootGnnNodes) throws Exception {
        int aggScope = jumpTimes - deep;
        for (GnnNode gnnNode : rootGnnNodes) {
            List<GnnNode> sonList = gnnNode.getNodeList();
            int jump = gnnNode.getJumpTimes();
            if (sonList != null && jump < aggScope) {
                Matrix feature = getAggMatrix(gnnNode, sonList);
                gnnNode.getFeatureList().add(feature);
                aggNode(sonList);
            }
        }
    }


    private Matrix getAggMatrix(GnnNode rootNode, List<GnnNode> sonList) throws Exception {
        int id = rootNode.getId();
        Matrix myFeature = rootNode.getFeatureList().get(deep);//节点本身特征
        int nodeType = connectionTable.getNodeType(id);
        Matrix connectionFeature = connectionTable.getConnectOut(id, powerMap, sonList, deep);
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


    public void connectSonLayer(GnnLayer sonLayer) {
        this.sonLayer = sonLayer;
    }

    public void connectFatherLayer(GnnLayer fatherLayer) {
        this.fatherLayer = fatherLayer;
    }
}
