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
        test2();
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
        matrix1 = MatrixOperation.push(matrix1, 5);
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
        matrix = MatrixOperation.push(matrix, 5);
        matrix = MatrixOperation.pushVector(matrix, matrix1, false);
        System.out.println(matrix.getString());
    }
}
