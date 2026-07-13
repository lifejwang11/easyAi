package org.dromara.easyai.recommend;

import org.dromara.easyai.config.GnnConfig;
import org.dromara.easyai.conv.DymStudy;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    private final Matrix featureMatrix;//该层离散特征
    private final int nodeSize;

    public GnnLayer(GnnConfig gnnConfig, ActiveFunction activeFunction, ConnectionTable connectionTable) {//特征维度 类别数量
        dymStudy = new DymStudy(gnnConfig.getgMaxTh(), gnnConfig.isAuto(), gnnConfig.getLayGMaxTh());
        this.activeFunction = activeFunction;
        int gnnTypeNumber = gnnConfig.getGnnTypeNumber();
        featureLength = gnnConfig.getFeatureLength();
        this.connectionTable = connectionTable;
        nodeSize = gnnConfig.getNodeSize();
        if (nodeSize > 1) {
            featureMatrix = new Matrix(nodeSize, featureLength);
            featureMatrix.randomInit(featureLength);
        } else {
            throw new IllegalArgumentException("节点数量不能小于1");
        }
        for (int i = 0; i < gnnTypeNumber; i++) {
            GnnPower gnnPower = initGnnPower(featureLength);
            powerMap.put(i + 1, gnnPower);
        }
    }

    //开始训练
    public void study(OutBack outBack, List<NodeStudy> nodeStudies) throws Exception {
        for (NodeStudy nodeStudy : nodeStudies) {
            int t = nodeStudy.getRootId() - 1;
            Matrix rootMatrix = getAggMatrix(t);//root的聚合特征
            Matrix connectionMatrix = connectionTable.getConnectMatrix();
            for (int j = 0; j < nodeSize; j++) {
                if (connectionMatrix.getValue(t, j) > 0.5) {//聚合特征
                    Matrix otherMatrix = getAggMatrix(j);
                }
            }
        }
    }

    private Matrix getAggMatrix(int i) throws Exception {
        Matrix myFeature = featureMatrix.getRow(i);//节点本身特征
        int nodeType = connectionTable.getNodeType(i);
        Matrix connectionFeature = connectionTable.getConnectOut(i, featureMatrix, powerMap);
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
