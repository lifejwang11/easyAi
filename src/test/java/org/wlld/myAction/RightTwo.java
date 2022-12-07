package org.wlld.myAction;

import org.wlld.gameRobot.Action;

import java.util.Arrays;
import java.util.List;

public class RightTwo extends Action {
    public GameConfig gameConfig;

    public RightTwo(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
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
        int x = stateId[0];//横坐标
        int y = stateId[1];//纵坐标
        x = x + 1;//左移
        if (x > 5) {
            x = 6;
            y = 6;
        }
        return new int[]{x, y};
    }

    @Override
    public int[] actionTest(int[] stateId) {
        int x = stateId[0];//横坐标
        int y = stateId[1];//纵坐标
        x = x + 1;//左移
        if (x > 5) {
            x = 6;
            y = 6;
        }
        return new int[]{x, y};
    }

    @Override
    protected int getProfit(int[] stateId) {
        int profit = 0;
        if (stateId[0] == 5) {//如果右移会撞墙，给奖励10
            profit = 10;
        } else {//如果没有撞墙
            int[] position = action(stateId);//移动后的坐标
            List<int[]> prizeList = gameConfig.getPrizeList();//奖项坐标集合
            int size = prizeList.size();
            for (int i = 0; i < size; i++) {
                int[] prize = prizeList.get(i);
                if (Arrays.equals(position, prize)) {//到指定奖品位置坐标了
                    profit = -10;//中奖的话 给个-10的收益奖励
                    break;
                }
            }
        }
        return profit;
    }
}