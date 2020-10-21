package org.wlld.imageRecognition.segmentation;

import org.wlld.i.PsoFunction;
import org.wlld.tools.Frequency;

import java.util.List;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 维度映射
 */
public class DimensionMapping extends Frequency implements PsoFunction {
    private List<double[]> features;
    private int size;

    public DimensionMapping(List<double[]> features) {
        this.features = features;
        size = features.size();
    }

    @Override
    public double getResult(double[] parameter, int id) throws Exception {
        double sigma = 0;
        for (int i = 0; i < parameter.length; i++) {
            sigma = sigma + Math.pow(parameter[i], 2);
        }
        sigma = Math.sqrt(sigma);
        double[] mapping = new double[size];//在新维度的映射集合
        for (int i = 0; i < size; i++) {
            double[] feature = features.get(i);
            double s = 0;
            for (int j = 0; j < feature.length; j++) {
                s = s + feature[j] * parameter[j];
            }
            mapping[i] = s / sigma;
        }
        return variance(mapping);
    }
}
