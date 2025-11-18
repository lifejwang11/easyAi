package org.dromara.easyai.gameRobot;

import org.dromara.easyai.matrixTools.Matrix;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lidapeng
 * @description 动态规划
 * @date 10:25 上午 2022/9/12
 */
public class DynamicProgramming {
    private final List<DynamicState> dynamicStateList = new ArrayList<>();//状态集合
    private final Map<Integer, Action> actionMap = new ConcurrentHashMap<>();//动作列表
    private final List<Integer> bestStrategy = new ArrayList<>();//最佳策略
    private float gaMa = 0.5F; //贴现因子
    private float valueTh = 0.0001f;//价值阈值
    private int maxTimes = 500;//策略改进最大迭代次数

    public void setMaxTimes(int maxTimes) {
        this.maxTimes = maxTimes;
    }

    public void setValueTh(float valueTh) {
        this.valueTh = valueTh;
    }

    public void setGaMa(float gaMa) {
        this.gaMa = gaMa;
    }

    public List<DynamicState> getDynamicStateList() {
        return dynamicStateList;
    }

    public Map<Integer, Action> getActionMap() {
        return actionMap;
    }

    public void gameStart() {//遍历所有状态
        for (DynamicState dynamicState : dynamicStateList) {
            if (!dynamicState.isFinish()) {
                dynamicState.add();//被执行次数+1
                Map<Integer, List<DynamicState>> sonStatesMap = dynamicState.getSonStatesMap();//动作-子状态集合 被执行的时候需要修改
                int[] stateId = dynamicState.getStateId();
                for (Map.Entry<Integer, Action> actionEntry : actionMap.entrySet()) {
                    Action action = actionEntry.getValue();
                    int actionId = action.getActionId();//动作id
                    List<int[]> stateList = action.action(stateId);
                    for (int[] myStateId : stateList) {
                        DynamicState state = getStateByStateId(myStateId);//经过动作产生的新状态
                        //产生一个新的动作-子状态合集的元素
                        if (sonStatesMap.containsKey(actionId)) {//在动作集合里
                            List<DynamicState> dynamicStates = sonStatesMap.get(actionId);
                            if (!isHere(dynamicStates, state.getStateId())) {
                                dynamicStates.add(state);
                            }
                        } else {//创建动作-子状态集合
                            List<DynamicState> dynamicStateList = new ArrayList<>();
                            dynamicStateList.add(state);
                            sonStatesMap.put(actionId, dynamicStateList);
                        }
                        Map<Integer, Integer> profitMap = state.getProfitMap();//该状态的收益集合，主键是收益，值是次数 被执行的时候需要修改
                        state.add();
                        //产生一个新的收益
                        int profit = action.getProfit(stateId);
                        if (profitMap.containsKey(profit)) {
                            profitMap.put(profit, profitMap.get(profit) + 1);
                        } else {
                            profitMap.put(profit, 1);
                        }
                    }

                }
            }
        }
    }

    public Matrix getValueMatrix() throws Exception {//获取价值矩阵
        int size = dynamicStateList.size();
        int maxX = 0, maxY = 0;
        for (int i = 0; i < size; i++) {
            DynamicState dynamicState = dynamicStateList.get(i);
            int[] stateId = dynamicState.getStateId();
            int x = stateId[0];
            int y = stateId[1];
            if (x > maxX) {
                maxX = x;
            }
            if (y > maxY) {
                maxY = y;
            }
        }
        Matrix matrix = new Matrix(maxY + 1, maxX + 1);
        for (int i = 0; i < size; i++) {
            DynamicState dynamicState = dynamicStateList.get(i);
            int[] stateId = dynamicState.getStateId();
            float value = dynamicState.getValue();
            matrix.setNub(stateId[1], stateId[0], value);
        }
        return matrix;
    }

    public List<ValueFunction> getValueFunction() {//获取价值函数
        List<ValueFunction> valueFunctions = new ArrayList<>();
        int size = dynamicStateList.size();
        for (int i = 0; i < size; i++) {
            DynamicState dynamicState = dynamicStateList.get(i);
            ValueFunction valueFunction = new ValueFunction();
            valueFunction.setStateId(dynamicState.getStateId());
            valueFunction.setValue(dynamicState.getValue());
            valueFunctions.add(valueFunction);
        }
        return valueFunctions;
    }

    public List<Integer> getBestAction(int[] stateId) {//根据当前环境获取策略
        List<Integer> actions = new ArrayList<>();
        DynamicState state = getStateByStateId(stateId);//当前的环境
        if (state != null) {
            Map<Integer, List<DynamicState>> sonStatesMap = state.getSonStatesMap();
            float maxValue = 0;//最大价值
            boolean isFirstOne = true;
            for (Map.Entry<Integer, List<DynamicState>> entry : sonStatesMap.entrySet()) {
                List<DynamicState> sonStates = entry.getValue();//子状态
                float maxValue2 = 0;//actionId 的最大价值
                boolean isFirstTwo = true;
                for (DynamicState dynamicState : sonStates) {
                    float myValue = dynamicState.getValue();
                    if (myValue > maxValue2 || isFirstTwo) {
                        isFirstTwo = false;
                        maxValue2 = myValue;
                    }
                }
                if (maxValue2 > maxValue || isFirstOne) {
                    isFirstOne = false;
                    maxValue = maxValue2;
                }
            }
            //筛选等价值策略
            for (Map.Entry<Integer, List<DynamicState>> entry : sonStatesMap.entrySet()) {
                int actionId = entry.getKey();//动作id
                List<DynamicState> sonStates = entry.getValue();//子状态
                for (DynamicState dynamicState : sonStates) {
                    if (dynamicState.getValue() == maxValue) {
                        actions.add(actionId);
                        break;
                    }
                }
            }
        }
        return actions;
    }

