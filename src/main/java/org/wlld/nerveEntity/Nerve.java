package org.wlld.nerveEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wlld.function.ActiveFunction;
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
    private Map<Integer, Double> dendrites = new HashMap<>();//上一层权重
    private int id;//同级神经元编号,注意在同层编号中ID应有唯一性
    protected int upNub;//上一层神经元数量
    protected Map<Long, List<Double>> features = new HashMap<>();
    static final Logger logger = LogManager.getLogger(Nerve.class);
    private double threshold;//此神经元的阈值
    protected ActiveFunction activeFunction = new ActiveFunction();
    protected String name;//该神经元所属类型
    protected double outNub;//输出数值（ps:只有训练模式的时候才可保存输出过的数值）

    protected Nerve(int id, int upNub, String name) {//该神经元在同层神经元中的编号
        this.id = id;
        this.upNub = upNub;
        this.name = name;
        initPower();//生成随机权重
    }

    public void sendMessage(long enevtId, double parameter, boolean isStudy) throws Exception {
        if (son.size() > 0) {
            for (Nerve nerve : son) {
                nerve.input(enevtId, parameter, isStudy);
            }
        } else {
            throw new Exception("this layer is lastIndex");
        }
    }

    public void backSendMessage(double parameter) throws Exception {//反向传播
        if (fathor.size() > 0) {
            for (Nerve nerve : fathor) {
                nerve.backGetMessage(parameter);
            }
        } else {
            throw new Exception("this layer is firstIndex");
        }
    }

    protected void input(long eventId, double parameter, boolean isStudy) throws Exception {//输入

    }

    private void backGetMessage(double parameter) {//反向传播

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
            sigma = ArithUtil.add(ArithUtil.mul(w, value), sigma);
            logger.debug("name:{},eventId:{},id:{},myId:{},w:{},value:{}", name, eventId, i + 1, id, w, value);
        }
        logger.debug("当前神经元线性变化已经完成,name:{},id:{}", name, getId());
        return ArithUtil.sub(sigma, threshold);
    }

    private void initPower() {//初始化权重及阈值
        if (upNub > 0) {
            Random random = new Random();
            for (int i = 1; i < upNub + 1; i++) {
                dendrites.put(i, random.nextDouble());
            }
            //生成随机阈值
            threshold = ArithUtil.mul(random.nextDouble(), 10);
        }
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
