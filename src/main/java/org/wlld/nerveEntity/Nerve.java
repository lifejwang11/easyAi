package org.wlld.nerveEntity;

import java.util.*;

/**
 * @author lidapeng
 * 神经元，所有类别神经元都要继承的类，具有公用属性
 * @date 9:36 上午 2019/12/21
 */
public abstract class Nerve {
    private double message;//该神经元输出结果
    private List<Nerve> axon = new ArrayList<>();//轴突下一层的连接神经元
    protected Map<Integer, Double> dendrites = new HashMap<>();//上一层权重
    private int id;//同级神经元编号,注意在同层编号中ID应有唯一性
    private int upNub;//上一层神经元数量

    protected Nerve(int id, int upNub) {//该神经元在同层神经元中的编号
        this.id = id;
        this.upNub = upNub;
        initPower();//生成随机权重
    }

    public boolean sendMessage() {
        boolean isFinish = false;
        if (axon.size() > 0) {
            isFinish = true;
            for (Nerve nerve : axon) {
                nerve.input(this);
            }
        }
        return isFinish;
    }

    public void input(Nerve nerve) {//输入
    }

    private void initPower() {//初始化权重
        if (upNub > 0) {
            Random random = new Random();
            for (int i = 1; i < upNub + 1; i++) {
                dendrites.put(i, random.nextDouble());
            }
        }
    }

    public double getMessage() {
        return message;
    }

    public void setMessage(double message) {
        this.message = message;
    }

    public int getId() {
        return id;
    }


    public void connect(List<Nerve> nerveList) {
        axon.addAll(nerveList);
    }
}
