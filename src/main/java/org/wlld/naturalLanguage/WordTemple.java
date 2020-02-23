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
