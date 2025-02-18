package org.dromara.easyai.rnnNerveEntity;


import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.config.RZ;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.i.OutBack;

import java.util.*;

/**
 * @author lidapeng
 * 神经元，所有类别神经元都要继承的类，具有公用属性
 * @date 9:36 上午 2019/12/21
 */
public abstract class Nerve {
    private final List<Nerve> son = new ArrayList<>();//轴突下一层的连接神经元
    private final List<Nerve> rnnOut = new ArrayList<>();//rnn隐层输出神经元集合
    private final List<Nerve> father = new ArrayList<>();//树突上一层的连接神经元
    protected Map<Integer, Float> dendrites = new HashMap<>();//上一层权重(需要取出)
    protected Map<Integer, Float> wg = new HashMap<>();//上一层权重与梯度的积
    private final int id;//同级神经元编号,注意在同层编号中ID应有唯一性
    boolean fromOutNerve = false;//是否是输出神经元
    protected int upNub;//上一层神经元数量
    protected int downNub;//下一层神经元的数量
    protected int rnnOutNub;//rnn输出神经元数量
    protected Map<Long, List<Float>> features = new HashMap<>();//上一层神经元输入的数值
    protected Matrix nerveMatrix;//权重矩阵可获取及注入
    protected float threshold;//此神经元的阈值需要取出
    protected String name;//该神经元所属类型
    protected float outNub;//输出数值（ps:只有训练模式的时候才可保存输出过的数值）
    protected float E;//模板期望值
    protected float gradient;//当前梯度
    protected float studyPoint;
    protected float sigmaW;//对上一层权重与上一层梯度的积进行求和
    private int backNub = 0;//当前节点被反向传播的次数
    protected ActiveFunction activeFunction;
    private final int rzType;//正则化类型，默认不进行正则化
    private final float lParam;//正则参数
    private final int step;//步长
    private final int kernLen;//核长
    private Matrix im2col;//输入矩阵
    private int xInput;//输入矩阵的x
    private int yInput;//输入矩阵的y
    private Matrix outMatrix;//输出矩阵
    private Map<Long, Integer> embeddingIndex = new HashMap<>();//记录词向量下标位置
    private final MatrixOperation matrixOperation = new MatrixOperation();

    public Map<Integer, Float> getDendrites() {
        return dendrites;
    }

    public Matrix getNerveMatrix() {
        return nerveMatrix;
    }

    public void setNerveMatrix(Matrix nerveMatrix) {
        this.nerveMatrix = nerveMatrix;
    }

