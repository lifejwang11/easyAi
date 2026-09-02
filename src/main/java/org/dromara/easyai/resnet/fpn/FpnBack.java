package org.dromara.easyai.resnet.fpn;

import org.dromara.easyai.i.CustomEncoding;
import org.dromara.easyai.matrixTools.Matrix;

import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @time 2026/8/28 09:08
 */
public class FpnBack implements CustomEncoding {
    private FpnBlock fpnBlock;
    private final int type;

    public FpnBack(int type) {
        this.type = type;
    }

    public void setFpnBlock(FpnBlock fpnBlock) {
        this.fpnBlock = fpnBlock;
    }

    @Override
    public void backError(Map<Integer, Float> wg, int id) throws Exception {

    }

    @Override
    public void backErrorList(List<Matrix> nextErrorMatrixList) throws Exception {
        if (type == 1) {
            fpnBlock.backByTypeLine(nextErrorMatrixList);
        } else {
            fpnBlock.backByPositionLine(nextErrorMatrixList);
        }
    }
}
