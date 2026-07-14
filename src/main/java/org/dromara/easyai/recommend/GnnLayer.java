package org.dromara.easyai.recommend;

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
    private int featureLength;//特征维度
    private final int jumpTimes;//跳跃数

    public GnnLayer(GnnConfig gnnConfig, ActiveFunction activeFunction, ConnectionTable connectionTable) {//特征维度 类别数量
        dymStudy = new DymStudy(gnnConfig.getgMaxTh(), gnnConfig.isAuto(), gnnConfig.getLayGMaxTh());
        this.activeFunction = activeFunction;
        this.jumpTimes = gnnConfig.getJumpTimes();
        int gnnTypeNumber = gnnConfig.getGnnTypeNumber();
        featureLength = gnnConfig.getFeatureLength();
        this.connectionTable = connectionTable;
        if (gnnTypeNumber > 0) {
            for (int i = 0; i < gnnTypeNumber; i++) {
                GnnPower gnnPower = initGnnPower(featureLength);
                powerMap.put(i + 1, gnnPower);
            }
        } else {
            throw new IllegalArgumentException("节点类别数必须大于0");
        }
    }

    //开始训练
    public void study(OutBack outBack, List<NodeStudy> nodeStudies) throws Exception {
        Map<Integer, Matrix> featureMatrixMap = connectionTable.getFeatureMatrixMap();
        Map<Integer, List<Integer>> connectMap = connectionTable.getConnectMap();
        for (NodeStudy nodeStudy : nodeStudies) {
            int t = nodeStudy.getRootId() - 1;
            Matrix rootMatrix = getAggMatrix(t, featureMatrixMap);//root的聚合特征
            Map<Integer, Matrix> gnnBodyMap = new HashMap<>();//特征图
            nodeStudy.setGnnBodyMap(gnnBodyMap);
            getAgg(connectMap, t, featureMatrixMap, rootMatrix, gnnBodyMap);
        }
        if (sonLayer != null) {//还有下一层
            sonLayer.nextStudy(outBack, nodeStudies);
        } else {//进入线性层

        }
    }

    public void nextStudy(OutBack outBack, List<NodeStudy> nodeStudies) throws Exception {
        Map<Integer, List<Integer>> connectMap = connectionTable.getConnectMap();
        for (NodeStudy nodeStudy : nodeStudies) {
            int t = nodeStudy.getRootId() - 1;
            Map<Integer, Matrix> featureMatrixMap = nodeStudy.getGnnBodyMap();
            Matrix rootMatrix = getAggMatrix(t, featureMatrixMap);//root的聚合特征
            Map<Integer, Matrix> gnnBodyMap = new HashMap<>();//特征图
            getAgg(connectMap, t, featureMatrixMap, rootMatrix, gnnBodyMap);
            nodeStudy.setGnnBodyMap(gnnBodyMap);
        }
        if (sonLayer != null) {//还有下一层
            sonLayer.nextStudy(outBack, nodeStudies);
        } else {//进入线性层

        }

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
            gnnBodyMap.putAll(gnus);
        }
    }


    private Map<Integer, Matrix> jump(Map<Integer, Matrix> gnnBodyMap, Map<Integer, Matrix> featureMatrixMap
            , Map<Integer, List<Integer>> connectMap) throws Exception {//跳跃
        Map<Integer, Matrix> map = new HashMap<>();
        for (Map.Entry<Integer, Matrix> entry : gnnBodyMap.entrySet()) {
            int t = entry.getKey();
            List<Integer> connectList = connectMap.get(t);
            for (int j : connectList) {//遍历所有邻居
                if (!gnnBodyMap.containsKey(j) && !map.containsKey(j)) {//聚合特征
                    Matrix otherMatrix = getAggMatrix(j, featureMatrixMap);
                    map.put(j, otherMatrix);
                }
            }
        }
        return map;
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

    private GnnPower initGnnPower(int featureLength) {
        GnnPower gnnPower = new GnnPower();
        Matrix selfPower = new Matrix(featureLength, featureLength);
        Matrix otherPower = new Matrix(featureLength, featureLength);
        Matrix bais = new Matrix(1, featureLength);
        selfPower.randomInit(featureLength);
        otherPower.randomInit(featureLength);
        bais.randomInit(featureLength);
        gnnPower.setArf(0.5f);
        gnnPower.setSelfPower(selfPower);
        gnnPower.setOtherPower(otherPower);
        gnnPower.setBais(bais);
        return gnnPower;
    }


    public void connectSonLayer(GnnLayer sonLayer) {
        this.sonLayer = sonLayer;
    }

    public void connectFatherLayer(GnnLayer fatherLayer) {
        this.fatherLayer = fatherLayer;
    }
}