    public void setDendrites(Map<Integer, Float> dendrites) {
        this.dendrites = dendrites;
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    protected Nerve(int id, int upNub, String name, int downNub,
                    float studyPoint, boolean init, ActiveFunction activeFunction
            , boolean isDynamic, int rzType, float lParam, int step, int kernLen, int rnnOutNub) throws Exception {//该神经元在同层神经元中的编号
        this.id = id;
        this.upNub = upNub;
        this.name = name;
        this.downNub = downNub;
        this.studyPoint = studyPoint;
        this.activeFunction = activeFunction;
        this.rzType = rzType;
        this.lParam = lParam;
        this.step = step;
        this.kernLen = kernLen;
        this.rnnOutNub = rnnOutNub;
        if (name.equals("OutNerve")) {
            fromOutNerve = true;
        }
        initPower(init, isDynamic);//生成随机权重
    }

    protected void setStudyPoint(float studyPoint) {
        this.studyPoint = studyPoint;
    }

    public void sendMessage(long eventId, float parameter, boolean isStudy, Map<Integer, Float> E
            , OutBack outBack, boolean isEmbedding, Matrix rnnMatrix) throws Exception {
        if (son.size() > 0) {
            for (Nerve nerve : son) {
                nerve.input(eventId, parameter, isStudy, E, outBack, isEmbedding, rnnMatrix);
            }
        } else {
            throw new Exception("this layer is lastIndex");
        }
    }

    public void sendRnnMessage(long eventId, float parameter, boolean isStudy, Map<Integer, Float> E
            , OutBack outBack, boolean isEmbedding, Matrix rnnMatrix) throws Exception {
        if (rnnOut.size() > 0) {
            for (Nerve nerve : rnnOut) {
                nerve.input(eventId, parameter, isStudy, E, outBack, isEmbedding, rnnMatrix);
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
                float nub = activeFunction.function(matrixOut.getNumber(i * y + j, 0));
                myMatrix.setNub(i, j, nub);
            }
        }
        outMatrix = myMatrix;
        return myMatrix;
    }

    public void sendMatrix(long eventId, Matrix parameter, boolean isStudy,
                           int E, OutBack outBack) throws Exception {
        if (son.size() > 0) {
            for (Nerve nerve : son) {
                nerve.inputMatrix(eventId, parameter, isStudy, E, outBack);
            }
        } else {
            throw new Exception("this layer is lastIndex");
        }
    }

    private void backSendMessage(long eventId, boolean fromOutNerve) throws Exception {//反向传播
        if (father.size() > 0) {
            for (int i = 0; i < father.size(); i++) {
                father.get(i).backGetMessage(wg.get(i + 1), eventId, fromOutNerve);
            }
        }
    }

    private void backMatrixMessage(Matrix g) throws Exception {//反向传播矩阵
        if (father.size() > 0) {
            for (int i = 0; i < father.size(); i++) {
                father.get(i).backMatrix(g);
            }
        }
    }

    protected void input(long eventId, float parameter, boolean isStudy
            , Map<Integer, Float> E, OutBack imageBack, boolean isEmbedding, Matrix rnnMatrix) throws Exception {//输入参数

    }

    protected void inputMatrix(long eventId, Matrix matrix, boolean isKernelStudy, int E, OutBack outBack) throws Exception {//输入动态矩阵
    }

    private void backGetMessage(float parameter, long eventId, boolean fromOutNerve) throws Exception {//反向传播
        backNub++;
        //sigmaW = ArithUtil.add(sigmaW, parameter);
        sigmaW = sigmaW + parameter;
        int number;
        if (fromOutNerve) {
            number = rnnOutNub;
        } else {
            number = downNub;
        }
        if (backNub == number) {//进行新的梯度计算
            backNub = 0;
            //gradient = ArithUtil.mul(activeFunction.functionG(outNub), sigmaW);
            gradient = activeFunction.functionG(outNub) * sigmaW;
            updatePower(eventId);//修改阈值
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
                float error = g.getNumber(i, j);
                float out = outMatrix.getNumber(i, j);
                error = error * activeFunction.functionG(out) * studyPoint;
                yc.setNub(index, 0, error);
                index++;
            }
        }
        //计算权重变化量
        Matrix trx = matrixOperation.transPosition(im2col);
        Matrix wSub = matrixOperation.mulMatrix(trx, yc);
        //给权重变化wSub增加正则项，抑制权重变化量
        //System.out.println(wSub.getString());
        //计算x变化量
        x = im2col.getX();
        y = im2col.getY() - 1;
        for (int i = 0; i < x; i++) {
            float ySub = yc.getNumber(i, 0);
            for (int j = 0; j < y; j++) {
                float k = nerveMatrix.getNumber(j, 0) * ySub;
                im2col.setNub(i, j, k);
            }
        }
        Matrix gNext = matrixOperation.reverseIm2col(im2col, kernLen, step, xInput, yInput);
        //更新权重
        nerveMatrix = matrixOperation.add(nerveMatrix, wSub);
        //将梯度继续回传
        backMatrixMessage(gNext);
    }

    protected void updatePower(long eventId) throws Exception {//修改阈值
        //float h = ArithUtil.mul(gradient, studyPoint);//梯度下降
        float h = gradient * studyPoint;
        //threshold = ArithUtil.add(threshold, -h);//更新阈值
        threshold = threshold - h;
        updateW(h, eventId);
        sigmaW = 0;//求和结果归零
        backSendMessage(eventId, fromOutNerve);
    }

