package org.wlld.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class SentenceModel {
    private List<String> sentenceList = new ArrayList<>();//每一句话
    private Set<String> wordSet = new HashSet<>();

    public void setSentence(String sentence) {//输入语句
        for (int i = 0; i < sentence.length(); i++) {
            wordSet.add(sentence.substring(i, i + 1));
        }
        sentenceList.add(sentence);
    }

    public List<String> getSentenceList() {
        return sentenceList;
    }

    public Set<String> getWordSet() {
        return wordSet;
    }
}
