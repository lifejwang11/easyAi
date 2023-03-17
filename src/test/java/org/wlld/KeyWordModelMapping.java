package org.wlld;

import org.wlld.naturalLanguage.languageCreator.KeyWordModel;

public class KeyWordModelMapping {//关键词模型映射
    private int key;
    private KeyWordModel keyWordModel;

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public KeyWordModel getKeyWordModel() {
        return keyWordModel;
    }

    public void setKeyWordModel(KeyWordModel keyWordModel) {
        this.keyWordModel = keyWordModel;
    }
}
