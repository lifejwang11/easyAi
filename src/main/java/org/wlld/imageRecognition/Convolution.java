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

    private List<ThreeChannelMatrix> regionThreeChannelMatrix(ThreeChannelMatrix threeChannelMatrix, int size) {
        List<ThreeChannelMatrix> threeChannelMatrixList = new ArrayList<>();
        Matrix matrixRAll = threeChannelMatrix.getMatrixR();
        Matrix matrixGAll = threeChannelMatrix.getMatrixG();
        Matrix matrixBAll = threeChannelMatrix.getMatrixB();
        int x = matrixRAll.getX();
        int y = matrixRAll.getY();
        for (int i = 0; i <= x - size; i += size) {
            for (int j = 0; j <= y - size; j += size) {
                ThreeChannelMatrix threeMatrix = new ThreeChannelMatrix();
                Matrix matrixR = matrixRAll.getSonOfMatrix(i, j, size, size);
                Matrix matrixG = matrixGAll.getSonOfMatrix(i, j, size, size);
                Matrix matrixB = matrixBAll.getSonOfMatrix(i, j, size, size);
                threeMatrix.setMatrixR(matrixR);
                threeMatrix.setMatrixG(matrixG);
                threeMatrix.setMatrixB(matrixB);
                threeChannelMatrixList.add(threeMatrix);
            }
        }
        return threeChannelMatrixList;
    }

    public List<List<Double>> kAvg(ThreeChannelMatrix threeMatrix, int poolSize, int sqNub
            , int regionSize) throws Exception {
        RGBSort rgbSort = new RGBSort();
        List<List<Double>> features = new ArrayList<>();
        Matrix matrixR = threeMatrix.getMatrixR();
        Matrix matrixG = threeMatrix.getMatrixG();
        Matrix matrixB = threeMatrix.getMatrixB();
        matrixR = late(matrixR, poolSize);
        matrixG = late(matrixG, poolSize);
        matrixB = late(matrixB, poolSize);
        threeMatrix.setMatrixR(matrixR);
        threeMatrix.setMatrixG(matrixG);
        threeMatrix.setMatrixB(matrixB);
        List<ThreeChannelMatrix> threeChannelMatrixList = regionThreeChannelMatrix(threeMatrix, regionSize);
        for (ThreeChannelMatrix threeChannelMatrix : threeChannelMatrixList) {
            List<Double> feature = new ArrayList<>();
            MeanClustering meanClustering = new MeanClustering(sqNub);
            matrixR = threeChannelMatrix.getMatrixR();
            matrixG = threeChannelMatrix.getMatrixG();
            matrixB = threeChannelMatrix.getMatrixB();
            int x = matrixR.getX();
            int y = matrixR.getY();
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    double[] color = new double[]{matrixR.getNumber(i, j) / 255, matrixG.getNumber(i, j) / 255, matrixB.getNumber(i, j) / 255};
                    meanClustering.setColor(color);
                }
            }
            meanClustering.start();
            List<RGBNorm> rgbNorms = meanClustering.getMatrices();
            Collections.sort(rgbNorms, rgbSort);
            double[] dm = new double[sqNub];
            for (RGBNorm rgbNorm : rgbNorms) {
                feature.add(rgbNorm.getNorm());
            }
            for (int t = 0; t < dm.length; t++) {
                dm[t] = rgbNorms.get(t).getNorm();
            }
            //System.out.println(Arrays.toString(dm));
            features.add(feature);
        }
        return features;
    }

    public void filtering(ThreeChannelMatrix threeChannelMatrix) throws Exception {//平滑滤波
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        Matrix matrixG = threeChannelMatrix.getMatrixG();
        Matrix matrixB = threeChannelMatrix.getMatrixB();
        int x = matrixR.getX();
        int y = matrixR.getY();
        Matrix matrixRFilter = new Matrix(x, y);//滤波后的R通道
        Matrix matrixGFilter = new Matrix(x, y);//滤波后的G通道
        Matrix matrixBFilter = new Matrix(x, y);//滤波后的B通道
        int row = 0;
        int column = 0;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double sigmaR = 0;
                double sigmaG = 0;
                double sigmaB = 0;
                double nub = 0;
                for (int t = 0; t < 8; t++) {
                    row = 0;
                    column = 0;
                    switch (t) {
                        case 0://上
                            row = i - 1;
                            break;
                        case 1://左
                            column = j - 1;
                            break;
                        case 2://下
                            row = i + 1;
                            break;
                        case 3://右
                            column = j + 1;
                            break;
                        case 4://左上
                            column = j - 1;
                            row = i - 1;
                            break;
                        case 5://左下
                            column = j - 1;
                            row = i + 1;
                            break;
                        case 6://右下
                            column = j + 1;
                            row = i + 1;
                            break;
                        case 7://右上
                            column = j + 1;
                            row = i - 1;
                            break;
                    }
                    if (row >= 0 && column >= 0 && row < x && column < y) {
                        double r = matrixR.getNumber(row, column);
                        double g = matrixG.getNumber(row, column);
                        double b = matrixB.getNumber(row, column);
                        sigmaR = sigmaR + r;
                        sigmaG = sigmaG + g;
                        sigmaB = sigmaB + b;
                        nub++;
                    }
                }
                double pixelR = sigmaR / nub;
                double pixelG = sigmaG / nub;
                double pixelB = sigmaB / nub;
                matrixRFilter.setNub(i, j, pixelR);
                matrixGFilter.setNub(i, j, pixelG);
                matrixBFilter.setNub(i, j, pixelB);
            }
        }
        Matrix rPic = MatrixOperation.matrixPointDiv(matrixR, matrixRFilter);
        Matrix gPic = MatrixOperation.matrixPointDiv(matrixG, matrixGFilter);
        Matrix bPic = MatrixOperation.matrixPointDiv(matrixB, matrixBFilter);
        threeChannelMatrix.setMatrixR(rPic);
        threeChannelMatrix.setMatrixG(gPic);
        threeChannelMatrix.setMatrixB(bPic);
    }

    public List<Double> getCenterColor(ThreeChannelMatrix threeChannelMatrix, int poolSize, int sqNub) throws Exception {
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        Matrix matrixG = threeChannelMatrix.getMatrixG();
        Matrix matrixB = threeChannelMatrix.getMatrixB();
        matrixR = late(matrixR, poolSize);
        matrixG = late(matrixG, poolSize);
        matrixB = late(matrixB, poolSize);
        RGBSort rgbSort = new RGBSort();
        int x = matrixR.getX();
        int y = matrixR.getY();
        MeanClustering meanClustering = new MeanClustering(sqNub);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double[] color = new double[]{matrixR.getNumber(i, j), matrixG.getNumber(i, j), matrixB.getNumber(i, j)};
                meanClustering.setColor(color);
            }
        }
        meanClustering.start();
        List<RGBNorm> rgbNorms = meanClustering.getMatrices();
        Collections.sort(rgbNorms, rgbSort);
        List<Double> features = new ArrayList<>();
        for (int i = 0; i < sqNub; i++) {
            double[] rgb = rgbNorms.get(i).getRgb();
            for (int j = 0; j < 3; j++) {
                features.add(rgb[j]);
            }
        }
        //System.out.println("feature==" + feature);
        return features;
    }

    private void regression(XYBody xyBody) {
        //计算当前图形的线性回归
        RegressionBody regressionBody = new RegressionBody();
        regressionBody.lineRegression(xyBody.getY(), xyBody.getX(), this);
        xyBody.setRegressionBody(regressionBody);
    }

    public XYBody imageTrance(Matrix matrix, int size) throws Exception {//矩阵和卷积核大小
        int xn = matrix.getX();
        int yn = matrix.getY();
        int xSize = xn / size;//求导后矩阵的行数
        int ySize = yn / size;//求导后矩阵的列数
        double[] Y = new double[xSize * ySize];
        double[] X = new double[xSize * ySize];
        double rgbN = Kernel.rgbN;
        for (int i = 0; i < xn - size; i += size) {
            for (int j = 0; j < yn - size; j += size) {
                Matrix matrix1 = matrix.getSonOfMatrix(i, j, size, size);
                double[] nubs = new double[size * size];//平均值数组
                for (int t = 0; t < size; t++) {
                    for (int k = 0; k < size; k++) {
                        double nub = matrix1.getNumber(t, k) / rgbN;
                        nubs[t * size + k] = nub;
                    }
                }
                double avg = average(nubs);//平均值
                //double dc = frequency.dcByAvg(nubs, avg);//当前离散系数
                double va = varianceByAve(nubs, avg);//方差
                //离散系数作为X，AVG作为Y
                int t = i / size * ySize + j / size;
                Y[t] = avg;
                X[t] = va;
            }
        }
        XYBody xyBody = new XYBody();
        xyBody.setX(X);
        xyBody.setY(Y);
        return xyBody;
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

    public ThreeChannelMatrix getRegionMatrix(ThreeChannelMatrix threeChannelMatrix, int x, int y, int xSize, int ySize) {
        ThreeChannelMatrix threeChannelMatrix1 = new ThreeChannelMatrix();
        Matrix matrixR = threeChannelMatrix.getMatrixR().getSonOfMatrix(x, y, xSize, ySize);
        Matrix matrixG = threeChannelMatrix.getMatrixG().getSonOfMatrix(x, y, xSize, ySize);
        Matrix matrixB = threeChannelMatrix.getMatrixB().getSonOfMatrix(x, y, xSize, ySize);
        threeChannelMatrix1.setMatrixR(matrixR);
        threeChannelMatrix1.setMatrixG(matrixG);
        threeChannelMatrix1.setMatrixB(matrixB);
        threeChannelMatrix1.setH(threeChannelMatrix.getH());
        threeChannelMatrix1.setMatrixRGB(threeChannelMatrix.getMatrixRGB());
        return threeChannelMatrix1;
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
