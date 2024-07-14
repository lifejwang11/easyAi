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
    protected Map<Integer, Double> dendrites = new HashMap<>();//上一层权重(需要取出)
    protected Matrix powerMatrix;//权重矩阵
    private final int id;//同级神经元编号,注意在同层编号中ID应有唯一性
    private final int hiddenNerveNub;//隐层神经元个数
    private final int sensoryNerveNub;//输入神经元个数
    private final int outNerveNub;//输出神经元个数
    private final boolean encoder;
    protected Map<Long, List<Double>> features = new HashMap<>();//上一层神经元输入的数值
    protected Map<Long, Matrix> reMatrixFeatures = new HashMap<>();
    protected double threshold;//此神经元的阈值需要取出
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

    public int getDepth() {
        return depth;
    }

    public Map<Integer, Double> getDendrites() {
        return dendrites;
    }


    public void setDendrites(Map<Integer, Double> dendrites) {
        this.dendrites = dendrites;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public void setBeforeLayNorm(LayNorm beforeLayNorm) {
        this.beforeLayNorm = beforeLayNorm;
    }

    public void setAfterLayNorm(LayNorm afterLayNorm) {
        this.afterLayNorm = afterLayNorm;
    }

    protected Nerve(int id, String name, double studyPoint, ActiveFunction activeFunction, int sensoryNerveNub,
                    int hiddenNerveNub, int outNerveNub, boolean encoder, LineBlock lineBlock) throws Exception {//该神经元在同层神经元中的编号
        this.id = id;
        this.lineBlock = lineBlock;
        this.encoder = encoder;
        this.hiddenNerveNub = hiddenNerveNub;//隐层神经元个数
        this.sensoryNerveNub = sensoryNerveNub;//输入神经元个数
        this.outNerveNub = outNerveNub;//输出神经元个数
        this.name = name;
        this.studyPoint = studyPoint;
        this.activeFunction = activeFunction;
        initPower();//生成随机权重
    }

    protected void setStudyPoint(double studyPoint) {
        this.studyPoint = studyPoint;
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
            sigmaW = MatrixOperation.add(sigmaW, parameter);
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
        MatrixOperation.mathMul(errorMatrix, studyPoint);
        Matrix error = updateW(errorMatrix);//更新本神经元参数与返回下层误差
        sigmaW = null;//求和结果归零
        backSendMessage(eventId, error, allError);
    }


    private Matrix updateW(Matrix errorMatrix) throws Exception {//
        Matrix subFeature = MatrixOperation.matrixMulPd(errorMatrix, featureMatrix, powerMatrix, true);
        Matrix subPower = MatrixOperation.matrixMulPd(errorMatrix, featureMatrix, powerMatrix, false);
        powerMatrix = MatrixOperation.add(powerMatrix, subPower);//更新权重
        return subFeature;
    }

    protected boolean insertMatrixParameter(long eventID, Matrix matrix) throws Exception {//reMatrixFeatures
        boolean allReady = false;
        Matrix feature;
        if (reMatrixFeatures.containsKey(eventID)) {
            Matrix myFeature = reMatrixFeatures.get(eventID);
            feature = MatrixOperation.pushVector(myFeature, matrix, false);
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
        Matrix matrix = MatrixOperation.pushVector(feature, th, false);
        Matrix sigma = MatrixOperation.mulMatrix(matrix, powerMatrix);
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

    protected double calculation(long eventId) throws Exception {//计算当前输出结果
        double sigma = 0;
        List<Double> featuresList = features.get(eventId);
        if (dendrites.size() != featuresList.size()) {
            throw new Exception("隐层参数数量与权重数量不一致");
        }
        for (int i = 0; i < featuresList.size(); i++) {
            double value = featuresList.get(i);
            double w = dendrites.get(i + 1);//当value不为0的时候把w取出来
            sigma = w * value + sigma;
        }
        return sigma - threshold;
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
            for (int i = 1; i < myUpNumber + 1; i++) {
                double nub = random.nextDouble() / sh;
                dendrites.put(i, nub);//random.nextDouble()
                powerMatrix.setNub(i - 1, 0, nub);
            }
            //生成随机阈值
            threshold = random.nextDouble() / sh;
            powerMatrix.setNub(myUpNumber, 0, threshold);
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
