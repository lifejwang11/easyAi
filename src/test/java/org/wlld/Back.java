package org.wlld;

import org.wlld.MatrixTools.Matrix;
import org.wlld.i.OutBack;

/**
 * @author lidapeng
 * @description
 * @date 9:47 上午 2020/2/15
 */
public class Back implements OutBack {
    @Override
    public void getBack(double v, int i, long l) {
        System.out.println("id==" + i + ",point==" + v + ",l==" + l);
    }

    @Override
    public void getBackMatrix(Matrix matrix, long l) {

    }
}
