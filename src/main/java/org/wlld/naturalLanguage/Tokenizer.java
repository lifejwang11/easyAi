package org.wlld.naturalLanguage;

import org.wlld.randomForest.DataTable;
import org.wlld.randomForest.RandomForest;
import org.wlld.tools.ArithUtil;
import org.wlld.tools.Frequency;

import java.util.*;

/**
 * @author lidapeng
 * @description 分词器
 * @date 7:42 上午 2020/2/23
 */
public class Tokenizer extends Frequency {
    private List<Sentence> sentences = WordTemple.get().getSentences();//所有断句
    private List<WorldBody> allWorld = WordTemple.get().getAllWorld();//所有词集合
    private List<List<String>> wordTimes = WordTemple.get().getWordTimes();//所有词编号
    private Word nowWord;//上一次出现的关键字

    public void start(Map<Integer, List<String>> model) throws Exception {
        //model的主键是类别，值是该类别语句的集合
        for (Map.Entry<Integer, List<String>> mod : model.entrySet()) {
            if (mod.getKey() != 0) {
                List<String> st = mod.getValue();//语句
                int key = mod.getKey();//类别
                for (String sentence : st) {//遍历每个类别的每个语句
                    Sentence sentenceWords = new Sentence(key);
                    catchSentence(sentence, sentenceWords);
                    Word word = sentenceWords.getFirstWord();
                    if (word != null) {
                        worldMuch(word, allWorld, key);//构建句子内的层级关系并添加词频
                    }
                }
            }
        }
        restructure();//对集合中的词进行词频统计
        //这里分词已经结束,对词进行编号
        number();
        //进入随机森林进行学习
        study();
    }

    private int getKey(List<String> words, String testWord) {
        int nub = 0;
        int size = words.size();
        for (int i = 0; i < size; i++) {
            String word = words.get(i);
            if (testWord.hashCode() == word.hashCode() && testWord.equals(word)) {
                nub = i + 1;
                break;
            }
        }
        if (nub == 0) {
            words.add(testWord);
            nub = words.size();
        }
        return nub;
    }

    private void number() {//分词编号
        System.out.println("开始编码:" + sentences.size());
        for (Sentence sentence : sentences) {
            List<Integer> features = sentence.getFeatures();
            List<String> sentenceList = sentence.getKeyWords();
            int size = sentenceList.size();//时间序列的深度
            for (int i = 0; i < size; i++) {
                if (wordTimes.size() < i + 1) {
                    wordTimes.add(new ArrayList<>());
                }
                String word = sentenceList.get(i);//当前关键字
                List<String> list = wordTimes.get(i);
                int nub = getKey(list, word);
                features.add(nub);
            }
        }
    }

    private void study() throws Exception {
        Set<String> column = new HashSet<>();
        for (int i = 0; i < 8; i++) {
            int t = i + 1;
            column.add("a" + t);
        }
        column.add("key");
        DataTable dataTable = new DataTable(column);
        dataTable.setKey("key");
        //初始化随机森林
        RandomForest randomForest = new RandomForest(11);
        WordTemple.get().setRandomForest(randomForest);//保存随机森林到模版
        randomForest.init(dataTable);
        for (Sentence sentence : sentences) {
            LangBody langBody = new LangBody();
            List<Integer> features = sentence.getFeatures();
            langBody.setKey(sentence.getKey());
            for (int i = 0; i < 8; i++) {
                int nub = 0;
                if (features.size() > i) {
                    nub = features.get(i);
                }
                int t = i + 1;
                switch (t) {
                    case 1:
                        langBody.setA1(nub);
                        break;
                    case 2:
                        langBody.setA2(nub);
                        break;
                    case 3:
                        langBody.setA3(nub);
                        break;
                    case 4:
                        langBody.setA4(nub);
                        break;
                    case 5:
                        langBody.setA5(nub);
                        break;
                    case 6:
                        langBody.setA6(nub);
                        break;
                    case 7:
                        langBody.setA7(nub);
                        break;
                    case 8:
                        langBody.setA8(nub);
                        break;
                }
            }
            randomForest.insert(langBody);
        }
        randomForest.study();
    }

    private void restructure() {//对句子里面的Word进行词频统计
        for (Sentence words : sentences) {
            List<WorldBody> listWord = allWorld;
            List<Word> waitWorld = words.getWaitWords();
            for (Word word : waitWorld) {
                String myWord = word.getWord();
                WorldBody body = getBody(myWord, listWord);
                listWord = body.getWorldBodies();
                word.setWordFrequency(body.getWordFrequency());
            }
        }
        for (Sentence words : sentences) {
            radiation(words);
        }
    }