    public void strategyStudy() throws Exception {//策略学习
        //记录当前最佳策略
        int times = 0;
        boolean isDifferent = true;//策略是否不同
        do {
            int size = dynamicStateList.size();
            for (int i = 0; i < size; i++) {
                DynamicState dynamicState = dynamicStateList.get(i);
                if (!dynamicState.isFinish()) {
                    int actionId = getBestStrategyByPro(dynamicState);
                    dynamicState.setBestActionId(actionId);//通过概率获取的当前状态最佳策略
                }
            }
            if (times > 0) {
                isDifferent = compareStrategy();
            }
            if (isDifferent) {//新老策略不同，重新评估策略
                updateBestStrategy();//更新新策略
                strategyEvaluation();
            }
            times++;
        } while (isDifferent && times < maxTimes);
    }

    private boolean isHere(List<DynamicState> dynamicStates, int[] stateId) {
        boolean isHere = false;
        for (DynamicState dynamicState : dynamicStates) {
            if (Arrays.equals(dynamicState.getStateId(), stateId)) {
                isHere = true;
                break;
            }
        }
        return isHere;
    }

    private DynamicState getStateByStateId(int[] stateId) {
        DynamicState state = null;
        for (DynamicState dynamicState : dynamicStateList) {
            if (Arrays.equals(dynamicState.getStateId(), stateId)) {
                state = dynamicState;
                break;
            }
        }
        return state;
    }

    private void updateBestStrategy() {//更新最佳策略
        int size = dynamicStateList.size();
        for (int i = 0; i < size; i++) {
            DynamicState dynamicState = dynamicStateList.get(i);
            if (!dynamicState.isFinish()) {
                bestStrategy.add(dynamicState.getBestActionId());
            } else {
                bestStrategy.add(0);
            }
        }
    }

    private boolean compareStrategy() {//比较新老策略
        int size = dynamicStateList.size();
        boolean isDifferent = false;
        for (int i = 0; i < size; i++) {
            DynamicState dynamicState = dynamicStateList.get(i);
            int actionId = bestStrategy.get(i);
            if (dynamicState.getBestActionId() != actionId) {
                isDifferent = true;
                break;
            }
        }
        return isDifferent;
    }

    private int getBestStrategyByPro(DynamicState dynamicState) throws Exception {//通过概率获取当前状态下的最佳策略
        Map<Integer, List<DynamicState>> sonStatesMap = dynamicState.getSonStatesMap();//动作-子状态集合
        float maxValue = 0;//最大价值
        boolean isFirst = true;
        int bestActionId = 0;//最佳动作
        for (Map.Entry<Integer, List<DynamicState>> entry : sonStatesMap.entrySet()) {
            int actionId = entry.getKey();//动作id
            float value = getValueByAction(dynamicState, actionId);//该动作的价值
            if (value > maxValue || isFirst) {
                isFirst = false;
                maxValue = value;
                bestActionId = actionId;
            }
        }
        //返回最佳动作
        return bestActionId;
    }

    private void strategyEvaluation() throws Exception {//策略评估
        float maxSub;//最大差值
        do {
            maxSub = 0;
            for (DynamicState dynamicState : dynamicStateList) {//当前状态
                if (!dynamicState.isFinish()) {//非终结态
                    float sub = valueEvaluation(dynamicState);//返回一个差
                    if (sub > maxSub) {
                        maxSub = sub;
                    }
                }
            }
        } while (maxSub >= valueTh);
    }

    private float getValueByAction(DynamicState dynamicState, int actionId) throws Exception {//通过动作获取价值
        Map<Integer, List<DynamicState>> sonStatesMap = dynamicState.getSonStatesMap();//动作-子状态集合
        List<DynamicState> sonStateListByAction = sonStatesMap.get(actionId);//当前状态最优策略动作下的子状态集合
        if (sonStateListByAction == null) {
            throw new Exception("该状态无下一步动作！可能该状态属于终结态，但并没有设置为终结态！");
        }
        float number = dynamicState.getNumber();//当前状态被执行的次数即所有子状态被执行的总数
        float updateValue = 0;//更新价值
        for (DynamicState sonState : sonStateListByAction) {
            Map<Integer, Integer> profitMap = sonState.getProfitMap();//子状态收益集合
            float sonNumber = sonState.getNumber();//该子状态执行的次数即该子状态所有收益被执行的总次数
            float sonPro = sonNumber / number;//当前子状态在当前最优策略动作下被执行的概率
            float value = sonState.getValue() * gaMa;//该子状态的价值
            //先对r求和
            float sigmaR = 0;
            for (Map.Entry<Integer, Integer> entryProfit : profitMap.entrySet()) {
                float profit = entryProfit.getKey();//--获取profit的收益
                float profitNumber = entryProfit.getValue();//--获取profit收益的次数
                float profitPro = (profitNumber / sonNumber) * sonPro;//在当前策略动作下产生的环境，产生profit收益的概率
                float v = (value + profit) * profitPro;//价值
                sigmaR = sigmaR + v;
            }
            updateValue = updateValue + sigmaR;
        }
        return updateValue;
    }

    private float valueEvaluation(DynamicState dynamicState) throws Exception {//价值评估
        float myValue = dynamicState.getValue();//当前价值
        int bestActionId = dynamicState.getBestActionId();//当前最优策略选择的动作
        float updateValue = getValueByAction(dynamicState, bestActionId);
        dynamicState.setValue(updateValue);//更新价值
        return (float)Math.abs(myValue - updateValue);
    }
    //策略迭代
    //状态是当前环境的向量主键与价值函数
    //动作（包含动作的规则）是执行完成一个动作之后返回一个新的状态
    //策略决定执行什么动作，动作执行结束之后的收益是多少
}
