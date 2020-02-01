package org.wlld.imageRecognition;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.tools.ArithUtil;

/**
 * @author lidapeng
 * @description K均值聚类矩阵
 * @date 9:05 上午 2020/2/1
 */
public class KMatrix {
    private Matrix sigmaMatrix;//矩阵和
    private int nub;//加和次数
    private boolean isFinish = false;//是否结算

    KMatrix(int x, int y) {
        sigmaMatrix = new Matrix(x, y);
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }

    public Matrix getSigmaMatrix() {
        return sigmaMatrix;
    }

    public double getEDist(Matrix matrix) throws Exception {//返回欧式距离
        if (isFinish && matrix.getX() == sigmaMatrix.getX()
                && matrix.getY() == sigmaMatrix.getY()) {
            double sigma = 0;
            for (int i = 0; i < matrix.getX(); i++) {
                for (int j = 0; j < matrix.getY(); j++) {
                    double sub = ArithUtil.sub(matrix.getNumber(i, j), sigmaMatrix.getNumber(i, j));
                    sigma = ArithUtil.add(Math.pow(sub, 2), sigma);
                }
            }
            sigma = Math.sqrt(sigma);
            return sigma;
        } else {
            throw new Exception("K is not finish or matrix not equal");
        }
    }

    public void addMatrix(Matrix matrix) throws Exception {
        sigmaMatrix = MatrixOperation.add(sigmaMatrix, matrix);
        nub++;
    }

    public void getK() throws Exception {//结算K均值
        if (nub != 0) {
            double aNub = ArithUtil.div(1, nub);
            MatrixOperation.mathMul(sigmaMatrix, aNub);
            isFinish = true;
        } else {
            throw new Exception("not value");
        }
    }
}
