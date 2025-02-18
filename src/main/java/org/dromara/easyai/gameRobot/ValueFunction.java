package org.dromara.easyai.gameRobot;

public class ValueFunction {//价值函数
    private int[] stateId;//状态id
    private float value;//价值

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
