package org.wlld.imageRecognition.segmentation;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.imageRecognition.DistSort;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.imageRecognition.border.DistBody;

import java.util.*;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 修正映射
 */
public class RegionMapping {
    private Map<Integer, List<Matrix>> featureMap;
    private double studyPoint = 0.01;
    private Map<Integer, Matrix> powerMatrixMap = new HashMap<>();//映射层的权重矩阵
    private int times = 200;

    public RegionMapping(TempleConfig templeConfig) {
        this.featureMap = templeConfig.getKnn().getFeatureMap();
    }

    public void detection() throws Exception {//对模型进行检查,先对当前模型最相近的三个模型进行检出
        Map<Integer, List<DistBody>> map = new HashMap<>();
        DistSort distSort = new DistSort();
        for (Map.Entry<Integer, List<Matrix>> entry : featureMap.entrySet()) {
            int key = entry.getKey();
            List<Matrix> myFeature = entry.getValue();
            //记录 哪个类别 每个类别距离这个类别的最小距离
            List<DistBody> distBodies = new ArrayList<>();
            map.put(key, distBodies);
            for (Map.Entry<Integer, List<Matrix>> entrySon : featureMap.entrySet()) {
                int type = entrySon.getKey();
                if (key != type) {//当前其余类别
                    DistBody distBody = new DistBody();
                    List<Matrix> features = entrySon.getValue();//其余类别的所有特征
                    double minError = -1;
                    for (Matrix myMatrix : myFeature) {//待验证类别所有特征
                        for (Matrix otherFeature : features) {
                            double dist = MatrixOperation.getEDistByMatrix(myMatrix, otherFeature);
                            if (minError < 0 || dist < minError) {
                                minError = dist;
                            }
                        }
                    }
                    //当前类别遍历结束
                    distBody.setId(type);
                    distBody.setDist(minError);
                    distBodies.add(distBody);
                }
            }
        }
        //进行排序
        for (Map.Entry<Integer, List<DistBody>> entry : map.entrySet()) {
            List<DistBody> list = entry.getValue();
            Collections.sort(list, distSort);
        }
        adjustWeight(map, 3);
        test(12, map, 3);
//        int testType = 5;
//        List<DistBody> testBody = map.get(testType);
//        Matrix matrix = powerMatrixMap.get(testType);
//        System.out.println(matrix.getString());
    }

    private Matrix mapping(Matrix feature, Matrix mapping) throws Exception {
        int y = feature.getY();
        if (y == mapping.getY()) {
            Matrix matrix = new Matrix(1, y);
            for (int i = 0; i < y; i++) {
                double nub = feature.getNumber(0, i) * mapping.getNumber(0, i);
                matrix.setNub(0, i, nub);
            }
            return matrix;
        } else {
            throw new Exception("matrix is not equals");
        }
    }

    private void test(int key, Map<Integer, List<DistBody>> map, int nub) throws Exception {
        Matrix matrixMapping = powerMatrixMap.get(key);//映射
        List<Matrix> rightFeature = featureMap.get(key);//正确的特征
        System.out.println("正确特征====================");
        for (Matrix matrix : rightFeature) {
            System.out.println(matrix.getString());
        }
        int y = rightFeature.get(0).getY();
        Matrix featureMatrix = getFeatureAvg(rightFeature, y);//均值
        List<DistBody> distBodies = map.get(key);
        List<Matrix> wrongFeature = new ArrayList<>();
        for (int i = 0; i < nub; i++) {
            DistBody distBody = distBodies.get(i);
            int id = distBody.getId();//相近的特征id
            wrongFeature.addAll(featureMap.get(id));
        }
        System.out.println("错误特征=============");
        for (Matrix matrix : wrongFeature) {
            System.out.println(matrix.getString());
        }
        //做验证
        double minOtherDist = -1;//其余特征最小距离
        for (Matrix wrongMatrix : wrongFeature) {
            Matrix wrongMapping = mapping(wrongMatrix, matrixMapping);
            double dist = MatrixOperation.getEDistByMatrix(featureMatrix, wrongMapping);
            System.out.println("异类距离：" + dist);
            if (minOtherDist < 0 || dist < minOtherDist) {
                minOtherDist = dist;
            }
        }
        System.out.println("异类最小距离：" + minOtherDist);
        for (Matrix rightMatrix : rightFeature) {
            Matrix rightMapping = mapping(rightMatrix, matrixMapping);
            double dist = MatrixOperation.getEDistByMatrix(featureMatrix, rightMapping);
            System.out.println("同类距离：" + dist);
        }
    }

