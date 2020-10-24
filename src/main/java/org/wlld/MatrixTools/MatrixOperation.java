package org.wlld.MatrixTools;

import org.wlld.tools.ArithUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MatrixOperation {

    private MatrixOperation() {
    }

    public static Matrix add(Matrix matrix1, Matrix matrix2) throws Exception {//矩阵相加
        if (matrix1.getX() == matrix2.getX() && matrix1.getY() == matrix2.getY()) {
            Matrix matrix = new Matrix(matrix1.getX(), matrix1.getY());
            int x = matrix1.getX();
            int y = matrix1.getY();
            for (int i = 0; i < x; i++) {//遍历行
                for (int j = 0; j < y; j++) {//遍历列
                    matrix.setNub(i, j, ArithUtil.add(matrix1.getNumber(i, j), matrix2.getNumber(i, j)));
                }
            }
            return matrix;
        } else {
            throw new Exception("matrix is not equals");
        }
    }

    //矩阵相减
    public static Matrix sub(Matrix matrix1, Matrix matrix2) throws Exception {//
        if (matrix1.getX() == matrix2.getX() && matrix1.getY() == matrix2.getY()) {
            Matrix matrix = new Matrix(matrix1.getX(), matrix1.getY());
            int x = matrix1.getX();
            int y = matrix1.getY();
            for (int i = 0; i < x; i++) {//遍历行
                for (int j = 0; j < y; j++) {//遍历列
                    matrix.setNub(i, j, ArithUtil.sub(matrix1.getNumber(i, j), matrix2.getNumber(i, j)));
                }
            }
            return matrix;
        } else {
            throw new Exception("matrix is not equals");
        }
    }

    //多元线性回归
    public static Matrix getLinearRegression(Matrix parameter, Matrix out) throws Exception {
        if (parameter.getX() == out.getX() && out.isVector()) {
            //将参数矩阵转置
            Matrix matrix1 = transPosition(parameter);
            //转置的参数矩阵乘以参数矩阵
            Matrix matrix2 = mulMatrix(matrix1, parameter);
            //求上一步的逆矩阵 这一步需要矩阵非奇异,若出现奇异矩阵，则返回0矩阵，意味失败
            Matrix matrix3 = getInverseMatrixs(matrix2);
            if (matrix3.getX() == 1 && matrix3.getY() == 1) {
                return matrix3;
            } else {
                //逆矩阵乘以转置矩阵
                Matrix matrix4 = mulMatrix(matrix3, matrix1);
                //最后乘以输出矩阵,生成权重矩阵并返回
                return mulMatrix(matrix4, out);
            }
        } else {
            throw new Exception("invalid regression matrix");
        }
    }

    public static double getEDistByMatrix(Matrix matrix1, Matrix matrix2) throws Exception {
        if (matrix1.getX() == matrix2.getX() && matrix1.getY() == matrix2.getY()) {
            int x = matrix1.getX();
            int y = matrix1.getY();
            double sigma = 0;
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    double sub = matrix1.getNumber(i, j) - matrix2.getNumber(i, j);
                    sigma = sigma + Math.pow(sub, 2);
                }
            }
            return sigma / (x * y);
        } else {
            throw new Exception("two matrixes is not equals");
        }
    }

    //返回两个向量之间的欧氏距离的平方
    public static double getEDist(Matrix matrix1, Matrix matrix2) throws Exception {
        if (matrix1.isRowVector() && matrix2.isRowVector() && matrix1.getY() == matrix2.getY()) {
            Matrix matrix = sub(matrix1, matrix2);
            return getNorm(matrix);
        } else {
            throw new Exception("this matrix is not  rowVector or length different");
        }
    }

    public static double errorNub(Matrix matrix, Matrix avgMatrix) throws Exception {//求均方误差
        int y = matrix.getY();
        if (matrix.isRowVector() && avgMatrix.isRowVector() && y == avgMatrix.getY()) {
            double[] subAll = new double[y];
            for (int j = 0; j < y; j++) {
                double mySelf = matrix.getNumber(0, j);
                double avg = avgMatrix.getNumber(0, j);
                double sub = Math.pow(avg - mySelf, 2);
                subAll[j] = sub;
            }
            double sigma = 0;
            for (int i = 0; i < y; i++) {
                sigma = sigma + subAll[i];
            }
            return sigma / y;
        } else {
            throw new Exception("this matrix is not  rowVector or length different");
        }
    }

    public static Matrix pushVector(Matrix myMatrix, Matrix matrix, boolean addRow) throws Exception {
        //向一个矩阵里合并一个行向量或者列向量到矩阵行或者列的末尾
        if (matrix.getX() == 1 || matrix.getY() == 1) {
            Matrix addMatrix;
            if (addRow) {//增加一行
                if (matrix.getY() != myMatrix.getY()) {
                    throw new Exception("this matrix column is not equals");
                }
                addMatrix = new Matrix(myMatrix.getX() + 1, myMatrix.getY());
            } else {//增加一列
                if (matrix.getX() != myMatrix.getX()) {
                    throw new Exception("this matrix row is not equals");
                }
                addMatrix = new Matrix(myMatrix.getX(), myMatrix.getY() + 1);
            }
            for (int i = 0; i < addMatrix.getX(); i++) {
                for (int j = 0; j < addMatrix.getY(); j++) {
                    if (addRow) {
                        if (i == addMatrix.getX() - 1) {//最后一行
                            addMatrix.setNub(i, j, matrix.getNumber(0, j));
                        } else {
                            addMatrix.setNub(i, j, myMatrix.getNumber(i, j));
                        }
                    } else {
                        if (j == addMatrix.getY() - 1) {//最后一列
                            addMatrix.setNub(i, j, matrix.getNumber(i, 0));
                        } else {
                            addMatrix.setNub(i, j, myMatrix.getNumber(i, j));
                        }
                    }
                }
            }
            return addMatrix;
        } else {
            throw new Exception("this matrix is not a vector");
        }
    }

    public static Matrix push(Matrix matrix, double nub, boolean isRow) throws Exception {//向一个向量里PUSH一个值
        if (matrix.getX() == 1 || matrix.getY() == 1) {
            Matrix myMatrix;
            int nubs;
            if (isRow) {//行向量
                nubs = matrix.getY() + 1;
                myMatrix = new Matrix(1, nubs);
            } else {//列向量
                nubs = matrix.getX() + 1;
                myMatrix = new Matrix(nubs, 1);
            }
            for (int i = 0; i < nubs; i++) {
                if (i == nubs - 1) {
                    if (isRow) {
                        myMatrix.setNub(0, i, nub);
                    } else {
                        myMatrix.setNub(i, 0, nub);
                    }
                } else {
                    if (isRow) {//行向量
                        myMatrix.setNub(0, i, matrix.getNumber(0, i));
                    } else {//列向量
                        myMatrix.setNub(i, 0, matrix.getNumber(i, 0));
                    }
                }
            }
            return myMatrix;
        } else {
            throw new Exception("this matrix is not a vector");
        }
    }

    public static Matrix getPoolVector(Matrix matrix) throws Exception {
        if (matrix.getX() == 1 || matrix.getY() == 1) {
            Matrix vector;
            int nub;
            boolean isRow = false;
            if (matrix.getX() == 1) {//行向量
                isRow = true;
                nub = matrix.getY() / 4;
                vector = new Matrix(1, nub);
            } else {//列向量
                nub = matrix.getX() / 4;
                vector = new Matrix(nub, 1);
            }
            int k = 0;
            for (int i = 0; i < nub * 4 - 3; i += 4) {
                double max = 0;
                if (isRow) {
                    max = matrix.getNumber(0, i);
                    max = getMax(max, matrix.getNumber(0, i + 1));
                    max = getMax(max, matrix.getNumber(0, i + 2));
                    max = getMax(max, matrix.getNumber(0, i + 3));
                    vector.setNub(0, k, max);
                } else {
                    max = matrix.getNumber(i, 0);
                    max = getMax(max, matrix.getNumber(i + 1, 0));
                    max = getMax(max, matrix.getNumber(i + 2, 0));
                    max = getMax(max, matrix.getNumber(i + 3, 0));
                    vector.setNub(k, 0, max);
                }
                k++;
            }
            return vector;
        } else {
            throw new Exception("this matrix is not a vector");
        }

    }

    private static double getMax(double o1, double o2) {
        if (o1 > o2) {
            return o1;
        } else {
            return o2;
        }
    }

    public static Matrix matrixToVector(Matrix matrix, boolean isRow) throws Exception {//将一个矩阵转成行向量
        int x = matrix.getX();
        int y = matrix.getY();
        Matrix myMatrix;
        if (isRow) {
            myMatrix = new Matrix(1, x * y);
        } else {
            myMatrix = new Matrix(x * y, 1);
        }
        int t = 0;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if (isRow) {
                    myMatrix.setNub(0, t, matrix.getNumber(i, j));
                } else {
                    myMatrix.setNub(t, 0, matrix.getNumber(i, j));
                }
                t++;
            }
        }
        return myMatrix;
    }

    public static double innerProduct(Matrix matrix1, Matrix matrix2) throws Exception {//两个向量内积
        Matrix matrix = transPosition(matrix1);
        Matrix matrix3 = mulMatrix(matrix, matrix2);
        return matrix3.getNumber(0, 0);
    }

    public static double getNorm(Matrix matrix) throws Exception {//求向量范数
        if (matrix.getY() == 1 || matrix.getX() == 1) {
            double nub = 0;
            for (int i = 0; i < matrix.getX(); i++) {
                for (int j = 0; j < matrix.getY(); j++) {
                    nub = Math.pow(matrix.getNumber(i, j), 2) + nub;
                }
            }
            return Math.sqrt(nub);
        } else {
            throw new Exception("this matrix is not vector");
        }
    }

    public static double getNormCos(Matrix matrix1, Matrix matrix2) throws Exception {//求两个向量之间的余弦
        double inner = innerProduct(matrix1, matrix2);
        double mulNorm = ArithUtil.mul(getNorm(matrix1), getNorm(matrix2));
        return ArithUtil.div(inner, mulNorm);
    }

    public static Matrix transPosition(Matrix matrix) throws Exception {//矩阵转置
        Matrix myMatrix = new Matrix(matrix.getY(), matrix.getX());
        for (int i = 0; i < matrix.getY(); i++) {
            Matrix matrixColumn = matrix.getColumn(i);
            for (int j = 0; j < matrixColumn.getX(); j++) {
                double myNode = matrixColumn.getNumber(j, 0);
                myMatrix.setNub(i, j, myNode);
            }
        }
        return myMatrix;
    }

    public static double convolution(Matrix matrix, Matrix kernel, int x, int y, boolean isAccurate) throws Exception {//计算卷积
        double allNub = 0;
        int xr = 0;
        int yr = 0;
        int kxMax = kernel.getX();
        int kyMax = kernel.getY();
        if (isAccurate) {
            for (int i = 0; i < kxMax; i++) {
                xr = i + x;
                for (int j = 0; j < kyMax; j++) {
                    yr = j + y;
                    allNub = ArithUtil.add(ArithUtil.mul(matrix.getNumber(xr, yr), kernel.getNumber(i, j)), allNub);
                }
            }
        } else {
            for (int i = 0; i < kxMax; i++) {
                xr = i + x;
                for (int j = 0; j < kyMax; j++) {
                    yr = j + y;
                    allNub = matrix.getNumber(xr, yr) * kernel.getNumber(i, j) + allNub;
                }
            }
        }
        return allNub;
    }

    public static double getKernelNub(Matrix matrix, Matrix kernel) throws Exception {
        double allNub = 0;
        int x = matrix.getX();
        int y = matrix.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                allNub = matrix.getNumber(i, j) * kernel.getNumber(i, j) + allNub;
            }
        }
        return allNub;
    }

    public static int inverseNumber(double[] myInverse) {//逆序数奇偶性判定
        int size = myInverse.length;
        int inverserNumber = 0;
        for (int i = 0; i < size; i++) {
            double element = myInverse[i];
            for (int j = (i + 1); j < size; j++) {
                if (myInverse[j] < element) {
                    inverserNumber++;
                }
            }
        }
        return inverserNumber;
    }

    public static Matrix getInverseMatrixs(Matrix matrix) throws Exception {//矩阵求逆
        double def = matrix.getDet();
        if (def != 0) {
            def = ArithUtil.div(1, def);
            Matrix myMatrix = adjointMatrix(matrix);//伴随矩阵
            mathMul(myMatrix, def);
            return myMatrix;
        } else {
            //System.out.println("matrix def is zero error:");
            //System.out.println(matrix.getString());
            return new Matrix(1, 1);
        }
    }

    public static Matrix adjointMatrix(Matrix matrix) throws Exception {//求伴随矩阵
        Matrix myMatrix = new Matrix(matrix.getX(), matrix.getY());
        for (int i = 0; i < matrix.getX(); i++) {
            for (int j = 0; j < matrix.getY(); j++) {
                myMatrix.setNub(i, j, algebraicCofactor(matrix, i, j));
            }
        }

        return transPosition(myMatrix);
    }

    public static double algebraicCofactor(Matrix matrix, int row, int column) throws Exception {//获取代数余子式
        if (row >= 0 && column >= 0 && row < matrix.getX() && column < matrix.getY()) {
            int x = matrix.getX() - 1;
            int y = matrix.getY() - 1;
            int ij = row + column + 2;
            int oldX = 0;//从目标矩阵中取值的X
            int oldY = 0;//从目标矩阵中取值的Y
            boolean isXNext = false;//行是否跳过一次
            Matrix myMatrix = new Matrix(x, y);
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    if (i == row && !isXNext) {
                        isXNext = true;
                        oldX++;
                    }
                    if (j == column) {
                        oldY++;
                    }
                    myMatrix.setNub(i, j, matrix.getNumber(oldX, oldY));
                    oldY++;
                }
                oldX++;
                oldY = 0;
            }
            double dm = myMatrix.getDet();
            if ((ij % 2) != 0) {//ij是奇数
                dm = ArithUtil.mul(-1, dm);
            }
            return dm;
        } else {
            throw new Exception("row or column index Beyond the limit");
        }
    }

    public static Matrix matrixPointDiv(Matrix matrix1, Matrix matrix2) throws Exception {//矩阵点除
        int x = matrix1.getX();
        int y = matrix1.getY();
        Matrix matrix = new Matrix(x, y);
        if (matrix2.getX() == x && matrix2.getY() == y) {
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    matrix.setNub(i, j, matrix1.getNumber(i, j) / matrix2.getNumber(i, j));
                }
            }
        } else {
            throw new Exception("two matrix is not equals");
        }
        return matrix;
    }

    public static Matrix mulMatrix(Matrix matrix1, Matrix matrix2) throws Exception {//矩阵相乘
        if (matrix1.getY() == matrix2.getX()) {
            Matrix matrix = new Matrix(matrix1.getX(), matrix2.getY());
            for (int i = 0; i < matrix1.getX(); i++) {
                Matrix matrixRow = matrix1.getRow(i);//行向量
                for (int j = 0; j < matrix2.getY(); j++) {
                    Matrix matrixColumn = matrix2.getColumn(j);
                    double columnAllNumber = 0;//对每一项的乘积求和
                    for (int h = 0; h < matrixColumn.getX(); h++) {
                        double columnNumber = matrixColumn.getNumber(h, 0);
                        double rowNumber = matrixRow.getNumber(0, h);
                        //double nowNumber = ArithUtil.mul(columnNumber, rowNumber);
                        //columnAllNumber = ArithUtil.add(columnAllNumber, nowNumber);
                        double nowNumber = columnNumber * rowNumber;
                        columnAllNumber = columnAllNumber + nowNumber;
                    }
                    matrix.setNub(i, j, columnAllNumber);
                }
            }
            return matrix;
        } else {
            throw new Exception("row is not equals column");
        }
    }

    public static void mathMul(Matrix matrix, double nub) throws Exception {//矩阵数乘
        for (int i = 0; i < matrix.getX(); i++) {
            for (int j = 0; j < matrix.getY(); j++) {
                matrix.setNub(i, j, matrix.getNumber(i, j) * nub);
            }
        }
    }

    public static void mathSub(Matrix matrix, double nub) throws Exception {//矩阵数减
        for (int i = 0; i < matrix.getX(); i++) {
            for (int j = 0; j < matrix.getY(); j++) {
                matrix.setNub(i, j, matrix.getNumber(i, j) - nub);
            }
        }
    }

    public static void mathDiv(Matrix matrix, double nub) throws Exception {//矩阵数除
        for (int i = 0; i < matrix.getX(); i++) {
            for (int j = 0; j < matrix.getY(); j++) {
                matrix.setNub(i, j, matrix.getNumber(i, j) / nub);
            }
        }
    }

    //行向量转LIST
    public static List<Double> rowVectorToList(Matrix matrix) throws Exception {
        List<Double> list = new ArrayList<>();
        for (int j = 0; j < matrix.getY(); j++) {
            list.add(matrix.getNumber(0, j));
        }
        return list;
    }

    //list转行向量
    public static Matrix listToRowVector(List<Double> list) throws Exception {
        Matrix matrix = new Matrix(1, list.size());
        for (int i = 0; i < list.size(); i++) {
            matrix.setNub(0, i, list.get(i));
        }
        return matrix;
    }

    //list转行向量定长
    public static Matrix listToRowVector(List<Double> list, int nub) throws Exception {
        Matrix matrix = new Matrix(1, nub);
        for (int i = 0; i < nub; i++) {
            double n = 0;
            if (list.size() > i) {
                n = list.get(i);
            }
            matrix.setNub(0, i, n);
        }
        return matrix;
    }
}
