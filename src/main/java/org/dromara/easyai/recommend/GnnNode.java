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
    private List<GnnNode> nodeList;//子节点集合
    private List<Matrix> featureList;//每一层的特征集合
    private Matrix error;//该节点本层误差
    private Matrix deepError;//该节点来自深层的误差
    private int jumpTimes;

    public Matrix getDeepError() {
        return deepError;
    }

    public void setDeepError(Matrix deepError) {
        this.deepError = deepError;
    }

    public Matrix getError() {
        return error;
    }

    public void setError(Matrix error) {
        this.error = error;
    }

    public List<Matrix> getFeatureList() {
        return featureList;
    }

    public void setFeatureList(List<Matrix> featureList) {
        this.featureList = featureList;
    }

    public int getJumpTimes() {
        return jumpTimes;
    }

    public void setJumpTimes(int jumpTimes) {
        this.jumpTimes = jumpTimes;
    }

    public List<GnnNode> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<GnnNode> nodeList) {
        this.nodeList = nodeList;
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
