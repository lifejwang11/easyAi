package org.dromara.easyai.conv.dcn;

import org.dromara.easyai.matrixTools.Matrix;

import java.util.List;

/**
 * @author lidapeng
 * @time 2026/6/4 15:20
 */
public class DcnStudyOut {
    private Matrix feature;
    private List<DcnBody> dcnBodyList;

    public Matrix getFeature() {
        return feature;
    }

    public void setFeature(Matrix feature) {
        this.feature = feature;
    }

    public List<DcnBody> getDcnBodyList() {
        return dcnBodyList;
    }

    public void setDcnBodyList(List<DcnBody> dcnBodyList) {
        this.dcnBodyList = dcnBodyList;
    }
}
