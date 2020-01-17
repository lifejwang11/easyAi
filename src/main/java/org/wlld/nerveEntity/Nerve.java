package org.wlld.nerveEntity;


import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.i.ActiveFunction;
import org.wlld.tools.ArithUtil;

import java.util.*;

/**
 * @author lidapeng
 * 神经元，所有类别神经元都要继承的类，具有公用属性
 * @date 9:36 上午 2019/12/21
 */
public abstract class Nerve {
    private List<Nerve> son = new ArrayList<>();//轴突下一层的连接神经元
    private List<Nerve> fathor = new ArrayList<>();//树突上一层的连接神经元
    protected Map<Integer, Double> dendrites = new HashMap<>();//上一层权重(需要取出)
    protected Map<Integer, Double> wg = new HashMap<>();//上一层权重与梯度的积
    private int id;//同级神经元编号,注意在同层编号中ID应有唯一性
    protected int upNub;//上一层神经元数量
    protected int downNub;//下一层神经元的数量
    protected Map<Long, List<Double>> features = new HashMap<>();
    //static final Logger logger = LogManager.getLogger(Nerve.class);
    protected Matrix nerveMatrix = new Matrix(3, 3);//权重矩阵可获取及注入
    protected Map<Long, Matrix> matrixMap = new HashMap<>();//参数矩阵
    protected double threshold;//此神经元的阈值需要取出
    protected String name;//该神经元所属类型
    protected double outNub;//输出数值（ps:只有训练模式的时候才可保存输出过的数值）
    protected double E;//模板期望值
    protected double gradient;//当前梯度
    protected double studyPoint;
    protected double sigmaW;//对上一层权重与上一层梯度的积进行求和
    private int backNub = 0;//当前节点被反向传播的次数
    protected ActiveFunction activeFunction;

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

    protected Nerve(int id, int upNub, String name, int downNub,
                    double studyPoint, boolean init, ActiveFunction activeFunction
            , boolean isDynamic) throws Exception {//该神经元在同层神经元中的编号
        this.id = id;
        this.upNub = upNub;
        this.name = name;
        this.downNub = downNub;
        this.studyPoint = studyPoint;
        this.activeFunction = activeFunction;
        initPower(init, isDynamic);//生成随机权重

    }

    protected void setStudyPoint(double studyPoint) {
        this.studyPoint = studyPoint;
    }

    public void sendMessage(long enevtId, double parameter, boolean isStudy, Map<Integer, Double> E) throws Exception {
        if (son.size() > 0) {
            for (Nerve nerve : son) {
                nerve.input(enevtId, parameter, isStudy, E);
            }
        } else {
            throw new Exception("this layer is lastIndex");
        }
    }

    protected Matrix dynamicNerve(Matrix matrix, long eventId, boolean isStudy) throws Exception {//动态矩阵处理
        Matrix powerMatrix = null;
        if (isStudy) {
            if (!matrixMap.containsKey(eventId)) {
                matrixMap.put(eventId, new Matrix(3, 3));
            }
            powerMatrix = matrixMap.get(eventId);
        }
        int xn = matrix.getX();
        int yn = matrix.getY();
        int x = xn / 3;//线性变换后矩阵的行数
        int y = yn / 3;//线性变换后矩阵的列数
        int nub = x * y;//个数
        Matrix myMatrix = new Matrix(x, y);//线性变化后的矩阵
        for (int i = 0; i < xn - 3; i += 3) {
            for (int j = 0; j < yn - 3; j += 3) {
                //取出矩阵分块 并相加
                if (isStudy) {
                    powerMatrix = MatrixOperation.add(powerMatrix, matrix.getSonOfMatrix(i, j, 3, 3));
                }
                double dm = MatrixOperation.convolution(matrix, nerveMatrix, i, j);
                dm = ArithUtil.sub(ArithUtil.div(dm, 9), threshold);//减偏置项
                //设置输出矩阵 经过激活函数
                myMatrix.setNub(i / 3, j / 3, activeFunction.function(dm));
            }
        }
        //取平均值
        if (isStudy) {
            MatrixOperation.mathMul(powerMatrix, ArithUtil.div(1, nub));
        }
        return myMatrix;
    }

    public void sendMatrix(long enevtId, Matrix parameter, boolean isStudy, boolean isNerveStudy, Map<Integer, Double> E) throws Exception {
        if (son.size() > 0) {
            for (Nerve nerve : son) {
                nerve.inputMartix(enevtId, parameter, isStudy, isNerveStudy, E);
            }
        } else {
            throw new Exception("this layer is lastIndex");
        }
    }

    private void backSendMessage(long eventId) throws Exception {//反向传播
        if (fathor.size() > 0) {
            for (int i = 0; i < fathor.size(); i++) {
                fathor.get(i).backGetMessage(wg.get(i + 1), eventId);
            }
        }
    }

    private void backMatrixMessage(double g, long eventId) throws Exception {//反向传播矩阵
        if (fathor.size() > 0) {
            for (int i = 0; i < fathor.size(); i++) {
                fathor.get(i).backMatrix(g, eventId);
            }
        }
    }

    protected void input(long eventId, double parameter, boolean isStudy
            , Map<Integer, Double> E) throws Exception {//输入参数

    }

