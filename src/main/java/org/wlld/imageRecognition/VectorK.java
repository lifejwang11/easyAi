package org.wlld.imageRecognition;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.tools.ArithUtil;

import java.util.*;

public class VectorK {
    private Map<Integer, List<Matrix>> matrixMap = new HashMap<>();
    private Map<Integer, Matrix> matrixK = new HashMap<>();//这个作为模型拿出来
    private int length;

    public VectorK(int length) {
        this.length = length;
    }

    public Map<Integer, Matrix> getMatrixK() {
        return matrixK;
    }

    public void insertMatrix(int type, Matrix matrix) throws Exception {
        if (matrix.isRowVector() && matrix.getY() == length) {
            if (matrixMap.containsKey(type)) {
                List<Matrix> matrixList = matrixMap.get(type);
                matrixList.add(matrix);
            } else {
                List<Matrix> list = new ArrayList<>();
                list.add(matrix);
                matrixMap.put(type, list);
            }
        } else {
            throw new Exception("MATRIX IS NOT RIGHT");
        }
    }

    private Matrix sigma(List<Matrix> matrixList) throws Exception {
        Matrix matrix = new Matrix(1, length);
        for (Matrix matrix1 : matrixList) {
            matrix = MatrixOperation.add(matrix, matrix1);
        }
        MatrixOperation.mathMul(matrix, ArithUtil.div(1, matrixList.size()));
        return matrix;
    }

    public void study() throws Exception {
        for (Map.Entry<Integer, List<Matrix>> entry : matrixMap.entrySet()) {
            List<Matrix> matrixList = entry.getValue();
            Matrix matrix = sigma(matrixList);
            matrixK.put(entry.getKey(), matrix);
        }
    }

    public Map<Integer, List<Double>> getKMatrix() throws Exception {
        Map<Integer, List<Double>> matrixList = new HashMap<>();
        for (Map.Entry<Integer, Matrix> entry : matrixK.entrySet()) {
            List<Double> list = MatrixOperation.rowVectorToList(entry.getValue());
            matrixList.put(entry.getKey(), list);
        }
        return matrixList;
    }

    public void insertKMatrix(Map<Integer, List<Double>> matrixList) throws Exception {
        for (Map.Entry<Integer, List<Double>> entry : matrixList.entrySet()) {
            Matrix matrix = MatrixOperation.listToRowVector(entry.getValue());
            matrixK.put(entry.getKey(), matrix);
        }
    }
}
