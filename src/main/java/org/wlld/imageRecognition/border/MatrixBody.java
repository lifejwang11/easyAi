package org.wlld.imageRecognition.border;

import org.wlld.MatrixTools.Matrix;

/**
 * @author lidapeng
 * @description
 * @date 3:33 下午 2020/2/4
 */
public class MatrixBody {//给矩阵绑定一个id
    private int id;//唯一ID
    private Matrix matrix;//矩阵

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }
}
