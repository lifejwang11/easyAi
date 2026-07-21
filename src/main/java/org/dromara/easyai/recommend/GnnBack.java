package org.dromara.easyai.recommend;

import org.dromara.easyai.i.CustomEncoding;
import org.dromara.easyai.matrixTools.Matrix;

import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @time 2026/7/15 16:18
 */
public class GnnBack implements CustomEncoding {
    private GnnLayer gnnLayer;

    public void setGnnLayer(GnnLayer gnnLayer) {
        this.gnnLayer = gnnLayer;
    }

    @Override
    public void backError(Map<Integer, Float> wg, int id) throws Exception {

    }

    @Override
    public void backErrorList(List<Matrix> nextErrorMatrixList) throws Exception {
        gnnLayer.backError(nextErrorMatrixList);
    }
}
