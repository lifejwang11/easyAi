package org.wlld.imageRecognition.modelEntity;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 映射体
 */
public class MappingBody {
    private double[] feature;//特征
    private double mappingNub;//映射好的值

    public MappingBody(double[] feature) {
        this.feature = feature;
        double sigma = 0;
        for (int i = 0; i < feature.length; i++) {
            sigma = sigma + Math.pow(feature[i], 2);
        }
        mappingNub = Math.sqrt(sigma);
    }

    public double[] getFeature() {
        return feature;
    }

    public double getMappingNub() {
        return mappingNub;
    }
}
