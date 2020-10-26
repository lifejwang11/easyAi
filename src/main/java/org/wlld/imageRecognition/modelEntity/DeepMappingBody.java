package org.wlld.imageRecognition.modelEntity;

import org.wlld.MatrixTools.Matrix;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.imageRecognition.border.Knn;
import org.wlld.imageRecognition.segmentation.DimensionMappingStudy;


import java.util.List;
import java.util.Set;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description深度映射
 */
public class DeepMappingBody {
    private DimensionMappingStudy dimensionAll;
    private TempleConfig templeConfig;
    private List<KeyMapping> mappingList;

    public DeepMappingBody(TempleConfig templeConfig) throws Exception {
        this.templeConfig = templeConfig;
        dimensionAll = new DimensionMappingStudy(templeConfig, true);
        mappingList = dimensionAll.start();
    }

    public int getType(Matrix feature) throws Exception {
        int type = dimensionAll.getType(feature);
        for (KeyMapping keyMapping : mappingList) {
            Set<Integer> region = keyMapping.getKeys();
            if (region.contains(type)) {
                DimensionMappingStudy mapping = keyMapping.getDimensionMapping();
                type = mapping.getType(feature);
                break;
            }
        }
        return type;
    }
}
