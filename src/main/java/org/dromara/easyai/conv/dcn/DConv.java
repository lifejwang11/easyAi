package org.dromara.easyai.conv.dcn;

import org.dromara.easyai.batchNerve.*;
import org.dromara.easyai.config.RZ;
import org.dromara.easyai.function.Sigmoid;
import org.dromara.easyai.function.Tanh;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixList;
import org.dromara.easyai.matrixTools.MatrixOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @time 2026/6/3 08:54
 * @des 可变形卷积
 */
public class DConv {
    private final BatchNerveManager batchNerveManager;
    private final DCNErrorBack dcnBack = new DCNErrorBack();
    private final int kerSize;//卷积核大小
    private final List<List<DcnBody>> allDcnBodyList = new ArrayList<>();
    private Matrix studyFeature;
    private final MatrixOperation matrixOperation = new MatrixOperation();


    public DConv(BatchNerveConfig batchNerveConfig, int kerSize) throws Exception {
        int powerSize = kerSize * kerSize;
        batchNerveConfig.setConcatenate(true);
        batchNerveConfig.setOutSize(powerSize * 3);
        batchNerveConfig.setInputSize(powerSize);
        batchNerveConfig.setHiddenSize(1);
        batchNerveConfig.setSoftMax(false);
        batchNerveConfig.setAuto(false);
        batchNerveConfig.setDeep(1);
        batchNerveConfig.setGMaxTh(1);
        batchNerveConfig.setShowLog(false);
        batchNerveConfig.setInitParameter(false);
        batchNerveConfig.setRegularModel(RZ.NOT_RZ);
        batchNerveConfig.setRegular(0);
        batchNerveConfig.setStudyRate(batchNerveConfig.getStudyRate() * 0.1f);
        this.kerSize = kerSize;
        batchNerveManager = new BatchNerveManager(batchNerveConfig, new Tanh(), dcnBack);
    }

    public Matrix movePosition(Matrix feature, long eventID) throws Exception {
        BatchInputBlock batchInputBlock = batchNerveManager.getInputBlock();
        DCNBack back = new DCNBack();
        Sigmoid sigmoid = new Sigmoid();
        List<FeatureBody> featureBodies = baisMatrix(feature);
        batchInputBlock.postMessage(featureBodies, false, back, eventID, null);
        List<Matrix> outMatrixList = back.getMatrixList();
        DcnStudyOut dcnStudyOut = positionBais(outMatrixList, feature, sigmoid, false);
        return dcnStudyOut.getFeature();
    }

    public List<Matrix> movePositionByList(List<Matrix> featureList, long eventID) throws Exception {
        List<Matrix> outMatrixList = new ArrayList<>();
        for (Matrix matrix : featureList) {
            outMatrixList.add(movePosition(matrix, eventID));
        }
        return outMatrixList;
    }

    public BatchNerveModel getModel() {
        return batchNerveManager.getModel();
    }

    public void insertModel(BatchNerveModel batchNerveModel) {
        batchNerveManager.insertModel(batchNerveModel);
    }

    public List<Matrix> study(List<Matrix> featureList, long eventID) throws Exception {
        allDcnBodyList.clear();
        BatchInputBlock batchInputBlock = batchNerveManager.getInputBlock();
        DCNBack back = new DCNBack();
        Sigmoid sigmoid = new Sigmoid();
        List<Matrix> outList = new ArrayList<>();
        for (Matrix matrix : featureList) {
            List<FeatureBody> featureBodies = baisMatrix(matrix);
            batchInputBlock.postMessage(featureBodies, true, back, eventID, null);
            List<Matrix> baisList = back.getMatrixList();//偏移集合
            DcnStudyOut dcnStudyOut = positionBais(baisList, matrix, sigmoid, true);
            outList.add(dcnStudyOut.getFeature());
            allDcnBodyList.add(dcnStudyOut.getDcnBodyList());
        }
        return outList;
    }

