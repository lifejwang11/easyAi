package org.wlld.imageRecognition.border;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.tools.ArithUtil;

import java.util.*;

/**
 * @author lidapeng
 * @description //k均值聚类工具
 * @date 10:14 上午 2020/2/4
 */
public class KClustering {
    private List<Box> matrixList = new ArrayList<>();//聚类集合
    private int length;//向量长度(模型需要返回)
    private int speciesQuantity;//种类数量(模型需要返回)
    private Matrix[] matrices;//均值K模型(模型需要返回)
    private Map<Integer, List<Box>> clusterMap = new HashMap<>();//簇
    private Map<Integer, Box> positionMap = new HashMap<>();//聚类K均值结果(需要返回)
    private boolean isReady = false;

    public void setLength(int length) {
        this.length = length;
    }

    public void setSpeciesQuantity(int speciesQuantity) {
        this.speciesQuantity = speciesQuantity;
    }

    public void setMatrices(Matrix[] matrices) {
        this.matrices = matrices;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public int getLength() {
        return length;
    }

    public int getSpeciesQuantity() {
        return speciesQuantity;
    }

    public Matrix[] getMatrices() {
        return matrices;
    }

    public boolean isReady() {
        return isReady;
    }

    public Map<Integer, Box> getPositionMap() throws Exception {
        if (!isReady) {
            throw new Exception("not ready");
        }
        return positionMap;
    }

    public KClustering(int speciesQuantity) {
        this.speciesQuantity = speciesQuantity;//聚类的数量
        matrices = new Matrix[speciesQuantity];
        for (int i = 0; i < speciesQuantity; i++) {
            clusterMap.put(i, new ArrayList<>());
        }
    }

    public void setMatrixList(Box matrixBody) throws Exception {
        if (matrixBody.getMatrix().isVector() && matrixBody.getMatrix().isRowVector()) {
            Matrix matrix = matrixBody.getMatrix();
            if (matrixList.size() == 0) {
                matrixList.add(matrixBody);
                length = matrix.getY();
            } else {
                if (length == matrix.getY()) {
                    matrixList.add(matrixBody);
                } else {
                    throw new Exception("vector length is different");
                }
            }
        } else {
            throw new Exception("this matrix is not vector or rowVector");
        }
    }

    private Matrix[] averageMatrix() throws Exception {
        Matrix[] matrices2 = new Matrix[speciesQuantity];//待比较均值K
        for (Box matrixBody : matrixList) {//遍历当前集合
            Matrix matrix = matrixBody.getMatrix();
            double min = -1;
            int id = 0;
            for (int i = 0; i < matrices.length; i++) {
                double dist = MatrixOperation.getEDist(matrix, matrices[i]);
                if (min == -1 || dist < min) {
                    min = dist;
                    id = i;
                }
            }
            List<Box> matrixList1 = clusterMap.get(id);
            matrixList1.add(matrixBody);
        }

        //重新计算均值
        for (Map.Entry<Integer, List<Box>> entry : clusterMap.entrySet()) {
            Matrix matrix = average(entry.getValue());
            matrices2[entry.getKey()] = matrix;
        }
        return matrices2;
    }

    private Matrix averagePosition(List<Box> boxes) throws Exception {
        Matrix matrix = new Matrix(1, 4);
        if (boxes.size() > 0) {
            double nub = ArithUtil.div(1, boxes.size());
            for (Box box : boxes) {
                matrix = MatrixOperation.add(matrix, box.getMatrixPosition());
            }
            MatrixOperation.mathMul(matrix, nub);
        }
        return matrix;
    }

    private void position() throws Exception {
        for (Map.Entry<Integer, List<Box>> entry : clusterMap.entrySet()) {
            List<Box> boxList = entry.getValue();
            int key = entry.getKey();
            Box box = new Box();
            Matrix position = averagePosition(boxList);
            Matrix kMatrix = matrices[key];
            box.setMatrix(kMatrix);
            box.setMatrixPosition(position);
            positionMap.put(key, box);
        }
    }

    private void clear() {
        for (Map.Entry<Integer, List<Box>> entry : clusterMap.entrySet()) {
            entry.getValue().clear();
        }
    }

    private Matrix average(List<Box> matrixList) throws Exception {//进行矩阵均值计算
        Matrix matrix = new Matrix(1, length);
        if (matrixList.size() > 0) {
            double nub = ArithUtil.div(1, matrixList.size());
            for (Box matrixBody1 : matrixList) {
                matrix = MatrixOperation.add(matrix, matrixBody1.getMatrix());
            }
            MatrixOperation.mathMul(matrix, nub);
        }
        return matrix;
    }


    public void start() throws Exception {//开始聚类
        if (matrixList.size() > 1) {
            Random random = new Random();
            for (int i = 0; i < matrices.length; i++) {//初始化均值向量
                int index = random.nextInt(matrixList.size());
                //要进行深度克隆
                matrices[i] = matrixList.get(index).getMatrix();
            }
            //进行两者的比较
            boolean isEqual;
            do {
                Matrix[] matrices2 = averageMatrix();
                isEqual = equals(matrices, matrices2);
                if (!isEqual) {
                    matrices = matrices2;
                    clear();
                }
            }
            while (!isEqual);
            //聚类结束，进行坐标均值矩阵计算
            position();
            isReady = true;
        } else {
            throw new Exception("matrixList number less than 2");
        }
    }

    public boolean equals(Matrix[] matrices1, Matrix[] matrices2) throws Exception {
        boolean isEquals = true;
        for (int i = 0; i < matrices1.length; i++) {
            Matrix matrix1 = matrices1[i];
            Matrix matrix2 = matrices2[i];
            for (int j = 0; j < length; j++) {
                if (matrix1.getNumber(0, j) != matrix2.getNumber(0, j)) {
                    isEquals = false;
                    break;
                }
            }
            if (!isEquals) {
                break;
            }
        }
        return isEquals;
    }
}
