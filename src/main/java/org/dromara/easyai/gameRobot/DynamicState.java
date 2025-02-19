package org.dromara.easyai.gameRobot;

import java.util.*;

/**
 * @author lidapeng
 * @description 状态
 * @date 10:27 上午 2022/9/12
 */
public class DynamicState {
    private int[] stateId;//状态id
    private int bestActionId = 1;//该状态的最优动作
    private float value = 0;//该状态价值
    private int number = 0;//该状态被执行了几次 被执行的时候需要修改
    private boolean isFinish = false;//是否是终结态
    private Map<Integer, List<DynamicState>> sonStatesMap = new HashMap<>();//动作-子状态集合 被执行的时候需要修改
    private Map<Integer, Integer> profitMap = new HashMap<>();//该状态的收益集合，主键是收益，值是次数 被执行的时候需要修改

    public void add() {
        number++;
    }

    public int getNumber() {
        return number;
    }

    public int getBestActionId() {
        return bestActionId;
    }

    public void setBestActionId(int bestActionId) {
        this.bestActionId = bestActionId;
    }

    public Map<Integer, List<DynamicState>> getSonStatesMap() {
        return sonStatesMap;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }

    public Map<Integer, Integer> getProfitMap() {
        return profitMap;
    }

    public DynamicState(int[] stateId) {//设置状态id
        this.stateId = stateId;
    }

    public int[] getStateId() {
        return stateId;
    }

    public void setStateId(int[] stateId) {
        this.stateId = stateId;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
