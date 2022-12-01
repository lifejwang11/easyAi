package org.wlld.naturalLanguage.languageCreator;

import org.wlld.config.RZ;
import org.wlld.function.Tanh;
import org.wlld.i.OutBack;
import org.wlld.naturalLanguage.Talk;
import org.wlld.naturalLanguage.WordTemple;
import org.wlld.nerveCenter.NerveManager;
import org.wlld.nerveEntity.SensoryNerve;

import java.util.*;

public class SentenceCreator {//语言生成器
    private List<String> sentenceList;
    private List<String> wordList = new ArrayList<>();//模型
    private NerveManager nerveManager;//模型
    private int maxWordNumber;
    private int maxId;//id最大值
    private WordTemple wordTemple;

    public void initModel(WordTemple wordTemple, CreatorSentenceModel creatorSentenceModel) throws Exception {
        this.wordTemple = wordTemple;
        List<String> modelList = creatorSentenceModel.getWordList();
        int size = modelList.size();
        for (int i = 0; i < size; i++) {
            wordList.add(modelList.get(i));
        }
        maxId = wordList.size() + 1;
        nerveManager = new NerveManager(wordList.size(), wordTemple.getWordVectorDimension(), wordList.size() + 1
                , 1, new Tanh(), false, wordTemple.getStudyPoint(), RZ.L1, 0);
        nerveManager.init(true, false, wordTemple.isShowLog(), true, 0, 0);
        maxWordNumber = wordTemple.getMaxWordNumber();
        nerveManager.insertModelParameter(creatorSentenceModel.getModelParameter());
    }

    public void initFirst(List<String> sentenceList, WordTemple wordTemple) throws Exception {
        this.wordTemple = wordTemple;
        this.sentenceList = anySort(sentenceList);
        int size = this.sentenceList.size();
        maxWordNumber = wordTemple.getMaxWordNumber();
        Set<String> wordSet = new HashSet<>();
        for (int i = 0; i < size; i++) {
            init(this.sentenceList.get(i), wordSet);
        }
        for (String word : wordSet) {
            wordList.add(word);
        }
        maxId = wordList.size() + 1;
        nerveManager = new NerveManager(wordList.size(), wordTemple.getWordVectorDimension(), wordList.size() + 1
                , 1, new Tanh(), false, wordTemple.getStudyPoint(), RZ.L1, 0);
        nerveManager.init(true, false, wordTemple.isShowLog(), true, 0, 0);
    }

    private List<String> anySort(List<String> sentences) {//做乱序
        Random random = new Random();
        List<String> sent = new ArrayList<>();
        int time = sentences.size();
        for (int i = 0; i < time; i++) {
            int size = sentences.size();
            int index = random.nextInt(size);
            sent.add(sentences.get(index));
            sentences.remove(index);
        }
        return sent;
    }

    public CreatorSentenceModel getModel() throws Exception {//获取模型
        CreatorSentenceModel creatorSentenceModel = new CreatorSentenceModel();
        creatorSentenceModel.setModelParameter(nerveManager.getModelParameter());
        creatorSentenceModel.setWordList(wordList);
        return creatorSentenceModel;
    }

    public String fill(String sentence, Talk talk) throws Exception {
        int splitSize = talk.getSplitWord(sentence).get(0).size();//切词数量
        boolean isFill = splitSize < wordTemple.getMaxSplitSize() && sentence.length() < wordTemple.getMaxWordSize();
        String upWord = null;
        while (isFill) {
            CreatorWord creatorWord = new CreatorWord();
            double[] feature = getFeature(sentence);
            studyDNN(feature, 0, creatorWord, false);
            if (creatorWord.getId() < maxId) {
                int id = creatorWord.getId() - 1;
                String word = wordList.get(id);//终止条件1 字数 2，拆词
                if (upWord == null) {
                    upWord = word;
                } else {
                    if (upWord.equals(word)) {
                        isFill = false;
                    } else {
                        upWord = word;
                    }
                }
                if (isFill) {
                    sentence = word + sentence;
                    splitSize = talk.getSplitWord(sentence).get(0).size();//切词数量
                    isFill = splitSize < wordTemple.getMaxSplitSize() && sentence.length() < wordTemple.getMaxWordSize();
                }
            } else {
                break;
            }
        }
        return sentence;
    }

    public void study() throws Exception {
        int index = 1;
        for (String sentence : sentenceList) {
            System.out.println("i===" + index);
            studyDNN(getFeature(sentence), maxId, null, true);//终结态
            for (int i = 0; i < sentence.length() - 1; i++) {
                String response = sentence.substring(i, i + 1);//单字
                String request = sentence.substring(i + 1);//后缀
                int restIndex = getRestIndex(response);
                double[] feature = getFeature(request);
                studyDNN(feature, restIndex, null, true);
            }
            index++;
        }
    }

    private int getRestIndex(String word) {
        int size = wordList.size();
        int index = 0;
        for (int i = 0; i < size; i++) {
            if (wordList.get(i).equals(word)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private double[] getFeature(String words) {//编码
        double feature[] = new double[wordList.size()];
        double timeIndex = 1D / maxWordNumber;
        int size = words.length();
        if (size > maxWordNumber) {
            size = maxWordNumber;
        }
        for (int i = 0; i < size; i++) {
            int index = getRestIndex(words.substring(i, i + 1));
            feature[index] = 1 - i * timeIndex;
        }
        return feature;
    }

    private void studyDNN(double[] feature, int resIndex, OutBack outBack, boolean isStudy) throws Exception {
        List<SensoryNerve> sensoryNerves = nerveManager.getSensoryNerves();
        int size = sensoryNerves.size();
        Map<Integer, Double> map = new HashMap<>();
        map.put(resIndex + 1, 1D);
        for (int i = 0; i < size; i++) {
            double myFeature = feature[i];
            sensoryNerves.get(i).postMessage(1, myFeature, isStudy, map, outBack);
        }
    }

    private void init(String sentence, Set<String> wordSet) {
        for (int i = 0; i < sentence.length(); i++) {
            wordSet.add(sentence.substring(i, i + 1));
        }
    }

}
