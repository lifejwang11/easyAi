package coverTest;

import coverTest.regionCut.RegionCut;
import coverTest.regionCut.RegionFeature;
import org.wlld.MatrixTools.Matrix;
import org.wlld.imageRecognition.Convolution;
import org.wlld.imageRecognition.Picture;
import org.wlld.imageRecognition.ThreeChannelMatrix;
import org.wlld.imageRecognition.segmentation.RgbRegression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description数据观察
 */
public class DataObservation {
    private static Convolution convolution = new Convolution();

    public static void main(String[] args) throws Exception {
        //372,330,右 最大值 147.44
        //377 ,330右 最大值 69.6
        int xp = 100;//100 2
        int yp = 720;//720 2
        observation2("/Users/lidapeng/Desktop/test/testOne/e2.jpg", xp, yp);//2
    }

    public static void observation2(String url, int xp, int yp) throws Exception {
        Picture picture = new Picture();
        ThreeChannelMatrix threeChannelMatrix = picture.getThreeMatrix(url);
        ThreeChannelMatrix myThreeChannelMatrix = convolution.getRegionMatrix(threeChannelMatrix, xp, yp, 300, 300);
        RegionCut regionCut = new RegionCut(myThreeChannelMatrix, xp, yp, 30);
        regionCut.start();
    }

    public static void observation(String url, int xp, int yp, int size) throws Exception {
        Picture picture = new Picture();
        ThreeChannelMatrix threeChannelMatrix = picture.getThreeMatrix(url);
        ThreeChannelMatrix myThreeChannelMatrix = convolution.getRegionMatrix(threeChannelMatrix, xp, yp, size, size);
        //右
        ThreeChannelMatrix threeChannelMatrix2 = convolution.getRegionMatrix(threeChannelMatrix, xp, yp + size, size, size);
        getDist(myThreeChannelMatrix, threeChannelMatrix2, "右");
        //左
        ThreeChannelMatrix threeChannelMatrix3 = convolution.getRegionMatrix(threeChannelMatrix, xp, yp - size, size, size);
        getDist(myThreeChannelMatrix, threeChannelMatrix3, "左");
        //上
        ThreeChannelMatrix threeChannelMatrix4 = convolution.getRegionMatrix(threeChannelMatrix, xp - size, yp, size, size);
        getDist(myThreeChannelMatrix, threeChannelMatrix4, "上");
        //下
        ThreeChannelMatrix threeChannelMatrix5 = convolution.getRegionMatrix(threeChannelMatrix, xp + size, yp, size, size);
        getDist(myThreeChannelMatrix, threeChannelMatrix5, "下");
        //左上
        ThreeChannelMatrix threeChannelMatrix6 = convolution.getRegionMatrix(threeChannelMatrix, xp - size, yp - size, size, size);
        getDist(myThreeChannelMatrix, threeChannelMatrix6, "左上");
        //左下
        ThreeChannelMatrix threeChannelMatrix7 = convolution.getRegionMatrix(threeChannelMatrix, xp + size, yp - size, size, size);
        getDist(myThreeChannelMatrix, threeChannelMatrix7, "左下");
        //右上
        ThreeChannelMatrix threeChannelMatrix8 = convolution.getRegionMatrix(threeChannelMatrix, xp - size, yp + size, size, size);
        getDist(myThreeChannelMatrix, threeChannelMatrix8, "右上");
        //右下
        ThreeChannelMatrix threeChannelMatrix9 = convolution.getRegionMatrix(threeChannelMatrix, xp + size, yp + size, size, size);
        getDist(myThreeChannelMatrix, threeChannelMatrix9, "右下");
        //getDist("/Users/lidapeng/Desktop/test/testOne/a1.jpg", 468, 713, rgbRegression, "测");

    }

    private static void getDist(ThreeChannelMatrix threeChannelMatrix1, ThreeChannelMatrix threeChannelMatrix2, String name) throws Exception {
        Matrix matrixR1 = threeChannelMatrix1.getMatrixR();
        Matrix matrixG1 = threeChannelMatrix1.getMatrixG();
        Matrix matrixB1 = threeChannelMatrix1.getMatrixB();
        Matrix matrixR2 = threeChannelMatrix2.getMatrixR();
        Matrix matrixG2 = threeChannelMatrix2.getMatrixG();
        Matrix matrixB2 = threeChannelMatrix2.getMatrixB();
        int x = matrixR1.getX();
        int y = matrixR1.getY();
        int nub = x * y;
        double sigmaR = 0;
        double sigmaG = 0;
        double sigmaB = 0;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double subR = Math.pow(matrixR1.getNumber(i, j) - matrixR2.getNumber(i, j), 2);
                double subG = Math.pow(matrixG1.getNumber(i, j) - matrixG2.getNumber(i, j), 2);
                double subB = Math.pow(matrixB1.getNumber(i, j) - matrixB2.getNumber(i, j), 2);
                sigmaR = subR + sigmaR;
                sigmaG = subG + sigmaG;
                sigmaB = subB + sigmaB;
            }
        }
        sigmaR = sigmaR / nub;
        sigmaG = sigmaG / nub;
        sigmaB = sigmaB / nub;
        double sigma = sigmaR + sigmaG + sigmaB;
        System.out.println(name + ":" + sigma);
    }
}