    protected void inputMartix(long eventId, Matrix matrix, boolean isKernelStudy
            , boolean isNerveStudy, Map<Integer, Double> E) throws Exception {//输入动态矩阵
    }

    private void backGetMessage(double parameter, long eventId) throws Exception {//反向传播
        backNub++;
        sigmaW = ArithUtil.add(sigmaW, parameter);
        if (backNub == downNub) {//进行新的梯度计算
            backNub = 0;
            gradient = ArithUtil.mul(activeFunction.functionG(outNub), sigmaW);
            updatePower(eventId);//修改阈值
        }
    }

    protected void backMatrix(double g, long eventId) throws Exception {//回传梯度
        gradient = g;
        double h = ArithUtil.mul(gradient, studyPoint);//梯度下降
        threshold = ArithUtil.add(threshold, -h);//更新阈值
        double dm = updateMatrixW(h, eventId);
        backMatrixMessage(dm, eventId);//将梯度继续回传
    }

    protected double updateMatrixW(double h, long eventId) throws Exception {//TODO 更新权重
        Matrix matrix = matrixMap.get(eventId);//参数矩阵 nerveMatrix
        MatrixOperation.mathMul(matrix, h);//参数与梯度做数乘，权重变化量矩阵
        nerveMatrix = MatrixOperation.add(nerveMatrix, matrix);//权重更新
        double allNub = 0;
        for (int i = 0; i < nerveMatrix.getX(); i++) {
            for (int j = 0; j < nerveMatrix.getY(); j++) {
                allNub = ArithUtil.add(allNub, ArithUtil.mul(nerveMatrix.getNumber(i, j), gradient));
            }
        }
        matrixMap.remove(eventId);//移除当前参数矩阵
        allNub = ArithUtil.div(allNub, 9);
        return allNub;
    }

    protected void updatePower(long eventId) throws Exception {//修改阈值
        double h = ArithUtil.mul(gradient, studyPoint);//梯度下降
        threshold = ArithUtil.add(threshold, -h);//更新阈值
        updateW(h, eventId);
        sigmaW = 0;//求和结果归零
        backSendMessage(eventId);
    }

    private void updateW(double h, long eventId) {//h是学习率 * 当前g（梯度）
        List<Double> list = features.get(eventId);
        for (Map.Entry<Integer, Double> entry : dendrites.entrySet()) {
            int key = entry.getKey();//上层隐层神经元的编号
            double w = entry.getValue();//接收到编号为KEY的上层隐层神经元的权重
            double bn = list.get(key - 1);//接收到编号为KEY的上层隐层神经元的输入
            double wp = ArithUtil.mul(bn, h);//编号为KEY的上层隐层神经元权重的变化值
            w = ArithUtil.add(w, wp);//修正后的编号为KEY的上层隐层神经元权重
            double dm = ArithUtil.mul(w, gradient);//返回给相对应的神经元
            // System.out.println("allG==" + allG + ",dm==" + dm);
            wg.put(key, dm);//保存上一层权重与梯度的积
            dendrites.put(key, w);//保存修正结果
        }
        features.remove(eventId); //清空当前上层输入参数参数
    }

    protected boolean insertParameter(long eventId, double parameter) {//添加参数
        boolean allReady = false;
        List<Double> featuresList;
        if (features.containsKey(eventId)) {
            featuresList = features.get(eventId);
        } else {
            featuresList = new ArrayList<>();
            features.put(eventId, featuresList);
        }
        featuresList.add(parameter);
        if (featuresList.size() >= upNub) {
            allReady = true;
        }
        return allReady;
    }

    protected void destoryParameter(long eventId) {//销毁参数
        features.remove(eventId);
    }

    protected double calculation(long eventId) {//计算当前输出结果
        double sigma = 0;
        List<Double> featuresList = features.get(eventId);
        for (int i = 0; i < featuresList.size(); i++) {
            double value = featuresList.get(i);
            double w = dendrites.get(i + 1);
            //System.out.println("w==" + w + ",value==" + value);
            sigma = ArithUtil.add(ArithUtil.mul(w, value), sigma);
            //logger.debug("name:{},eventId:{},id:{},myId:{},w:{},value:{}", name, eventId, i + 1, id, w, value);
        }
        //logger.debug("当前神经元线性变化已经完成,name:{},id:{}", name, getId());
        return ArithUtil.sub(sigma, threshold);
    }

    private void initPower(boolean init, boolean isDynamic) throws Exception {//初始化权重及阈值
        Random random = new Random();
        if (!isDynamic) {//静态神经元
            if (upNub > 0) {
                for (int i = 1; i < upNub + 1; i++) {
                    double nub = 0;
                    if (init) {
                        nub = random.nextDouble();
                    }
                    dendrites.put(i, nub);//random.nextDouble()
                }
            }
        } else {//动态神经元
            double nub;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    nub = 0;
                    if (init) {
                        nub = random.nextDouble();
                    }
                    nerveMatrix.setNub(i, j, nub);
                }
            }
        }
        //生成随机阈值
        double nub = 0;
        if (init) {
            nub = random.nextDouble();
        }
        threshold = nub;
    }

    public int getId() {
        return id;
    }


    public void connect(List<Nerve> nerveList) {
        son.addAll(nerveList);//连接下一层
    }

    public void connectFathor(List<Nerve> nerveList) {
        fathor.addAll(nerveList);//连接上一层
    }
}
