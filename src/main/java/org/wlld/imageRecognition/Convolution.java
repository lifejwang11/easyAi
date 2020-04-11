package org.wlld.imageRecognition;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.config.Kernel;
import org.wlld.imageRecognition.border.Border;
import org.wlld.imageRecognition.border.Frame;
import org.wlld.imageRecognition.border.FrameBody;
import org.wlld.imageRecognition.modelEntity.RegressionBody;
import org.wlld.tools.ArithUtil;
import org.wlld.tools.Frequency;

import java.util.*;

/**
 * @author lidapeng
 * 图像卷积
 * @date 9:23 上午 2020/1/2
 */
public class Convolution extends Frequency {
    private MeanClustering meanClustering;

    protected Matrix getFeatures(Matrix matrix, int maxNub, TempleConfig templeConfig
            , int id) throws Exception {
        boolean isFirst = true;
        Border border = null;
        if (id > -1 && templeConfig.isHavePosition()) {
            border = new Border(templeConfig, matrix.getY() - 2, matrix.getX() - 2);
        }
        do {
            matrix = convolution(matrix, Kernel.ALL_Two, isFirst, border, false);
            isFirst = false;
        }
        while (matrix.getX() > maxNub && matrix.getY() > maxNub);
        normalization(matrix);//矩阵做归一化处理
        if (id > -1 && templeConfig.isHavePosition()) {
            border.end(matrix, id);
        }
        //已经不可以再缩小了，最后做一层卷积，然后提取最大值
        return matrix;
    }

