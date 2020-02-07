package org.wlld.imageRecognition.modelEntity;

import org.wlld.imageRecognition.border.Box;

import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @description
 * @date 5:08 下午 2020/2/7
 */
public class KBorder {
    private int length;//向量长度(模型需要返回)
    private int speciesQuantity;//种类数量(模型需要返回)
    private List<List<Double>> lists;//均值K模型参数
    private Map<Integer, BoxList> positionMap;//均值模型

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getSpeciesQuantity() {
        return speciesQuantity;
    }

    public void setSpeciesQuantity(int speciesQuantity) {
        this.speciesQuantity = speciesQuantity;
    }

    public List<List<Double>> getLists() {
        return lists;
    }

    public void setLists(List<List<Double>> lists) {
        this.lists = lists;
    }

    public Map<Integer, BoxList> getPositionMap() {
        return positionMap;
    }

    public void setPositionMap(Map<Integer, BoxList> positionMap) {
        this.positionMap = positionMap;
    }
}
