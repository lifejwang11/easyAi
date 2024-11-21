package org.dromara.entity;


import org.dromara.nerveCenter.ModelParameter;

import java.util.Map;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 食物模型
 */
public class Model {
    private ModelParameter modelR;
    private ModelParameter modelG;
    private ModelParameter modelB;
    private ModelParameter modelFood;
    private Map<Integer, Integer> mapping;

    public Map<Integer, Integer> getMapping() {
        return mapping;
    }

    public void setMapping(Map<Integer, Integer> mapping) {
        this.mapping = mapping;
    }

    public ModelParameter getModelR() {
        return modelR;
    }

    public void setModelR(ModelParameter modelR) {
        this.modelR = modelR;
    }

    public ModelParameter getModelG() {
        return modelG;
    }

    public void setModelG(ModelParameter modelG) {
        this.modelG = modelG;
    }

    public ModelParameter getModelB() {
        return modelB;
    }

    public void setModelB(ModelParameter modelB) {
        this.modelB = modelB;
    }

    public ModelParameter getModelFood() {
        return modelFood;
    }

    public void setModelFood(ModelParameter modelFood) {
        this.modelFood = modelFood;
    }
}