    public void kc(ThreeChannelMatrix threeChannelMatrix, int size, int sqNub) throws Exception {
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        Matrix matrixG = threeChannelMatrix.getMatrixG();
        Matrix matrixB = threeChannelMatrix.getMatrixB();
        matrixR = late(matrixR, size);
        matrixG = late(matrixG, size);
        matrixB = late(matrixB, size);
        int x = matrixR.getX();
        int y = matrixR.getY();
        meanClustering = new MeanClustering(sqNub);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double[] color = new double[]{matrixR.getNumber(i, j) / 255, matrixG.getNumber(i, j) / 255, matrixB.getNumber(i, j) / 255};
                meanClustering.setColor(color);
            }
        }
        meanClustering.start();
        List<RGBNorm> rgbNorms = meanClustering.getMatrices();
        double minNorm = 0;
        int normSize = rgbNorms.size();
        for (int i = 0; i < normSize; i++) {
            RGBNorm rgbNorm = rgbNorms.get(i);
            double[] rgb = rgbNorm.getRgb();
            for (int j = 0; j < normSize; j++) {
                if (j != i) {
                    double normSub = getEDist(rgb, rgbNorms.get(j).getRgb());
                    if (minNorm == 0 || normSub < minNorm) {
                        minNorm = normSub;
                    }
                }
            }
        }
        minNorm = ArithUtil.div(minNorm, 2);
        System.out.println("min==" + minNorm);
    }

    private void checkImage(Matrix matrixR, Matrix matrixG, Matrix matrixB, double minNorm, int size) {
        List<List<Double>> lists = new ArrayList<>();
        int x = matrixR.getX() - size;//求导后矩阵的行数
        int y = matrixR.getY() - size;//求导后矩阵的列数
        for (int i = 0; i < x; i += size) {//遍历行
            for (int j = 0; j < y; j += size) {//遍历每行的列
                Matrix myMatrixR = matrixR.getSonOfMatrix(i, j, size, size);
                Matrix myMatrixG = matrixG.getSonOfMatrix(i, j, size, size);
                Matrix myMatrixB = matrixB.getSonOfMatrix(i, j, size, size);

            }
        }
    }

    private void getListFeature(Matrix matrixR, Matrix matrixG, Matrix matrixB, double minNorm) throws Exception {
        int x = matrixR.getX();
        int y = matrixR.getY();
        Map<Double, Integer> map = new HashMap<>();
        List<RGBNorm> rgbNormList = meanClustering.getMatrices();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double[] color = new double[]{matrixR.getNumber(i, j) / 255, matrixG.getNumber(i, j) / 255, matrixB.getNumber(i, j) / 255};
                int id = -1;
                double feature = 0;
                double minDist = 0;
                for (int t = 0; t < rgbNormList.size(); t++) {
                    RGBNorm rgbNorm = rgbNormList.get(t);
                    double dist = getEDist(color, rgbNorm.getRgb());
                    if (minDist == 0 || dist < minDist) {
                        minDist = dist;
                        id = t;
                    }
                }
                if (minDist >= minNorm) {
                    id = -1;
                }
                if (id > -1) {
                    feature = rgbNormList.get(id).getNorm();
                }
                if (!map.containsKey(feature)) {
                    map.put(feature, 1);
                }
            }
        }
    }

    public List<List<Double>> imageTrance(Matrix matrix, int size, int featureNub) throws Exception {//矩阵和卷积核大小
        int xn = matrix.getX();
        int yn = matrix.getY();
        int xSize = xn / size;//求导后矩阵的行数
        int ySize = yn / size;//求导后矩阵的列数
        double[] Y = new double[xSize * ySize];
        double[] X = new double[xSize * ySize];
        for (int i = 0; i < xn - size; i += size) {
            for (int j = 0; j < yn - size; j += size) {
                Matrix matrix1 = matrix.getSonOfMatrix(i, j, size, size);
                double[] nubs = new double[size * size];//平均值数组
                for (int t = 0; t < size; t++) {
                    for (int k = 0; k < size; k++) {
                        double nub = matrix1.getNumber(t, k) / 255;
                        nubs[t * size + k] = nub;
                    }
                }
                double avg = average(nubs);//平均值
                double dc = dcByAvg(nubs, avg);//当前离散系数
                //double va = varianceByAve(nubs, avg);//方差
                //离散系数作为X，AVG作为Y
                int t = i / size * ySize + j / size;
                Y[t] = avg;
                X[t] = dc;
            }
        }
        //计算当前图形的线性回归
        RegressionBody regressionBody = new RegressionBody();
        regressionBody.lineRegression(Y, X, this);
        return regressionBody.mappingMatrix(featureNub);
    }


    private void normalization(Matrix matrix) throws Exception {
        for (int i = 0; i < matrix.getX(); i++) {
            for (int j = 0; j < matrix.getY(); j++) {
                matrix.setNub(i, j, ArithUtil.div(matrix.getNumber(i, j), 1000000));
            }
        }
    }

    protected Border borderOnce(Matrix matrix, TempleConfig templeConfig) throws Exception {
        Border border = new Border(templeConfig, matrix.getY() - 2, matrix.getX() - 2);
        convolution(matrix, Kernel.ALL_Two, true, border, true);
        return border;
    }

    public List<FrameBody> getRegion(Matrix matrix, Frame frame) {
        int xFrame = frame.getHeight();
        int yFrame = frame.getWidth();
        int x = matrix.getX();
        int y = matrix.getY();
        List<FrameBody> frameBodies = new ArrayList<>();
        double xNub = frame.getLengthHeight();
        double yNub = frame.getLengthWidth();
        for (int i = 0; i <= x - xFrame; i += xNub) {
            for (int j = 0; j <= y - yFrame; j += yNub) {
                FrameBody frameBody = new FrameBody();
                Matrix myMatrix = matrix.getSonOfMatrix(i, j, xFrame, yFrame);
                frameBody.setMatrix(myMatrix);
                frameBody.setX(i);
                frameBody.setY(j);
                frameBodies.add(frameBody);
            }
        }
        return frameBodies;
    }

    private Matrix convolution(Matrix matrix, Matrix kernel, boolean isFirst
            , Border border, boolean isOnce) throws Exception {
        int x = matrix.getX() - 2;//求导后矩阵的行数
        int y = matrix.getY() - 2;//求导后矩阵的列数
        Matrix myMatrix = null;
        if (!isOnce) {
            myMatrix = new Matrix(x, y);//最终合成矩阵
        }
        for (int i = 0; i < x; i++) {//遍历行
            for (int j = 0; j < y; j++) {//遍历每行的列
                double dm = MatrixOperation.convolution(matrix, kernel, i, j, false);
                if (dm > 0) {//存在边缘
                    if (isFirst && border != null) {
                        border.setPosition(i, j);
                    }
                    if (!isOnce) {
                        myMatrix.setNub(i, j, dm);
                    }
                }
            }
        }
        if (isOnce) {
            return null;
        } else {
            return late(myMatrix, 2);
        }
    }

    public Matrix getBorder(Matrix matrix, Matrix kernel) throws Exception {
        int x = matrix.getX() - 2;//求导后矩阵的行数
        int y = matrix.getY() - 2;//求导后矩阵的列数
        Matrix myMatrix = new Matrix(x, y);//最终合成矩阵
        for (int i = 0; i < x; i++) {//遍历行
            for (int j = 0; j < y; j++) {//遍历每行的列
                double dm = MatrixOperation.convolution(matrix, kernel, i, j, false);
                if (dm > 0) {//存在边缘
                    myMatrix.setNub(i, j, dm);
                }
            }
        }
        return myMatrix;
    }

    protected Matrix late(Matrix matrix, int size) throws Exception {//迟化处理
        int xn = matrix.getX();
        int yn = matrix.getY();
        int x = xn / size;//求导后矩阵的行数
        int y = yn / size;//求导后矩阵的列数
        Matrix myMatrix = new Matrix(x, y);//迟化后的矩阵
        for (int i = 0; i < xn - size; i += size) {
            for (int j = 0; j < yn - size; j += size) {
                Matrix matrix1 = matrix.getSonOfMatrix(i, j, size, size);
                double maxNub = 0;
                int n = size * size;
                double sigma = 0;
                for (int t = 0; t < matrix1.getX(); t++) {
                    for (int k = 0; k < matrix1.getY(); k++) {
                        double nub = matrix1.getNumber(t, k);
                        sigma = sigma + nub;
                        if (nub > maxNub) {
                            maxNub = nub;
                        }
                    }
                }
                maxNub = ArithUtil.div(sigma, n);
                //迟化的最大值是 MAXNUB
                myMatrix.setNub(i / size, j / size, maxNub);
            }
        }
        return myMatrix;
    }
}
