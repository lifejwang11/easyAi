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

    public MappingBody(double[] mapping, double[] feature) {
        this.feature = feature;
        double sigma = 0;
        for (int i = 0; i < mapping.length; i++) {
            sigma = sigma + Math.pow(mapping[i], 2);
        }
        sigma = Math.sqrt(sigma);//映射维度的莫
        double s = 0;
        for (int j = 0; j < feature.length; j++) {
            s = s + feature[j] * mapping[j];
        }
        mappingNub = s / sigma;

    }

    public double[] getFeature() {
        return feature;
    }

    public double getMappingNub() {
        return mappingNub;
    }
}
