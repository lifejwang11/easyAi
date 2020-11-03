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
    private Map<Integer, GMClustering> meanMap = new HashMap<>();//干食混高模型
    private Matrix regionMap;
    private double foodFilterTh;

    public CutFood(TempleConfig templeConfig) {
        this.templeConfig = templeConfig;
        foodFilterTh = templeConfig.getFood().getFoodFilterTh();
    }

    public void setMeanMap(Map<Integer, GMClustering> meanMap) {
        this.meanMap = meanMap;
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

    private boolean getAvgPro(ThreeChannelMatrix threeChannelMatrix, GMClustering gm) throws Exception {
        int size = templeConfig.getFood().getRegionSize();
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        Matrix matrixG = threeChannelMatrix.getMatrixG();
        Matrix matrixB = threeChannelMatrix.getMatrixB();
        int x = matrixR.getX();
        int y = matrixR.getY();
        regionMap = new Matrix(x / size, y / size);
        double sigmaPro = 0;
        double sigmaOther = 0;
        for (int i = 0; i <= x - size; i += size) {
            for (int j = 0; j <= y - size; j += size) {
                double r = getAvg(matrixR.getSonOfMatrix(i, j, size, size));
                double g = getAvg(matrixG.getSonOfMatrix(i, j, size, size));
                double b = getAvg(matrixB.getSonOfMatrix(i, j, size, size));
                double[] rgb = new double[]{r / 255, g / 255, b / 255};
                TypePro typePro = getType(rgb);
                int index = typePro.index;//该区域所属类别
                sigmaPro = sigmaPro + typePro.pro;
                if (gm != null) {
                    sigmaOther = sigmaOther + gm.getProbabilityDensity(rgb);//概率密度
                }
                regionMap.setNub(i / size, j / size, index);
            }
        }
        return sigmaPro > sigmaOther;
    }

    public Map<Integer, Integer> getTypeNub(ThreeChannelMatrix threeChannelMatrix,
                                            GMClustering gm) throws Exception {
        if (getAvgPro(threeChannelMatrix, gm)) {//进行二次判定
            return getTypeNub();
        }
        return null;
    }

    private Map<Integer, Integer> getTypeNub() throws Exception {
        int size = templeConfig.getFood().getRegionSize();
        double s = Math.pow(size, 2);
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
        Map<Integer, Integer> gmTypeNub = new HashMap<>();
        for (int i = 0; i < gmBodies.size(); i++) {//过滤侯选区
            GMBody gmBody = gmBodies.get(i);
            double regionSize = gmBody.getPixelNub() * s;
            int type = gmBody.getType();
            if (type != 1) {//背景直接过滤
                double oneSize = meanMap.get(type).getRegionSize();
                int nub = (int) (regionSize / oneSize);
                if (nub > 0) {
                    if (gmTypeNub.containsKey(type)) {
                        int myNub = gmTypeNub.get(type);
                        if (nub > myNub) {
                            gmTypeNub.put(type, nub);
                        }
                    } else {
                        gmTypeNub.put(type, nub);
                    }
                }
            }
        }
        return gmTypeNub;
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

    class TypePro {
        private int index;
        private double pro;//概率密度

        TypePro(int index, double pro) {
            this.index = index;
            this.pro = pro;
        }
    }

    private TypePro getType(double[] rgb) throws Exception {
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
            max = 0;
        }

        return new TypePro(index, max);
    }

    public void study(int type, ThreeChannelMatrix threeChannelMatrix) throws Exception {
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        int x = matrixR.getX();
        int y = matrixR.getY();
        GMClustering mean = new GMClustering(templeConfig.getFeatureNub());
        mean.setRegionSize(x * y * foodFilterTh);
        meanMap.put(type, mean);
        mean(threeChannelMatrix, mean);
        //记录非背景的单物体面积
    }
}
