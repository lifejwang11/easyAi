package org.wlld.test;

import org.wlld.MatrixTools.Matrix;
import org.wlld.imageRecognition.Operation;
import org.wlld.imageRecognition.Picture;
import org.wlld.imageRecognition.TempleConfig;

/**
 * @author lidapeng
 * @description
 * @date 2:11 下午 2020/1/7
 */
public class Test {
    public static Matrix E;

    static {
        try {
            E = getE();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        test();
    }

    public static void test() throws Exception {
        Picture picture = new Picture();
        TempleConfig templeConfig = new TempleConfig();
        templeConfig.initConvolutionVision(true, 3024, 4032);
        Operation operation = new Operation(templeConfig);
        for (int i = 1; i < 500; i++) {
            System.out.println("开始学习1==" + i);
            //读取本地URL地址图片,并转化成矩阵
            Matrix right = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/c/c" + i + ".png");
            // Matrix wrong = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/b/b" + i + ".png");
            //将图像矩阵和标注加入进行学习，
            operation.learning(right, E);
        }
    }

    public static Matrix getE() throws Exception {
        Matrix matrix = new Matrix(5, 4);
        String name = "[10,10,10,0]#" +
                "[10,10,10,0]#" +
                "[10,10,10,0]#" +
                "[10,10,10,0]#" +
                "[10,10,10,0]#";
        matrix.setAll(name);
        return matrix;
    }
}
