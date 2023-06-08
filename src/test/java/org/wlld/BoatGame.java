package org.wlld;

import org.wlld.gameRobot.Action;
import org.wlld.gameRobot.DynamicProgramming;
import org.wlld.gameRobot.DynamicState;
import org.wlld.myAction.MyAction;

import java.util.*;


public class BoatGame {

    public List<int[]> start() {
        Random random = new Random();
        int index = random.nextInt(3);
        int winTime;
        switch (index) {
            case 0:
                winTime = 8;
                break;
            case 1:
                winTime = 9;
                break;
            default:
                winTime = 10;
                break;
        }
        List<int[]> myList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            DynamicProgramming dynamicProgramming = new DynamicProgramming();
            dynamicProgramming.setMaxTimes(200);
            boolean isWin;
            if (i == 0) {
                isWin = true;
            } else {
                isWin = false;
            }
            List<int[]> winList = init(isWin, winTime, dynamicProgramming);
            myList.add(winList.get(random.nextInt(winList.size())));
        }
        return myList;
    }

    private List<int[]> test(boolean isWin, int winTime, DynamicProgramming dynamicProgramming) {
        Random random = new Random();
        List<Integer> one = getAllStateByIndex(1, dynamicProgramming);
        List<Integer> two = getAllStateByIndex(2, dynamicProgramming);
        List<Integer> three = getAllStateByIndex(3, dynamicProgramming);
        List<int[]> list = new ArrayList<>();
        for (int time : one) {
            for (int i = 1; i < 5; i++) {
                int timeTwo = time + i;
                if (two.contains(timeTwo)) {//存在
                    for (int j = 1; j < 5; j++) {
                        int timeThree = timeTwo + j;
                        boolean next;
                        if (isWin) {
                            next = timeThree < winTime;
                        } else {
                            next = timeThree > (winTime - 4);
                        }
                        if (three.contains(timeThree) && next) {
                            int timeFour;
                            int four = winTime - timeThree;
                            if (isWin) {
                                timeFour = random.nextInt(four) + 1;
                            } else {
                                if (four <= 0) {
                                    timeFour = random.nextInt(4) + 1;
                                } else {
                                    timeFour = random.nextInt(4 - four) + 1 + four;
                                }
                            }
                            int[] times = new int[]{time, timeTwo - time, timeThree - timeTwo, timeFour};
                            list.add(times);
                        }
                    }
                }
            }
        }
        return list;
    }

    private List<Integer> getAllStateByIndex(int index, DynamicProgramming dynamicProgramming) {
        List<DynamicState> dynamicStates = dynamicProgramming.getDynamicStateList();
        List<Integer> list = new ArrayList<>();
        for (DynamicState dynamicState : dynamicStates) {
            if (dynamicState.getStateId()[0] == index && dynamicState.getValue() > 0) {
                list.add(dynamicState.getStateId()[1]);
            }
        }
        return list;
    }

    private List<int[]> init(boolean isWin, int winTime, DynamicProgramming dynamicProgramming) {//进行状态初始化
        List<DynamicState> dynamicStates = dynamicProgramming.getDynamicStateList();
        for (int i = 1; i < 5; i++) {
            //获取上一层的所有状态
            if (i > 1) {
                List<DynamicState> myStates = getState(i - 1, dynamicProgramming);
                for (DynamicState dynamicState : myStates) {
                    int[] state = dynamicState.getStateId();
                    for (int j = 1; j < 5; j++) {//第一层四个耗时\
                        int[] nextState = new int[]{state[0] + 1, state[1] + j};//如果存在该状态则不生成
                        if (!isHere(nextState, dynamicProgramming)) {
                            DynamicState myState = new DynamicState(nextState);
                            if (nextState[0] == 4) {
                                myState.setFinish(true);
                            }
                            dynamicStates.add(myState);
                        }
                    }
                }
            } else {
                for (int j = 1; j < 5; j++) {//第一层四个耗时
                    DynamicState myState = new DynamicState(new int[]{i, j});
                    dynamicStates.add(myState);
                }
            }
        }
        Map<Integer, Action> actionMap = dynamicProgramming.getActionMap();
        for (int i = 1; i < 5; i++) {
            MyAction myAction = new MyAction(winTime, i, isWin);
            myAction.setActionId(i);
            actionMap.put(i, myAction);
        }
        dynamicProgramming.gameStart();
        dynamicProgramming.strategyStudy();
        return test(isWin, winTime, dynamicProgramming);
    }

    private boolean isHere(int[] state, DynamicProgramming dynamicProgramming) {
        boolean isHere = false;
        List<DynamicState> dynamicStates = dynamicProgramming.getDynamicStateList();
        for (DynamicState dynamicState : dynamicStates) {
            if (Arrays.equals(dynamicState.getStateId(), state)) {
                isHere = true;
                break;
            }
        }
        return isHere;
    }

    private List<DynamicState> getState(int index, DynamicProgramming dynamicProgramming) {
        List<DynamicState> myStates = new ArrayList<>();
        List<DynamicState> dynamicStates = dynamicProgramming.getDynamicStateList();
        for (DynamicState dynamicState : dynamicStates) {
            if (dynamicState.getStateId()[0] == index) {
                myStates.add(dynamicState);
            }
        }
        return myStates;
    }

}
