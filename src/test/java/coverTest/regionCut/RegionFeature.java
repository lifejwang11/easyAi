package coverTest.regionCut;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.config.Kernel;
import org.wlld.imageRecognition.ThreeChannelMatrix;
import org.wlld.imageRecognition.segmentation.RgbRegression;
import org.wlld.tools.Frequency;

import java.util.Arrays;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 利用边缘算子提取特征
 */
public class RegionFeature extends Frequency {
    private Matrix matrix;
    private Matrix matrixR;
    private Matrix matrixG;
    private Matrix matrixB;
    private Matrix kernel = Kernel.Big;
    private int fatherX;
    private int fatherY;
    private RgbRegression rgbRegression = new RgbRegression(25);

    public RegionFeature(ThreeChannelMatrix threeChannelMatrix, int fatherX, int fatherY) {
        this.matrix = threeChannelMatrix.getMatrixRGB();
        this.fatherX = fatherX;
        this.fatherY = fatherY;
        matrixR = threeChannelMatrix.getMatrixR();
        matrixG = threeChannelMatrix.getMatrixG();
        matrixB = threeChannelMatrix.getMatrixB();
    }

    private double getMatrixVar(Matrix matrix) throws Exception {
        int x = matrix.getX();
        int y = matrix.getY();
        double[] data = new double[x * y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                int index = i * y + j;
                data[index] = matrix.getNumber(i, j);
            }
        }
        //System.out.println(Arrays.toString(data));
        return variance(data);
    }

    public void start() throws Exception {
        int x = matrix.getX();
        int y = matrix.getY();
        int size = kernel.getX();
        for (int i = 0; i <= x - size; i += 2) {
            int xSize = fatherX + i;
            System.out.println("==================================" + xSize);
            for (int j = 0; j <= y - size; j += 2) {
                //  double conNub = Math.abs(convolution(i, j));//卷积值
                double var = getMatrixVar(matrix.getSonOfMatrix(i, j, size, size));
                int ySize = fatherY + j;
                System.out.println("x:" + xSize + ",y:" + ySize + ",var:" + var);


            }
        }
    }

    private double convolution(int x, int y) throws Exception {//计算卷积
        double allNub = 0;
        int xr;
        int yr;
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
}
