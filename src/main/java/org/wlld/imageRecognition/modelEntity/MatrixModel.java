package org.wlld.imageRecognition.modelEntity;

import java.util.List;

/**
 * @author lidapeng
 * @description
 * @date 2:15 下午 2020/2/7
 */
public class MatrixModel {
    private int id;//唯一ID
    private List<Double> rowVector;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Double> getRowVector() {
        return rowVector;
    }

    public void setRowVector(List<Double> rowVector) {
        this.rowVector = rowVector;
    }
}
