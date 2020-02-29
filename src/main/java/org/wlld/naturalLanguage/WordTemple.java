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
    private static WordTemple Word_Temple = new WordTemple();
    private List<Sentence> sentences = new ArrayList<>();//所有断句
    private List<WorldBody> allWorld = new ArrayList<>();//所有词集合
    private List<List<String>> wordTimes = new ArrayList<>();//词编号
    private RandomForest randomForest;//保存的随机森林模型
    private double garbageTh = 0.5;//垃圾分类的阈值默认0.5
    private double trustPunishment = 0.1;//信任惩罚

    public WordModel getModel() {//获取模型
        WordModel wordModel = new WordModel();
        wordModel.setAllWorld(allWorld);
        wordModel.setWordTimes(wordTimes);
        wordModel.setGarbageTh(garbageTh);
        wordModel.setTrustPunishment(trustPunishment);
        wordModel.setTrustTh(randomForest.getTrustTh());
        wordModel.setRfModel(randomForest.getModel());
        return wordModel;
    }

    public void insertModel(WordModel wordModel) throws Exception {//注入模型
        allWorld = wordModel.getAllWorld();
        wordTimes = wordModel.getWordTimes();
        garbageTh = wordModel.getGarbageTh();
        trustPunishment = wordModel.getTrustPunishment();
        randomForest = new RandomForest();
        randomForest.setTrustTh(wordModel.getTrustTh());
        randomForest.insertModel(wordModel.getRfModel());
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

    private WordTemple() {
    }

    public List<List<String>> getWordTimes() {
        return wordTimes;
    }

    public void setWordTimes(List<List<String>> wordTimes) {
        this.wordTimes = wordTimes;
    }

    public static WordTemple get() {
        return Word_Temple;
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
