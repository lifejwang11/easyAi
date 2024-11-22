package org.dromara.easyai.naturalLanguage.languageCreator;

import org.dromara.easyai.nerveCenter.ModelParameter;

import java.util.List;

public class CreatorSentenceModel {
    private List<String> wordList;//模型
    private ModelParameter modelParameter;//模型

    public List<String> getWordList() {
        return wordList;
    }

    public void setWordList(List<String> wordList) {
        this.wordList = wordList;
    }

    public ModelParameter getModelParameter() {
        return modelParameter;
    }

    public void setModelParameter(ModelParameter modelParameter) {
        this.modelParameter = modelParameter;
    }
}
