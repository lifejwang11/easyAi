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
    private static DynamicProgramming dynamicProgramming = new DynamicProgramming();//智能机器人

    //游戏机器人测试
    //这是一个寻找宝藏的游戏，地图是一个6*6大小的格子地图，游戏里有1个宝藏，三个炸弹，每次宝藏和炸弹刷新的位置随机。
    //一个小人从地图的随机一个位置出发，共走六步，如果能拿到指定宝藏就成功。
    // 如果六步没有拿到宝藏就失败，或者六步之内撞到地图边缘同样也是失败，如果碰到炸弹同样失败。
    public static void main(String[] args) throws Exception {
        //dynamicProgramming.setGaMa(0.9);//取值范围(0-1)，值越低越注重短期收益，值越高越注重长期收益
        //dynamicProgramming.setMaxTimes(500);//取值范围（正整数），值越低速度越快精度越低，值越大速度越慢，精度越高
        //dynamicProgramming.setValueTh(0.0001);//取值范围（很小的正数），值越低精度越大速度越慢，值越大精度越小速度越快
        GameConfig gameConfig = new GameConfig();
        init(gameConfig);
        List<int[]> prizeList = gameConfig.getPrizeList();//生成随机四个奖品坐标
        int size = prizeList.size();
        for (int i = 0; i < size; i++) {
            System.out.println("i==" + i + ",cood==" + Arrays.toString(prizeList.get(i)));
        }
        dynamicProgramming.gameStart();//探索
        dynamicProgramming.strategyStudy();//研究策略
        //List<ValueFunction> valueFunctions = dynamicProgramming.getValueFunction();//返回一个多维的获取价值函数
        //List<Integer> actionList = dynamicProgramming.getBestAction(new int[]{1, 2});//获取下一步行动
        //返回一个动作id集合
        Matrix matrix = dynamicProgramming.getValueMatrix();//返回一个二维状态主键可打印的价值函数
        System.out.println(matrix.getString());
        start(gameConfig);//执行游戏
    }

    private static int isPrize(GameConfig gameConfig, int[] position) {//是否有奖品
        Map<Integer, Action> actionMap = dynamicProgramming.getActionMap();
        int[] prize = gameConfig.getPrizeList().get(gameConfig.getPrizeIndex());
        int key = 0;
        for (Map.Entry<Integer, Action> entry : actionMap.entrySet()) {
            int actionId = entry.getKey();
            Action action = entry.getValue();
            if (Arrays.equals(action.action(position), prize)) {
                key = actionId;
                break;
            }
        }
        return key;
    }

    private static boolean isBoom(int[] position, GameConfig gameConfig) {//是否踩雷或撞墙
        List<int[]> prizeList = gameConfig.getPrizeList();
        boolean isBoom = false;
        //验证是否撞到炸弹
        for (int i = 0; i < prizeList.size(); i++) {
            if (i != gameConfig.getPrizeIndex() && Arrays.equals(position, prizeList.get(i))) {
                isBoom = true;
            }
        }
        //验证是否撞到墙
        int x = position[0];
        int y = position[1];
        if ((!isBoom) && (x < 0 || y < 0 || x > 5 || y > 5)) {
            isBoom = true;
        }
        return isBoom;
    }

    public static void start(GameConfig gameConfig) {
        Random random = new Random();
        int[] startPosition;
        do {
            startPosition = new int[]{random.nextInt(6), random.nextInt(6)};
        } while (isFinish(gameConfig, startPosition));
        int step = 12;//步数
        int[] actionId = startPosition;
        Map<Integer, Action> actionMap = dynamicProgramming.getActionMap();
        for (int i = 0; i < step; i++) {
            if (i == 0) {
                System.out.println("我从" + Arrays.toString(actionId) + "出发了！");
            }
            //首先确认下一步有没有奖品
            int key = isPrize(gameConfig, actionId);
            if (key != 0) {//移动中奖了
                int[] finish = actionMap.get(key).action(actionId);
                System.out.println("我走到目的地:" + Arrays.toString(finish) + ",且总共走了" + (i + 1) + "步!");
                break;
            } else {//随机获取下一步行动
                List<Integer> actionList = dynamicProgramming.getBestAction(actionId);//获取下一步行动
                int size = actionList.size();
                int nextActionId = actionList.get(random.nextInt(size));
                actionId = actionMap.get(nextActionId).action(actionId);
                System.out.println("第" + (i + 1) + "步,我走到了" + Arrays.toString(actionId));
            }
            if (isBoom(actionId, gameConfig)) {
                System.out.println("撞墙或者踩到炸弹了！坐标:" + Arrays.toString(actionId));
            }
        }
    }

    public static void init(GameConfig gameConfig) {//初始化数据
        Random random = new Random();
        //初始化随机奖品位置
        List<int[]> prizeList = gameConfig.getPrizeList();//生成随机四个奖品坐标
        int[] cup = new int[]{random.nextInt(3) + 3, random.nextInt(3) + 3};
        for (int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    prizeList.add(new int[]{random.nextInt(3), random.nextInt(3)});
                    break;
                case 1:
                    prizeList.add(new int[]{random.nextInt(3) + 3, random.nextInt(3)});
                    break;
                case 2:
                    prizeList.add(cup);
                    break;
                case 3:
                    prizeList.add(new int[]{random.nextInt(3), random.nextInt(3) + 3});
                    break;
            }
        }
        //加载状态 生成地图
        List<DynamicState> dynamicStateList = dynamicProgramming.getDynamicStateList();
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                int[] stateId = new int[]{i, j};
                DynamicState dynamicState = new DynamicState(stateId);
                dynamicState.setFinish(isFinish(gameConfig, stateId));//设置终结态
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
