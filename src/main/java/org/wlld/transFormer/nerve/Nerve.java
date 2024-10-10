package org.wlld.transFormer.nerve;

import org.wlld.matrixTools.Matrix;
import org.wlld.config.RZ;
import org.wlld.i.ActiveFunction;
import org.wlld.i.OutBack;
import org.wlld.matrixTools.MatrixOperation;
import org.wlld.transFormer.LineBlock;
import org.wlld.transFormer.seflAttention.LayNorm;
import org.wlld.transFormer.seflAttention.MultiSelfAttention;

import java.util.*;

/**
 * @author lidapeng
 * 神经元，所有类别神经元都要继承的类，具有公用属性
 * &#064;date  9:36 上午 2019/12/21
 */
public abstract class Nerve {
    private final List<Nerve> son = new ArrayList<>();//轴突下一层的连接神经元
    private final List<Nerve> father = new ArrayList<>();//树突上一层的连接神经元
    protected LayNorm beforeLayNorm;//多头自注意力层
    protected LayNorm afterLayNorm;//多头自注意力层
    protected Matrix powerMatrix;//权重矩阵 作为模型取出
    private final int id;//同级神经元编号,注意在同层编号中ID应有唯一性
    private final int hiddenNerveNub;//隐层神经元个数
    private final int sensoryNerveNub;//输入神经元个数
    private final int outNerveNub;//输出神经元个数
    protected Map<Long, Matrix> reMatrixFeatures = new HashMap<>();
    protected String name;//该神经元所属类型
    protected Matrix featureMatrix;
    protected double E;//模板期望值
    protected double studyPoint;
    protected LineBlock lineBlock;//是否为最后线性层
    protected Matrix sigmaW;//对上一层权重与上一层梯度的积进行求和
    private int backNub = 0;//当前节点被反向传播的次数
    protected ActiveFunction activeFunction;
    protected Matrix outMatrix;
    protected int myUpNumber;//统计参数数量
    protected int depth;//所处深度
    private final int regularModel;//正则模式
    private final double regular;//正则系数
    private final MatrixOperation matrixOperation;

    public int getDepth() {
        return depth;
    }

    public void setBeforeLayNorm(LayNorm beforeLayNorm) {
        this.beforeLayNorm = beforeLayNorm;
    }

    public void setAfterLayNorm(LayNorm afterLayNorm) {
        this.afterLayNorm = afterLayNorm;
    }

    protected Nerve(int id, String name, double studyPoint, ActiveFunction activeFunction, int sensoryNerveNub,
                    int hiddenNerveNub, int outNerveNub, LineBlock lineBlock, int regularModel,
                    double regular, int coreNumber) throws Exception {//该神经元在同层神经元中的编号
        this.id = id;
        matrixOperation = new MatrixOperation(coreNumber);
        this.regular = regular;
        this.regularModel = regularModel;
        this.lineBlock = lineBlock;
        this.hiddenNerveNub = hiddenNerveNub;//隐层神经元个数
        this.sensoryNerveNub = sensoryNerveNub;//输入神经元个数
        this.outNerveNub = outNerveNub;//输出神经元个数
        this.name = name;
        this.studyPoint = studyPoint;
        this.activeFunction = activeFunction;
        initPower();//生成随机权重
    }

    public double[][] getModel() {
        return powerMatrix.getMatrix();
    }

    public void insertModel(double[][] modelPower) throws Exception {
        for (int i = 0; i < powerMatrix.getX(); i++) {
            for (int j = 0; j < powerMatrix.getY(); j++) {
                powerMatrix.setNub(i, j, modelPower[i][j]);
            }
        }
    }

    protected void sendMessage(long eventId, Matrix parameter, boolean isStudy, Matrix allFeature, OutBack outBack,
                               List<Integer> E, Matrix encoderFeature) throws Exception {
        if (!son.isEmpty()) {
            for (Nerve nerve : son) {
                nerve.input(eventId, parameter, isStudy, allFeature, outBack, E, encoderFeature);
            }
        }

    }


    private void backSendMessage(long eventId, Matrix errorMatrix, Matrix allError) throws Exception {//反向传播
        if (!father.isEmpty()) {//要先判定是不是可以继续往后传
            if (errorMatrix.getY() - 1 != father.size()) {
                throw new Exception("回传参数数量不一致!");
            }
            for (int i = 0; i < father.size(); i++) {
                father.get(i).backGetMessage(errorMatrix.getColumn(i), eventId, allError);
            }
        } else if (lineBlock != null) {//最后线性层
            lineBlock.backError(eventId, errorMatrix);
        } else {//从fnn往后传
            afterLayNorm.backErrorFromFNN(errorMatrix, eventId, allError);
        }
    }

    protected void input(long eventId, Matrix parameter, boolean isStudy, Matrix allFeature, OutBack outBack,
                         List<Integer> E, Matrix encoderFeature) throws Exception {//输入参数

    }

    protected void toOut(long eventId, Matrix parameter, boolean isStudy, OutBack outBack, List<Integer> E) throws Exception {

    }

    protected void sendOutMessage(long eventId, Matrix parameter, boolean isStudy, OutBack outBack, List<Integer> E) throws Exception {
        if (!son.isEmpty()) {
            for (Nerve nerve : son) {
                nerve.toOut(eventId, parameter, isStudy, outBack, E);
            }
        }
    }

