package org.dromara.easyai.batchNerve;

import java.util.List;

/**
 * @author lidapeng
 * @time 2026/1/7 15:52
 */
public class BatchNerveModel {
    private List<QBlockModel> blockModelList;

    public List<QBlockModel> getBlockModelList() {
        return blockModelList;
    }

    public void setBlockModelList(List<QBlockModel> blockModelList) {
        this.blockModelList = blockModelList;
    }
}
