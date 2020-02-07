package org.wlld.imageRecognition.modelEntity;

import java.util.List;

/**
 * @author lidapeng
 * @description
 * @date 5:04 下午 2020/2/7
 */
public class BoxList {
    private List<Double> list;
    private List<Double> positionList;

    public List<Double> getList() {
        return list;
    }

    public void setList(List<Double> list) {
        this.list = list;
    }

    public List<Double> getPositionList() {
        return positionList;
    }

    public void setPositionList(List<Double> positionList) {
        this.positionList = positionList;
    }
}
