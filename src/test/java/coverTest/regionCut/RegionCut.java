package coverTest.regionCut;

import org.wlld.MatrixTools.Matrix;
import org.wlld.imageRecognition.ThreeChannelMatrix;
import org.wlld.pso.PSO;

import java.util.*;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 分区切割
 */
public class RegionCut {
    private ThreeChannelMatrix threeChannelMatrix;
    private int fatherX;
    private int fatherY;
    private int size;
    private int id = 1;//分区id
    private Map<Integer, Double> minMap = new HashMap<>();//保存最小值
    private Map<Integer, Double> maxMap = new HashMap<>();//保存最大值

    public RegionCut(ThreeChannelMatrix threeChannelMatrix, int fatherX, int fatherY, int size) {
        this.fatherX = fatherX;
        this.fatherY = fatherY;
        this.size = size;
        this.threeChannelMatrix = threeChannelMatrix;
    }


    private void firstCut() throws Exception {//进行第一次切割
        ColorFunction colorFunction = new ColorFunction(threeChannelMatrix);
        int[] minBorder = new int[]{0, 0};
        int[] maxBorder = new int[]{299, 299};
        PSO pso = new PSO(2, minBorder, maxBorder, 400, 200,
                colorFunction, 0.1, 0.1, 0.1, true, 10);
        pso.start(fatherX, fatherY);
        //        Matrix matrixR = threeChannelMatrix.getMatrixR();
//        Matrix matrixG = threeChannelMatrix.getMatrixG();
//        Matrix matrixB = threeChannelMatrix.getMatrixB();
//        Matrix matrixRGB = threeChannelMatrix.getMatrixRGB();
//        int x = matrixR.getX();
//        int y = matrixR.getY();
//        int index = 0;
//        for (int i = 0; i <= x - size; i += size) {
//            for (int j = 0; j <= y - size; j += size) {
//                Matrix matrixRs = matrixR.getSonOfMatrix(i, j, size, size);
//                Matrix matrixGs = matrixG.getSonOfMatrix(i, j, size, size);
//                Matrix matrixBs = matrixB.getSonOfMatrix(i, j, size, size);
//                index++;
//            }
//        }


    }


    public void start() throws Exception {
        firstCut();
    }

}
