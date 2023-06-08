package org.wlld.entity;

import org.wlld.rnnNerveCenter.ModelParameter;

import java.util.List;

public class WordTwoVectorModel {
    private ModelParameter modelParameter;
    private List<String> wordList;

    public ModelParameter getModelParameter() {
        return modelParameter;
    }

    public void setModelParameter(ModelParameter modelParameter) {
        this.modelParameter = modelParameter;
    }

    public List<String> getWordList() {
        return wordList;
    }

    public void setWordList(List<String> wordList) {
        this.wordList = wordList;
    }
}
