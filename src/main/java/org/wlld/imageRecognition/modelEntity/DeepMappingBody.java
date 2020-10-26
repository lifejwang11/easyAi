package org.wlld.imageRecognition.modelEntity;

import org.wlld.imageRecognition.TempleConfig;
import org.wlld.imageRecognition.border.Knn;
import org.wlld.imageRecognition.segmentation.DimensionMappingStudy;

import java.util.HashMap;
import java.util.Map;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description深度映射
 */
public class DeepMappingBody {
    private DimensionMappingStudy dimensionAll;
    private TempleConfig templeConfig;
    private Map<Integer, DimensionMappingStudy> mappingMap = new HashMap<>();

    public DeepMappingBody(TempleConfig templeConfig) throws Exception {
        this.templeConfig = templeConfig;
        dimensionAll = new DimensionMappingStudy(templeConfig);
        dimensionAll.start();
    }
}
