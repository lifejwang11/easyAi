package org.wlld.yolo;

import org.wlld.nerveCenter.ModelParameter;

import java.util.List;

public class YoloModel {
    private List<TypeModel> typeModels;
    private ModelParameter typeModel;

    public List<TypeModel> getTypeModels() {
        return typeModels;
    }

    public void setTypeModels(List<TypeModel> typeModels) {
        this.typeModels = typeModels;
    }

    public ModelParameter getTypeModel() {
        return typeModel;
    }

    public void setTypeModel(ModelParameter typeModel) {
        this.typeModel = typeModel;
    }

}
