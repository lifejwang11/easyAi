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

    public SentenceCreator(WordTemple wordTemple, CreatorSentenceModel creatorSentenceModel) throws Exception {
        List<String> modelList = creatorSentenceModel.getWordList();
        int size = modelList.size();
        for (int i = 0; i < size; i++) {
            wordList.add(modelList.get(i));
        }
        nerveManager = new NerveManager(wordList.size(), wordTemple.getWordVectorDimension(), wordList.size()
                , 1, new Tanh(), false, wordTemple.getStudyPoint(), RZ.NOT_RZ, 0);
        nerveManager.init(true, false, wordTemple.isShowLog(), true, 0, 0);
        maxWordNumber = wordTemple.getMaxWordNumber();
        nerveManager.insertModelParameter(creatorSentenceModel.getModelParameter());
    }

    public SentenceCreator(List<String> sentenceList, WordTemple wordTemple) throws Exception {
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
        nerveManager = new NerveManager(wordList.size(), wordTemple.getWordVectorDimension(), wordList.size()
                , 1, new Tanh(), false, wordTemple.getStudyPoint(), RZ.NOT_RZ, 0);
        nerveManager.init(true, false, wordTemple.isShowLog(), true, 0, 0);
    }

    public List<String> anySort(List<String> sentences) {//做乱序
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

    public void insertModel(CreatorSentenceModel creatorSentenceModel) throws Exception {
        nerveManager.insertModelParameter(creatorSentenceModel.getModelParameter());
        List<String> modelList = creatorSentenceModel.getWordList();
        int size = modelList.size();
        for (int i = 0; i < size; i++) {
            wordList.add(modelList.get(i));
        }
    }

    public String fill(String sentence, Talk talk) throws Exception {
        int splitSize = talk.getSplitWord(sentence).size();//切词数量
        boolean isFill = splitSize < 5 && sentence.length() < 15;
        String upWord = null;
        while (isFill) {
            CreatorWord creatorWord = new CreatorWord();
            double[] feature = getFeature(sentence);
            studyDNN(feature, 0, creatorWord, false);
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
                splitSize = talk.getSplitWord(sentence).size();//切词数量
                isFill = splitSize < 5 && sentence.length() < 15;
            }
        }
        return sentence;
    }

    public void study() throws Exception {
        int index = 1;
        for (String sentence : sentenceList) {
            System.out.println("i===" + index);
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
