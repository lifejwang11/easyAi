package org.wlld;

import org.wlld.MatrixTools.Matrix;
import org.wlld.gameRobot.Action;
import org.wlld.gameRobot.DynamicProgramming;
import org.wlld.gameRobot.DynamicState;
import org.wlld.myAction.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameRobotTest {
    private static DynamicProgramming dynamicProgramming = new DynamicProgramming();
    //游戏机器人测试
    //这是一个寻找宝藏的游戏，地图是一个6*6大小的格子地图，游戏里有1个宝藏，三个炸弹，每次宝藏和炸弹刷新的位置随机。
    //一个小人从地图的随机一个位置出发，共走六步，如果能拿到四个宝藏中的指定宝藏就成功。
    // 如果五步没有拿到宝藏就失败，或者五步之内撞到地图边缘同样也是失败，如果碰到炸弹同样失败。
    public static void main(String[] args) throws Exception {
        dynamicProgramming.setGaMa(0.9);//取值范围(0-1)，值越低越注重短期收益，值越高越注重长期收益
        dynamicProgramming.setMaxTimes(500);//取值范围（正整数），值越低速度越快精度越低，值越大速度越慢，精度越高
        dynamicProgramming.setValueTh(0.0001);//取值范围（很小的正数），值越低精度越大速度越慢，值越大精度越小速度越快
        GameConfig gameConfig = new GameConfig();
        init(gameConfig);
        List<int[]> prizeList = gameConfig.getPrizeList();//生成随机四个奖品坐标
        int size = prizeList.size();
        for (int i = 0; i < size; i++) {
            System.out.println("i==" + i + ",cood==" + Arrays.toString(prizeList.get(i)));
        }
        dynamicProgramming.gameStart();//先跑数据
        dynamicProgramming.strategyStudy();//研究策略
        Matrix matrix = dynamicProgramming.getValueMatrix();
        System.out.println(matrix.getString());
    }

    public static void init(GameConfig gameConfig) {//初始化数据
        Random random = new Random();
        //初始化随机奖品位置
        List<int[]> prizeList = gameConfig.getPrizeList();//生成随机四个奖品坐标
        for (int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    prizeList.add(new int[]{random.nextInt(3), random.nextInt(3)});
                    break;
                case 1:
                    prizeList.add(new int[]{random.nextInt(3) + 3, random.nextInt(3)});
                    break;
                case 2:
                    prizeList.add(new int[]{random.nextInt(3) + 3, random.nextInt(3) + 3});
                    break;
                case 3:
                    prizeList.add(new int[]{random.nextInt(3), random.nextInt(3) + 3});
                    break;
            }
        }
        //加载状态
        List<DynamicState> dynamicStateList = dynamicProgramming.getDynamicStateList();
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                int[] stateId = new int[]{i, j};
                DynamicState dynamicState = new DynamicState(stateId);
                //dynamicState.setFinish(isFinish(gameConfig, stateId));//设置终结态
                dynamicStateList.add(dynamicState);
            }
        }
        //加载动作
        Map<Integer, Action> actionMap = dynamicProgramming.getActionMap();
        Left left = new Left(gameConfig);
        Right right = new Right(gameConfig);
        Up up = new Up(gameConfig);
        Down down = new Down(gameConfig);
        left.setActionId(1);
        right.setActionId(2);
        up.setActionId(3);
        down.setActionId(4);
        actionMap.put(1, left);
        actionMap.put(2, right);
        actionMap.put(3, up);
        actionMap.put(4, down);
    }

    private static boolean isFinish(GameConfig gameConfig, int[] state) {
        boolean isFinish = false;
        List<int[]> prizeList = gameConfig.getPrizeList();
        for (int[] prize : prizeList) {
            if (Arrays.equals(state, prize)) {
                isFinish = true;
                break;
            }
        }
        return isFinish;
    }
}
