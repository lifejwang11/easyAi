package org.dromara.easyai.rnnNerveEntity;

import java.util.List;

public class RnnOutNerveBody {
    private int depth;//深度
    private List<Nerve> outNerves;//输出神经元集合

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public List<Nerve> getOutNerves() {
        return outNerves;
    }

    public void setOutNerves(List<Nerve> outNerves) {
        this.outNerves = outNerves;
    }
}
