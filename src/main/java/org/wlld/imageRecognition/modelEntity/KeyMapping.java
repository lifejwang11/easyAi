package org.wlld.imageRecognition.modelEntity;

import org.wlld.imageRecognition.segmentation.DimensionMappingStudy;

import java.util.Set;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class KeyMapping {
    private Set<Integer> keys;
    private DimensionMappingStudy dimensionMapping;

    public Set<Integer> getKeys() {
        return keys;
    }

    public void setKeys(Set<Integer> keys) {
        this.keys = keys;
    }

    public DimensionMappingStudy getDimensionMapping() {
        return dimensionMapping;
    }

    public void setDimensionMapping(DimensionMappingStudy dimensionMapping) {
        this.dimensionMapping = dimensionMapping;
    }
}
