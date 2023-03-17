package org.wlld.myAction;

import java.util.ArrayList;
import java.util.List;

public class GameConfig {//记录游戏全局信息
    private List<int[]> prizeList = new ArrayList<>();//游戏宝藏坐标集合
    private int prizeIndex = 2;//指定宝藏下标
    private int maxStep = 6;//最大步数

    public int getMaxStep() {
        return maxStep;
    }

    public void setMaxStep(int maxStep) {
        this.maxStep = maxStep;
    }

    public List<int[]> getPrizeList() {
        return prizeList;
    }

    public int getPrizeIndex() {
        return prizeIndex;
    }

    public void setPrizeIndex(int prizeIndex) {
        this.prizeIndex = prizeIndex;
    }
}
