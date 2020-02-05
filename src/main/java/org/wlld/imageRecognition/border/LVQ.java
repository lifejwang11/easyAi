package org.wlld.imageRecognition.border;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.tools.ArithUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author lidapeng
 * @description LVQ 向量量化
 * @date 5:36 下午 2020/2/4
 */
public class LVQ {
    private int typeNub;//原型聚类个数,即分类个数
    private MatrixBody[] model = new MatrixBody[typeNub];//原型向量
    private List<MatrixBody> matrixList = new ArrayList<>();
    private double studyPoint = 0.1;//量化学习率
    private int length;//向量长度

    public LVQ(int typeNub) {
        this.typeNub = typeNub;
    }

    public MatrixBody[] getModel() {
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

    private double study() throws Exception {
        double error = 0;
        for (MatrixBody matrixBody : matrixList) {
            Matrix matrix = matrixBody.getMatrix();//特征向量
            long type = matrixBody.getId();//类别
            for (int i = 0; i < typeNub; i++) {
                MatrixBody modelBody = model[i];
                Matrix modelMatrix = modelBody.getMatrix();
                long id = modelBody.getId();
                boolean isRight = id == type;//类别是否相同
                //对矩阵进行修正
                Matrix matrix1 = op(matrix, modelMatrix, isRight);
                //修正矩阵与原矩阵的范数差
                double dist = vectorEqual(modelMatrix, matrix1);
                //将修正后的向量进行赋值
                modelBody.setMatrix(matrix1);
                //统计变化值
                error = ArithUtil.add(error, dist);
            }
        }
        return error;
    }

    //比较两个向量之间的范数差
    private double vectorEqual(Matrix matrix1, Matrix matrix2) throws Exception {
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
                matrix.setNub(0, j, random.nextInt(10));
            }
            model[i] = matrixBody;
        }
        //初始化完成
        for (int k = 0; k < 1000; k++) {
            double error = study();
            System.out.println("error==" + error);
        }
    }
}
