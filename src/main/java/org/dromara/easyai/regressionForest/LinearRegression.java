package org.dromara.easyai.regressionForest;


import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description rgb回归 Y = r *wr + g * wg + b* wb
 */
public class LinearRegression {
    private float w1;
    private float w2;
    private float b;
    private Matrix XY;//坐标矩阵
    private Matrix NormSequence;//内积序列矩阵
    private int xIndex = 0;//记录插入数量
    private boolean isRegression = false;//是否进行了回归
    private float avg;//结果平均值
    private final MatrixOperation matrixOperation = new MatrixOperation();

    public LinearRegression(int size) {//初始化rgb矩阵
        XY = new Matrix(size, 3);
        NormSequence = new Matrix(size, 1);
        xIndex = 0;
        avg = 0;
    }

    public LinearRegression() {

    }

    public void insertXY(float[] xy, float sequence) throws Exception {//rgb插入矩阵
        if (xy.length == 2) {
            XY.setNub(xIndex, 0, xy[0]);
            XY.setNub(xIndex, 1, xy[1]);
            XY.setNub(xIndex, 2, 1.0f);
            NormSequence.setNub(xIndex, 0, sequence);
            xIndex++;
        } else {
            throw new Exception("rgb length is not equals three");
        }
    }

    public float getValue(float x, float y) {//获取值
        if (isRegression) {
            return w1 * x + w2 * y + b;
        }
        return avg;
    }

    public float getCos(Matrix vector) throws Exception {//获取该直线与指定向量之间的余弦
        Matrix matrix = new Matrix(1, 3);
        matrix.setNub(0, 0, w1);
        matrix.setNub(0, 1, w2);
        matrix.setNub(0, 2, b);
        return matrixOperation.getNormCos(matrix, vector);
    }

    public void regression() throws Exception {//开始进行回归
        if (xIndex > 0) {
            Matrix ws = matrixOperation.getLinearRegression(XY, NormSequence);
            if (ws.getX() == 1 && ws.getY() == 1) {//矩阵奇异
                isRegression = false;
                for (int i = 0; i < xIndex; i++) {
                    avg = avg + NormSequence.getNumber(xIndex, 0);
                }
                avg = avg / xIndex;
            } else {
                w1 = ws.getNumber(0, 0);
                w2 = ws.getNumber(1, 0);
                b = ws.getNumber(2, 0);
                isRegression = true;
            }
            // System.out.println("wr==" + wr + ",wg==" + wg + ",b==" + b);
        } else {
            throw new Exception("regression matrix size is zero");
        }
    }

    public float getW1() {
        return w1;
    }

    public void setW1(float w1) {
        this.w1 = w1;
    }

    public float getW2() {
        return w2;
    }

    public void setW2(float w2) {
        this.w2 = w2;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }
}
