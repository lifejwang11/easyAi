package org.wlld.myAction;

import org.wlld.gameRobot.Action;

import java.util.Arrays;
import java.util.List;

public class MyAction extends Action {
    private int allTime;
    private int stepTime;
    private boolean isGood;

    public MyAction(int allTime, int stepTime, boolean isGood) {
        this.allTime = allTime;
        this.stepTime = stepTime;
        this.isGood = isGood;
    }

    @Override
    public int getActionId() {
        return super.getActionId();
    }

    @Override
    public void setActionId(int actionId) {
        super.setActionId(actionId);
    }

    @Override
    public int[] action(int[] stateId) {
        return new int[]{stateId[0] + 1, stateId[1] + stepTime};
    }

    @Override
    protected int getProfit(int[] stateId) {
        int profit = 0;
        int[] position = action(stateId);//移动后的坐标
        int nowTime = position[1];//当前所消耗的时间
        if (isGood) {
            if (position[0] == 4 && nowTime <= allTime) {
                profit = 10;
            } else if (position[0] == 4) {
                profit = -10;
            }
        } else {//不中奖
            if (position[0] == 4 && nowTime > allTime) {
                profit = 10;
            } else if (position[0] == 4) {
                profit = -10;
            }
        }
        return profit;
    }
}
