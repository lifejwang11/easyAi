package org.wlld.nerveEntity;

import java.util.List;

public class BodyList {
    private int type;
    private List<List<Double>> lists;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<List<Double>> getLists() {
        return lists;
    }

    public void setLists(List<List<Double>> lists) {
        this.lists = lists;
    }
}
