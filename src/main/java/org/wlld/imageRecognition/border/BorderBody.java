package org.wlld.imageRecognition.border;

import org.wlld.MatrixTools.Matrix;

/**
 * @author lidapeng
 * @description
 *
 * @date 8:49 上午 2020/1/24
 */
public class BorderBody {
    private Matrix xW;//平移X权重
    private Matrix yW;//平移Y权重
    private Matrix wW;//缩放宽度权重
    private Matrix hW;//缩放高度权重
    private Matrix x;//参数矩阵
    private Matrix tx;//X轴偏移量
    private Matrix ty;//Y轴偏移量
    private Matrix tw;//W缩放量
    private Matrix th;//H缩放量

    public Matrix getxW() {
        return xW;
    }

    public void setxW(Matrix xW) {
        this.xW = xW;
    }

    public Matrix getyW() {
        return yW;
    }

    public void setyW(Matrix yW) {
        this.yW = yW;
    }

    public Matrix getwW() {
        return wW;
    }

    public void setwW(Matrix wW) {
        this.wW = wW;
    }

    public Matrix gethW() {
        return hW;
    }

    public void sethW(Matrix hW) {
        this.hW = hW;
    }

    public Matrix getX() {
        return x;
    }

    public void setX(Matrix x) {
        this.x = x;
    }

    public Matrix getTx() {
        return tx;
    }

    public void setTx(Matrix tx) {
        this.tx = tx;
    }

    public Matrix getTy() {
        return ty;
    }

    public void setTy(Matrix ty) {
        this.ty = ty;
    }

    public Matrix getTw() {
        return tw;
    }

    public void setTw(Matrix tw) {
        this.tw = tw;
    }

    public Matrix getTh() {
        return th;
    }

    public void setTh(Matrix th) {
        this.th = th;
    }
}
