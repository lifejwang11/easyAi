package org.wlld.naturalLanguage.languageCreator;

import org.wlld.entity.DyStateModel;
import org.wlld.entity.KeyWordForSentence;
import org.wlld.gameRobot.Action;
import org.wlld.gameRobot.DynamicProgramming;
import org.wlld.gameRobot.DynamicState;

import java.util.*;

public class CatchKeyWord {//抓取关键词
    private DynamicProgramming dynamicProgramming = new DynamicProgramming();//保存它的状态集合
    private List<String> keyWords = new ArrayList<>();//保存词列表
    private List<String> finishWords = new ArrayList<>();//终结态词集合
    private Set<String> noList = new HashSet<>();//禁止词集合
    private double proTh = 0.1;//收益阈值

    public void setProTh(double proTh) {
        this.proTh = proTh;
    }

    public void study(List<KeyWordForSentence> keyWordForSentenceList) {
        int size = keyWordForSentenceList.size();
        List<DynamicState> dynamicStateList = dynamicProgramming.getDynamicStateList();
        DynamicState dynamicState0 = new DynamicState(new int[]{0});
        dynamicState0.setFinish(true);
        dynamicStateList.add(dynamicState0);
        for (int i = 0; i < size; i++) {//添加禁止词
            KeyWordForSentence keyWords = keyWordForSentenceList.get(i);
            noList.add(keyWords.getKeyWord());
        }
        for (int i = 0; i < size; i++) {
            KeyWordForSentence keyWords = keyWordForSentenceList.get(i);
            String sentence = keyWords.getSentence();//句子
            String keyWord = keyWords.getKeyWord();//关键词
            int startIndex = getIndex(sentence, keyWord);
            if (startIndex >= 0) {
                creatID(sentence, startIndex, startIndex + keyWord.length() - 1);
            }
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
                            if (wordsValue.isFinish) {
                                wordsValue.value = 10;
                            } else {
                                if (dynamic.value > value) {
                                    wordsValue.value = dynamic.value;
                                } else {
                                    wordsValue.value = value;
                                }
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

    public KeyWordModel getModel() {
        KeyWordModel keyWordModel = new KeyWordModel();
        List<DyStateModel> dyStateModels = new ArrayList<>();
        List<DynamicState> dynamicStateList = dynamicProgramming.getDynamicStateList();
        int size = dynamicStateList.size();
        for (int i = 0; i < size; i++) {
            DynamicState dynamicState = dynamicStateList.get(i);
            DyStateModel dyStateModel = new DyStateModel();
            dyStateModel.setId(dynamicState.getStateId()[0]);
            dyStateModel.setFinish(dynamicState.isFinish());
            dyStateModel.setValue(dynamicState.getValue());
            dyStateModels.add(dyStateModel);
        }
        keyWordModel.setDynamicStateList(dyStateModels);
        keyWordModel.setKeyWords(keyWords);
        return keyWordModel;
    }

    public void insertModel(KeyWordModel keyWordModel) {
        List<String> myKeyWords = keyWordModel.getKeyWords();
        List<DyStateModel> dynamicStates = keyWordModel.getDynamicStateList();
        List<DynamicState> dynamicStateList = dynamicProgramming.getDynamicStateList();
        int size = myKeyWords.size();
        for (int i = 0; i < size; i++) {
            keyWords.add(myKeyWords.get(i));
        }
        int s = dynamicStates.size();
        for (int i = 0; i < s; i++) {
            DyStateModel modelDy = dynamicStates.get(i);
            DynamicState dynamicState = new DynamicState(new int[]{modelDy.getId()});
            dynamicState.setValue(modelDy.getValue());
            dynamicState.setFinish(modelDy.isFinish());
            dynamicStateList.add(dynamicState);
        }
    }

    private void insertValue(WordsValue wordsValue, DynamicState dynamicState, int startIndex, int endIndex) {//传输数值
        wordsValue.isFinish = dynamicState.isFinish();
        wordsValue.value = dynamicState.getValue();
        wordsValue.startIndex = startIndex;
        wordsValue.endIndex = endIndex;
        wordsValue.id = dynamicState.getStateId()[0];
        wordsValue.isMerge = false;
    }

    private List<WordsValue> getBestDyRight(String sentence) {
        List<WordsValue> myDyList = new ArrayList<>();
        List<DynamicState> dynamicStateList = dynamicProgramming.getDynamicStateList();
        int size = sentence.length() - 1;
        for (int i = size; i >= 0; i--) {
            WordsValue maxDy = null;        //0,1,2,3
            for (int j = i; j >= 0; j--) {//我是好人
                String word = sentence.substring(j, i + 1);//对该词进行收益判定
                DynamicState dynamicState = getDynamicState(word, dynamicStateList);
                if (dynamicState != null && dynamicState.getValue() > proTh) {
                    //System.out.println("word:" + word + ",value:" + dynamicState.getValue());
                    if (maxDy == null) {
                        maxDy = new WordsValue();
                        //System.out.println("第一次:" + word);
                        insertValue(maxDy, dynamicState, j, i);
                    } else {//先查询是否为终结态，若终结态跳出
                        if (dynamicState.isFinish()) {//当前为终结态
                            //System.out.println("终结态:" + word);
                            insertValue(maxDy, dynamicState, j, i);
                            break;
                        } else {//当前不是终结态
                            int index = sentence.indexOf(word);
                            double myValue = maxDy.value;
                            DynamicState state = null;
                            if (index + word.length() < sentence.length()) {
                                int t = index + 1;
                                String upWord = sentence.substring(t, t + word.length());
                                state = getDynamicState(upWord, dynamicStateList);
                                if (state != null) {
                                    if (state.isFinish()) {
                                        myValue = 10;
                                    } else {
                                        myValue = state.getValue();
                                    }
                                    //System.out.println("word:" + upWord + ",value==" + myValue + ",终结态:" + state.isFinish());
                                    //System.out.println("testWord:" + word + ",value==" + dynamicState.getValue() + ",myValue:" + maxDy.value);
                                }
                            }
                            String maxWord = keyWords.get(maxDy.id - 1);
                            if (maxWord.length() > 1) {
                                if (myValue <= dynamicState.getValue() && maxDy.value <= dynamicState.getValue()) {
                                    insertValue(maxDy, dynamicState, j, i);
                                } else {
                                    if (state != null && maxDy.value < myValue) {
                                        insertValue(maxDy, state, j + 1, i + 1);
                                    }
                                    break;
                                }
                            }else {
                                if (myValue <= dynamicState.getValue()) {
                                    insertValue(maxDy, dynamicState, j, i);
                                } else {
                                    if (state != null) {
                                        insertValue(maxDy, state, j + 1, i + 1);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                } else {//延伸后不存在该词，跳出循环
                    break;
                }
            }
            if (maxDy != null) {
                //该字延伸最大价值的词
                String word = keyWords.get(maxDy.id - 1);
                //System.out.println("测试===========" + word);
                if (word.length() > 1) {
                    myDyList.add(maxDy);
                }
            }
        }
        return myDyList;
    }

    private List<WordsValue> getBestDyLeft(String sentence) {
        List<WordsValue> myDyList = new ArrayList<>();
        List<DynamicState> dynamicStateList = dynamicProgramming.getDynamicStateList();
        int size = sentence.length();
        for (int i = 0; i < size; i++) {
            WordsValue maxDy = null;
            for (int j = i; j < size; j++) {
                String word = sentence.substring(i, j + 1);//对该词进行收益判定
                DynamicState dynamicState = getDynamicState(word, dynamicStateList);
                if (dynamicState != null && dynamicState.getValue() >= proTh) {
                    //System.out.println("文字:" + word + ",value:" + dynamicState.getValue());
                    if (maxDy == null) {
                        maxDy = new WordsValue();
                        insertValue(maxDy, dynamicState, i, j);
                    } else {//先查询是否为终结态，若终结态跳出
                        if (dynamicState.isFinish()) {//当前为终结态
                            insertValue(maxDy, dynamicState, i, j);
                            break;
                        } else {//当前不是终结态
                            int index = sentence.indexOf(word);
                            double myValue = maxDy.value;
                            DynamicState state = null;
                            String upWord;
                            if (index > 0) {
                                int t = index - 1;
                                upWord = sentence.substring(t, t + word.length());
                                state = getDynamicState(upWord, dynamicStateList);
                                if (state != null) {
                                    if (state.isFinish()) {
                                        myValue = 10;
                                    } else {
                                        myValue = state.getValue();
                                    }
                                }
                            }
                            String maxWord = keyWords.get(maxDy.id - 1);
                            //System.out.println("word:" + upWord + ",value==" + myValue);
                            if (maxWord.length() > 1) {//maxDy参与比较
                                if (myValue <= dynamicState.getValue() && maxDy.value <= dynamicState.getValue()) {
                                    insertValue(maxDy, dynamicState, i, j);
                                } else {
                                    if (state != null && maxDy.value < myValue) {
                                        insertValue(maxDy, state, i - 1, j - 1);
                                    }
                                    break;
                                }
                            } else {
                                if (myValue <= dynamicState.getValue()) {
                                    insertValue(maxDy, dynamicState, i, j);
                                } else {
                                    if (state != null) {
                                        insertValue(maxDy, state, i - 1, j - 1);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                } else {//延伸后不存在该词，跳出循环
                    break;
                }
            }
            if (maxDy != null) {
                //该字延伸最大价值的词
                String maxWord = keyWords.get(maxDy.id - 1);
                if (maxWord.length() > 1) {
                    //System.out.println("测试===========" + maxWord);
                    myDyList.add(maxDy);
                }
            }
        }
        return myDyList;
    }

    public Set<String> getKeyWord(String sentence) {
        List<String> first = getMyKeyWord(sentence, true);
        List<String> second = getMyKeyWord(sentence, false);
        Set<String> set = new HashSet<>();
        set.addAll(first);
        set.addAll(second);
        //System.out.println("first:" + first + ",second:" + second);
        return set;
    }

    private List<String> getMyKeyWord(String sentence, boolean first) {//获取关键词
        List<WordsValue> wordsValues;
        if (first) {
            wordsValues = getBestDyRight(sentence);
        } else {
            wordsValues = getBestDyLeft(sentence);
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
                if (i == sen.length - 1) {
                    wordsValueList.add(wordsValue);
                }
            } else {//不存在名词
                if (wordsValue != null) {
                    wordsValueList.add(wordsValue);
                    wordsValue = null;
                }
            }
        }
        List<String> keyWords = new ArrayList<>();
        for (int i = 0; i < wordsValueList.size(); i++) {
            WordsValue wordsValue1 = wordsValueList.get(i);
            String keyWord = sentence.substring(wordsValue1.startIndex, wordsValue1.endIndex + 1);
            keyWords.add(keyWord);
        }
        return keyWords;
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
        int size = sentence.length();
        for (int i = 0; i < size; i++) {
            if (i < startIndex) {//处于关键词左侧
                for (int j = i; j < startIndex; j++) {//遍历每一个起始字
                    for (int k = 1; k <= startIndex - j; k++) {//从每个起始字向右延伸
                        String word = sentence.substring(j, j + k);
                        if (!noList.contains(word)) {
                            int id = getID(word);
                            if (id > 0) {
                                DynamicState dynamicState = new DynamicState(new int[]{id});
                                if (j == 0 && k > 1 && k == startIndex) {//设置终结态
                                    dynamicState.setFinish(true);
                                    finishWords.add(word);
                                }
                                dynamicStateList.add(dynamicState);
                            }
                        } else {
                            break;
                        }
                    }
                }
            } else if (i > endIndex) {//处于关键词右侧
                for (int j = i; j < size; j++) {
                    for (int k = 1; k <= size - j; k++) {//从每个起始字向右延伸
                        String word = sentence.substring(j, j + k);
                        if (!noList.contains(word)) {
                            int id = getID(word);
                            if (id > 0) {
                                DynamicState dynamicState = new DynamicState(new int[]{id});
                                if (j == endIndex + 1 && k > 1 && k == size - endIndex - 1) {//设置终结态
                                    dynamicState.setFinish(true);
                                    finishWords.add(word);
                                }
                                dynamicStateList.add(dynamicState);
                            }
                        } else {
                            break;
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
        int id;//词id
        int startIndex;//起始下标
        int endIndex;//末尾下标
        double value;//该状态收益
        boolean isFinish;//是否为终结态
        boolean isMerge;//是否被合并过
    }
}
