package org.dromara.easyai.naturalLanguage.languageCreator;

import org.dromara.easyai.entity.DyStateModel;
import org.dromara.easyai.entity.KeyWordForSentence;
import org.dromara.easyai.gameRobot.Action;
import org.dromara.easyai.gameRobot.DynamicProgramming;
import org.dromara.easyai.gameRobot.DynamicState;

import java.util.*;

public class CatchKeyWord {//抓取关键词
    private final DynamicProgramming dynamicProgramming = new DynamicProgramming();//保存它的状态集合
    private final List<String> keyWords = new ArrayList<>();//保存词列表
    private final List<String> finishWords = new ArrayList<>();//终结态词集合
    private float proTh = 0.1f;//收益阈值
    private float maxFinishValue = 100;//终结态value

    public void setMaxFinishValue(float maxFinishValue) {
        this.maxFinishValue = maxFinishValue;
    }

    public void setProTh(float proTh) {
        this.proTh = proTh;
    }

    public void study(List<KeyWordForSentence> keyWordForSentenceList) throws Exception {
        int size = keyWordForSentenceList.size();
        List<DynamicState> dynamicStateList = dynamicProgramming.getDynamicStateList();
        DynamicState dynamicState0 = new DynamicState(new int[]{0});
        dynamicState0.setFinish(true);
        dynamicStateList.add(dynamicState0);
        for (int i = 0; i < size; i++) {
            KeyWordForSentence keyWords = keyWordForSentenceList.get(i);
            String sentence = keyWords.getSentence();//句子
            String keyWord = keyWords.getKeyWord();//关键词
            int startIndex = sentence.indexOf(keyWord);
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


    public KeyWordModel getModel() {
        KeyWordModel keyWordModel = new KeyWordModel();
        List<DyStateModel> dyStateModels = new ArrayList<>();
        List<DynamicState> dynamicStateList = dynamicProgramming.getDynamicStateList();
        for (DynamicState dynamicState : dynamicStateList) {
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
        keyWords.addAll(myKeyWords);
        for (DyStateModel modelDy : dynamicStates) {
            DynamicState dynamicState = new DynamicState(new int[]{modelDy.getId()});
            dynamicState.setValue(modelDy.getValue());
            dynamicState.setFinish(modelDy.isFinish());
            dynamicStateList.add(dynamicState);
        }
    }

    private void insertValue(WordsValue wordsValue, DynamicState dynamicState, int startIndex, int endIndex
            , String word) {//传输数值
        if (dynamicState.getValue() > wordsValue.value) {
            wordsValue.value = dynamicState.getValue();
        }
        if (dynamicState.isFinish()) {
            wordsValue.isFinish = dynamicState.isFinish();
        }
        // wordsValue.value = dynamicState.getValue();
        wordsValue.startIndex = startIndex;
        wordsValue.endIndex = endIndex;
        wordsValue.id = dynamicState.getStateId()[0];
        wordsValue.word = word;
    }

    private List<WordsValue> getBestDy(String sentence) {
        List<WordsValue> myDyList = new ArrayList<>();
        List<DynamicState> dynamicStateList = dynamicProgramming.getDynamicStateList();
        int size = sentence.length();
        for (int i = 0; i < size; i++) {
            WordsValue maxDy = null;
            for (int j = i; j < size; j++) {
                String word = sentence.substring(i, j + 1);//对该词进行收益判定
                DynamicState dynamicState = getDynamicState(word, dynamicStateList);
                if (dynamicState != null && dynamicState.getValue() >= proTh) {
                    if (maxDy == null) {
                        maxDy = new WordsValue();
                        myDyList.add(maxDy);
                    }
                    insertValue(maxDy, dynamicState, i, j, word);
                } else {//延伸后不存在该词，跳出循环
                    break;
                }
            }

        }
        return myDyList;
    }


    public Set<String> getKeyWord(String sentence) {//获取关键词
        List<WordsValue> wordsValues = getBestDy(sentence);
        Set<String> keyWords = new HashSet<>();
        if (!wordsValues.isEmpty()) {
            mergeWord(wordsValues);
            //合并完成，进行规则判断 第一步 先找出最高的一座山峰
            float maxValue = -2000;
            float maxLeftValue = -2000;
            float maxRightValue = -2000;
            WordsValue maxWordsValue = null;//最高山峰
            WordsValue leftWordsValue = null;//左峰
            WordsValue rightWordsValue = null;//右峰
            for (WordsValue wordsValue : wordsValues) {
                if (wordsValue.isFinish) {
                    maxWordsValue = wordsValue;
                    break;
                } else if (wordsValue.value > maxValue) {
                    maxValue = wordsValue.value;
                    maxWordsValue = wordsValue;
                }
            }
            if (maxWordsValue != null) {
                //第二步找出最高山峰左右两边的最高峰
                for (WordsValue wordsValue : wordsValues) {
                    boolean leftGo = wordsValue.isFinish || (leftWordsValue != null && !leftWordsValue.isFinish && wordsValue.value > maxLeftValue) ||
                            leftWordsValue == null;
                    boolean rightGo = wordsValue.isFinish || (rightWordsValue != null && !rightWordsValue.isFinish && wordsValue.value > maxRightValue) ||
                            rightWordsValue == null;
                    if (wordsValue.endIndex < maxWordsValue.startIndex && leftGo) {
                        maxLeftValue = wordsValue.value;
                        leftWordsValue = wordsValue;
                    } else if (wordsValue.startIndex > maxWordsValue.endIndex && rightGo) {
                        maxRightValue = wordsValue.value;
                        rightWordsValue = wordsValue;
                    }
                }
                //第三步，如果最高峰为终结峰，左右两峰之一也为终结峰，那关键词在两个终结峰中间的位置
                boolean first = false;
                if (maxWordsValue.isFinish) {//当最高峰是终结峰
                    if (leftWordsValue != null && leftWordsValue.isFinish &&
                            maxWordsValue.startIndex > leftWordsValue.endIndex + 1) {
                        keyWords.add(sentence.substring(leftWordsValue.endIndex + 1, maxWordsValue.startIndex));
                        first = true;
                    }
                    if (rightWordsValue != null && rightWordsValue.isFinish &&
                            maxWordsValue.endIndex + 1 < rightWordsValue.startIndex) {
                        keyWords.add(sentence.substring(maxWordsValue.endIndex + 1, rightWordsValue.startIndex));
                        first = true;
                    }
                    if (rightWordsValue != null && leftWordsValue != null && !rightWordsValue.isFinish && !leftWordsValue.isFinish) {
                        if (rightWordsValue.value > leftWordsValue.value) {
                            keyWords.add(sentence.substring(maxWordsValue.endIndex + 1));
                        } else {
                            keyWords.add(sentence.substring(0, maxWordsValue.startIndex));
                        }
                        first = true;
                    }
                }
                if (!first) {
                    if (rightWordsValue == null && maxWordsValue.endIndex < sentence.length() - 1) {
                        keyWords.add(sentence.substring(maxWordsValue.endIndex + 1));
                    } else if (rightWordsValue == null) {
                        keyWords.add(sentence.substring(0, maxWordsValue.startIndex));
                    }
                    if (leftWordsValue == null && maxWordsValue.startIndex > 0) {
                        keyWords.add(sentence.substring(0, maxWordsValue.startIndex));
                    } else if (leftWordsValue == null) {
                        keyWords.add(sentence.substring(maxWordsValue.endIndex + 1));
                    }
                    if (rightWordsValue != null && leftWordsValue != null) {
                        if (rightWordsValue.value > leftWordsValue.value) {
                            if (maxWordsValue.endIndex + 1 < rightWordsValue.startIndex) {
                                keyWords.add(sentence.substring(maxWordsValue.endIndex + 1, rightWordsValue.startIndex));
                            } else {
                                keyWords.add(sentence.substring(rightWordsValue.startIndex, rightWordsValue.endIndex + 1));
                            }
                        } else {
                            if (leftWordsValue.endIndex + 1 < maxWordsValue.startIndex) {
                                keyWords.add(sentence.substring(leftWordsValue.endIndex + 1, maxWordsValue.startIndex));
                            } else {
                                keyWords.add(sentence.substring(leftWordsValue.startIndex, leftWordsValue.endIndex + 1));
                            }
                        }
                    }

                }
            }
        }
        return keyWords;
    }

    private WordsValue isContinuity(int start1, int end1, int start2, int end2) {
        boolean isContinuity = false;
        WordsValue wordsValue = null;
        if ((start2 >= start1 && start2 <= end1) || (end2 >= start1 && end2 <= end1) ||
                (start1 >= start2 && start1 <= end2) || (end1 >= start2 && end1 <= end2)) {//相交
            isContinuity = true;
        }
        if (isContinuity) {
            wordsValue = new WordsValue();
            wordsValue.isMerge = false;
            wordsValue.startIndex = Math.min(start1, start2);
            wordsValue.endIndex = Math.max(end1, end2);
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
                        String word = dynamicState.word;
                        float value = dynamicState.value;
                        WordsValue dynamic = myDyList.get(j);
                        int myStart = dynamic.startIndex;
                        int myEnd = dynamic.endIndex;
                        WordsValue wordsValue = isContinuity(startIndex, endIndex, myStart, myEnd);
                        if (wordsValue != null) {//可进行合并
                            dynamic.isMerge = true;
                            wordsValue.isFinish = isFinish || dynamic.isFinish;
                            if (word.length() > dynamic.word.length()) {
                                wordsValue.word = word;
                            } else {
                                wordsValue.word = dynamic.word;
                            }
                            if (wordsValue.isFinish) {
                                wordsValue.value = maxFinishValue;
                            } else {
                                wordsValue.value = (float)Math.max(dynamic.value, value);
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
                for (int k = 1; k <= startIndex - i; k++) {//从每个起始字向右延伸
                    String word = sentence.substring(i, i + k);
                    int id = getID(word);
                    if (id > 0) {
                        DynamicState dynamicState = new DynamicState(new int[]{id});
                        if ((i + k) == startIndex) {//设置终结态
                            //System.out.println("终结态1:" + word);
                            dynamicState.setFinish(true);
                            dynamicState.setValue(maxFinishValue);
                            finishWords.add(word);
                        }
                        //System.out.println("语句:" + sentence + ",拆分语句:" + word + ",id:" + id + ",终结态：" + dynamicState.isFinish());
                        dynamicStateList.add(dynamicState);
                    } else if ((i + k) == startIndex) {
                        DynamicState finish = getDynamicState(word, dynamicStateList);
                        finish.setFinish(true);
                        finish.setValue(maxFinishValue);
                    }
                }
            } else if (i > endIndex) {//处于关键词右侧
                for (int k = 1; k <= size - i; k++) {//从每个起始字向右延伸
                    String word = sentence.substring(i, i + k);
                    int id = getID(word);
                    if (id > 0) {
                        DynamicState dynamicState = new DynamicState(new int[]{id});
                        if (i == endIndex + 1) {//设置终结态
                            //System.out.println("终结态2:" + word);
                            dynamicState.setFinish(true);
                            dynamicState.setValue(maxFinishValue);
                            finishWords.add(word);
                        }
                        dynamicStateList.add(dynamicState);
                    } else if (i == endIndex + 1) {
                        DynamicState finish = getDynamicState(word, dynamicStateList);
                        finish.setFinish(true);
                        finish.setValue(maxFinishValue);
                    }
                }

            }
        }
    }


    private int getID(String word) {
        int id = 0;
        boolean isHere = false;
        for (String myWord : keyWords) {
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

    static class WordsValue {
        int id;//词id
        int startIndex;//起始下标
        int endIndex;//末尾下标
        float value = 0;//该状态收益
        boolean isFinish;//是否为终结态
        String word;//词
        boolean isMerge = false;
    }
}
