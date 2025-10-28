package org.dromara.easyai.entity;

import java.util.List;

/**
 * @author lidapeng
 * @time 2025/10/27 16:38
 */
public class GMModel {
    private List<ClusterModel> gmmBodyList;

    public List<ClusterModel> getGmmBodyList() {
        return gmmBodyList;
    }

    public void setGmmBodyList(List<ClusterModel> gmmBodyList) {
        this.gmmBodyList = gmmBodyList;
    }
}
