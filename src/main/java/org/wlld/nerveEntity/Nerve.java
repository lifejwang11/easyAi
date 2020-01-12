package org.wlld.nerveEntity;


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
                    double studyPoint, boolean init, ActiveFunction activeFunction) {//该神经元在同层神经元中的编号
        this.id = id;
        this.upNub = upNub;
        this.name = name;
        this.downNub = downNub;
        this.studyPoint = studyPoint;
        this.activeFunction = activeFunction;
        initPower(init);//生成随机权重
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

    private void backSendMessage(long eventId) throws Exception {//反向传播
        if (fathor.size() > 0) {
            for (int i = 0; i < fathor.size(); i++) {
                fathor.get(i).backGetMessage(wg.get(i + 1), eventId);
            }
        }
    }

    protected void input(long eventId, double parameter, boolean isStudy
            , Map<Integer, Double> E) throws Exception {//输入

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

    private void initPower(boolean init) {//初始化权重及阈值
        if (upNub > 0) {
            Random random = new Random();
            for (int i = 1; i < upNub + 1; i++) {
                double nub = 0;
                if (init) {
                    nub = random.nextDouble();
                }
                dendrites.put(i, nub);//random.nextDouble()
            }
            //生成随机阈值
            double nub = 0;
            if (init) {
                nub = random.nextDouble();
            }
            threshold = nub;
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
