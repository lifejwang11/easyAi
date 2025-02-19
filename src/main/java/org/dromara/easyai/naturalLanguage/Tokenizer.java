package org.dromara.easyai.naturalLanguage;

import org.dromara.easyai.tools.ArithUtil;
import org.dromara.easyai.tools.Frequency;

import java.util.*;

/**
 * @author lidapeng
 * @description 分词器
 * @date 7:42 上午 2020/2/23
 */
public class Tokenizer extends Frequency {
    private final List<Sentence> sentences;//所有断句
    private final List<WorldBody> allWorld;//所有词集合
    private final float wordNoise;

    public Tokenizer(WordTemple wordTemple) {
        sentences = wordTemple.getSentences();//所有断句
        allWorld = wordTemple.getAllWorld();//所有词集合
        wordNoise = wordTemple.getWordNoise();
    }

    public void start(List<String> st) throws Exception {
        //model的主键是类别，值是该类别语句的集合
        for (String sentence : st) {//遍历每个类别的每个语句
            Sentence sentenceWords = new Sentence();
            catchSentence(sentence, sentenceWords);
            Word word = sentenceWords.getFirstWord();
            if (word != null) {
                worldMuch(word, allWorld);//构建句子内的层级关系并添加词频
            }
        }
        restructure();//对集合中的词进行词频统计
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
        //上一次出现的关键字
        Word nowWord = null;
        Word firstWord = sentenceWords.getFirstWord();
        KeyWord word = new KeyWord();
        word.setWord(firstWord);
        word.setOk(false);
        List<String> keyWords = new ArrayList<>();
        while (word.getWord() != null) {
            word = keyWord(-1, word, new float[]{firstWord.getWordFrequency()});
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

    private float[] getDiff(float[] diff, Word word) {
        float[] diffef = new float[diff.length + 1];
        for (int i = 0; i < diffef.length; i++) {
            if (i == diffef.length - 1) {
                diffef[i] = word.getWordFrequency();
            } else {
                diffef[i] = diff[i];
            }
        }
        return diffef;
    }

    private KeyWord keyWord(float dm, KeyWord words, float[] diff) {//平均差值，离散系数，是否为关键字
        float right = 0;
        boolean bm = words.isOk();
        if (!bm) {
            Word word = words.getWord();
            if (word.getSon() != null) {
                float db = wordEnd(word, new ArrayList<>(), 0);//计算身前平均值
                //与它儿子词频的差要小于辐射向前的词频差的平均值
                boolean isAvgOk = (ArithUtil.mul(word.getWordFrequency() - word.getSon().getWordFrequency(), wordNoise)) <= db;
                if (isAvgOk) {//平均值检测
                    diff = getDiff(diff, word.getSon());
                    right = dc(diff);
                    if (dm > -1) {
                        if (ArithUtil.mul(right, wordNoise) <= dm) {//继续向下探索
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

    private float wordEnd(Word word, List<Integer> av, float a) {//对一句话中的词进行处理
        //先取全句平均差值
        Word son = word.getSon();
        if (son != null) {
            av.add(word.getWordFrequency() - son.getWordFrequency());
            a = wordEnd(son, av, a);
        } else {//最后计算平均值
            float[] allNub = new float[av.size()];
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

    private void worldMuch(Word word, List<WorldBody> worldBodies) {//分类词频处理
        boolean bm = false;
        String check = word.getWord();
        for (WorldBody myWorld : worldBodies) {
            String waitCheck = myWorld.getWordName();
            if (waitCheck.hashCode() == check.hashCode() && waitCheck.equals(check)) {
                bm = true;
                myWorld.addNub();
                if (word.getSon() != null) {//没有找到最后一级了
                    worldMuch(word.getSon(), myWorld.getWorldBodies());
                }
                break;
            }
        }
        if (!bm) {//找不到了
            saveList(word, worldBodies);
        }
    }

    private void saveList(Word word, List<WorldBody> myWorld) {//保存新词
        WorldBody body = new WorldBody();
        List<WorldBody> list = new ArrayList<>();
        body.setWordName(word.getWord());
        body.addNub();
        body.setWorldBodies(list);
        body.setWord(word);
        myWorld.add(body);
        if (word.getSon() != null) {
            saveList(word.getSon(), list);
        }
    }
}