    public List<Matrix> backError(List<Matrix> errorMatrixList) throws Exception {
        int size = errorMatrixList.size();
        Sigmoid sigmoid = new Sigmoid();
        List<Matrix> nextErrorMatrixList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Matrix errorMatrix = errorMatrixList.get(i);
            List<DcnBody> dcnBodyList = allDcnBodyList.get(i);
            Matrix error = error(errorMatrix, dcnBodyList, sigmoid);
            nextErrorMatrixList.add(error);
        }
        return nextErrorMatrixList;
    }

    private Matrix error(Matrix errorMatrix, List<DcnBody> dcnBodyList, Sigmoid sigmoid) throws Exception {
        int xSize = errorMatrix.getX();
        int powerSize = kerSize * kerSize;
        Matrix myErrorMatrix = new Matrix(studyFeature.getX(), studyFeature.getY());
        List<Matrix> baisErrorMatrixList = new ArrayList<>();
        for (int i = 0; i < xSize; i++) {//遍历这张特征图的每一行
            Matrix rowError = errorMatrix.getRow(i);
            List<DcnBody> dcnBodies = dcnBodyList.subList(i * powerSize, i * powerSize + powerSize);
            Matrix baisErrorMatrix = new Matrix(1, powerSize * 3);
            baisErrorMatrixList.add(baisErrorMatrix);
            for (int j = 0; j < powerSize; j++) {
                int index = j * 3;
                DcnBody dcnBody = dcnBodies.get(j);
                float error = rowError.getValue(0, j);//误差
                float nextX = dcnBody.getNextX();
                float nextY = dcnBody.getNextY();
                float m = dcnBody.getM();
                float mg = sigmoid.functionG(m);
                int x = (int) nextX;
                int y = (int) nextY;
                DcnErrorBody dcnErrorBody = backErrorBilinear(nextX, nextY, m, dcnBody.getBilinearValue(), error);
                insertMatrixError(myErrorMatrix, dcnErrorBody, x, y);
                baisErrorMatrix.setValue(0, index, dcnErrorBody.getErrorNextX());
                baisErrorMatrix.setValue(0, index + 1, dcnErrorBody.getErrorNextY());
                baisErrorMatrix.setValue(0, index + 2, mg * dcnErrorBody.getErrorM());
            }
        }
        batchNerveManager.getOutBlock().backError(baisErrorMatrixList);
        Matrix error = backRealErrorMatrix(dcnBack.getNextErrorMatrixList());
        return matrixOperation.add(myErrorMatrix, error);
    }

    private Matrix backRealErrorMatrix(List<Matrix> nextErrorMatrixList) throws Exception {
        int size = nextErrorMatrixList.size();
        MatrixList matrixList = null;
        for (int i = 0; i < size; i++) {
            Matrix matrix = nextErrorMatrixList.get(i);
            if (i == 0) {
                matrixList = new MatrixList(matrix, true);
            } else {
                matrixList.add(matrix);
            }
        }
        Matrix im2colMatrix = matrixList.getMatrix();
        return matrixOperation.reverseIm2col(im2colMatrix, kerSize, 1, studyFeature.getX(),
                studyFeature.getY());
    }

    private void insertMatrixError(Matrix myErrorMatrix, DcnErrorBody dcnErrorBody, int x, int y) {
        int maxX = myErrorMatrix.getX();
        int maxY = myErrorMatrix.getY();
        float f00, f01, f10, f11;
        if (pass(x, y, maxX, maxY)) {//左上
            f00 = myErrorMatrix.getValue(x, y) + dcnErrorBody.getErrorF00();
            myErrorMatrix.setValue(x, y, f00);
        }
        if (pass(x + 1, y, maxX, maxY)) {//左下
            f01 = myErrorMatrix.getValue(x + 1, y) + dcnErrorBody.getErrorF01();
            myErrorMatrix.setValue(x + 1, y, f01);
        }
        if (pass(x, y + 1, maxX, maxY)) {//右上
            f10 = myErrorMatrix.getValue(x, y + 1) + dcnErrorBody.getErrorF10();
            myErrorMatrix.setValue(x, y + 1, f10);
        }
        if (pass(x + 1, y + 1, maxX, maxY)) {//右下
            f11 = myErrorMatrix.getValue(x + 1, y + 1) + dcnErrorBody.getErrorF11();
            myErrorMatrix.setValue(x + 1, y + 1, f11);
        }
    }

    private DcnStudyOut positionBais(List<Matrix> baisList, Matrix feature, Sigmoid sigmoid, boolean study) throws Exception {
        int powerSize = kerSize * kerSize;
        int maxY = feature.getY() - (kerSize - 1);
        int size = baisList.size();
        MatrixList matrixList = null;
        List<DcnBody> dcnBodies = new ArrayList<>();
        DcnStudyOut dcnStudyOut = new DcnStudyOut();
        dcnStudyOut.setDcnBodyList(dcnBodies);
        if (study) {
            studyFeature = feature;
        }
        for (int i = 0; i < size; i++) {//遍历一张图的每一行
            Matrix baisMatrix = baisList.get(i);
            int startX = i / maxY;
            int startY = i % maxY;
            Matrix matrix = new Matrix(1, powerSize);
            for (int j = 0; j < powerSize; j++) {//遍历一行中的每个坐标
                int index = j * 3;
                int realX = startX + j / kerSize;//该组偏移值对应的行坐标
                int realY = startY + j % kerSize;//该组偏移值对应的列坐标
                float offerX = baisMatrix.getValue(0, index);
                float offerY = baisMatrix.getValue(0, index + 1);
                float nextX = realX + offerX;//偏移后X结果 需要保存
                float nextY = realY + offerY;//偏移后Y结果 需要保存
                float m = sigmoid.function(baisMatrix.getValue(0, index + 2));//需要保存
                DcnBody dcnBody = null;
                if (study) {
                    dcnBody = new DcnBody();
                    dcnBody.setNextX(nextX);
                    dcnBody.setNextY(nextY);
                    dcnBody.setM(m);
                    dcnBodies.add(dcnBody);
                }
                float p = bilinear(nextX, nextY, m, feature, dcnBody);
                matrix.setValue(0, j, p);
            }
            if (i == 0) {
                matrixList = new MatrixList(matrix, true);
            } else {
                matrixList.add(matrix);
            }
        }
        dcnStudyOut.setFeature(matrixList.getMatrix());
        return dcnStudyOut;
    }

    private DcnErrorBody backErrorBilinear(float nextX, float nextY, float m, float value, float error) {
        int x = (int) nextX;
        int y = (int) nextY;
        float subX = nextX - x;
        float subY = nextY - y;
        int maxX = studyFeature.getX();
        int maxY = studyFeature.getY();
        float f00 = 0, f01 = 0, f10 = 0, f11 = 0;
        if (pass(x, y, maxX, maxY)) {//左上
            f00 = studyFeature.getValue(x, y);
        }
        if (pass(x + 1, y, maxX, maxY)) {//左下
            f01 = studyFeature.getValue(x + 1, y);
        }
        if (pass(x, y + 1, maxX, maxY)) {//右上
            f10 = studyFeature.getValue(x, y + 1);
        }
        if (pass(x + 1, y + 1, maxX, maxY)) {//右下
            f11 = studyFeature.getValue(x + 1, y + 1);
        }
        DcnErrorBody dcnErrorBody = new DcnErrorBody();
        float myError = m * error;
        float errorM = value * error;
        float errorSubX = (subY * (f00 - f01 - f10 + f11) - f00 + f10) * myError;
        float errorSubY = (subX * (f00 - f01 - f10 + f11) - f00 + f01) * myError;
        float errorNextX = normError(errorSubX, subX);
        float errorNextY = normError(errorSubY, subY);
        dcnErrorBody.setErrorM(errorM);
        dcnErrorBody.setErrorNextX(errorNextX);
        dcnErrorBody.setErrorNextY(errorNextY);
        dcnErrorBody.setErrorF00((1 - subX) * (1 - subY) * myError);
        dcnErrorBody.setErrorF01((1 - subX) * subY * myError);
        dcnErrorBody.setErrorF10(subX * (1 - subY) * myError);
        dcnErrorBody.setErrorF11(subX * subY * myError);
        return dcnErrorBody;
    }

    private float normError(float errorSub, float sub) {//梯度裁剪
        if (errorSub > 0) {
            if (errorSub >= 1 - sub) {
                errorSub = 1 - sub - 0.000001f;
            }
        } else {
            if (errorSub <= -sub) {
                errorSub = -sub + 0.000001f;
            }
        }
        return errorSub;
    }

    private float bilinear(float nextX, float nextY, float m, Matrix feature, DcnBody dcnBody) {//双线性插值
        int x = (int) nextX;
        int y = (int) nextY;
        float subX = nextX - x;
        float subY = nextY - y;
        int maxX = feature.getX();
        int maxY = feature.getY();
        float f00 = 0, f01 = 0, f10 = 0, f11 = 0;
        if (pass(x, y, maxX, maxY)) {//左上
            f00 = feature.getValue(x, y);
        }
        if (pass(x + 1, y, maxX, maxY)) {//左下
            f01 = feature.getValue(x + 1, y);
        }
        if (pass(x, y + 1, maxX, maxY)) {//右上
            f10 = feature.getValue(x, y + 1);
        }
        if (pass(x + 1, y + 1, maxX, maxY)) {//右下
            f11 = feature.getValue(x + 1, y + 1);
        }
        float value = (1 - subX) * (1 - subY) * f00 + (1 - subX) * subY * f01 + subX * (1 - subY) * f10
                + subX * subY * f11;
        if (dcnBody != null) {
            dcnBody.setBilinearValue(value);
        }
        return value * m;
    }

    private boolean pass(int x, int y, int maxX, int maxY) {
        return x >= 0 && y >= 0 && x < maxX && y < maxY;
    }

    private List<FeatureBody> baisMatrix(Matrix feature) {
        List<FeatureBody> featureBodies = new ArrayList<>();
        int maxX = feature.getX() - (kerSize - 1);
        int maxY = feature.getY() - (kerSize - 1);
        int matrixSize = kerSize * kerSize;
        for (int i = 0; i < maxX; i++) {
            for (int j = 0; j < maxY; j++) {
                FeatureBody featureBody = new FeatureBody();
                Matrix myMatrix = new Matrix(1, matrixSize);
                toVector(feature, i, j, myMatrix);
                featureBody.setFeature(myMatrix);
                featureBodies.add(featureBody);
            }
        }
        return featureBodies;
    }

    private void toVector(Matrix feature, int startX, int startY, Matrix myMatrix) {
        int endX = startX + kerSize;
        int endY = startY + kerSize;
        int t = 0;
        for (int i = startX; i < endX; i++) {
            for (int j = startY; j < endY; j++) {
                myMatrix.setValue(0, t, feature.getValue(i, j));
                t++;
            }
        }
    }
}
