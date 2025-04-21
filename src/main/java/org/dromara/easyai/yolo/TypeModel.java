package org.dromara.easyai.yolo;

import org.dromara.easyai.nerveCenter.ModelParameter;
import org.dromara.easyai.resnet.entity.ResnetModel;

public class TypeModel {
    private int typeID;//该类型的类别id
    private int mappingID;//该类型的映射id
    private int maxWidth;//该类别选择框最大宽度
    private int maxHeight;//该类别映射的最大高度
    private int minWidth;//该类别最小宽度
    private int minHeight;//该类别最小高度
    private int winWidth;//该类别所用检测窗口宽（如果需要的话）
    private int winHeight;//该类别所用检测窗口高（如果需要的话）
    private ModelParameter positionModel;//该类别位置模型

    public int getWinWidth() {
        return winWidth;
    }

    public void setWinWidth(int winWidth) {
        this.winWidth = winWidth;
    }

    public int getWinHeight() {
        return winHeight;
    }

    public void setWinHeight(int winHeight) {
        this.winHeight = winHeight;
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public int getMappingID() {
        return mappingID;
    }

    public void setMappingID(int mappingID) {
        this.mappingID = mappingID;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }

    public ModelParameter getPositionModel() {
        return positionModel;
    }

    public void setPositionModel(ModelParameter positionModel) {
        this.positionModel = positionModel;
    }
}
