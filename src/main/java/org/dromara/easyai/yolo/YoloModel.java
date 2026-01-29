package org.dromara.easyai.yolo;

import org.dromara.easyai.nerveCenter.ModelParameter;
import org.dromara.easyai.resnet.entity.ResnetModel;

import java.util.List;

public class YoloModel {
    private List<TypeModel> typeModels;
    private ModelParameter typeModel;
    private ResnetModel resnetModel;
    private int ready;

    public int getReady() {
        return ready;
    }

    public void setReady(int ready) {
        this.ready = ready;
    }

    public ResnetModel getResnetModel() {
        return resnetModel;
    }

    public void setResnetModel(ResnetModel resnetModel) {
        this.resnetModel = resnetModel;
    }

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
