package org.wlld;

import org.wlld.gameRobot.DynamicProgramming;
import org.wlld.gameRobot.DynamicState;

import java.util.List;


public class GameRobotTest {
    private static DynamicProgramming dynamicProgramming = new DynamicProgramming();//中奖机器人智能机器人(中奖)

    //游戏机器人测试
    //这是一个寻找宝藏的游戏，地图是一个6*6大小的格子地图，游戏里有1个宝藏，三个炸弹，每次宝藏和炸弹刷新的位置随机。
    //一个小人从地图的随机一个位置出发，共走六步，如果能拿到指定宝藏就成功。
    // 如果六步没有拿到宝藏就失败，或者六步之内撞到地图边缘同样也是失败，如果碰到炸弹同样失败。
    public static void main(String[] args) throws Exception {


    }

    private static void init() {//进行状态初始化
        List<DynamicState> dynamicStates = dynamicProgramming.getDynamicStateList();
        for (int i = 1; i < 5; i++) {//
            DynamicState dynamicState = new DynamicState(new int[]{1, i});
            dynamicStates.add(dynamicState);
        }
        for (int i = 1; i < 5; i++) {
            for (DynamicState dynamicState : dynamicStates) {
                if (dynamicState.getStateId()[0] == i) {

                }
            }
        }
    }

}