    private float regularization(float w, float param) {//正则化类型
        float re = 0.0F;
        if (rzType != RZ.NOT_RZ) {
            if (rzType == RZ.L2) {
                //re = ArithUtil.mul(param, -w);
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

    private void updateW(float h, long eventId) {//h是学习率 * 当前g（梯度）
        List<Float> list = features.get(eventId);
        float param = 0;
        if (rzType != RZ.NOT_RZ) {
            double sigma = 0;
            for (Map.Entry<Integer, Float> entry : dendrites.entrySet()) {
                if (rzType == RZ.L2) {
                    sigma = sigma + (float)Math.pow(entry.getValue(), 2);
                } else {
                    sigma = sigma + (float)Math.abs(entry.getValue());
                }
            }
            param = (float) sigma * lParam * studyPoint;
        }
        for (Map.Entry<Integer, Float> entry : dendrites.entrySet()) {
            int key = entry.getKey();//上层隐层神经元的编号
            float w = entry.getValue();//接收到编号为KEY的上层隐层神经元的权重
            float bn = list.get(key - 1);//接收到编号为KEY的上层隐层神经元的输入
            float wp = bn * h;
            float dm = w * gradient;
            float regular = regularization(w, param);//正则化抑制权重s
            w = w + regular;
            w = w + wp;
            wg.put(key, dm);//保存上一层权重与梯度的积
            dendrites.put(key, w);//保存修正结果
        }
        features.remove(eventId); //清空当前上层输入参数参数
    }

    protected boolean insertParameter(long eventId, float parameter, boolean embedding) {//添加参数
        boolean allReady = false;
        List<Float> featuresList;
        if (features.containsKey(eventId)) {
            featuresList = features.get(eventId);
        } else {
            featuresList = new ArrayList<>();
            features.put(eventId, featuresList);
        }
        if (embedding && parameter > 0.5) {
            embeddingIndex.put(eventId, featuresList.size());
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

    protected float getWOne(long eventId) {
        int index = embeddingIndex.get(eventId);
        return dendrites.get(index + 1);
    }

    protected float calculation(long eventId, boolean isEmbedding) {//计算当前输出结果
        float sigma = 0;
        List<Float> featuresList = features.get(eventId);
        if (!isEmbedding) {
            for (int i = 0; i < featuresList.size(); i++) {
                float value = featuresList.get(i);
                float w = dendrites.get(i + 1);//当value不为0的时候把w取出来
                sigma = w * value + sigma;
            }
        } else {
            int index = embeddingIndex.get(eventId);
            sigma = featuresList.get(index) * dendrites.get(index + 1);
            embeddingIndex.remove(eventId);
        }
        return sigma - threshold;//ArithUtil.sub(sigma, threshold);
    }

    private void initPower(boolean init, boolean isDynamic) throws Exception {//初始化权重及阈值
        Random random = new Random();
        if (!isDynamic) {//静态神经元
            //设置初始化权重范围收缩系数
            if (upNub > 0) {//输入个数
                float sh = (float)Math.sqrt(upNub);
                for (int i = 1; i < upNub + 1; i++) {
                    float nub = 0;
                    if (init) {
                        nub = random.nextFloat() / sh;
                    }
                    dendrites.put(i, nub);//random.nextFloat()
                }
                //生成随机阈值
                float nub = 0;
                if (init) {
                    nub = random.nextFloat() / sh;
                }
                threshold = nub;
            }
        } else {//动态神经元
            int nerveNub = kernLen * kernLen;
            float sh = (float)Math.sqrt(2D / nerveNub);
            float nub;
            nerveMatrix = new Matrix(nerveNub + 1, 1);
            for (int i = 0; i < nerveMatrix.getX(); i++) {
                nub = 0;
                if (init) {
                    nub = random.nextFloat() * sh;
                }
                nerveMatrix.setNub(i, 0, nub);
            }
        }
    }

    public int getId() {
        return id;
    }


    public void connect(List<Nerve> nerveList) {
        son.addAll(nerveList);//连接下一层
    }

    public void connectOut(List<Nerve> nerveList) {
        rnnOut.addAll(nerveList);
    }

    public void connectFather(List<Nerve> nerveList) {
        father.addAll(nerveList);//连接上一层
    }
}
