package org.wlld.rnnNerveCenter;

import org.wlld.entity.TypeMapping;

import java.util.List;

public class RandomModel {
    private List<RandomModelParameter> randomModelParameters;
    private List<TypeMapping> typeMappings;

    public List<TypeMapping> getTypeMappings() {
        return typeMappings;
    }

    public void setTypeMappings(List<TypeMapping> typeMappings) {
        this.typeMappings = typeMappings;
    }

    public List<RandomModelParameter> getRandomModelParameters() {
        return randomModelParameters;
    }

    public void setRandomModelParameters(List<RandomModelParameter> randomModelParameters) {
        this.randomModelParameters = randomModelParameters;
    }
}
