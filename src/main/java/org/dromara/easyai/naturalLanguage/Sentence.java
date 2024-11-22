package org.dromara.easyai.naturalLanguage;


import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @description
 * @date 8:01 上午 2020/2/23
 */
public class Sentence {
    private Word firstWord;
    private final List<Word> waitWords = new ArrayList<>();//词
    private List<String> keyWords;//分词结果下标按照时间序列排序
    private final List<Integer> features = new ArrayList<>();//时序特征

    public List<Integer> getFeatures() {
        return features;
    }

    public List<String> getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(List<String> keyWords) {
        this.keyWords = keyWords;
    }

    public List<Word> getWaitWords() {
        return waitWords;
    }

    public Word getFirstWord() {
        return firstWord;
    }

    public Sentence() {

    }

    private void lineWord(Word word, Word wordSon) {//给词连线
        if (firstWord != null) {
            if (word.getSon() != null) {//右连接不是空的
                lineWord(word.getSon(), wordSon);
            } else {//右连接是空的
                wordSon.setLv(word.getLv() + 1);
                word.setSon(wordSon);
            }
        } else {
            firstWord = wordSon;
            firstWord.setLv(1);
        }
    }

    public void setWord(String word) {//编号
        Word word1 = new Word();
        word1.setWord(word);
        lineWord(firstWord, word1);//词之间做连线
        waitWords.add(word1);
    }
}