    public void radiation(Sentence sentenceWords) {//对句子中的词开始辐射延伸
        //首先词与它自己的右节点和左节点进行比较
        nowWord = null;
        Word firstWord = sentenceWords.getFirstWord();
        KeyWord word = new KeyWord();
        word.setWord(firstWord);
        word.setOk(false);
        List<String> keyWords = new ArrayList<>();
        while (word.getWord() != null) {
            word = keyWord(-1, word, new double[]{firstWord.getWordFrequency()});
            Word myWord = word.getWord();
            String wordT = myWord.getWord();//当前截取到的分词串
            String keyWord;
            if (nowWord == null) {//这句话的第一个分词还没有产生*****
                //此时的分词结果就是WordT
                keyWord = wordT;
            } else {//之前产生了分词
                keyWord = wordT.substring(nowWord.getWord().length());
            }
            keyWords.add(keyWord);
            nowWord = myWord;
            word.setOk(false);
            word.setWord(word.getWord().getSon());
        }
        sentenceWords.setKeyWords(keyWords);
    }

    private double[] getDiff(double[] diff, Word word) {
        double[] diffef = new double[diff.length + 1];
        for (int i = 0; i < diffef.length; i++) {
            if (i == diffef.length - 1) {
                diffef[i] = word.getWordFrequency();
            } else {
                diffef[i] = diff[i];
            }
        }
        return diffef;
    }

    private KeyWord keyWord(double dm, KeyWord words, double[] diff) {//平均差值，离散系数，是否为关键字
        double right = 0;
        boolean bm = words.isOk();
        if (!bm) {
            Word word = words.getWord();
            if (word.getSon() != null) {
                double db = wordEnd(word, new ArrayList<>(), 0);//计算身前平均值
                //与它儿子词频的差要小于辐射向前的词频差的平均值
                boolean isAvgOk = (ArithUtil.mul(word.getWordFrequency() - word.getSon().getWordFrequency(), WordConst.Word_Noise)) <= db;
                if (isAvgOk) {//平均值检测
                    diff = getDiff(diff, word.getSon());
                    right = dc(diff);
                    if (dm > -1) {
                        if (ArithUtil.mul(right, WordConst.Word_Noise) <= dm) {//继续向下探索
                            words.setOk(false);
                            words.setWord(word.getSon());
                            words = keyWord(right, words, diff);
                        } else {//截断，停止探索
                            words.setOk(true);//是关键字
                        }
                    } else {//第一次 继续向下探索
                        words.setOk(false);
                        words.setWord(word.getSon());
                        words = keyWord(right, words, diff);
                    }
                } else {//截断 停止探索
                    words.setOk(true);
                }
            } else {//截断 停止探索
                words.setOk(true);
            }
        }
        return words;
    }

    private double wordEnd(Word word, List<Integer> av, double a) {//对一句话中的词进行处理
        //先取全句平均差值
        Word son = word.getSon();
        if (son != null) {
            av.add(word.getWordFrequency() - son.getWordFrequency());
            a = wordEnd(son, av, a);
        } else {//最后计算平均值
            double[] allNub = new double[av.size()];
            for (int i = 0; i < av.size(); i++) {
                allNub[i] = av.get(i);
            }
            a = average(allNub);//平均差值
        }
        return a;
    }

    private WorldBody getBody(String word, List<WorldBody> worlds) {
        WorldBody myBody = null;
        for (WorldBody body : worlds) {
            if (body.getWordName().hashCode() == word.hashCode() && body.getWordName().equals(word)) {
                myBody = body;
                break;
            }
        }
        return myBody;
    }

    private void catchSentence(String sentence, Sentence sentenceWords) {//把句子拆开
        int len = sentence.length();
        for (int i = 0; i < len; i++) {
            String word = sentence.substring(0, i + 1);
            sentenceWords.setWord(word);
        }
        sentences.add(sentenceWords);
    }

    private void worldMuch(Word word, List<WorldBody> worldBodies, int type) {//分类词频处理
        boolean bm = false;
        String check = word.getWord();
        for (WorldBody myWorld : worldBodies) {
            String waitCheck = myWorld.getWordName();
            if (waitCheck.hashCode() == check.hashCode() && waitCheck.equals(check)) {
                bm = true;
                myWorld.addNub(type);
                if (word.getSon() != null) {//没有找到最后一级了
                    worldMuch(word.getSon(), myWorld.getWorldBodies(), type);
                }
                break;
            }
        }
        if (!bm) {//找不到了
            saveList(word, worldBodies, type);
        }
    }

    private void saveList(Word word, List<WorldBody> myWorld, int type) {//保存新词
        WorldBody body = new WorldBody();
        List<WorldBody> list = new ArrayList<>();
        body.setWordName(word.getWord());
        body.addNub(type);
        body.setWorldBodies(list);
        body.setWord(word);
        myWorld.add(body);
        if (word.getSon() != null) {
            saveList(word.getSon(), list, type);
        }
    }
}
