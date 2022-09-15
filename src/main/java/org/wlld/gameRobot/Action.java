package org.wlld.gameRobot;

public abstract class Action {//动作
    private int actionId;//动作主键

    public int getActionId() {
        return actionId;
    }

    protected void setActionId(int actionId) {//设置动作id
        this.actionId = actionId;
    }

    protected int[] action(int[] stateId) {
        return new int[0];
    }

    protected int getProfit(int[] stateId) {
        return 0;
    }


}
