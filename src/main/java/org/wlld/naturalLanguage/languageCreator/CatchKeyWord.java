package org.wlld.naturalLanguage.languageCreator;

import org.wlld.entity.KeyWordForSentence;
import org.wlld.gameRobot.Action;
import org.wlld.gameRobot.DynamicProgramming;
import org.wlld.gameRobot.DynamicState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CatchKeyWord {//抓取关键词
    private List<KeyWordForSentence> keyWordForSentenceList;
    private DynamicProgramming dynamicProgramming = new DynamicProgramming();
    private List<String> keyWords = new ArrayList<>();
    private List<String> finishWords = new ArrayList<>();//终结态词集合

    public void study(List<KeyWordForSentence> keyWordForSentenceList) {
        this.keyWordForSentenceList = keyWordForSentenceList;
        int size = keyWordForSentenceList.size();
        for (int i = 0; i < size; i++) {
            KeyWordForSentence keyWords = keyWordForSentenceList.get(i);
            String sentence = keyWords.getSentence();//句子
            String keyWord = keyWords.getKeyWord();//关键词
            int startIndex = getIndex(sentence, keyWord);
            creatID(sentence, startIndex, startIndex + keyWord.length() - 1);
        }
        Map<Integer, Action> actionMap = dynamicProgramming.getActionMap();
        WordRight wordRight = new WordRight(keyWords, finishWords);
        WordLeft wordLeft = new WordLeft(keyWords, finishWords);
        wordRight.setActionId(1);
        wordLeft.setActionId(2);
        actionMap.put(1, wordRight);
        actionMap.put(2, wordLeft);
        dynamicProgramming.gameStart();//探索中奖
        dynamicProgramming.strategyStudy();//研究策略中奖
    }

    private void creatID(String sentence, int startIndex, int endIndex) {//创建状态
        List<DynamicState> dynamicStateList = dynamicProgramming.getDynamicStateList();
        DynamicState dynamicState0 = new DynamicState(new int[]{0});
        dynamicState0.setFinish(true);
        dynamicStateList.add(dynamicState0);
        int size = sentence.length();
        for (int i = 0; i < size; i++) {
            if (i < startIndex) {//处于关键词左侧
                for (int j = i; j < startIndex; j++) {//遍历每一个起始字
                    for (int k = 1; k <= startIndex - j; k++) {//从每个起始字向右延伸
                        String word = sentence.substring(j, j + k);
                        int id = getID(word);
                        if (id > 0) {
                            DynamicState dynamicState = new DynamicState(new int[]{id});
                            if (j == 0 && k > 1 && k == startIndex) {//设置终结态
                                dynamicState.setFinish(true);
                                finishWords.add(word);
                            }
                            dynamicStateList.add(dynamicState);
                        }
                    }
                }
            } else if (i > endIndex) {//处于关键词右侧
                for (int j = i; j < size; j++) {
                    for (int k = 1; k <= size - j; k++) {//从每个起始字向右延伸
                        String word = sentence.substring(j, j + k);
                        int id = getID(word);
                        if (id > 0) {
                            DynamicState dynamicState = new DynamicState(new int[]{id});
                            if (j == endIndex + 1 && k > 1 && k == size - endIndex - 1) {//设置终结态
                                dynamicState.setFinish(true);
                                finishWords.add(word);
                            }
                            dynamicStateList.add(dynamicState);
                        }
                    }
                }
            }
        }
    }

    private int getIndex(String word, String keyWord) {
        int keyLen = keyWord.length();
        int sub = word.length() - keyLen;
        int startIndex = 0;
        for (int i = 0; i <= sub; i++) {
            String subWord = word.substring(i, i + keyLen);
            if (subWord.equals(keyWord)) {
                startIndex = i;
                break;
            }
        }
        return startIndex;
    }

    private int getID(String word) {
        int id = 0;
        int size = keyWords.size();
        boolean isHere = false;
        for (int i = 0; i < size; i++) {
            String myWord = keyWords.get(i);
            if (myWord.hashCode() == word.hashCode() && myWord.equals(word)) {
                isHere = true;
                break;
            }
        }
        if (!isHere) {//列表里没有该词
            keyWords.add(word);
            id = keyWords.size();
        }
        return id;
    }
}
