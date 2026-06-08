package org.dromara.easyai.conv.dcn;

import org.dromara.easyai.i.CustomEncoding;
import org.dromara.easyai.matrixTools.Matrix;

import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @time 2026/6/3 10:37
 */
public class DCNErrorBack implements CustomEncoding {
    private List<Matrix> nextErrorMatrixList;

    public List<Matrix> getNextErrorMatrixList() {
        return nextErrorMatrixList;
    }

    @Override
    public void backError(Map<Integer, Float> wg, int id) throws Exception {

    }

    @Override
    public void backErrorList(List<Matrix> nextErrorMatrixList) {
        this.nextErrorMatrixList = nextErrorMatrixList;
    }
}
