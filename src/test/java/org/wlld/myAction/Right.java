package org.wlld.myAction;

import org.wlld.gameRobot.Action;

import java.util.Arrays;
import java.util.List;

public class Right extends Action {
    public GameConfig gameConfig;

    public Right(GameConfig gameConfig) {
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
    protected int[] action(int[] stateId) {//右移
        int x = stateId[0];//横坐标
        int y = stateId[1];//纵坐标
        if (x < 5) {
            x = x + 1;//左移
        }
        return new int[]{x, y};
    }

    @Override
    protected int getProfit(int[] stateId) {
        int profit = 0;
        if (stateId[0] == 5) {//如果右移会撞墙，给惩罚-10
            profit = -10;
        } else {//如果没有撞墙
            int[] position = action(stateId);//移动后的坐标
            List<int[]> prizeList = gameConfig.getPrizeList();//奖项坐标集合
            int gameIndex = gameConfig.getPrizeIndex();//中奖下标
            int size = prizeList.size();
            for (int i = 0; i < size; i++) {
                int[] prize = prizeList.get(i);
                if (i == gameIndex && Arrays.equals(position, prize)) {//到指定奖品位置坐标了
                    profit = 10;//中奖的话 给个10的收益奖励
                    break;
                } else if (i != gameIndex && Arrays.equals(position, prize)) {//中的不是我想要的奖品，给-10的惩罚
                    profit = -10;
                    break;
                }
            }
        }
        return profit;
    }
}
