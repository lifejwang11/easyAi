package org.dromara.easyai.resnet.entity;

import org.dromara.easyai.nerveCenter.ModelParameter;

import java.util.List;

/**
 * @author lidapeng
 * @time 2025/4/18 09:23
 */
public class ResnetModel {
    private List<ResBlockModel> resBlockModelList;
    private ModelParameter parameter;

    public List<ResBlockModel> getResBlockModelList() {
        return resBlockModelList;
    }

    public void setResBlockModelList(List<ResBlockModel> resBlockModelList) {
        this.resBlockModelList = resBlockModelList;
    }

    public ModelParameter getParameter() {
        return parameter;
    }

    public void setParameter(ModelParameter parameter) {
        this.parameter = parameter;
    }
}
