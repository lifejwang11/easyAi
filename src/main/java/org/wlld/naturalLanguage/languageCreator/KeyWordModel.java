package org.wlld.naturalLanguage.languageCreator;

import org.wlld.entity.DyStateModel;

import java.util.List;

public class KeyWordModel {
    private List<DyStateModel> dynamicStateList;
    private List<String> keyWords;//词

    public List<DyStateModel> getDynamicStateList() {
        return dynamicStateList;
    }

    public void setDynamicStateList(List<DyStateModel> dynamicStateList) {
        this.dynamicStateList = dynamicStateList;
    }

    public List<String> getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(List<String> keyWords) {
        this.keyWords = keyWords;
    }
}
