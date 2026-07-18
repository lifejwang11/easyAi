package org.dromara.easyai.recommend;

import org.dromara.easyai.matrixTools.Matrix;

import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @time 2026/7/13 16:29
 */
public class NodeStudy {
    private int rootId;//起始id
    private Map<Integer, Float> e;//期望
    private List<GnnNode> gnnFeatures;//临时图与特征

    public List<GnnNode> getGnnFeatures() {
        return gnnFeatures;
    }

    public void setGnnFeatures(List<GnnNode> gnnFeatures) {
        this.gnnFeatures = gnnFeatures;
    }

    public int getRootId() {
        return rootId;
    }

    public void setRootId(int rootId) {
        this.rootId = rootId;
    }

    public Map<Integer, Float> getE() {
        return e;
    }

    public void setE(Map<Integer, Float> e) {
        this.e = e;
    }
}
