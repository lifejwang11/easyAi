package org.dromara.easyai.recommend;

import org.dromara.easyai.matrixTools.Matrix;

import java.util.Map;

/**
 * @author lidapeng
 * @time 2026/7/13 16:29
 */
public class NodeStudy {
    private int rootId;//起始id
    private Map<Integer, Float> e;//期望
    private Map<Integer, Matrix> gnnBodyMap;//特征图

    public Map<Integer, Matrix> getGnnBodyMap() {
        return gnnBodyMap;
    }

    public void setGnnBodyMap(Map<Integer, Matrix> gnnBodyMap) {
        this.gnnBodyMap = gnnBodyMap;
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
