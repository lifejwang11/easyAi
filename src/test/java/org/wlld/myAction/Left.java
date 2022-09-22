package org.wlld.myAction;

import org.wlld.gameRobot.Action;

import java.util.Arrays;
import java.util.List;

public class Left extends Action {//向左移动
    public GameConfig gameConfig;

    public Left(GameConfig gameConfig) {
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

    @Override//动作 参数是 执行该动作的状态
    public int[] action(int[] stateId) {//左移动作 动作方法 用来逻辑运算动作的
        int x = stateId[0];//横坐标
        int y = stateId[1];//纵坐标
        x = x - 1;//左移
        if (x < 0) {
            x = 6;
            y = 6;
        }
        return new int[]{x, y};
    }

    @Override
    public int[] actionTest(int[] stateId) {//动作测试方法，用来测试实际移动效果的
        int x = stateId[0];//横坐标
        int y = stateId[1];//纵坐标
        x = x - 1;//左移
        if (x < 0) {//设置一个规则，即一旦撞墙，则自动跳转到[6,6]的状态
            x = 6;
            y = 6;
        }
        return new int[]{x, y};
    }

    @Override//该动作带来的收益
    protected int getProfit(int[] stateId) {//左移后的惩罚或者奖励值设置
        int profit = 0;
        if (stateId[0] == 0) {//如果左移会撞墙，给惩罚-10
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
