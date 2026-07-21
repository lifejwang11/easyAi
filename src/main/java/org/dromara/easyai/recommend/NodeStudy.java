package org.dromara.easyai.recommend;

import org.dromara.easyai.matrixTools.Matrix;

import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @time 2026/7/13 16:29
 */
public class NodeStudy {
    private Map<Integer, Float> e;//期望
    private GnnNode rootGnnNode;//根节点
    private List<GnnNode> gnnFeatures;//临时图与特征

    public GnnNode getRootGnnNode() {
        return rootGnnNode;
    }

    public void setRootGnnNode(GnnNode rootGnnNode) {
        this.rootGnnNode = rootGnnNode;
    }

    public List<GnnNode> getGnnFeatures() {
        return gnnFeatures;
    }

    public void setGnnFeatures(List<GnnNode> gnnFeatures) {
        this.gnnFeatures = gnnFeatures;
    }


    public Map<Integer, Float> getE() {
        return e;
    }

    public void setE(Map<Integer, Float> e) {
        this.e = e;
    }
}
