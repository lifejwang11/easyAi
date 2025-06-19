package org.dromara.easyai.matrixTools;

/**
 * @author lidapeng
 * @time 2025/6/19 10:36
 * @des 升腾接口
 */
public interface AscendMatrix {
    void init() throws Exception;

    Matrix mulMatrix(Matrix matrix1, Matrix matrix2) throws Exception;
}
