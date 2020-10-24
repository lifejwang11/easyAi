package org.wlld.imageRecognition;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.config.Kernel;
import org.wlld.imageRecognition.border.Border;
import org.wlld.imageRecognition.border.Frame;
import org.wlld.imageRecognition.border.FrameBody;
import org.wlld.imageRecognition.segmentation.ColorFunction;
import org.wlld.pso.PSO;
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

    public List<List<Double>> kAvg(ThreeChannelMatrix threeMatrix, int sqNub
            , int regionSize, TempleConfig templeConfig) throws Exception {
        RGBSort rgbSort = new RGBSort();
        List<List<Double>> features = new ArrayList<>();
        List<ThreeChannelMatrix> threeChannelMatrixList = regionThreeChannelMatrix(threeMatrix, regionSize);
        for (ThreeChannelMatrix threeChannelMatrix : threeChannelMatrixList) {
            List<Double> feature = new ArrayList<>();
            MeanClustering meanClustering = new MeanClustering(sqNub, templeConfig, true);
            Matrix matrixR = threeChannelMatrix.getMatrixR();
            Matrix matrixG = threeChannelMatrix.getMatrixG();
            Matrix matrixB = threeChannelMatrix.getMatrixB();
            int x = matrixR.getX();
            int y = matrixR.getY();
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    double[] color = new double[]{matrixR.getNumber(i, j) / 255, matrixG.getNumber(i, j) / 255, matrixB.getNumber(i, j) / 255};
                    meanClustering.setColor(color);
                }
            }
            meanClustering.start(false);
            List<RGBNorm> rgbNorms = meanClustering.getMatrices();
            Collections.sort(rgbNorms, rgbSort);
            for (RGBNorm rgbNorm : rgbNorms) {
                double[] rgb = rgbNorm.getRgb();
                for (int i = 0; i < rgb.length; i++) {
                    feature.add(rgb[i]);
                }
            }
            features.add(feature);
        }
        return features;
    }


    public List<Double> getCenterColor(ThreeChannelMatrix threeChannelMatrix, TempleConfig templeConfig,
                                       int sqNub) throws Exception {
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        Matrix matrixG = threeChannelMatrix.getMatrixG();
        Matrix matrixB = threeChannelMatrix.getMatrixB();
        MeanClustering meanClustering = new MeanClustering(sqNub, templeConfig, true);
        int maxX = matrixR.getX();
        int maxY = matrixR.getY();
        ColorFunction colorFunction = new ColorFunction(threeChannelMatrix);
        int[] minBorder = new int[]{0, 0};
        int[] maxBorder = new int[]{maxX - 1, maxY - 1};
        //创建粒子群
        PSO pso = new PSO(2, minBorder, maxBorder, 200, 200,
                colorFunction, 0.2, 1, 0.5, true, 10, 1);
        List<double[]> positions = pso.start();
        for (int i = 0; i < positions.size(); i++) {
            double[] parameter = positions.get(i);
            //获取取样坐标
            int x = (int) parameter[0];
            int y = (int) parameter[1];
            double[] rgb = new double[]{matrixR.getNumber(x, y), matrixG.getNumber(x, y),
                    matrixB.getNumber(x, y)};
            meanClustering.setColor(rgb);
        }
        meanClustering.start(true);
        List<RGBNorm> rgbNorms = meanClustering.getMatrices();
        List<Double> features = new ArrayList<>();
        for (int i = 0; i < sqNub; i++) {
            double[] rgb = rgbNorms.get(i).getRgb();
            for (int j = 0; j < rgb.length; j++) {
                features.add(rgb[j]);
            }
        }

        return features;
    }

    public List<Double> getCenterTexture(ThreeChannelMatrix threeChannelMatrix, int size, TempleConfig templeConfig
            , int sqNub, boolean isStudy) throws Exception {
        MeanClustering meanClustering = new MeanClustering(sqNub, templeConfig, true);
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        Matrix matrixG = threeChannelMatrix.getMatrixG();
        Matrix matrixB = threeChannelMatrix.getMatrixB();
        Matrix matrixRGB = threeChannelMatrix.getMatrixRGB();
        int xn = matrixR.getX();
        int yn = matrixR.getY();
        //局部特征选区筛选
        int nub = size * size;
        int twoNub = nub * 2;
        for (int i = 0; i <= xn - size; i += 3) {
            for (int j = 0; j <= yn - size; j += 3) {
                Matrix sonR = matrixR.getSonOfMatrix(i, j, size, size);
                Matrix sonG = matrixG.getSonOfMatrix(i, j, size, size);
                Matrix sonB = matrixB.getSonOfMatrix(i, j, size, size);
                Matrix sonRGB = matrixRGB.getSonOfMatrix(i, j, size, size);
                double[] h = new double[nub];
                double[] rgb = new double[nub * 3];
                for (int t = 0; t < size; t++) {
                    for (int k = 0; k < size; k++) {
                        int index = t * size + k;
                        h[index] = sonRGB.getNumber(t, k);
                        rgb[index] = sonR.getNumber(t, k) / 255;
                        rgb[nub + index] = sonG.getNumber(t, k) / 255;
                        rgb[twoNub + index] = sonB.getNumber(t, k) / 255;
                    }
                }
                //900 200
                double dispersed = variance(h);
                if (dispersed < 900 && dispersed > 200) {
                    for (int m = 0; m < nub; m++) {
                        double[] color = new double[]{rgb[m], rgb[m + nub], rgb[m + twoNub]};
                        meanClustering.setColor(color);
                    }
                }
            }
        }
        //List<double[]> list = meanClustering.start(true);//开始聚类
        meanClustering.start(true);
//        if (tag == 0) {//识别
//            templeConfig.getFood().getkNerveManger().look(list);
//        } else {//训练
//            templeConfig.getFood().getkNerveManger().setFeature(tag, list);
//        }
        List<RGBNorm> rgbNorms = meanClustering.getMatrices();
        List<Double> features = new ArrayList<>();
        for (int i = 0; i < sqNub; i++) {
            double[] rgb = rgbNorms.get(i).getRgb();
            if (!isStudy) {
                rgb = rgbMapping(rgb, i, templeConfig);
            }
            for (int j = 0; j < rgb.length; j++) {
                features.add(rgb[j]);
            }

        }
        // System.out.println(features);
        return features;
    }

    private double[] rgbMapping(double[] rgb, int index, TempleConfig templeConfig) {//进行映射
        double[] mapping = templeConfig.getFood().getMappingParameter();
        int size = rgb.length;
        int allSize = mapping.length / 2;
        double[] mappingFeature = new double[size];
        for (int i = 0; i < size; i++) {
            int myIndex = size * index + i;
            mappingFeature[i] = rgb[i] * mapping[myIndex] + mapping[allSize + myIndex];
        }
        return mappingFeature;
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

    public void imgNormalization(ThreeChannelMatrix threeChannelMatrix) throws Exception {
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        Matrix matrixG = threeChannelMatrix.getMatrixG();
        Matrix matrixB = threeChannelMatrix.getMatrixB();
        int x = matrixR.getX();
        int y = matrixR.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double colorR = matrixR.getNumber(i, j) / 255;
                double colorG = matrixG.getNumber(i, j) / 255;
                double colorB = matrixB.getNumber(i, j) / 255;
                matrixR.setNub(i, j, colorR);
                matrixG.setNub(i, j, colorG);
                matrixB.setNub(i, j, colorB);
            }
        }
    }

    public ThreeChannelMatrix getRegionMatrix(ThreeChannelMatrix threeChannelMatrix, int x, int y, int xSize, int ySize) {
        ThreeChannelMatrix threeChannelMatrix1 = new ThreeChannelMatrix();
        Matrix matrixR = threeChannelMatrix.getMatrixR().getSonOfMatrix(x, y, xSize, ySize);
        Matrix matrixG = threeChannelMatrix.getMatrixG().getSonOfMatrix(x, y, xSize, ySize);
        Matrix matrixB = threeChannelMatrix.getMatrixB().getSonOfMatrix(x, y, xSize, ySize);
        Matrix matrixH = threeChannelMatrix.getH().getSonOfMatrix(x, y, xSize, ySize);
        Matrix matrixRGB = threeChannelMatrix.getMatrixRGB().getSonOfMatrix(x, y, xSize, ySize);
        threeChannelMatrix1.setMatrixR(matrixR);
        threeChannelMatrix1.setMatrixG(matrixG);
        threeChannelMatrix1.setMatrixB(matrixB);
        threeChannelMatrix1.setH(matrixH);
        threeChannelMatrix1.setMatrixRGB(matrixRGB);
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

    private void convolutionByThree(ThreeChannelMatrix threeChannelMatrix, Matrix kernel) throws Exception {
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        Matrix matrixG = threeChannelMatrix.getMatrixG();
        Matrix matrixB = threeChannelMatrix.getMatrixB();
        int x = matrixR.getX() - 2;//求导后矩阵的行数
        int y = matrixR.getY() - 2;//求导后矩阵的列数
        Matrix myMatrixR = new Matrix(x, y);
        Matrix myMatrixG = new Matrix(x, y);
        Matrix myMatrixB = new Matrix(x, y);
        for (int i = 0; i < x; i++) {//遍历行
            for (int j = 0; j < y; j++) {//遍历每行的列
                double rm = MatrixOperation.convolution(matrixR, kernel, i, j, false);
                double gm = MatrixOperation.convolution(matrixG, kernel, i, j, false);
                double bm = MatrixOperation.convolution(matrixB, kernel, i, j, false);
                myMatrixR.setNub(i, j, rm);
                myMatrixG.setNub(i, j, gm);
                myMatrixB.setNub(i, j, bm);
            }
        }
        myMatrixR = late(myMatrixR, 2);
        myMatrixG = late(myMatrixG, 2);
        myMatrixB = late(myMatrixB, 2);
        threeChannelMatrix.setMatrixR(myMatrixR);
        threeChannelMatrix.setMatrixG(myMatrixG);
        threeChannelMatrix.setMatrixB(myMatrixB);
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

    public Matrix late(Matrix matrix, int size) throws Exception {//池化处理
        int xn = matrix.getX();
        int yn = matrix.getY();
        int x = xn / size;//求导后矩阵的行数
        int y = yn / size;//求导后矩阵的列数
        Matrix myMatrix = new Matrix(x, y);//迟化后的矩阵
        for (int i = 0; i <= xn - size; i += size) {
            for (int j = 0; j <= yn - size; j += size) {
                Matrix matrix1 = matrix.getSonOfMatrix(i, j, size, size);
                double maxNub = 0.0;
                double n = size * size;
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
                maxNub = sigma / n;
                //迟化的最大值是 MAXNUB
                myMatrix.setNub(i / size, j / size, maxNub);
            }
        }
        return myMatrix;
    }
}
