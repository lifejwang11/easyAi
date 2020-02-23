package org.wlld.naturalLanguage;

public class KeyWord {
    private  Word word;//关键字
    private  boolean isOk;//是否完成此关键字

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public boolean isOk() {
        return isOk;
    }

    public void setOk(boolean ok) {
        isOk = ok;
    }
}
