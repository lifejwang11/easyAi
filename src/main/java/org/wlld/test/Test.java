package org.wlld.test;

import org.wlld.MatrixTools.Matrix;
import org.wlld.imageRecognition.Operation;
import org.wlld.imageRecognition.Picture;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.tools.ArithUtil;

/**
 * @author lidapeng
 * @description
 * @date 2:11 下午 2020/1/7
 */
public class Test {
    public static Matrix E;
    public static Matrix F;

    static {
        try {
            E = getE(true);
            F = getE(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        double d = ArithUtil.div(3204, 4032);
        int a = (int) (d * 5);
        System.out.println(a);
    }

    public static void test() throws Exception {

    }

    public static Matrix getE(boolean isRight) throws Exception {
        Matrix matrix = new Matrix(5, 4);
        String name;
        if (isRight) {
            name = "[10,10,10,0]#" +
                    "[10,10,10,0]#" +
                    "[10,10,10,0]#" +
                    "[10,10,10,0]#" +
                    "[10,10,10,0]#";
        } else {
            name = "[1,1,1,0]#" +
                    "[1,1,1,0]#" +
                    "[1,1,1,0]#" +
                    "[1,1,1,0]#" +
                    "[1,1,1,0]#";
        }

        matrix.setAll(name);
        return matrix;
    }
}
