package org.dromara.naturalLanguage;

import java.util.List;

public class WorldBody {
    private String wordName;//词
    private int wordFrequency;//词频
    private List<WorldBody> worldBodies;//辐射集合
    private Word word;

    public String getWordName() {
        return wordName;
    }

    public void setWordName(String wordName) {
        this.wordName = wordName;
    }

    public int getWordFrequency() {
        return wordFrequency;
    }

    public void addNub() {
        wordFrequency++;
    }

    public List<WorldBody> getWorldBodies() {
        return worldBodies;
    }

    public void setWorldBodies(List<WorldBody> worldBodies) {
        this.worldBodies = worldBodies;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }
}
