package org.wlld.imageRecognition;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.tools.ArithUtil;

import java.util.*;

public class VectorK {
    private Map<Integer, List<Matrix>> matrixMap = new HashMap<>();
    private Map<Integer, Matrix> matrixK = new HashMap<>();
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

    private Matrix mind(List<Matrix> matrixList) throws Exception {//拿中位数
        Matrix matrix = new Matrix(1, length);
        List<List<Double>> lists = new ArrayList<>();
        for (Matrix matrix1 : matrixList) {
            for (int i = 0; i < matrix1.getY(); i++) {
                if (lists.size() <= i) {
                    lists.add(new ArrayList<>());
                }
                List<Double> list = lists.get(i);
                list.add(matrix1.getNumber(0, i));
                Collections.sort(list);
            }
        }
        for (int i = 0; i < length; i++) {
            List<Double> list = lists.get(i);
            int index = list.size() / 2;
            matrix.setNub(0, i, list.get(index));
        }
        return matrix;
    }

    public void study() throws Exception {
        for (Map.Entry<Integer, List<Matrix>> entry : matrixMap.entrySet()) {
            List<Matrix> matrixList = entry.getValue();
            Matrix matrix = sigma(matrixList);
            matrixK.put(entry.getKey(), matrix);
        }
    }
}
