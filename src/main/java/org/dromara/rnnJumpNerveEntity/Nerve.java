package org.dromara.rnnJumpNerveEntity;

import org.dromara.matrixTools.Matrix;
import org.dromara.matrixTools.MatrixOperation;
import org.dromara.config.RZ;
import org.dromara.i.ActiveFunction;
import org.dromara.i.OutBack;

import java.util.*;

/**
 * @author lidapeng
 * 神经元，所有类别神经元都要继承的类，具有公用属性
 * &#064;date  9:36 上午 2019/12/21
 */
public abstract class Nerve {
    private final Map<Integer, List<Nerve>> son = new HashMap<>();//轴突下一层的连接神经元
    private final Map<Integer, List<Nerve>> father = new HashMap<>();//树突上一层的连接神经元
    private final List<Nerve> rnnOut = new ArrayList<>();//rnn隐层输出神经元集合
    protected Map<Integer, Double> dendrites = new HashMap<>();//上一层权重(需要取出)
    protected Map<Integer, Double> wg = new HashMap<>();//上一层权重与梯度的积
    private final int id;//同级神经元编号,注意在同层编号中ID应有唯一性
    boolean fromOutNerve = false;//是否是输出神经元
    private final int hiddenNerveNub;//隐层神经元个数
    private final int sensoryNerveNub;//输入神经元个数
    private final int outNerveNub;//输出神经元个数
    protected Map<Long, List<Double>> features = new HashMap<>();//上一层神经元输入的数值
    protected Matrix nerveMatrix;//权重矩阵可获取及注入
    protected double threshold;//此神经元的阈值需要取出
    protected String name;//该神经元所属类型
    protected double outNub;//输出数值（ps:只有训练模式的时候才可保存输出过的数值）
    protected double E;//模板期望值
    protected double gradient;//当前梯度
    protected double studyPoint;
    protected double sigmaW;//对上一层权重与上一层梯度的积进行求和
    private int backNub = 0;//当前节点被反向传播的次数
    protected ActiveFunction activeFunction;
    private final int rzType;//正则化类型，默认不进行正则化
    private final double lParam;//正则参数
    private final int step;//步长
    private final int kernLen;//核长
    private Matrix im2col;//输入矩阵
    private int xInput;//输入矩阵的x
    private int yInput;//输入矩阵的y
    private Matrix outMatrix;//输出矩阵
    private int myUpNumber;//统计参数数量
    protected int depth;//所处深度
    protected int allDepth;//总深度
    protected boolean creator;//是否为创建网络
    protected int startDepth;//开始深度
    private final MatrixOperation matrixOperation = new MatrixOperation();

    public int getDepth() {
        return depth;
    }

    public Map<Integer, Double> getDendrites() {
        return dendrites;
    }

    public Matrix getNerveMatrix() {
        return nerveMatrix;
    }

