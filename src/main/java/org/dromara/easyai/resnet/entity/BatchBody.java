package org.dromara.easyai.resnet.entity;

import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.matrixTools.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @time 2026/1/5 13:15
 */
public class BatchBody {
    private List<Matrix> featureList = new ArrayList<>();
    private Map<Integer, Float> E;
    private ResnetError resnetError;//残差

    public ResnetError getResnetError() {
        return resnetError;
    }

    public void setResnetError(ResnetError resnetError) {
        this.resnetError = resnetError;
    }

    public List<Matrix> getFeatureList() {
        return featureList;
    }

    public void setFeatureList(List<Matrix> featureList) {
        this.featureList = featureList;
    }

    public void insertPicture(ThreeChannelMatrix picture) {
        featureList.clear();
        featureList.add(picture.getMatrixR());
        featureList.add(picture.getMatrixG());
        featureList.add(picture.getMatrixB());
    }

    public Map<Integer, Float> getE() {
        return E;
    }

    public void setE(Map<Integer, Float> e) {
        E = e;
    }
}
