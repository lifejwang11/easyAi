package org.wlld.naturalLanguage;


import org.wlld.randomForest.RandomForest;
import org.wlld.tools.ArithUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidapeng
 * @description 语句分类
 * @date 4:14 下午 2020/2/23
 */
public class Talk {
    private List<WorldBody> allWorld = WordTemple.get().getAllWorld();//所有词集合
    private RandomForest randomForest = WordTemple.get().getRandomForest();//获取随机森林模型
    private List<List<String>> wordTimes = WordTemple.get().getWordTimes();

    public List<Integer> talk(String sentence) throws Exception {
        List<Integer> typeList = new ArrayList<>();
        String rgm = null;
        if (sentence.indexOf(",") > -1) {
            rgm = ",";
        } else if (sentence.indexOf("，") > -1) {
            rgm = "，";
        }
        String[] sens;
        if (rgm != null) {
            sens = sentence.split(rgm);
        } else {
            sens = new String[]{sentence};
        }
        //拆词
        List<Sentence> sentences = new ArrayList<>();
        for (int i = 0; i < sens.length; i++) {
            List<Sentence> sentenceList = catchSentence(sentence);
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
                            wrong++;
                        }
                    }
                    features.add(nub);
                }
                int type = 0;
                if (ArithUtil.div(wrong, wordNumber) < WordTemple.get().getGarbageTh()) {
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
    }

    private int getNub(List<String> words, String testWord) {
        int nub = 0;
        int size = words.size();
        for (int i = 0; i < size; i++) {
            String word = words.get(i);
            if (testWord.hashCode() == word.hashCode() && testWord.equals(word)) {
                nub = i + 1;
                break;
            }
        }
        return nub;
    }

    private List<Sentence> catchSentence(String sentence) {//把句子拆开
        int len = sentence.length();
        List<Sentence> sentences = new ArrayList<>();
        for (int j = 0; j < len - 2; j++) {
            Sentence sentenceWords = new Sentence();
            for (int i = j; i < len; i++) {
                String word = sentence.substring(j, i + 1);
                sentenceWords.setWord(word);
            }
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
        Tokenizer tokenizer = new Tokenizer();
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
