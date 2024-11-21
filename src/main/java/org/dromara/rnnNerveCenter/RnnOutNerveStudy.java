package org.dromara.rnnNerveCenter;

import org.dromara.rnnNerveEntity.NerveStudy;

import java.util.List;

public class RnnOutNerveStudy {
    private int depth;//深度
    private List<NerveStudy> nerveStudies;

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public List<NerveStudy> getNerveStudies() {
        return nerveStudies;
    }

    public void setNerveStudies(List<NerveStudy> nerveStudies) {
        this.nerveStudies = nerveStudies;
    }
}