    public void setNerveMatrix(Matrix nerveMatrix) {
        this.nerveMatrix = nerveMatrix;
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

    protected Nerve(int id, String name,
                    double studyPoint, boolean init, ActiveFunction activeFunction
            , boolean isDynamic, int rzType, double lParam, int step, int kernLen, int sensoryNerveNub
            , int hiddenNerveNub, int outNerveNub, int allDepth, boolean creator, int startDepth) throws Exception {//该神经元在同层神经元中的编号
        this.id = id;
        this.creator = creator;
        this.startDepth = startDepth;
        this.allDepth = allDepth;
        this.hiddenNerveNub = hiddenNerveNub;//隐层神经元个数
        this.sensoryNerveNub = sensoryNerveNub;//输入神经元个数
        this.outNerveNub = outNerveNub;//输出神经元个数
        this.name = name;
        this.studyPoint = studyPoint;
        this.activeFunction = activeFunction;
        this.rzType = rzType;
        this.lParam = lParam;
        this.step = step;
        this.kernLen = kernLen;
        if (name.equals("OutNerve")) {
            fromOutNerve = true;
        }
        initPower(init, isDynamic);//生成随机权重
    }

    protected void setStudyPoint(double studyPoint) {
        this.studyPoint = studyPoint;
    }

    private int getNextStorey(int[] storeys, int index) {
        int nextStorey = -1;
        int nextIndex = index + 1;
        if (storeys.length > nextIndex) {//可以继续前进
            nextStorey = storeys[nextIndex];
        }
        return nextStorey;
    }

    protected void sendSoftMaxBack(long eventId, double parameter, Matrix rnnMatrix, OutBack outBack, String myWord) throws Exception {
        if (!son.isEmpty()) {
            List<Nerve> nerverList = son.get(0);
            for (Nerve nerve : nerverList) {
                nerve.sendAppointSoftMax(eventId, parameter, rnnMatrix, outBack, myWord);
            }
        } else {
            throw new Exception("this storey is lastIndex");
        }
    }

    protected void sendSoftMax(long eventId, double parameter, boolean isStudy, Map<Integer, Double> E
            , OutBack outBack, Matrix rnnMatrix, int[] storeys, int index) throws Exception {
        if (!son.isEmpty()) {
            List<Nerve> nerverList = son.get(0);
            for (Nerve nerve : nerverList) {
                nerve.input(eventId, parameter, isStudy, E, outBack, rnnMatrix, storeys, index, 0);
            }
        } else {
            throw new Exception("this storey is lastIndex");
        }
    }

    protected void clearData(long eventId) {
    }

    protected void sendMyTestMessage(long eventId, Matrix featureMatrix, OutBack outBack, String word) throws Exception {

    }

    protected void sendAppointSoftMax(long eventId, double parameter, Matrix rnnMatrix, OutBack outBack, String myWord) throws Exception {
    }

    protected void sendAppointTestMessage(long eventId, double parameter, Matrix featureMatrix, OutBack outBack, String myWord) throws Exception {
    }

    protected void sendTestMessage(long eventId, double parameter, Matrix featureMatrix, OutBack outBack, String myWord) throws Exception {
        if (!son.isEmpty()) {
            List<Nerve> nerveList = son.get(depth + 1);
            if (nerveList != null) {
                for (Nerve nerve : nerveList) {
                    nerve.sendAppointTestMessage(eventId, parameter, featureMatrix, outBack, myWord);
                }
            } else {
                throw new Exception("Insufficient layer:" + depth + 1);
            }
        } else {
            throw new Exception("this layer is lastIndex");
        }
    }

    protected void sendRnnTestMessage(long eventId, double parameter, Matrix featureMatrix, OutBack outBack, String myWord) throws Exception {
        if (!rnnOut.isEmpty()) {
            for (Nerve nerve : rnnOut) {
                nerve.sendAppointTestMessage(eventId, parameter, featureMatrix, outBack, myWord);
            }
        } else {
            throw new Exception("this layer is lastIndex");
        }
    }

    protected void sendMessage(long eventId, double parameter, boolean isStudy, Map<Integer, Double> E
            , OutBack outBack, Matrix rnnMatrix, int[] storeys, int index, int questionLength) throws Exception {
        List<Nerve> nerveList = null;
        int nextStorey = 0;
        if (storeys == null) {
            nerveList = son.get(0);
        } else {
            nextStorey = getNextStorey(storeys, index);
            if (nextStorey > -1) {//可以继续向前
                nerveList = son.get(nextStorey);
                index++;
                if (nerveList == null) {
                    throw new Exception("向前->要查找的层数不存在链接，序列：" + index + "层数:" + nextStorey +
                            ",当前所在层数:" + depth + ",我的身份:" + name);
                }
            }
        }
        if (nerveList != null) {
            if (creator && !isStudy && nextStorey == startDepth) {
                for (Nerve nerve : nerveList) {
                    nerve.sendAppointTestMessage(eventId, parameter, rnnMatrix, outBack, null);
                }
            } else {
                for (Nerve nerve : nerveList) {
                    nerve.input(eventId, parameter, isStudy, E, outBack, rnnMatrix, storeys, index, questionLength);
                }
            }
        } else {//发送到输出神经元
            sendRnnMessage(eventId, parameter, isStudy, E, outBack, rnnMatrix, storeys, index);
        }
    }

    private void sendRnnMessage(long eventId, double parameter, boolean isStudy, Map<Integer, Double> E
            , OutBack outBack, Matrix rnnMatrix, int[] storeys, int index) throws Exception {
        if (!rnnOut.isEmpty()) {
            for (Nerve nerve : rnnOut) {
                nerve.input(eventId, parameter, isStudy, E, outBack, rnnMatrix, storeys, index, 0);
            }
        } else {
            throw new Exception("this layer is lastIndex");
        }
    }

    protected Matrix conv(Matrix matrix) throws Exception {//正向卷积，下取样
        xInput = matrix.getX();
        yInput = matrix.getY();
        int sub = kernLen - step;
        int x = (xInput - sub) / step;//线性变换后矩阵的行数 （图片长度-（核长-步长））/步长
        int y = (yInput - sub) / step;//线性变换后矩阵的列数
        Matrix myMatrix = new Matrix(x, y);//线性变化后的矩阵
        im2col = matrixOperation.im2col(matrix, kernLen, step);
        //输出矩阵
        Matrix matrixOut = matrixOperation.mulMatrix(im2col, nerveMatrix);
        //输出矩阵重新排序
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double nub = activeFunction.function(matrixOut.getNumber(i * y + j, 0));
                myMatrix.setNub(i, j, nub);
            }
        }
        outMatrix = myMatrix;
        return myMatrix;
    }

    protected void sendMatrix(long eventId, Matrix parameter, boolean isStudy,
                              int E, OutBack outBack) throws Exception {
        if (!son.isEmpty()) {
            for (Nerve nerve : son.get(0)) {
                nerve.inputMatrix(eventId, parameter, isStudy, E, outBack);
            }
        } else {
            throw new Exception("this layer is lastIndex");
        }
    }

    private void backSendMessage(long eventId, boolean fromOutNerve, int[] storeys, int index) throws Exception {//反向传播
        if (!father.isEmpty()) {//要先判定是不是可以继续往后传
            List<Nerve> nerveList = null;
            if (storeys == null) {//常规依次回退
                nerveList = father.get(0);
            } else if (index > 0) {//可以继续向后传递
                nerveList = father.get(storeys[index]);
                if (nerveList == null) {
                    throw new Exception("向后->要查找的层数不存在链接，序列：" + index + "目标层数:" + storeys[index]
                            + ",当前所在层数:" + depth + ",我的身份:" + name);
                }
                index--;
            }
            if (nerveList != null) {
                for (int i = 0; i < nerveList.size(); i++) {
                    nerveList.get(i).backGetMessage(wg.get(i + 1), eventId, fromOutNerve, storeys, index);
                }
            }
        }
    }

    private void backMatrixMessage(Matrix g) throws Exception {//反向传播矩阵
        if (!father.isEmpty()) {
            for (Nerve nerve : father.get(0)) {
                nerve.backMatrix(g);
            }
        }
    }

    protected void input(long eventId, double parameter, boolean isStudy
            , Map<Integer, Double> E, OutBack imageBack, Matrix rnnMatrix
            , int[] storeys, int index, int questionLength) throws Exception {//输入参数

    }

    protected void inputMatrix(long eventId, Matrix matrix, boolean isKernelStudy, int E, OutBack outBack) throws Exception {//输入动态矩阵
    }

    private void backGetMessage(double parameter, long eventId, boolean fromOutNerve, int[] storeys, int index) throws Exception {//反向传播
        backNub++;
        sigmaW = sigmaW + parameter;
        int number;
        if (fromOutNerve) {
            number = outNerveNub;
        } else {
            number = hiddenNerveNub;
        }
        if (backNub == number) {//进行新的梯度计算
            backNub = 0;
            gradient = activeFunction.functionG(outNub) * sigmaW;
            updatePower(eventId, storeys, index);//修改阈值
        }
    }

    protected void backMatrix(Matrix g) throws Exception {//回传梯度
        //对g进行重新排序
        int x = g.getX();
        int y = g.getY();
        Matrix yc = new Matrix(x * y, 1);
        int index = 0;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double error = g.getNumber(i, j);
                double out = outMatrix.getNumber(i, j);
                error = error * activeFunction.functionG(out) * studyPoint;
                yc.setNub(index, 0, error);
                index++;
            }
        }
        //计算权重变化量
        Matrix trx = matrixOperation.transPosition(im2col);
        Matrix wSub = matrixOperation.mulMatrix(trx, yc);
        //给权重变化wSub增加正则项，抑制权重变化量
        //计算x变化量
        x = im2col.getX();
        y = im2col.getY() - 1;
        for (int i = 0; i < x; i++) {
            double ySub = yc.getNumber(i, 0);
            for (int j = 0; j < y; j++) {
                double k = nerveMatrix.getNumber(j, 0) * ySub;
                im2col.setNub(i, j, k);
            }
        }
        Matrix gNext = matrixOperation.reverseIm2col(im2col, kernLen, step, xInput, yInput);
        //更新权重
        nerveMatrix = matrixOperation.add(nerveMatrix, wSub);
        //将梯度继续回传
        backMatrixMessage(gNext);
    }

    protected void updatePower(long eventId, int[] storeys, int index) throws Exception {//修改阈值
        double h = gradient * studyPoint;
        threshold = threshold - h;
        updateW(h, eventId);
        sigmaW = 0;//求和结果归零
        backSendMessage(eventId, fromOutNerve, storeys, index);
    }

    private double regularization(double w, double param) {//正则化类型
        double re = 0.0;
        if (rzType != RZ.NOT_RZ) {
            if (rzType == RZ.L2) {
                re = param * -w;
            } else if (rzType == RZ.L1) {
                if (w > 0) {
                    re = -param;
                } else if (w < 0) {
                    re = param;
                }
            }
        }
        return re;
    }

    private void updateW(double h, long eventId) {//h是学习率 * 当前g（梯度）
        List<Double> list = features.get(eventId);
        double param = 0;
        if (rzType != RZ.NOT_RZ) {
            double sigma = 0;
            for (Map.Entry<Integer, Double> entry : dendrites.entrySet()) {
                if (rzType == RZ.L2) {
                    sigma = sigma + Math.pow(entry.getValue(), 2);
                } else {
                    sigma = sigma + Math.abs(entry.getValue());
                }
            }
            param = sigma * lParam * studyPoint;
        }
        for (Map.Entry<Integer, Double> entry : dendrites.entrySet()) {
            int key = entry.getKey();//上层隐层神经元的编号
            double w = entry.getValue();//接收到编号为KEY的上层隐层神经元的权重
            double bn = list.get(key - 1);//接收到编号为KEY的上层隐层神经元的输入
            double wp = bn * h;
            double dm = w * gradient;
            double regular = regularization(w, param);//正则化抑制权重s
            w = w + regular;
            w = w + wp;
            wg.put(key, dm);//保存上一层权重与梯度的积
            dendrites.put(key, w);//保存修正结果
        }
        features.remove(eventId); //清空当前上层输入参数参数
    }

    protected boolean insertParameter(long eventId, double parameter) throws Exception {//添加参数
        boolean allReady = false;
        List<Double> featuresList;
        if (features.containsKey(eventId)) {
            featuresList = features.get(eventId);
        } else {
            featuresList = new ArrayList<>();
            features.put(eventId, featuresList);
        }
        featuresList.add(parameter);
        if (featuresList.size() == myUpNumber) {
            allReady = true;
        } else if (featuresList.size() > myUpNumber) {
            throw new Exception("接收参数数量异常");
        }
        return allReady;
    }

    protected void destroyParameter(long eventId) {//销毁参数
        features.remove(eventId);
    }

    protected double calculation(long eventId) {//计算当前输出结果
        double sigma = 0;
        List<Double> featuresList = features.get(eventId);
        for (int i = 0; i < featuresList.size(); i++) {
            double value = featuresList.get(i);
            double w = dendrites.get(i + 1);//当value不为0的时候把w取出来
            sigma = w * value + sigma;
        }
        return sigma - threshold;
    }

    private void initPower(boolean init, boolean isDynamic) throws Exception {//初始化权重及阈值
        Random random = new Random();
        if (!isDynamic) {//静态神经元
            //设置初始化权重范围收缩系数
            if (name.equals("HiddenNerve")) {//隐层神经元
                myUpNumber = sensoryNerveNub;
            } else if (name.equals("OutNerve")) {//输出神经元
                myUpNumber = hiddenNerveNub;
            } else {//softmax
                myUpNumber = outNerveNub;
            }
            if (myUpNumber > 0) {//输入个数
                double sh = Math.sqrt(myUpNumber);
                for (int i = 1; i < myUpNumber + 1; i++) {
                    double nub = 0;
                    if (init) {
                        nub = random.nextDouble() / sh;
                    }
                    dendrites.put(i, nub);//random.nextDouble()
                }
                //生成随机阈值
                double nub = 0;
                if (init) {
                    nub = random.nextDouble() / sh;
                }
                threshold = nub;
            }
        } else {//动态神经元
            int nerveNub = kernLen * kernLen;
            double sh = Math.sqrt(2D / nerveNub);
            double nub;
            nerveMatrix = new Matrix(nerveNub + 1, 1);
            for (int i = 0; i < nerveMatrix.getX(); i++) {
                nub = 0;
                if (init) {
                    nub = random.nextDouble() * sh;
                }
                nerveMatrix.setNub(i, 0, nub);
            }
        }
    }

    public int getId() {
        return id;
    }


    public void connect(int depth, List<Nerve> nerveList) {
        son.put(depth, nerveList);//连接下一层
    }

    public void connectOut(List<Nerve> nerveList) {
        rnnOut.addAll(nerveList);
    }

    public void connectFather(int depth, List<Nerve> nerveList) {
        father.put(depth, nerveList);
    }
}
