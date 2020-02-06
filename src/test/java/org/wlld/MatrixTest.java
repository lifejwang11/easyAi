package org.wlld;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;

/**
 * @author lidapeng
 * @description
 * @date 3:35 下午 2020/1/23
 */
public class MatrixTest {
    public static void main(String[] args) throws Exception {
        test4();
    }

    public static void test4() throws Exception {
        Matrix matrix = new Matrix(1, 12);
        String a = "[1,2,3,4,5,6,7,8,9,10,11,12]#";
        matrix.setAll(a);
        Matrix matrix1 = MatrixOperation.getPoolVector(matrix);
        System.out.println(matrix1.getString());
    }

    public static void test3() throws Exception {
        Matrix matrix = new Matrix(4, 3);
        Matrix matrixY = new Matrix(4, 1);
        String b = "[7]#" +
                "[8]#" +
                "[9]#" +
                "[19]#";
        matrixY.setAll(b);
        String a = "[1,2,17]#" +
                "[3,4,18]#" +
                "[5,6,10]#" +
                "[15,16,13]#";
        matrix.setAll(a);
        //将参数矩阵转置
        Matrix matrix1 = MatrixOperation.transPosition(matrix);
        //转置的参数矩阵乘以参数矩阵
        Matrix matrix2 = MatrixOperation.mulMatrix(matrix1, matrix);
        //求上一步的逆矩阵
        Matrix matrix3 = MatrixOperation.getInverseMatrixs(matrix2);
        //逆矩阵乘以转置矩阵
        Matrix matrix4 = MatrixOperation.mulMatrix(matrix3, matrix1);
        //最后乘以输出矩阵,生成权重矩阵
        Matrix matrix5 = MatrixOperation.mulMatrix(matrix4, matrixY);
        System.out.println(matrix5.getString());
    }

    public static void test1() throws Exception {
        Matrix matrix = new Matrix(2, 2);
        Matrix matrix2 = new Matrix(1, 5);
        String b = "[6,7,8,9,10]#";
        String a = "[1,2]#" +
                "[3,4]#";
        matrix.setAll(a);
        matrix2.setAll(b);
        Matrix matrix1 = MatrixOperation.matrixToVector(matrix, true);
        matrix1 = MatrixOperation.push(matrix1, 5, true);
        matrix1 = MatrixOperation.pushVector(matrix1, matrix2, true);
        System.out.println(matrix1.getString());
    }

    public static void test2() throws Exception {
        Matrix matrix = new Matrix(2, 2);
        Matrix matrix1 = new Matrix(1, 5);
        String a = "[1,2]#" +
                "[3,4]#";
        String b = "[6,7,8,9,10]#";
        matrix.setAll(a);
        matrix1.setAll(b);
        matrix1 = MatrixOperation.matrixToVector(matrix1, false);
        matrix = MatrixOperation.matrixToVector(matrix, false);
        matrix = MatrixOperation.push(matrix, 5, true);
        matrix = MatrixOperation.pushVector(matrix, matrix1, false);
        System.out.println(matrix.getString());
    }
}
