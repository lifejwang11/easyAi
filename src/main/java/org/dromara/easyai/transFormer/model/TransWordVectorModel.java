package org.dromara.easyai.transFormer.model;

import java.util.List;

/**
 * @author lidapeng
 * @time 2025/4/3 15:27
 * @des transFormer词向量模型
 */
public class TransWordVectorModel {
    private List<String> wordList;//词离散id
    private List<Float[]> wordVectorModel;//词向量
    private Float[] positionMatrix;//位置编码
    private int x;//词向量行数
    private int y;//词向量列数

    public Float[] getPositionMatrix() {
        return positionMatrix;
    }

    public void setPositionMatrix(Float[] positionMatrix) {
        this.positionMatrix = positionMatrix;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public List<String> getWordList() {
        return wordList;
    }

    public void setWordList(List<String> wordList) {
        this.wordList = wordList;
    }

    public List<Float[]> getWordVectorModel() {
        return wordVectorModel;
    }

    public void setWordVectorModel(List<Float[]> wordVectorModel) {
        this.wordVectorModel = wordVectorModel;
    }
}
