package org.dromara.rnnJumpNerveCenter;


import org.dromara.entity.TypeMapping;

import java.util.List;

public class RandomModel {
    private ModelParameter typeModelParameter;
    private List<TypeMapping> typeMappings;//映射

    public ModelParameter getTypeModelParameter() {
        return typeModelParameter;
    }

    public void setTypeModelParameter(ModelParameter typeModelParameter) {
        this.typeModelParameter = typeModelParameter;
    }

    public List<TypeMapping> getTypeMappings() {
        return typeMappings;
    }

    public void setTypeMappings(List<TypeMapping> typeMappings) {
        this.typeMappings = typeMappings;
    }
}
