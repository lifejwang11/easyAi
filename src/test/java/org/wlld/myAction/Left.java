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
    protected int[] action(int[] stateId) {//左移动作
        int x = stateId[0];//横坐标
        int y = stateId[1];//纵坐标
        //小人向左移动，如果小人自身就在左边缘则坐标原地不动（注意这里计算时为了方便学习策略逻辑用原地不动，游戏实际操作遇到这种移动则直接触发炸毁）
        //在逻辑运算中，因为实际并不存在 x=-1 的状态的实体类，但是我们又不能让它报空指针的错误，所以这里必须这么处理
        //至于为什么不能他添加一个x=-1状态的实体类，是因为如果添加了x=-1，那么必然还会遇到x=-1时减一的情况，那么就要创建x=-2,之后无穷尽。
        // 所以在逻辑运算中必须原地不动来作为处理方式。
        if (x > 0) {
            x = x - 1;//左移
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
