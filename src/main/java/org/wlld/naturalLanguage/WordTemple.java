package org.wlld.naturalLanguage;

import org.wlld.randomForest.RandomForest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @description分词模版
 * @date 4:15 下午 2020/2/23
 */
public class WordTemple {
    private List<Sentence> sentences = new ArrayList<>();//所有断句
    private List<WorldBody> allWorld = new ArrayList<>();//所有词集合
    private List<List<String>> wordTimes = new ArrayList<>();//词编号
    private RandomForest randomForest;//保存的随机森林模型
    //四大参数
    private double garbageTh = 0.5;//垃圾分类的阈值默认0.5
    private double trustPunishment = 0.1;//信任惩罚
    private double trustTh = 0.1;//信任阈值,相当于一次信任惩罚的数值
    private int treeNub = 9;//丛林里面树的数量
    private boolean isSplitWord = false;//是否使用拆分词模式,默认是不使用

    public boolean isSplitWord() {
        return isSplitWord;
    }

    public void setSplitWord(boolean splitWord) {
        isSplitWord = splitWord;
    }

    public int getTreeNub() {
        return treeNub;
    }

    public void setTreeNub(int treeNub) {
        this.treeNub = treeNub;
    }

    public double getTrustTh() {
        return trustTh;
    }

    public void setTrustTh(double trustTh) {
        this.trustTh = trustTh;
    }

    public double getTrustPunishment() {
        return trustPunishment;
    }

    public void setTrustPunishment(double trustPunishment) {
        this.trustPunishment = trustPunishment;
    }

    public double getGarbageTh() {
        return garbageTh;
    }

    public void setGarbageTh(double garbageTh) {
        this.garbageTh = garbageTh;
    }

    public RandomForest getRandomForest() {
        return randomForest;
    }

    public void setRandomForest(RandomForest randomForest) {
        this.randomForest = randomForest;
    }

    public List<List<String>> getWordTimes() {
        return wordTimes;
    }

    public void setWordTimes(List<List<String>> wordTimes) {
        this.wordTimes = wordTimes;
    }

    public List<Sentence> getSentences() {
        return sentences;
    }

    public void setSentences(List<Sentence> sentences) {
        this.sentences = sentences;
    }

    public List<WorldBody> getAllWorld() {
        return allWorld;
    }

    public void setAllWorld(List<WorldBody> allWorld) {
        this.allWorld = allWorld;
    }
}
