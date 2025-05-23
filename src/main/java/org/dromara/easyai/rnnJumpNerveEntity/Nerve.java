package org.dromara.easyai.rnnJumpNerveEntity;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.config.RZ;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.i.OutBack;

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
    protected Map<Integer, Float> dendrites = new HashMap<>();//上一层权重(需要取出)
    protected Map<Integer, Float> wg = new HashMap<>();//上一层权重与梯度的积
    private final int id;//同级神经元编号,注意在同层编号中ID应有唯一性
    boolean fromOutNerve = false;//是否是输出神经元
    private final int hiddenNerveNub;//隐层神经元个数
    private final int sensoryNerveNub;//输入神经元个数
    private final int outNerveNub;//输出神经元个数
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
    private int myUpNumber;//统计参数数量
    protected int depth;//所处深度
    protected int allDepth;//总深度
    protected boolean creator;//是否为创建网络
    protected int startDepth;//开始深度

    public int getDepth() {
        return depth;
    }

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

    protected Nerve(int id, String name,
                    float studyPoint, boolean init, ActiveFunction activeFunction, int rzType, float lParam, int sensoryNerveNub
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
        if (name.equals("OutNerve")) {
            fromOutNerve = true;
        }
        initPower(init);//生成随机权重
    }

    protected void setStudyPoint(float studyPoint) {
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

    protected void sendSoftMaxBack(long eventId, float parameter, Matrix rnnMatrix, OutBack outBack, String myWord) throws Exception {
        if (!son.isEmpty()) {
            List<Nerve> nerverList = son.get(0);
            for (Nerve nerve : nerverList) {
                nerve.sendAppointSoftMax(eventId, parameter, rnnMatrix, outBack, myWord);
            }
        } else {
            throw new Exception("this storey is lastIndex");
        }
    }

    protected void sendSoftMax(long eventId, float parameter, boolean isStudy, Map<Integer, Float> E
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

    protected void sendAppointSoftMax(long eventId, float parameter, Matrix rnnMatrix, OutBack outBack, String myWord) throws Exception {
    }

    protected void sendAppointTestMessage(long eventId, float parameter, Matrix featureMatrix, OutBack outBack, String myWord) throws Exception {
    }

    protected void sendTestMessage(long eventId, float parameter, Matrix featureMatrix, OutBack outBack, String myWord) throws Exception {
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

    protected void sendRnnTestMessage(long eventId, float parameter, Matrix featureMatrix, OutBack outBack, String myWord) throws Exception {
        if (!rnnOut.isEmpty()) {
            for (Nerve nerve : rnnOut) {
                nerve.sendAppointTestMessage(eventId, parameter, featureMatrix, outBack, myWord);
            }
        } else {
            throw new Exception("this layer is lastIndex");
        }
    }

    protected void sendMessage(long eventId, float parameter, boolean isStudy, Map<Integer, Float> E
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

    private void sendRnnMessage(long eventId, float parameter, boolean isStudy, Map<Integer, Float> E
            , OutBack outBack, Matrix rnnMatrix, int[] storeys, int index) throws Exception {
        if (!rnnOut.isEmpty()) {
            for (Nerve nerve : rnnOut) {
                nerve.input(eventId, parameter, isStudy, E, outBack, rnnMatrix, storeys, index, 0);
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

    protected void input(long eventId, float parameter, boolean isStudy
            , Map<Integer, Float> E, OutBack imageBack, Matrix rnnMatrix
            , int[] storeys, int index, int questionLength) throws Exception {//输入参数

    }

    private void backGetMessage(float parameter, long eventId, boolean fromOutNerve, int[] storeys, int index) throws Exception {//反向传播
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

    protected void updatePower(long eventId, int[] storeys, int index) throws Exception {//修改阈值
        float h = gradient * studyPoint;
        threshold = threshold - h;
        updateW(h, eventId);
        sigmaW = 0;//求和结果归零
        backSendMessage(eventId, fromOutNerve, storeys, index);
    }

    private float regularization(float w, float param) {//正则化类型
        float re = 0.0f;
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

    private void updateW(float h, long eventId) {//h是学习率 * 当前g（梯度）
        List<Float> list = features.get(eventId);
        float param = 0;
        if (rzType != RZ.NOT_RZ) {
            float sigma = 0;
            for (Map.Entry<Integer, Float> entry : dendrites.entrySet()) {
                if (rzType == RZ.L2) {
                    sigma = sigma + (float) Math.pow(entry.getValue(), 2);
                } else {
                    sigma = sigma + (float) Math.abs(entry.getValue());
                }
            }
            param = sigma * lParam * studyPoint;
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

    protected boolean insertParameter(long eventId, float parameter) throws Exception {//添加参数
        boolean allReady = false;
        List<Float> featuresList;
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

    protected float calculation(long eventId) {//计算当前输出结果
        float sigma = 0;
        List<Float> featuresList = features.get(eventId);
        for (int i = 0; i < featuresList.size(); i++) {
            float value = featuresList.get(i);
            float w = dendrites.get(i + 1);//当value不为0的时候把w取出来
            sigma = w * value + sigma;
        }
        return sigma - threshold;
    }

    private void initPower(boolean init) throws Exception {//初始化权重及阈值
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
            float sh = (float) Math.sqrt(myUpNumber);
            for (int i = 1; i < myUpNumber + 1; i++) {
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