    private void adjustWeight(Map<Integer, List<DistBody>> map, int nub) throws Exception {//进行权重调整
        for (Map.Entry<Integer, List<DistBody>> entry : map.entrySet()) {
            int key = entry.getKey();//当前类别的id
            List<Matrix> rightFeature = featureMap.get(key);//正确的特征
            List<DistBody> distBodies = entry.getValue();
            List<Matrix> wrongFeature = new ArrayList<>();
            for (int i = 0; i < nub; i++) {
                DistBody distBody = distBodies.get(i);
                int id = distBody.getId();//相近的特征id
                wrongFeature.addAll(featureMap.get(id));
            }
            //对每个分类的特征图进行权重调整
            updatePower(wrongFeature, rightFeature, key);
        }
    }

    //调整权重矩阵
    private void updatePower(List<Matrix> wrongFeature, List<Matrix> rightFeature, int key) throws Exception {
        int size = wrongFeature.size();
        int y = rightFeature.get(0).getY();
        //特征均值向量
        Matrix featureMatrix = getFeatureAvg(rightFeature, y);
        Matrix powerMatrix = new Matrix(1, y);
        powerMatrixMap.put(key, powerMatrix);
        Random random = new Random();
        for (int j = 0; j < times; j++) {
            for (int i = 0; i < size; i++) {
                Matrix feature = rightFeature.get(random.nextInt(rightFeature.size()));
                Matrix noFeature = wrongFeature.get(i);
                powerDeflection(feature, featureMatrix, true, powerMatrix);
                powerDeflection(noFeature, featureMatrix, false, powerMatrix);
            }
        }
        end();
    }

    private void end() throws Exception {
        for (Map.Entry<Integer, Matrix> entry : powerMatrixMap.entrySet()) {
            Matrix powerMatrix = entry.getValue();
            int y = powerMatrix.getY();
            double min = 0;
            for (int i = 0; i < y; i++) {
                double nub = powerMatrix.getNumber(0, i);
                if (nub < min) {
                    min = nub;
                }
            }
            //获取最小值完毕
            for (int i = 0; i < y; i++) {
                double nub = powerMatrix.getNumber(0, i);
                powerMatrix.setNub(0, i, nub - min);
            }
        }
    }

    private void powerDeflection(Matrix matrix1, Matrix matrix2, boolean polymerization,
                                 Matrix powerMatrix) throws Exception {
        int y = matrix1.getY();
        for (int i = 0; i < y; i++) {
            double sub = Math.abs(matrix1.getNumber(0, i) - matrix2.getNumber(0, i))
                    * studyPoint;
            double power = powerMatrix.getNumber(0, i);//当前矩阵中的权值
            if (polymerization) {//同类聚合 聚合是减
                power = power - sub;
            } else {//异类离散
                power = power + sub;
            }
            powerMatrix.setNub(0, i, power);
        }
    }

    private Matrix getFeatureAvg(List<Matrix> rightFeature, int size) throws Exception {//求特征均值
        Matrix feature = new Matrix(1, size);
        int nub = rightFeature.size();
        for (int i = 0; i < nub; i++) {
            Matrix matrix = rightFeature.get(i);
            for (int j = 0; j < size; j++) {
                double sigma = matrix.getNumber(0, j) + feature.getNumber(0, j);
                feature.setNub(0, j, sigma);
            }
        }
        MatrixOperation.mathDiv(feature, nub);
        return feature;
    }
}
