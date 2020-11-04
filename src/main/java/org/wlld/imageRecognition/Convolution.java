package org.wlld.imageRecognition;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.config.Kernel;
import org.wlld.imageRecognition.border.Border;
import org.wlld.imageRecognition.border.Frame;
import org.wlld.imageRecognition.border.FrameBody;
import org.wlld.imageRecognition.border.GMClustering;
import org.wlld.imageRecognition.segmentation.ColorFunction;
import org.wlld.param.Food;
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
            MeanClustering meanClustering = new MeanClustering(sqNub);
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
            meanClustering.start();
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

    private int[] insertFoodTypes(int[] foodTypes, int type) {
        int[] foods;
        if (foodTypes == null) {
            foods = new int[]{type};
        } else {
            foods = new int[foodTypes.length + 1];
            boolean isLife = false;
            for (int myType : foodTypes) {
                if (type == myType) {
                    isLife = true;
                    break;
                }
            }
            if (!isLife) {
                for (int i = 0; i < foodTypes.length; i++) {
                    foods[i] = foodTypes[i];
                }
                foods[foodTypes.length] = type;
            }
        }
        return foods;
    }

    public List<Double> getCenterTexture(ThreeChannelMatrix threeChannelMatrix, TempleConfig templeConfig
            , int sqNub, boolean isStudy, int tag, boolean isFood) throws Exception {
        //MeanClustering meanClustering = new MeanClustering(sqNub);
        Food food = templeConfig.getFood();
        Map<Integer, Double> foods = food.getFoodS();
        GMClustering meanClustering = new GMClustering(sqNub);
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        Matrix matrixG = threeChannelMatrix.getMatrixG();
        Matrix matrixB = threeChannelMatrix.getMatrixB();
        int xn = matrixR.getX();
        int yn = matrixR.getY();
        if (isStudy) {
            Map<Integer, GMClustering> meanMap;
            if (isFood) {//干食学习
                food.setFoodType(insertFoodTypes(food.getFoodType(), tag));
                meanMap = food.getFoodMeanMap();
                double size = xn * yn * food.getFoodFilterTh();
                meanClustering.setRegionSize(size);
                foods.put(tag, size);
            } else {//非干食学习
                meanMap = food.getNotFoodMeanMap();
            }
            meanMap.put(tag, meanClustering);
        }
        //局部特征选区筛选
        for (int i = 0; i < xn; i++) {
            for (int j = 0; j < yn; j++) {
                double r = matrixR.getNumber(i, j);
                double g = matrixG.getNumber(i, j);
                double b = matrixB.getNumber(i, j);
                if ((r + g + b) / 3 < 245) {
                    double[] rgb = new double[]{r / 255, g / 255, b / 255};
                    meanClustering.setColor(rgb);
                }
            }
        }
        meanClustering.start();
        List<RGBNorm> rgbNorms = meanClustering.getMatrices();
        List<Double> features = new ArrayList<>();
        for (int i = 0; i < sqNub; i++) {
            //double[] rgb = rgbNorms.get(i).getRgb();
            double[] rgb = rgbNorms.get(i).getFeature();
            for (int j = 0; j < rgb.length; j++) {
                features.add(rgb[j]);
            }

        }
        return features;
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
