package org.wlld.entity;


import org.wlld.rnnJumpNerveCenter.ModelParameter;

public class CreatorModel {
    private ModelParameter semanticsModel;
    private ModelParameter customModel;

    public ModelParameter getSemanticsModel() {
        return semanticsModel;
    }

    public void setSemanticsModel(ModelParameter semanticsModel) {
        this.semanticsModel = semanticsModel;
    }

    public ModelParameter getCustomModel() {
        return customModel;
    }

    public void setCustomModel(ModelParameter customModel) {
        this.customModel = customModel;
    }
}
