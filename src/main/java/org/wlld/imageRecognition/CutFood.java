package org.wlld.imageRecognition;


import org.wlld.MatrixTools.Matrix;
import org.wlld.imageRecognition.border.GMClustering;
import org.wlld.imageRecognition.modelEntity.GMBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class CutFood {
    private TempleConfig templeConfig;
    private Map<Integer, GMClustering> meanMap = new HashMap<>();
    private Matrix regionMap;

    public CutFood(TempleConfig templeConfig) {
        this.templeConfig = templeConfig;
    }

    private void mean(ThreeChannelMatrix threeChannelMatrix, GMClustering mean) throws Exception {
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        Matrix matrixG = threeChannelMatrix.getMatrixG();
        Matrix matrixB = threeChannelMatrix.getMatrixB();
        int x = matrixR.getX();
        int y = matrixR.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double[] rgb = new double[]{matrixR.getNumber(i, j) / 255, matrixG.getNumber(i, j) / 255
                        , matrixB.getNumber(i, j) / 255};
                mean.setColor(rgb);
            }
        }
        mean.start();
    }

    private double getAvg(Matrix matrix) throws Exception {
        double sigma = 0;
        int x = matrix.getX();
        int y = matrix.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                sigma = sigma + matrix.getNumber(i, j);
            }
        }
        return sigma / (x * y);
    }

    public void createRegion(ThreeChannelMatrix threeChannelMatrix) throws Exception {
        int size = templeConfig.getFood().getRegionSize();
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        Matrix matrixG = threeChannelMatrix.getMatrixG();
        Matrix matrixB = threeChannelMatrix.getMatrixB();
        int x = matrixR.getX();
        int y = matrixR.getY();
        double s = Math.pow(size, 2);
        regionMap = new Matrix(x / size, y / size);
        for (int i = 0; i <= x - size; i += size) {
            for (int j = 0; j <= y - size; j += size) {
                double r = getAvg(matrixR.getSonOfMatrix(i, j, size, size));
                double g = getAvg(matrixG.getSonOfMatrix(i, j, size, size));
                double b = getAvg(matrixB.getSonOfMatrix(i, j, size, size));
                double[] rgb = new double[]{r / 255, g / 255, b / 255};
                int index = getType(rgb);//该区域所属类别
                regionMap.setNub(i / size, j / size, index);
            }
        }
        int xr = regionMap.getX();
        int yr = regionMap.getY();
        List<GMBody> gmBodies = new ArrayList<>();
        for (int i = 0; i < xr; i++) {
            for (int j = 0; j < yr; j++) {
                int type = (int) regionMap.getNumber(i, j);
                if (!insertBodies(gmBodies, i, j, type)) {//需要创建一个新的
                    gmBodies.add(new GMBody(type, i, j));
                }
            }
        }
        List<GMBody> gmBodies2 = new ArrayList<>();
        for (int i = 0; i < gmBodies.size(); i++) {//过滤侯选区
            GMBody gmBody = gmBodies.get(i);
            double regionSize = gmBody.getPixelNub() * s;
            int type = gmBody.getType();
            if (type != 1) {//背景直接过滤
                double oneSize = meanMap.get(type).getRegionSize();
                if (regionSize > oneSize * 0.8) {
                    gmBodies2.add(gmBody);
                }
            }
        }
        for (GMBody gmBody : gmBodies2) {
            int type = gmBody.getType();
            double regionSize = gmBody.getPixelNub() * s;
            double oneSize = meanMap.get(type).getRegionSize();
            double nub = regionSize / (double) oneSize;
            System.out.println("type==" + type + ",nub==" + nub + ",onSize==" + oneSize + ",gmNub=="
                    + gmBody.getPixelNub());
        }

    }

    private boolean insertBodies(List<GMBody> gmBodies, int x, int y, int type) {
        boolean isInsert = false;
        for (GMBody gmBody : gmBodies) {
            if (gmBody.insertRgb(x, y, type)) {
                isInsert = true;
                break;
            }
        }
        return isInsert;
    }

    private int getType(double[] rgb) throws Exception {
        int index = 0;
        double max = 0;
        for (Map.Entry<Integer, GMClustering> entry : meanMap.entrySet()) {
            GMClustering gmClustering = entry.getValue();
            double probability = gmClustering.getProbabilityDensity(rgb);
            if (probability > max) {
                max = probability;
                index = entry.getKey();
            }
        }
        if (max < 2) {
            index = 1;
        }
        return index;
    }

    public void study(int type, ThreeChannelMatrix threeChannelMatrix) throws Exception {
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        int x = matrixR.getX();
        int y = matrixR.getY();
        GMClustering mean = new GMClustering(templeConfig.getFeatureNub());
        mean.setRegionSize(x * y * 0.8);
        meanMap.put(type, mean);
        mean(threeChannelMatrix, mean);
        //记录非背景的单物体面积
    }
}