    private void backGetMessage(Matrix parameter, long eventId, Matrix allError) throws Exception {//反向传播
        backNub++;
        if (sigmaW == null) {
            sigmaW = parameter;
        } else {
            sigmaW = matrixOperation.add(sigmaW, parameter);
        }
        if (backNub == outNerveNub) {//进行新的梯度计算
            backNub = 0;
            if (activeFunction != null) {
                for (int i = 0; i < sigmaW.getX(); i++) {
                    double out = outMatrix.getNumber(i, 0);
                    double value = activeFunction.functionG(out) * sigmaW.getNumber(i, 0);
                    sigmaW.setNub(i, 0, value);
                }
            }
            updatePower(eventId, sigmaW, allError);//修改阈值
        }
    }


    protected void updatePower(long eventId, Matrix errorMatrix, Matrix allError) throws Exception {//修改阈值
        Matrix myError = matrixOperation.mathMulBySelf(errorMatrix, studyPoint);
        Matrix error = updateW(myError, errorMatrix);//更新本神经元参数与返回下层误差
        sigmaW = null;//求和结果归零
        backSendMessage(eventId, error, allError);
    }

    private Matrix getRegularizationMatrix() throws Exception {
        int size = powerMatrix.getX();
        double sigma = 0;
        for (int i = 0; i < size; i++) {
            double value = powerMatrix.getNumber(i, 0);
            if (regularModel == RZ.L1) {//l1正则化
                sigma = sigma + Math.abs(value);
            } else {
                sigma = sigma + Math.pow(value, 2);
            }
        }
        double param = sigma * regular * studyPoint;
        Matrix rzMatrix = new Matrix(powerMatrix.getX(), powerMatrix.getY());
        for (int i = 0; i < size; i++) {
            double value = powerMatrix.getNumber(i, 0);
            double re = 0.0;
            if (regularModel == RZ.L2) {
                re = param * -value;
            } else if (regularModel == RZ.L1) {
                if (value > 0) {
                    re = -param;
                } else if (value < 0) {
                    re = param;
                }
            }
            rzMatrix.setNub(i, 0, re);
        }
        return rzMatrix;
    }

    private Matrix updateW(Matrix errorMatrix, Matrix error) throws Exception {//
        Matrix rzMatrix = null;
        if (regularModel != RZ.NOT_RZ) {
            rzMatrix = getRegularizationMatrix();
        }
        Matrix subFeature = matrixOperation.matrixMulPd(error, featureMatrix, powerMatrix, true);
        Matrix subPower = matrixOperation.matrixMulPd(errorMatrix, featureMatrix, powerMatrix, false);
        if (regularModel != RZ.NOT_RZ) {
            powerMatrix = matrixOperation.add(powerMatrix, rzMatrix);//正则化抑制权重
        }
        powerMatrix = matrixOperation.add(powerMatrix, subPower);//更新权重
        return subFeature;
    }

    protected boolean insertMatrixParameter(long eventID, Matrix matrix) throws Exception {//reMatrixFeatures
        boolean allReady = false;
        Matrix feature;
        if (reMatrixFeatures.containsKey(eventID)) {
            Matrix myFeature = reMatrixFeatures.get(eventID);
            feature = matrixOperation.pushVector(myFeature, matrix, false);
        } else {
            feature = matrix;
        }
        reMatrixFeatures.put(eventID, feature);
        if (feature.getY() == myUpNumber) {
            allReady = true;
        } else if (feature.getY() > myUpNumber) {
            throw new Exception("接收矩阵参数数量异常");
        }
        return allReady;
    }

    protected Matrix opMatrix(Matrix feature, boolean isStudy) throws Exception {
        Matrix th = new Matrix(feature.getX(), 1);
        for (int i = 0; i < th.getX(); i++) {
            th.setNub(i, 0, 1D);
        }
        Matrix matrix = matrixOperation.pushVector(feature, th, false);
        Matrix sigma = matrixOperation.mulMatrix(matrix, powerMatrix);
        if (activeFunction != null) {
            for (int i = 0; i < sigma.getX(); i++) {
                double value = activeFunction.function(sigma.getNumber(i, 0));
                sigma.setNub(i, 0, value);
            }
        }
        if (isStudy) {
            featureMatrix = matrix;
            outMatrix = sigma;
        }
        return sigma;
    }


    private void initPower() throws Exception {//初始化权重及阈值
        Random random = new Random();
        //设置初始化权重范围收缩系数
        if (name.equals("HiddenNerve")) {//隐层神经元
            myUpNumber = sensoryNerveNub;
        } else if (name.equals("OutNerve")) {//输出神经元
            myUpNumber = hiddenNerveNub;
        } else {//softmax
            myUpNumber = outNerveNub;
        }
        if (myUpNumber > 0) {//输入个数
            powerMatrix = new Matrix(myUpNumber + 1, 1);
            double sh = Math.sqrt(myUpNumber);
            for (int i = 0; i < myUpNumber; i++) {
                double nub = random.nextDouble() / sh;
                powerMatrix.setNub(i, 0, nub);
            }
            //生成随机阈值
            powerMatrix.setNub(myUpNumber, 0, random.nextDouble() / sh);
        }
    }

    public int getId() {
        return id;
    }


    public void connect(List<Nerve> nerveList) {
        son.addAll(nerveList);//连接下一层
    }


    public void connectFather(List<Nerve> nerveList) {
        father.addAll(nerveList);//连接上一层
    }
}
