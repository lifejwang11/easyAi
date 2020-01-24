package org.wlld.MatrixTools;

import org.wlld.tools.ArithUtil;

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

    public static Matrix getLinearRegression(Matrix parameter, Matrix Out) throws Exception {
        //将参数矩阵转置
        Matrix matrix1 = transPosition(parameter);
        //转置的参数矩阵乘以参数矩阵
        Matrix matrix2 = mulMatrix(matrix1, parameter);
        //求上一步的逆矩阵
        Matrix matrix3 = getInverseMatrixs(matrix2);
        //逆矩阵乘以转置矩阵
        Matrix matrix4 = mulMatrix(matrix3, matrix1);
        //最后乘以输出矩阵,生成权重矩阵
        return mulMatrix(matrix4, Out);
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

    public static Matrix push(Matrix matrix, double nub) throws Exception {//向一个向量里PUSH一个值
        if (matrix.getX() == 1 || matrix.getY() == 1) {
            Matrix myMatrix;
            int nubs;
            boolean isRow = true;
            if (matrix.getX() == 1) {//行向量
                nubs = matrix.getY() + 1;
                myMatrix = new Matrix(1, nubs);
            } else {//列向量
                isRow = false;
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
            Matrix matrix1 = transPosition(matrix);
            return Math.sqrt(mulMatrix(matrix1, matrix).getNumber(0, 0));
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

    public static double convolution(Matrix matrix, Matrix kernel, int x, int y) throws Exception {//计算卷积
        double allNub = 0;
        int xr = 0;
        int yr = 0;
        int kxMax = kernel.getX();
        int kyMax = kernel.getY();
        for (int i = 0; i < kxMax; i++) {
            xr = i + x;
            for (int j = 0; j < kyMax; j++) {
                yr = j + y;
                allNub = matrix.getNumber(xr, yr) * kernel.getNumber(i, j) + allNub;
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

    public static Matrix getInverseMatrixs(Matrix matrixs) throws Exception {//矩阵求逆
        double def = matrixs.getDet();
        if (def != 0) {
            def = ArithUtil.div(1, def);
            Matrix myMatrix = adjointMatrix(matrixs);//伴随矩阵
            mathMul(myMatrix, def);
            return myMatrix;
        } else {
            throw new Exception("this matrixs do not have InverseMatrixs");
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
                        double nowNumber = ArithUtil.mul(columnNumber, rowNumber);
                        columnAllNumber = ArithUtil.add(columnAllNumber, nowNumber);
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
                matrix.setNub(i, j, ArithUtil.mul(matrix.getNumber(i, j), nub));
            }
        }
    }
}
