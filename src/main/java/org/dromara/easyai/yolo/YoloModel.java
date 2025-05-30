package org.dromara.easyai.yolo;

import org.dromara.easyai.nerveCenter.ModelParameter;
import org.dromara.easyai.resnet.entity.ResnetModel;

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
