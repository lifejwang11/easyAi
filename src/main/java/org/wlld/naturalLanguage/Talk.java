package org.wlld.naturalLanguage;


import org.wlld.randomForest.RandomForest;
import org.wlld.tools.ArithUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @description 语句分类
 * @date 4:14 下午 2020/2/23
 */
public class Talk {
    private List<WorldBody> allWorld;//所有词集合
    private RandomForest randomForest;//获取随机森林模型
    private List<List<String>> wordTimes;
    private WordTemple wordTemple;

    public Talk(WordTemple wordTemple) {
        this.wordTemple = wordTemple;
        allWorld = wordTemple.getAllWorld();//所有词集合
        randomForest = wordTemple.getRandomForest();//获取随机森林模型
        wordTimes = wordTemple.getWordTimes();
    }

    public List<List<String>> getSplitWord(String sentence) {//单纯进行拆词
        List<Sentence> sentences = splitSentence(sentence);
        List<List<String>> words = new ArrayList<>();
        for (Sentence sentence1 : sentences) {
            words.add(sentence1.getKeyWords());
        }
        return words;
    }

    private List<Sentence> splitSentence(String sentence) {
        String[] sens = sentence.split("，|。|？|！|；|、|：");
        //拆词
        List<Sentence> sentences = new ArrayList<>();
        for (int i = 0; i < sens.length; i++) {
            String mySentence = sens[i];
            List<Sentence> sentenceList = catchSentence(mySentence);
            int key = 0;
            int nub = 0;
            for (int j = 0; j < sentenceList.size(); j++) {
                Sentence sentence1 = sentenceList.get(j);
                restructure(sentence1);
                int size = sentence1.getKeyWords().size();
                if (size > nub) {
                    key = j;
                    nub = size;
                }
            }
            //System.out.println(sentenceList.get(key).getKeyWords());
            sentences.add(sentenceList.get(key));
        }
        return sentences;
    }

    public List<Integer> talk(String sentence) throws Exception {
        if (!wordTemple.isSplitWord()) {
            List<Integer> typeList = new ArrayList<>();
            List<Sentence> sentences = splitSentence(sentence);
            //进行识别
            if (randomForest != null) {
                for (Sentence sentence1 : sentences) {
                    List<Integer> features = sentence1.getFeatures();
                    List<String> keyWords = sentence1.getKeyWords();//拆分的关键词
                    int wrong = 0;
                    int wordNumber = keyWords.size();
                    for (int i = 0; i < 8; i++) {
                        int nub = 0;
                        if (keyWords.size() > i) {
                            List<String> words = wordTimes.get(i);
                            nub = getNub(words, keyWords.get(i));
                            if (nub == 0) {//出现了不认识的词
                                System.out.println("不认识的词：" + keyWords.get(i));
                                wrong++;
                            } else {
                                System.out.println("认识的词:" + keyWords.get(i));
                            }
                        }
                        features.add(nub);
                    }
                    int type = 0;
                    if (ArithUtil.div(wrong, wordNumber) < wordTemple.getGarbageTh()) {
                        LangBody langBody = new LangBody();
                        langBody.setA1(features.get(0));
                        langBody.setA2(features.get(1));
                        langBody.setA3(features.get(2));
                        langBody.setA4(features.get(3));
                        langBody.setA5(features.get(4));
                        langBody.setA6(features.get(5));
                        langBody.setA7(features.get(6));
                        langBody.setA8(features.get(7));
                        type = randomForest.forest(langBody);
                    }
                    typeList.add(type);
                }
                return typeList;
            } else {
                throw new Exception("forest is not study");
            }
        } else {
            throw new Exception("isSplitWord is true");
        }
    }

    private int getDifferentPoint(String word1, String word2) {
        String maxWord;
        String minWord;
        if (word1.length() >= word2.length()) {
            maxWord = word1;
            minWord = word2;
        } else {
            maxWord = word2;
            minWord = word1;
        }
        int times = maxWord.length() - minWord.length();
        boolean isRight = false;
        for (int i = 0; i <= times; i++) {
            String testWord = maxWord.substring(i, i + minWord.length());
            if (testWord.hashCode() == minWord.hashCode() && testWord.equals(minWord)) {
                isRight = true;
                break;
            }
        }
        if (isRight) {
            return times;
        } else {
            return -1;
        }
    }

    private int getNub(List<String> words, String testWord) {
        int nub = 0;
        int size = words.size();
        boolean isHere = false;//是否查询到了
        for (int i = 0; i < size; i++) {
            String word = words.get(i);
            if (testWord.hashCode() == word.hashCode() && testWord.equals(word)) {
                isHere = true;
                nub = i + 1;
                break;
            }
        }
        if (!isHere) {//进行模糊查询
            int minTimes = -1;
            for (int i = 0; i < size; i++) {
                int times = getDifferentPoint(words.get(i), testWord);
                if (times > 0) {
                    if (minTimes < 0 || times < minTimes) {
                        minTimes = times;
                        nub = i + 1;
                    }
                }
            }
        }
        return nub;
    }

    private List<Sentence> catchSentence(String sentence) {//把句子拆开
        int len = sentence.length();
        List<Sentence> sentences = new ArrayList<>();
        if (len > 1) {
            for (int j = 0; j < len - 1; j++) {
                Sentence sentenceWords = new Sentence();
                for (int i = j; i < len; i++) {
                    String word = sentence.substring(j, i + 1);
                    sentenceWords.setWord(word);
                }
                sentences.add(sentenceWords);
            }
        } else {
            Sentence sentenceWords = new Sentence();
            sentenceWords.setWord(sentence);
            sentences.add(sentenceWords);
        }
        return sentences;
    }

    private void restructure(Sentence words) {//对句子里面的Word进行词频统计
        List<WorldBody> listWord = allWorld;
        List<Word> waitWorld = words.getWaitWords();
        for (Word word : waitWorld) {
            String myWord = word.getWord();
            WorldBody body = getBody(myWord, listWord);
            if (body == null) {//已经无法查找到对应的词汇了
                word.setWordFrequency(1);
                break;
            }
            listWord = body.getWorldBodies();//这个body报了一次空指针
            word.setWordFrequency(body.getWordFrequency());
        }
        Tokenizer tokenizer = new Tokenizer(wordTemple);
        tokenizer.radiation(words);

    }

    private WorldBody getBody(String word, List<WorldBody> worlds) {
        //TODO 这里有个BUG 当myBody出现空的时候断词已经找不到了
        WorldBody myBody = null;
        for (WorldBody body : worlds) {
            if (body.getWordName().hashCode() == word.hashCode() && body.getWordName().equals(word)) {
                myBody = body;
                break;
            }
        }
        return myBody;
    }
}
