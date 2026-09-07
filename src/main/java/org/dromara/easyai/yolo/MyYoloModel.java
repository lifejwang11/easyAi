package org.dromara.easyai.yolo;

import org.dromara.easyai.resnet.entity.ResnetModel;

import java.util.List;

/**
 * @author lidapeng
 * @time 2026/9/2 17:59
 */
public class MyYoloModel {
    private List<MappingIDBody> mappingIDBodyList;
    private ResnetModel resnetModel;

    public List<MappingIDBody> getMappingIDBodyList() {
        return mappingIDBodyList;
    }

    public void setMappingIDBodyList(List<MappingIDBody> mappingIDBodyList) {
        this.mappingIDBodyList = mappingIDBodyList;
    }

    public ResnetModel getResnetModel() {
        return resnetModel;
    }

    public void setResnetModel(ResnetModel resnetModel) {
        this.resnetModel = resnetModel;
    }
}
