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
    private final int studyMaxJumpNumber;//训练时每一跳最多聚合邻居的数量
    private final int studyMinJumpNumber;//训练时每一跳最小聚合邻居的数量
    private final List<Map<Integer, Matrix>> insertFeatureList = new ArrayList<>();
    private final List<Map<Integer, Matrix>> outFeatureList = new ArrayList<>();
    private final List<Integer> rootIdList = new ArrayList<>();
    private final Random random = new Random();
    private final int deep;//所处深度
    private int updateTimes = 0;
    private final float studyRate;//全局学习率

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
        studyMaxJumpNumber = gnnConfig.getStudyMaxJumpNumber();
        studyMinJumpNumber = gnnConfig.getStudyMinJumpNumber();
        if (studyMaxJumpNumber <= 0) {
            throw new IllegalArgumentException("每一层聚合邻居数量上限必须大于0");
        }
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
        Map<Integer, Matrix> featureMatrixMap = connectionTable.getFeatureMatrixMap();
        Map<Integer, List<Integer>> connectMap = connectionTable.getConnectMap();
        insertFeatureList.clear();
        outFeatureList.clear();
        rootIdList.clear();
        for (NodeStudy nodeStudy : nodeStudies) {
            int t = nodeStudy.getRootId() - 1;
            Matrix rootMatrix = getAggMatrix(t, featureMatrixMap);//root的聚合特征
            Map<Integer, Matrix> gnnBodyMap = new HashMap<>();//特征图
            nodeStudy.setGnnBodyMap(gnnBodyMap);
            getAgg(connectMap, t, featureMatrixMap, rootMatrix, gnnBodyMap);
            insertFeatureList.add(getIdList(gnnBodyMap, featureMatrixMap));
            outFeatureList.add(gnnBodyMap);
            rootIdList.add(t);
        }
        if (sonLayer != null) {//还有下一层
            sonLayer.nextStudy(outBack, nodeStudies, study, eventID, pd);
        } else {//进入线性层
            toBatchNerve(outBack, nodeStudies, study, eventID, pd);
        }
    }

    private Map<Integer, Matrix> getIdList(Map<Integer, Matrix> gnnBodyMap, Map<Integer, Matrix> featureMatrixMap) {
        Map<Integer, Matrix> map = new HashMap<>();
        for (Map.Entry<Integer, Matrix> entry : gnnBodyMap.entrySet()) {
            int key = entry.getKey();
            map.put(key, featureMatrixMap.get(key));
        }
        return map;
    }


    public void nextStudy(OutBack outBack, List<NodeStudy> nodeStudies, boolean study
            , long eventID, Map<Integer, Float> pd) throws Exception {
        updateTimes++;
        Map<Integer, List<Integer>> connectMap = connectionTable.getConnectMap();
        if (study) {
            insertFeatureList.clear();
            outFeatureList.clear();
            rootIdList.clear();
        }
        for (NodeStudy nodeStudy : nodeStudies) {
            int t = nodeStudy.getRootId() - 1;
            Map<Integer, Matrix> featureMatrixMap = nodeStudy.getGnnBodyMap();
            if (study) {
                insertFeatureList.add(featureMatrixMap);
                rootIdList.add(t);
            }
            Matrix rootMatrix = getAggMatrix(t, featureMatrixMap);//root的聚合特征
            Map<Integer, Matrix> gnnBodyMap = new HashMap<>();//输出特征图
            getAgg(connectMap, t, featureMatrixMap, rootMatrix, gnnBodyMap);
            nodeStudy.setGnnBodyMap(gnnBodyMap);
            if (study) {
                outFeatureList.add(gnnBodyMap);
            }
        }
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
            Matrix matrix = nextErrorMatrixList.get(i);
            int x = matrix.getX();
            int y = matrix.getY();
            Matrix rootError = matrix.getSonOfMatrix(0, 0, x, y / 2);
            Matrix otherError = matrix.getSonOfMatrix(0, y / 2, x, y / 2);
            Map<Integer, Matrix> insertMap = insertFeatureList.get(i);
            float otherSize = 1f / (insertMap.size() - 1f);
            matrixOperation.mathMul(otherError, otherSize);
            Map<Integer, Matrix> outMap = outFeatureList.get(i);//输出矩阵
            int rootID = rootIdList.get(i);
            unActiveMatrix(rootError, outMap.get(rootID));//脱激活函数
            Matrix rootNextError = addNodeError(rootError, rootID, insertMap, rootErrorMap, 0, connectMap);
            for (Map.Entry<Integer, Matrix> insert : insertMap.entrySet()) {
                int key = insert.getKey();
                if (key != rootID) {
                    Matrix otherNextError = addNodeError(otherError, key, insertMap, otherErrorMap, rootID, connectMap);
                }
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

    private Matrix addNodeError(Matrix error, int id, Map<Integer, Matrix> insertMap,
                                Map<Integer, NodeError> errorMap, int rootID,
                                Map<Integer, List<Integer>> connectMap) throws Exception {
        Matrix nextError;
        int nodeType = connectionTable.getNodeType(id);//节点类别
        GnnPower gnnPower = powerMap.get(nodeType);//该类别权重
        Matrix rootFeature = insertMap.get(id);//该节点特征
        Matrix powerMatrix;
        if (rootID == 0) {
            powerMatrix = gnnPower.getSelfPower();//根节点
        } else {
            powerMatrix = gnnPower.getOtherPower();//邻居节点
        }
        Matrix errorPower;
        if (rootID > 0) {//非主节点
            float du = getDu(id, rootID, connectMap) * gnnPower.getArf();
            Matrix feature = matrixOperation.mathMulBySelf(rootFeature, du);
            errorPower = matrixOperation.matrixMulPd(error, feature, powerMatrix, false);
            Matrix power = matrixOperation.mathMulBySelf(powerMatrix, du);
            nextError = matrixOperation.matrixMulPd(error, rootFeature, power, true);
        } else {
            errorPower = matrixOperation.matrixMulPd(error, rootFeature, powerMatrix, false);
            nextError = matrixOperation.matrixMulPd(error, rootFeature, powerMatrix, true);
        }
        if (errorMap.containsKey(nodeType)) {
            NodeError nodeError = errorMap.get(nodeType);
            Matrix myErrorPower = nodeError.getErrorPower();
            Matrix addError = matrixOperation.add(myErrorPower, errorPower);
            nodeError.setErrorPower(addError);
            if (rootID == 0) {
                Matrix bais = nodeError.getErrorBais();
                Matrix addBais = matrixOperation.add(bais, error);
                nodeError.setErrorBais(addBais);
            }
            nodeError.setAddTimes(nodeError.getAddTimes() + 1);
        } else {
            NodeError nodeError = new NodeError();
            nodeError.setAddTimes(1);
            nodeError.setErrorPower(errorPower);
            if (rootID == 0) {
                nodeError.setErrorBais(error.copy());
            }
            errorMap.put(nodeType, nodeError);
        }
        return nextError;
    }


    private void toBatchNerve(OutBack outBack, List<NodeStudy> nodeStudies, boolean study
            , long eventID, Map<Integer, Float> pd) throws Exception {//输入进线性层
        List<FeatureBody> featureBodies = new ArrayList<>();
        for (NodeStudy nodeStudy : nodeStudies) {
            int t = nodeStudy.getRootId() - 1;
            Map<Integer, Matrix> featureMatrixMap = nodeStudy.getGnnBodyMap();
            Matrix rootMatrix = featureMatrixMap.get(t);
            Matrix otherMatrix = avgMatrix(featureMatrixMap, t);
            Matrix feature = concatMatrix(rootMatrix, otherMatrix);
            FeatureBody featureBody = new FeatureBody();
            featureBody.setFeature(feature);
            featureBody.setE(nodeStudy.getE());
            featureBodies.add(featureBody);
        }
        batchNerveManager.getInputBlock().postMessage(featureBodies, study, outBack, eventID, pd);
    }

    private Matrix concatMatrix(Matrix rootMatrix, Matrix otherMatrix) {
        int size = rootMatrix.getY();
        int allSize = size * 2;
        Matrix feature = new Matrix(1, allSize);
        for (int i = 0; i < allSize; i++) {
            float value;
            if (otherMatrix != null) {
                if (i < size) {
                    value = rootMatrix.getValue(0, i);
                } else {
                    value = otherMatrix.getValue(0, i - size);
                }
            } else {
                if (i < size) {
                    value = rootMatrix.getValue(0, i);
                } else {
                    value = rootMatrix.getValue(0, i - size);
                }
            }
            feature.setValue(0, i, value);
        }
        return feature;
    }

    private Matrix avgMatrix(Map<Integer, Matrix> featureMatrixMap, int t) throws Exception {
        Matrix connectFeature = null;
        if (featureMatrixMap.size() > 1) {
            float size = 1f / (featureMatrixMap.size() - 1f);
            for (Map.Entry<Integer, Matrix> entry : featureMatrixMap.entrySet()) {
                if (entry.getKey() != t) {
                    Matrix feature = entry.getValue();
                    if (connectFeature == null) {
                        connectFeature = feature;
                    } else {
                        connectFeature = matrixOperation.add(connectFeature, feature);
                    }
                }
            }
            if (connectFeature != null) {
                matrixOperation.mathMul(connectFeature, size);
            }
        }
        return connectFeature;
    }

    private void getAgg(Map<Integer, List<Integer>> connectMap, int t, Map<Integer, Matrix> featureMatrixMap, Matrix rootMatrix, Map<Integer, Matrix> gnnBodyMap) throws Exception {
        gnnBodyMap.put(t, rootMatrix);
        Map<Integer, Matrix> gnus = null;
        for (int i = 1; i < jumpTimes; i++) {//根据跳跃次数聚合
            if (i == 1) {
                gnus = jump(gnnBodyMap, featureMatrixMap, connectMap);
            } else {
                gnus = jump(gnus, featureMatrixMap, connectMap);
            }
            if (!gnus.isEmpty()) {
                gnnBodyMap.putAll(gnus);
            }
        }
    }


    private Map<Integer, Matrix> jump(Map<Integer, Matrix> gnnBodyMap, Map<Integer, Matrix> featureMatrixMap
            , Map<Integer, List<Integer>> connectMap) throws Exception {//跳跃
        Map<Integer, Matrix> map = new HashMap<>();
        for (Map.Entry<Integer, Matrix> entry : gnnBodyMap.entrySet()) {
            int t = entry.getKey();
            List<Integer> sonList = connectMap.get(t);
            if (sonList != null && !sonList.isEmpty()) {
                List<Integer> connectList = getSonOfConnect(sonList);
                for (int j : connectList) {//遍历所有邻居
                    if (!gnnBodyMap.containsKey(j) && !map.containsKey(j)) {//聚合特征
                        Matrix otherMatrix = getAggMatrix(j, featureMatrixMap);
                        map.put(j, otherMatrix);
                    }
                }
            }
        }
        return map;
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


    private Matrix getAggMatrix(int i, Map<Integer, Matrix> featureMatrixMap) throws Exception {
        Matrix myFeature = featureMatrixMap.get(i);//节点本身特征
        int nodeType = connectionTable.getNodeType(i);
        Matrix connectionFeature = connectionTable.getConnectOut(i, featureMatrixMap, powerMap);
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

    private void unActiveMatrix(Matrix errorMatrix, Matrix outMatrix) {
        int x = errorMatrix.getX();
        int y = errorMatrix.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float error = errorMatrix.getValue(i, j);
                float out = outMatrix.getValue(i, j);
                float value = activeFunction.functionG(out) * error;
                errorMatrix.setValue(i, j, value);
            }
        }
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
