package org.wlld.tools;

import org.wlld.matrixTools.Matrix;
import org.wlld.matrixTools.MatrixOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author lidapeng
 * @description LVQ 向量量化
 * @date 5:36 下午 2020/2/4
 */
public class LVQ {
    private int typeNub;//原型聚类个数,即分类个数(需要模型返回)
    private MatrixBody[] model;//原型向量(需要模型返回)
    private List<MatrixBody> matrixList = new ArrayList<>();
    private double studyPoint = 0.01;//量化学习率
    private int length;//向量长度(需要返回)
    private boolean isReady = false;
    private int lvqNub;

    public void setTypeNub(int typeNub) {
        this.typeNub = typeNub;
    }

    public void setModel(MatrixBody[] model) {
        this.model = model;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public boolean isReady() {
        return isReady;
    }

    public int getTypeNub() {
        return typeNub;
    }

    public int getLength() {
        return length;
    }

    public LVQ(int typeNub, int lvqNub, double studyPoint) {
        this.typeNub = typeNub;
        this.lvqNub = lvqNub;
        this.studyPoint = studyPoint;
        model = new MatrixBody[typeNub];
    }

    public MatrixBody[] getModel() throws Exception {
        if (!isReady) {
            throw new Exception("not study");
        }
        return model;
    }

    public void insertMatrixBody(MatrixBody matrixBody) throws Exception {
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

    private void study() throws Exception {
        for (MatrixBody matrixBody : matrixList) {
            Matrix matrix = matrixBody.getMatrix();//特征向量
            long type = matrixBody.getId();//类别
            double distEnd = 0;
            int id = 0;
            for (int i = 0; i < typeNub; i++) {
                MatrixBody modelBody = model[i];
                Matrix modelMatrix = modelBody.getMatrix();
                //修正矩阵与原矩阵的范数差
                double dist = vectorEqual(modelMatrix, matrix);
                if (distEnd == 0 || dist < distEnd) {
                    id = modelBody.getId();
                    distEnd = dist;
                }
            }
            MatrixBody modelBody = model[id];
            Matrix modelMatrix = modelBody.getMatrix();
            boolean isRight = id == type;
            Matrix matrix1 = op(matrix, modelMatrix, isRight);
            modelBody.setMatrix(matrix1);
        }
    }

    //比较两个向量之间的范数差
    public double vectorEqual(Matrix matrix1, Matrix matrix2) throws Exception {
        Matrix matrix = MatrixOperation.sub(matrix1, matrix2);
        return MatrixOperation.getNorm(matrix);
    }

    private Matrix op(Matrix matrix, Matrix modelMatrix, boolean isRight) throws Exception {
        Matrix matrix1 = MatrixOperation.sub(matrix, modelMatrix);
        MatrixOperation.mathMul(matrix1, studyPoint);
        Matrix matrix2;
        if (isRight) {
            matrix2 = MatrixOperation.add(modelMatrix, matrix1);
        } else {
            matrix2 = MatrixOperation.sub(modelMatrix, matrix1);
        }
        return matrix2;
    }

    public void start() throws Exception {//开始向量量化聚类
        Random random = new Random();
        for (int i = 0; i < typeNub; i++) {
            MatrixBody matrixBody = new MatrixBody();
            Matrix matrix = new Matrix(1, length);
            matrixBody.setMatrix(matrix);
            matrixBody.setId(i);
            for (int j = 0; j < length; j++) {
                matrix.setNub(0, j, random.nextDouble());
            }
            model[i] = matrixBody;
        }
        //初始化完成
        for (int i = 0; i < lvqNub; i++) {
            //System.out.println("================================");
            study();
        }
        isReady = true;
    }
}
