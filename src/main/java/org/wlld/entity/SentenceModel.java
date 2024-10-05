package org.wlld.entity;

import org.wlld.config.SentenceConfig;
import org.wlld.config.TfConfig;

import java.util.*;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class SentenceModel {
    private final List<String[]> sentenceList = new ArrayList<>();//每一句话
    private final Set<String> wordSet = new HashSet<>();
    private final String splitWord;//词向量默认隔断符，无隔断则会逐字隔断

    public SentenceModel(String splitWord) {
        this.splitWord = splitWord;
    }

    public SentenceModel() {
        this.splitWord = null;
    }

    public void setSentenceBySplitWord(String sentence) throws Exception {
        if (splitWord != null && !splitWord.isEmpty()) {
            String[] words = sentence.split(splitWord);
            Collections.addAll(wordSet, words);
            sentenceList.add(words);
        } else {
            throw new Exception("没有设置隔断符，无法使用基于隔断符的切割");
        }
    }

    public void setSentence(String sentence) {//输入语句没有隔断符
        String[] words = new String[sentence.length()];
        for (int i = 0; i < sentence.length(); i++) {
            String word = sentence.substring(i, i + 1);
            words[i] = word;
            wordSet.add(word);
        }
        sentenceList.add(words);
    }

    public List<String[]> getSentenceList() {
        return sentenceList;
    }

    public String getSplitWord() {
        return splitWord;
    }

    public Set<String> getWordSet() {
        return wordSet;
    }
}
