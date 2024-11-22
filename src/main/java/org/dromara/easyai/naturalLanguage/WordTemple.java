package org.dromara.easyai.naturalLanguage;

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
    private double wordNoise = 0.65;

    public double getWordNoise() {
        return wordNoise;
    }

    public void setWordNoise(double wordNoise) {
        this.wordNoise = wordNoise;
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
