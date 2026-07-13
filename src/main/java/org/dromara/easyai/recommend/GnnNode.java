package org.dromara.easyai.recommend;

import org.dromara.easyai.matrixTools.Matrix;

import java.util.List;

/**
 * @author lidapeng
 * @time 2026/7/11 08:53
 * @des 节点
 */
public class GnnNode {
    private int id;//该节点的离散id
    private int typeID;//属性类别id
    private boolean discreteFeature = true;//是否为离散特征
    private List<GnnNode> nodeList;//子节点集合

    public List<GnnNode> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<GnnNode> nodeList) {
        this.nodeList = nodeList;
    }

    public boolean isDiscreteFeature() {
        return discreteFeature;
    }

    public void setDiscreteFeature(boolean discreteFeature) {
        this.discreteFeature = discreteFeature;
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
