package org.wlld.naturalLanguage;

import org.wlld.randomForest.RfModel;

import java.util.List;

/**
 * @author lidapeng
 * @description中文语言分类模型
 * @date 11:24 上午 2020/2/29
 */
public class WordModel {
    private RfModel rfModel;//随机森林模型
    private List<WorldBody> allWorld;//所有词集合
    private List<List<String>> wordTimes;//所有分词编号

    public RfModel getRfModel() {
        return rfModel;
    }

    public void setRfModel(RfModel rfModel) {
        this.rfModel = rfModel;
    }

    public List<WorldBody> getAllWorld() {
        return allWorld;
    }

    public void setAllWorld(List<WorldBody> allWorld) {
        this.allWorld = allWorld;
    }

    public List<List<String>> getWordTimes() {
        return wordTimes;
    }

    public void setWordTimes(List<List<String>> wordTimes) {
        this.wordTimes = wordTimes;
    }
}
