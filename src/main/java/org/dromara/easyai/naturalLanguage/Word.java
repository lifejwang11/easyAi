package org.dromara.easyai.naturalLanguage;


public class Word {
    private String word;
    private Word son;
    private int wordFrequency;//词频
    private int lv;//该词的时间序列

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Word getSon() {
        return son;
    }

    public void setSon(Word son) {
        this.son = son;
    }

    public int getWordFrequency() {
        return wordFrequency;
    }

    public void setWordFrequency(int wordFrequency) {
        this.wordFrequency = wordFrequency;
    }
}
