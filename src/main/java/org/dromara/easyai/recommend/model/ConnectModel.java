package org.dromara.easyai.recommend.model;

import java.util.List;

/**
 * @author lidapeng
 * @time 2026/7/20 09:07
 */
public class ConnectModel {
    private int id;
    private List<Integer> sonList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getSonList() {
        return sonList;
    }

    public void setSonList(List<Integer> sonList) {
        this.sonList = sonList;
    }
}
