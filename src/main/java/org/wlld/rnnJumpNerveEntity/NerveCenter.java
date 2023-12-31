package org.wlld.rnnJumpNerveEntity;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.i.OutBack;
import org.wlld.naturalLanguage.word.WordEmbedding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NerveCenter {//神经中枢
    private final int depth;//该神经中枢的深度
    private final double powerTh;//权重阈值
    private final int outNumber;//输出神经元数量
    private final List<Nerve> nerveList;//该神经中枢控制的对应隐层神经元集合
    private final Map<Long, List<MyPower>> myPowerMap = new HashMap<>();//接收前方各层输出层返回权重
    private WordEmbedding wordEmbedding;//词嵌入
    private final boolean isFinish;//是否是最后一层

    public int getDepth() {
        return depth;
    }

    public NerveCenter(int depth, List<Nerve> nerveList, double powerTh, int outNumber, boolean isFinish) {
        this.depth = depth;
        this.nerveList = nerveList;
        this.powerTh = powerTh;
        this.outNumber = outNumber;
        this.isFinish = isFinish;
    }

    public void setWordEmbedding(WordEmbedding wordEmbedding) {
        this.wordEmbedding = wordEmbedding;
    }

    private int getMax(List<MyPower> myPowerList) {
        double maxPower = 0;
        int id = 0;
        for (MyPower myPower : myPowerList) {
            if (myPower.power > powerTh && myPower.power > maxPower) {
                maxPower = myPower.power;
                id = myPower.id;
            }
        }
        return id;
    }

    public void backType(long eventId, double parameter, int id, Matrix featureMatrix, OutBack outBack, String myWord
            , Matrix semanticsMatrix) throws Exception {
        MyPower myPower = new MyPower();
        myPower.id = id;
        myPower.power = parameter;
        if (myPowerMap.containsKey(eventId)) {
            List<MyPower> myPowerList = myPowerMap.get(eventId);
            myPowerList.add(myPower);
            if (myPowerList.size() == outNumber) {//接收完毕
                myPowerMap.remove(eventId);
                int typeID = getMax(myPowerList);
                if (typeID > 0) {//增加新特征继续传播
                    String nextWord = wordEmbedding.getWord(typeID - 1);
                    if (myWord == null) {
                        myWord = nextWord;
                    } else {
                        myWord = myWord + nextWord;
                    }
                    if (isFinish) {//输出结果
                        outBack.backWord(myWord, eventId);
                    } else {
                        Matrix matrix = wordEmbedding.getEmbedding(nextWord, eventId).getFeatureMatrix();
                        if (depth > 1) {
                            matrix = MatrixOperation.add(matrix, semanticsMatrix);
                            MatrixOperation.mathMul(matrix, 0.5);
                        }
                        featureMatrix = MatrixOperation.pushVector(featureMatrix, matrix, true);
                        go(eventId, featureMatrix, outBack, myWord, semanticsMatrix);
                    }
                } else {//停止继续传播 进行输出
                    outBack.backWord(myWord, eventId);
                }
            }
        } else {
            List<MyPower> myPowerList = new ArrayList<>();
            myPowerList.add(myPower);
            myPowerMap.put(eventId, myPowerList);
        }
    }

    private void go(long eventId, Matrix featureMatrix, OutBack outBack, String word, Matrix semanticsMatrix) throws Exception {
        //神经中枢收到传递命令 将命令传递给本层神经元
        for (Nerve nerve : nerveList) {//将信息发送给目标层隐层神经元
            nerve.sendMyTestMessage(eventId, featureMatrix, outBack, word, semanticsMatrix);
        }
    }

    static class MyPower {
        int id;
        double power;
    }

}
