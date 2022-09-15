package org.wlld.gameRobot;

public class ValueFunction {//价值函数
    private int[] stateId;//状态id
    private double value;//价值

    public int[] getStateId() {
        return stateId;
    }

    public void setStateId(int[] stateId) {
        this.stateId = stateId;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
