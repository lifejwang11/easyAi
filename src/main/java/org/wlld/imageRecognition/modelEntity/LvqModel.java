package org.wlld.imageRecognition.modelEntity;

import java.util.List;

/**
 * @author lidapeng
 * @description
 * @date 2:11 下午 2020/2/7
 */
public class LvqModel {
    private int typeNub;//原型聚类个数
    private int length;//向量长度
    private List<MatrixModel> matrixModelList;//原型向量

    public int getTypeNub() {
        return typeNub;
    }

    public void setTypeNub(int typeNub) {
        this.typeNub = typeNub;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public List<MatrixModel> getMatrixModelList() {
        return matrixModelList;
    }

    public void setMatrixModelList(List<MatrixModel> matrixModelList) {
        this.matrixModelList = matrixModelList;
    }
}
