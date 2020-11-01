package org.wlld.imageRecognition.modelEntity;

import org.wlld.MatrixTools.Matrix;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.imageRecognition.segmentation.DimensionMappingStudy;


/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description深度映射
 */
public class DeepMappingBody {
    private DimensionMappingStudy dimensionAll;

    public DeepMappingBody(TempleConfig templeConfig) throws Exception {
        dimensionAll = new DimensionMappingStudy(templeConfig, true);
        dimensionAll.start();
    }

    public int getType(Matrix feature) throws Exception {
        int type = dimensionAll.getType(feature);
        return type;
    }
}
