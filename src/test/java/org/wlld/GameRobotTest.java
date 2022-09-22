package org.wlld;

import org.wlld.MatrixTools.Matrix;
import org.wlld.gameRobot.Action;
import org.wlld.gameRobot.DynamicProgramming;
import org.wlld.gameRobot.DynamicState;
import org.wlld.myAction.*;

import java.util.*;

public class GameRobotTest {
    private static DynamicProgramming dynamicProgramming = new DynamicProgramming();//中奖机器人智能机器人(中奖)
    private static DynamicProgramming dynamicProgrammingTwo = new DynamicProgramming();//不中奖智能机器人(不中奖即往墙上撞)

    //游戏机器人测试
    //这是一个寻找宝藏的游戏，地图是一个6*6大小的格子地图，游戏里有1个宝藏，三个炸弹，每次宝藏和炸弹刷新的位置随机。
    //一个小人从地图的随机一个位置出发，共走六步，如果能拿到指定宝藏就成功。
    // 如果六步没有拿到宝藏就失败，或者六步之内撞到地图边缘同样也是失败，如果碰到炸弹同样失败。
    public static void main(String[] args) throws Exception {
        Random random = new Random();
        GameConfig gameConfig = new GameConfig();
        int gameIndex = random.nextInt(8);//设定随机中奖下标，其他下标为炸弹
        gameConfig.setPrizeIndex(gameIndex);
        List<int[]> steps = start(gameConfig, true);//执行游戏 (配置参数，是否获奖)

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

    private static List<int[]> isCollisionWall(int[] position) {//是否撞墙
        Map<Integer, Action> actionMap = dynamicProgrammingTwo.getActionMap();
        List<int[]> states = new ArrayList<>();
        for (Map.Entry<Integer, Action> entry : actionMap.entrySet()) {
            Action action = entry.getValue();
            int[] stateID = action.actionTest(position);
            int x = stateID[0];
            int y = stateID[1];
            if (x == 6 && y == 6) {
                states.add(stateID);
            }
        }
        return states;
    }

    private static boolean isCup(int[] position, GameConfig gameConfig) {//是否踩雷或撞墙
        List<int[]> prizeList = gameConfig.getPrizeList();
        boolean isCup = false;
        //验证是否撞到炸弹
        for (int i = 0; i < prizeList.size(); i++) {
            if (Arrays.equals(position, prizeList.get(i))) {
                isCup = true;
            }
        }
        return isCup;
    }

    private static boolean isBoom(int[] position, GameConfig gameConfig) throws Exception {//是否踩雷或撞墙
        boolean isBoom = false;
        List<int[]> prizeList = gameConfig.getPrizeList();
        //验证是否撞到炸弹
        for (int i = 0; i < prizeList.size(); i++) {
            if (i != gameConfig.getPrizeIndex() && Arrays.equals(position, prizeList.get(i))) {
                isBoom = true;
            }
        }
        //撞墙了
        int x = position[0];
        int y = position[1];
        if ((!isBoom) && x == 6 && y == 6) {
            isBoom = true;
            throw new Exception("失败======");
        }
        return isBoom;
    }

    private static List<int[]> getBadStrategy(GameConfig gameConfig) throws Exception {
        List<int[]> steps = new ArrayList<>();
        Random random = new Random();
        int[] startPosition;
        Matrix matrix = dynamicProgrammingTwo.getValueMatrix();
        double value;
        do {
            startPosition = new int[]{random.nextInt(6), random.nextInt(6)};
            value = matrix.getNumber(startPosition[1], startPosition[0]);
        } while (value <= 0);
        int step = 6;//步数
        int[] actionId = startPosition;
        steps.add(startPosition);
        Map<Integer, Action> actionMap = dynamicProgrammingTwo.getActionMap();
        for (int i = 0; i < step; i++) {
            if (i == 0) {
                System.out.println("我从" + Arrays.toString(actionId) + "出发了！");
            }
            List<int[]> actionIDS = isCollisionWall(actionId);
            if (actionIDS.size() > 0) {//撞墙了
                actionId = actionIDS.get(random.nextInt(actionIDS.size()));
                steps.add(actionId);
                System.out.println("撞墙：我走到目的地:" + Arrays.toString(actionId) + ",且总共走了" + (i + 1) + "步!");
                break;
            } else {
                List<Integer> actionList = dynamicProgrammingTwo.getBestAction(actionId);//获取下一步行动
                int size = actionList.size();
                int nextActionId = actionList.get(random.nextInt(size));
                actionId = actionMap.get(nextActionId).actionTest(actionId);
                steps.add(actionId);
                System.out.println("第" + (i + 1) + "步,我走到了" + Arrays.toString(actionId));
            }

            if (isCup(actionId, gameConfig)) {
                System.out.println("我踩到奖品了！坐标:" + Arrays.toString(actionId));
            }
        }

        return steps;
    }

    private static List<int[]> getBestStrategy(GameConfig gameConfig) throws Exception {
        Random random = new Random();
        List<int[]> steps = new ArrayList<>();
        Matrix matrix = dynamicProgramming.getValueMatrix();
        do {
            steps.clear();
            int[] startPosition;
            double value;
            do {
                startPosition = new int[]{random.nextInt(6), random.nextInt(6)};
                value = matrix.getNumber(startPosition[1], startPosition[0]);
            } while (value <= 0);//起点的价值必须大于0，因为价值为0是终结态，价值小于0 的状态四周被终结态围堵。
            int step = 12;//步数
            int[] actionId = startPosition;
            steps.add(startPosition);
            Map<Integer, Action> actionMap = dynamicProgramming.getActionMap();
            for (int i = 0; i < step; i++) {
                if (i == 0) {
                    System.out.println("我从" + Arrays.toString(actionId) + "出发了！");
                }
                //首先确认下一步有没有奖品
                int key = isPrize(gameConfig, actionId);
                if (key != 0) {//移动中奖了
                    int[] finish = actionMap.get(key).actionTest(actionId);
                    steps.add(finish);
                    System.out.println("中奖：我走到目的地:" + Arrays.toString(finish) + ",且总共走了" + (i + 1) + "步!");
                    break;
                } else {//随机获取下一步行动
                    List<Integer> actionList = dynamicProgramming.getBestAction(actionId);//获取下一步行动
                    int size = actionList.size();
                    int nextActionId = actionList.get(random.nextInt(size));
                    actionId = actionMap.get(nextActionId).actionTest(actionId);
                    System.out.println("第" + (i + 1) + "步,我走到了" + Arrays.toString(actionId));
                }
                if (isBoom(actionId, gameConfig)) {
                    System.out.println("撞墙或者踩到炸弹了！坐标:" + Arrays.toString(actionId));
                }
                steps.add(actionId);
            }
        } while ((steps.size() - 1) > 12);
        return steps;
    }

    //配置参数，是否中奖
    public static List<int[]> start(GameConfig gameConfig, boolean isWinPrize) throws Exception {//初始化数据
        List<int[]> steps;
        Random random = new Random();
        //初始化随机奖品位置
        List<int[]> prizeList = gameConfig.getPrizeList();//生成随机四个奖品坐标
        int[] position = new int[0];
        int[] myPosition;
        for (int i = 0; i < 8; i++) {//设置8个坐标集合
            switch (i) {
                case 0:
                    position = new int[]{random.nextInt(3), random.nextInt(3)};
                    prizeList.add(position);
                    break;
                case 1:
                    do {
                        myPosition = new int[]{random.nextInt(3), random.nextInt(3)};
                    } while (Arrays.equals(myPosition, position));
                    prizeList.add(myPosition);
                    break;
                case 2:
                    position = new int[]{random.nextInt(3) + 3, random.nextInt(3)};
                    prizeList.add(position);
                    break;
                case 3:
                    do {
                        myPosition = new int[]{random.nextInt(3) + 3, random.nextInt(3)};
                    } while (Arrays.equals(myPosition, position));
                    prizeList.add(myPosition);
                    break;
                case 4:
                    position = new int[]{random.nextInt(3) + 3, random.nextInt(3) + 3};
                    prizeList.add(position);
                    break;
                case 5:
                    do {
                        myPosition = new int[]{random.nextInt(3) + 3, random.nextInt(3) + 3};
                    } while (Arrays.equals(myPosition, position));
                    prizeList.add(myPosition);
                    break;
                case 6:
                    position = new int[]{random.nextInt(3), random.nextInt(3) + 3};
                    prizeList.add(position);
                    break;
                case 7:
                    do {
                        myPosition = new int[]{random.nextInt(3), random.nextInt(3) + 3};
                    } while (Arrays.equals(myPosition, position));
                    prizeList.add(myPosition);
                    break;
            }
        }
        //加载状态 生成地图
        List<DynamicState> dynamicStateList = dynamicProgramming.getDynamicStateList();
        List<DynamicState> dynamicStateListTwo = dynamicProgrammingTwo.getDynamicStateList();
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                int[] stateId = new int[]{i, j};
                DynamicState dynamicState = new DynamicState(stateId);
                DynamicState dynamicState2 = new DynamicState(stateId);
                if (isFinish(gameConfig, stateId)) {
                    dynamicState.setFinish(true);//设置终结态
                    dynamicState2.setFinish(true);//设置终结态
                }
                dynamicStateList.add(dynamicState);
                dynamicStateListTwo.add(dynamicState2);
            }
        }
        int[] state = new int[]{6, 6};//创建一个撞墙终结态，即一旦撞墙，则自动跳转到[6,6]的状态
        DynamicState dynamicState = new DynamicState(state);
        DynamicState dynamicState2 = new DynamicState(state);
        dynamicState.setFinish(true);
        dynamicState2.setFinish(true);
        dynamicStateList.add(dynamicState);
        dynamicStateListTwo.add(dynamicState2);
        //加载动作
        Map<Integer, Action> actionMap = dynamicProgramming.getActionMap();
        Map<Integer, Action> actionMapTwo = dynamicProgrammingTwo.getActionMap();
        Left left = new Left(gameConfig);
        Right right = new Right(gameConfig);
        Up up = new Up(gameConfig);
        Down down = new Down(gameConfig);
        LeftTwo left2 = new LeftTwo(gameConfig);
        RightTwo right2 = new RightTwo(gameConfig);
        UpTwo up2 = new UpTwo(gameConfig);
        DownTwo down2 = new DownTwo(gameConfig);
        left.setActionId(1);
        right.setActionId(2);
        up.setActionId(3);
        down.setActionId(4);
        left2.setActionId(1);
        right2.setActionId(2);
        up2.setActionId(3);
        down2.setActionId(4);
        actionMap.put(1, left);
        actionMap.put(2, right);
        actionMap.put(3, up);
        actionMap.put(4, down);
        actionMapTwo.put(1, left2);
        actionMapTwo.put(2, right2);
        actionMapTwo.put(3, up2);
        actionMapTwo.put(4, down2);
        List<int[]> p = gameConfig.getPrizeList();//获取这个
        int size = p.size();
        for (int i = 0; i < size; i++) {
            System.out.println("i==" + i + ":" + Arrays.toString(p.get(i)));
        }
        System.out.println("中奖下标：" + gameConfig.getPrizeIndex());
        if (isWinPrize) {
            dynamicProgramming.gameStart();//探索中奖
            dynamicProgramming.strategyStudy();//研究策略中奖
            System.out.println(dynamicProgramming.getValueMatrix().getString());
            steps = getBestStrategy(gameConfig);
        } else {
            dynamicProgrammingTwo.gameStart();//探索不中奖
            dynamicProgrammingTwo.strategyStudy();//研究策略不中奖
            steps = getBadStrategy(gameConfig);
        }
        return steps;
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
