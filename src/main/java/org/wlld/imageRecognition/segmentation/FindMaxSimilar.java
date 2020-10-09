package org.wlld.imageRecognition.segmentation;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.imageRecognition.ThreeChannelMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 寻找相似度最大的选区
 */
public class FindMaxSimilar {

    public void findMaxSimilar(ThreeChannelMatrix threeChannelMatrix, int size) throws Exception {
        Map<Integer, ThreeChannelMatrix> threeChannelMatrices = new HashMap<>();
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        Matrix matrixG = threeChannelMatrix.getMatrixG();
        Matrix matrixB = threeChannelMatrix.getMatrixB();
        Matrix matrixRGB = threeChannelMatrix.getMatrixRGB();
        int x = matrixR.getX();
        int y = matrixR.getY();
        System.out.println("初始区域数量:" + (x * y));
        int index = 0;
        for (int i = 0; i <= x - size; i += size) {
            for (int j = 0; j <= y - size; j += size) {
                ThreeChannelMatrix threeChannelMatrix1 = new ThreeChannelMatrix();
                Matrix bodyR = matrixR.getSonOfMatrix(i, j, size, size);
                Matrix bodyG = matrixG.getSonOfMatrix(i, j, size, size);
                Matrix bodyB = matrixB.getSonOfMatrix(i, j, size, size);
                Matrix bodyRGB = matrixRGB.getSonOfMatrix(i, j, size, size);
                threeChannelMatrix1.setMatrixR(bodyR);
                threeChannelMatrix1.setMatrixG(bodyG);
                threeChannelMatrix1.setMatrixB(bodyB);
                threeChannelMatrix1.setMatrixRGB(bodyRGB);
                threeChannelMatrices.put(index, threeChannelMatrix1);
                index++;
            }
        }
        //切割完毕 ，开始寻找最大相似性
        for (Map.Entry<Integer, ThreeChannelMatrix> entry : threeChannelMatrices.entrySet()) {
            int key = entry.getKey();
            ThreeChannelMatrix threeChannelMatrix1 = entry.getValue();
            Matrix matrix1 = threeChannelMatrix1.getMatrixRGB();
            double minDist = -1;
            int similarId = 0;
            for (Map.Entry<Integer, ThreeChannelMatrix> entrySon : threeChannelMatrices.entrySet()) {
                int sonKey = entrySon.getKey();
                if (key != sonKey) {
                    Matrix matrix2 = entrySon.getValue().getMatrixRGB();
                    double dist = MatrixOperation.getEDistByMatrix(matrix1, matrix2);
                    if (minDist == -1 || dist < minDist) {
                        minDist = dist;
                        similarId = sonKey;
                    }
                }
            }
            threeChannelMatrix1.setSimilarId(similarId);
        }
        //最大相似性区域已经查找完毕,开始进行连线
        Map<Integer, List<Integer>> lineMap = line(threeChannelMatrices);
        //System.out.println("size:" + lineMap.size());
        int max = 0;
        int maxK = 0;
        for (Map.Entry<Integer, List<Integer>> entry : lineMap.entrySet()) {
            int key = entry.getKey();
            int nub = entry.getValue().size();
            if (nub > max) {
                max = nub;
                maxK = key;
            }
        }
        System.out.println("max:" + max);
        int key = lineMap.get(maxK).get(0);
        Matrix matrix = threeChannelMatrices.get(key).getMatrixR();
        System.out.println(matrix.getString());


    }

    private void merge(Map<Integer, List<Integer>> lineMap, Map<Integer, ThreeChannelMatrix> map,
                       int x, int y) throws Exception {
        Map<Integer, Matrix> map2 = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : lineMap.entrySet()) {
            int key = entry.getKey();
            Matrix matrixAll = new Matrix(x, y);
            List<Integer> list = entry.getValue();
            int len = list.size();
            for (int i = 0; i < len; i++) {
                ThreeChannelMatrix threeChannelMatrix = map.get(list.get(i));
                Matrix matrixRGB = threeChannelMatrix.getMatrixRGB();
                matrixAll = MatrixOperation.add(matrixAll, matrixRGB);
            }
            MatrixOperation.mathDiv(matrixAll, len);//矩阵数除
            map2.put(key, matrixAll);
        }

    }

    private Map<Integer, List<Integer>> line(Map<Integer, ThreeChannelMatrix> threeChannelMatrices) {//开始进行连线
        Map<Integer, List<Integer>> lineMap = new HashMap<>();
        for (Map.Entry<Integer, ThreeChannelMatrix> entry : threeChannelMatrices.entrySet()) {
            int key = entry.getKey();//当前进行连线的id
            ThreeChannelMatrix myThreeChannelMatrix = entry.getValue();
            boolean isLine = myThreeChannelMatrix.isLine();//是否被连线
            int upIndex = key;//上一个进行连线的id
            if (!isLine) {//可以进行连线
                List<Integer> list = new ArrayList<>();
                lineMap.put(key, list);
                list.add(key);
                myThreeChannelMatrix.setLine(true);
                boolean line;
                do {
                    int similarId = myThreeChannelMatrix.getSimilarId();//距离它最近的id
                    ThreeChannelMatrix threeChannelMatrix = threeChannelMatrices.get(similarId);
                    line = threeChannelMatrix.isLine();
                    if (!line) {//进行连线
                        list.add(similarId);
                        threeChannelMatrix.setLine(true);
                        //如果当前被连线的矩阵的最近id为连线者本身，则连线后停止遍历
                        if (upIndex == threeChannelMatrix.getSimilarId()) {//停止连线
                            line = true;
                        } else {//继续连线z
                            upIndex = similarId;
                            myThreeChannelMatrix = threeChannelMatrix;
                        }
                    }
                } while (!line);
            }
        }
        return lineMap;
    }
}
