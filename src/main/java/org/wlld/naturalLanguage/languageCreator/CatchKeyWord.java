package org.wlld.naturalLanguage.languageCreator;

import org.wlld.entity.KeyWordForSentence;
import org.wlld.gameRobot.Action;
import org.wlld.gameRobot.DynamicProgramming;
import org.wlld.gameRobot.DynamicState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CatchKeyWord {//抓取关键词
    private DynamicProgramming dynamicProgramming = new DynamicProgramming();//保存它的状态集合
    private List<String> keyWords = new ArrayList<>();//保存词列表
    private List<String> finishWords = new ArrayList<>();//终结态词集合

    public void study(List<KeyWordForSentence> keyWordForSentenceList) {
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

    private WordsValue isContinuity(int start1, int end1, int start2, int end2) {
        boolean isContinuity = false;
        WordsValue wordsValue = null;
        if (end1 + 1 == start2 || end2 + 1 == start1) {//相接
            isContinuity = true;
        } else if ((start2 >= start1 && start2 <= end1) || (end2 >= start1 && end2 <= end1) ||
                (start1 >= start2 && start1 <= end2) || (end1 >= start2 && end1 <= end2)) {//相交
            isContinuity = true;
        }
        if (isContinuity) {
            wordsValue = new WordsValue();
            wordsValue.isMerge = false;
            if (start1 > start2) {
                wordsValue.startIndex = start2;
            } else {
                wordsValue.startIndex = start1;
            }
            if (end1 > end2) {
                wordsValue.endIndex = end1;
            } else {
                wordsValue.endIndex = end2;
            }
        }
        return wordsValue;
    }

    private void mergeWord(List<WordsValue> myDyList) {//合并状态
        for (int i = 0; i < myDyList.size(); i++) {
            WordsValue dynamicState = myDyList.get(i);
            if (!dynamicState.isMerge) {
                for (int j = 0; j < myDyList.size(); j++) {
                    if (j != i) {
                        int startIndex = dynamicState.startIndex;//开始下标
                        int endIndex = dynamicState.endIndex;//结束下标
                        boolean isFinish = dynamicState.isFinish;
                        double value = dynamicState.value;
                        WordsValue dynamic = myDyList.get(j);
                        int myStart = dynamic.startIndex;
                        int myEnd = dynamic.endIndex;
                        WordsValue wordsValue = isContinuity(startIndex, endIndex, myStart, myEnd);
                        if (wordsValue != null) {//可进行合并
                            dynamic.isMerge = true;
                            if (isFinish || dynamic.isFinish) {
                                wordsValue.isFinish = true;
                            } else {
                                wordsValue.isFinish = false;
                            }
                            if (dynamic.value > value) {
                                wordsValue.value = dynamic.value;
                            } else {
                                wordsValue.value = value;
                            }
                            dynamicState = wordsValue;
                        }
                    }
                }
                myDyList.set(i, dynamicState);//替换合并后的结果
            }
        }
        for (int i = 0; i < myDyList.size(); i++) {
            if (myDyList.get(i).isMerge) {
                myDyList.remove(i);
                i--;
            }
        }
    }

    public String getKeyWord(String sentence) {//获取关键词
        List<DynamicState> dynamicStateList = dynamicProgramming.getDynamicStateList();
        int size = sentence.length();
        List<DynamicState> myDyList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            DynamicState maxDy = null;
            for (int j = i; j < size; j++) {
                String word = sentence.substring(i, j + 1);//对该词进行收益判定
                DynamicState dynamicState = getDynamicState(word, dynamicStateList);
                if (dynamicState != null) {
                    if (maxDy == null) {
                        maxDy = dynamicState;
                    } else {//先查询是否为终结态，若终结态跳出
                        if (dynamicState.isFinish()) {//当前为终结态
                            maxDy = dynamicState;
                            break;
                        } else {//当前不是终结态
                            if (maxDy.getValue() <= dynamicState.getValue()) {
                                maxDy = dynamicState;
                            } else {
                                break;
                            }
                        }
                    }
                } else {//延伸后不存在该词，跳出循环
                    break;
                }
            }
            //该字延伸最大价值的词
            myDyList.add(maxDy);
        }
        List<WordsValue> wordsValues = new ArrayList<>();
        for (DynamicState dynamicState : myDyList) {
            WordsValue wordsValue = new WordsValue();
            String word = keyWords.get(dynamicState.getStateId()[0] - 1);
            int startIndex = sentence.indexOf(word);//开始下标
            int endIndex = startIndex + word.length() - 1;//结束下标
            wordsValue.isFinish = dynamicState.isFinish();
            wordsValue.value = dynamicState.getValue();
            wordsValue.startIndex = startIndex;
            wordsValue.endIndex = endIndex;
            wordsValue.isMerge = false;
            wordsValues.add(wordsValue);
        }
        mergeWord(wordsValues);
        //合并完成，连接空档
        int[] sen = new int[sentence.length()];
        Arrays.fill(sen, 1);
        for (int i = 0; i < wordsValues.size(); i++) {
            WordsValue wordsValue = wordsValues.get(i);
            int startIndex = wordsValue.startIndex;
            int endIndex = wordsValue.endIndex;
            for (int j = startIndex; j <= endIndex; j++) {
                sen[j] = 0;
            }
        }
        WordsValue wordsValue = null;
        List<WordsValue> wordsValueList = new ArrayList<>();
        for (int i = 0; i < sen.length; i++) {
            if (sen[i] == 1) {//存在名词
                if (wordsValue == null) {
                    wordsValue = new WordsValue();
                    wordsValue.startIndex = i;
                }
                wordsValue.endIndex = i;
            } else {//不存在名词
                if (wordsValue != null) {
                    wordsValueList.add(wordsValue);
                    wordsValue = null;
                }
            }
        }
        //最后计算优先度，选取一个关键词
        double maxValue = -200;
        int keyIndex = -1;
        for (int i = 0; i < wordsValueList.size(); i++) {
            WordsValue wordsValue1 = wordsValueList.get(i);
            int startIndex = wordsValue1.startIndex;
            int endIndex = wordsValue1.endIndex;
            double value;
            double value1 = -100;//前
            double value2 = -100;//后
            if (startIndex > 0) {
                value1 = getValue(wordsValues, startIndex - 1);
            }
            if (endIndex < sen.length - 1) {
                value2 = getValue(wordsValues, endIndex + 1);
            }
            if (value1 > value2) {
                value = value1;
            } else {
                value = value2;
            }
            if (value > maxValue) {
                maxValue = value;
                keyIndex = i;
            }
        }
        String keyWord = null;
        if (keyIndex > -1) {
            WordsValue wordsValue1 = wordsValueList.get(keyIndex);
            keyWord = sentence.substring(wordsValue1.startIndex, wordsValue1.endIndex + 1);
        }
        return keyWord;
    }

    private double getValue(List<WordsValue> wordsValues, int index) {
        double value = -100;
        for (WordsValue wordsValue : wordsValues) {
            if (wordsValue.startIndex <= index && wordsValue.endIndex >= index) {
                value = wordsValue.value;
                break;
            }
        }
        return value;
    }

    private DynamicState getDynamicState(String myWord, List<DynamicState> dynamicStateList) {
        DynamicState myDy = null;
        int size = keyWords.size();
        int id = 0;
        for (int i = 0; i < size; i++) {
            String keyWord = keyWords.get(i);
            if (keyWord.hashCode() == myWord.hashCode() && keyWord.equals(myWord)) {
                id = i + 1;
                break;
            }
        }
        if (id > 0) {//存在
            for (DynamicState dynamicState : dynamicStateList) {
                if (dynamicState.getStateId()[0] == id) {
                    myDy = dynamicState;
                    break;
                }
            }
        }
        return myDy;
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

    class WordsValue {
        int startIndex;//起始下标
        int endIndex;//末尾下标
        double value;//该状态收益
        boolean isFinish;//是否为终结态
        boolean isMerge;//是否被合并过
    }
}
