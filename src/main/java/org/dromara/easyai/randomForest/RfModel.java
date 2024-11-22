package org.dromara.easyai.randomForest;

import java.util.Map;

/**
 * @author lidapeng
 * @description
 * @date 5:20 下午 2020/2/22
 */
public class RfModel {
    Map<Integer, Node> nodeMap;

    public Map<Integer, Node> getNodeMap() {
        return nodeMap;
    }

    public void setNodeMap(Map<Integer, Node> nodeMap) {
        this.nodeMap = nodeMap;
    }
}
