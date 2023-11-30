package org.wlld.rnnJumpNerveEntity;



import org.wlld.MatrixTools.Matrix;
import org.wlld.i.OutBack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NerveCenter {//神经中枢
    private final int depth;//该神经中枢的深度
    private final double powerTh;//权重阈值
    private final List<Nerve> nerveList;//该神经中枢控制的对应隐层神经元集合
    private final List<NerveCenter> nerveCenterList;//所有神经中枢
    private final Map<Long, List<MyPower>> myPowerMap = new HashMap<>();//接收前方各层输出层返回权重

    public int getDepth() {
        return depth;
    }

    public NerveCenter(int depth, List<Nerve> nerveList, List<NerveCenter> nerveCenterList, double powerTh) {
        this.depth = depth;
        this.nerveList = nerveList;
        this.nerveCenterList = nerveCenterList;
        this.powerTh = powerTh;
    }

    public void backPower(long eventId, double parameter, int fromDepth, List<Integer> storeys, Matrix featureMatrix, OutBack outBack) throws Exception {
        //System.out.println("开始回传给指定中枢,本层为：" + depth + ",中枢集合数量为：" + nerveCenterList.size());
        for (NerveCenter nerveCenter : nerveCenterList) {
            if (nerveCenter.getDepth() == fromDepth) {
                //System.out.println("回传结果:" + fromDepth + ",我的深度为:" + depth);
                nerveCenter.collectPower(eventId, parameter, storeys, depth, featureMatrix, outBack);
                break;
            }
        }
    }

    private void go(long eventId, int fromDepth, Matrix featureMatrix, List<Integer> storeys, OutBack outBack) throws Exception {
        //神经中枢收到传递命令 将命令传递给本层神经元
        for (Nerve nerve : nerveList) {//将信息发送给目标层隐层神经元
            nerve.sendMyTestMessage(eventId, fromDepth, featureMatrix, storeys, outBack);
        }
    }

    private void clearAllDate(long eventId) {
        if (depth > 0) {
            for (Nerve nerve : nerveList) {//将信息发送给目标层神经中枢
                nerve.clearData(eventId);
            }
        }
    }

    private void toDepthNerveCenter(long eventId, int fromDepth, Matrix featureMatrix, List<Integer> storeys, OutBack outBack) throws Exception {
        for (NerveCenter nerveCenter : nerveCenterList) {//将信息发送给目标层神经中枢
            if (nerveCenter.getDepth() == fromDepth) {
                nerveCenter.go(eventId, fromDepth, featureMatrix, storeys, outBack);
                break;
            }
        }
    }

    private void clearData(long eventId) {
        for (NerveCenter nerveCenter : nerveCenterList) {
            nerveCenter.clearAllDate(eventId);
        }
    }

    private int getToDepth(List<MyPower> myPowerList) {
        int toDepth = -1;
        int size = myPowerList.size();
        double allPower = 0;
        for (MyPower myPower : myPowerList) {
            allPower = allPower + myPower.power;
        }
        double avg = 0;// allPower / size;
        for (int i = 0; i < size; i++) {
            MyPower myPower = myPowerList.get(i);
            if (myPower.power >= avg) {
                toDepth = myPower.depth;
                break;
            }
        }
        return toDepth;
    }

    private void collectPower(long eventId, double parameter, List<Integer> storeys, int fromDepth, Matrix featureMatrix, OutBack outBack) throws Exception {
        MyPower myPower = new MyPower();
        myPower.depth = fromDepth;
        myPower.power = parameter;
        if (myPowerMap.containsKey(eventId)) {
            List<MyPower> myPowerList = myPowerMap.get(eventId);
            myPowerList.add(myPower);
            if (myPowerList.size() == featureMatrix.getX() - 1 - depth) {//进行跃层判断
                int toDepth = getToDepth(myPowerList);
                myPowerMap.remove(eventId);
                if (myPowerList.size() == 1) {//终止继续传递，并清除内存数据
                    if (toDepth > 0) {
                        storeys.add(toDepth);
                    }
                    //System.out.println("权重结果：" + storeys);
                    outBack.backPower(storeys, eventId);//输出权重路径
                    //TODO 清除内存
                    clearData(eventId);
                } else {//通知toDepth隐层继续传递
                    storeys.add(toDepth);
                    if (featureMatrix.getX() - 1 == toDepth) {
                        myPowerMap.remove(eventId);
                        //System.out.println("权重结果：" + storeys);
                        outBack.backPower(storeys, eventId);//输出权重路径
                        //TODO 清除内存
                        clearData(eventId);
                    } else {
                        //System.out.println("隐层继续传递：" + storeys);
                        toDepthNerveCenter(eventId, toDepth, featureMatrix, storeys, outBack);
                    }
                }
            }
        } else {
            List<MyPower> myPowerList = new ArrayList<>();
            myPowerList.add(myPower);
            myPowerMap.put(eventId, myPowerList);
            if (featureMatrix.getX() == 2 + depth) {//此时也要进行结算
                myPowerMap.remove(eventId);
                if (parameter >= powerTh) {
                    storeys.add(fromDepth);
                }
                //System.out.println("权重结果：" + storeys);
                outBack.backPower(storeys, eventId);//输出权重路径
                //TODO 清除内存
                clearData(eventId);
            }
        }
    }

    static class MyPower {
        int depth;
        double power;
    }

}
