package org.dromara.easyai.naturalLanguage.word;


import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.config.RZ;
import org.dromara.easyai.config.SentenceConfig;
import org.dromara.easyai.entity.SentenceModel;
import org.dromara.easyai.entity.WordMatrix;
import org.dromara.easyai.entity.WordTwoVectorModel;
import org.dromara.easyai.function.Tanh;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.rnnJumpNerveEntity.MyWordFeature;
import org.dromara.easyai.rnnNerveCenter.NerveManager;
import org.dromara.easyai.rnnNerveEntity.SensoryNerve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 词嵌入向量训练
 */
public class WordEmbedding extends MatrixOperation {
    private NerveManager nerveManager;
    private SentenceModel sentenceModel;
    private final List<String> wordList = new ArrayList<>();//单字集合
    private SentenceConfig config;
    private int wordVectorDimension;
    private int studyTimes = 1;

    public void setStudyTimes(int studyTimes) {
        this.studyTimes = studyTimes;
    }

    public void setConfig(SentenceConfig config) {
        this.config = config;
    }

    public int getWordVectorDimension() {
        return wordVectorDimension;
    }

    public void init(SentenceModel sentenceModel, int wordVectorDimension) throws Exception {
        this.wordVectorDimension = wordVectorDimension;
        this.sentenceModel = sentenceModel;
        wordList.addAll(sentenceModel.getWordSet());
        nerveManager = new NerveManager(wordList.size(), wordVectorDimension, wordList.size()
                , 1, new Tanh(), false, config.getWeStudyPoint(), config.getRzModel(),
                config.getWeLParam());
        nerveManager.init(true, false, false, true, 0, 0);
    }

    public List<String> getWordList() {
        return wordList;
    }

    public String getWord(int id) {
        return wordList.get(id);
    }

    public void insertModel(WordTwoVectorModel wordTwoVectorModel, int wordVectorDimension) throws Exception {
        wordList.clear();
        this.wordVectorDimension = wordVectorDimension;
        List<String> myWordList = wordTwoVectorModel.getWordList();
        wordList.addAll(myWordList);
        nerveManager = new NerveManager(wordList.size(), wordVectorDimension, wordList.size()
                , 1, new Tanh(), false, config.getWeStudyPoint(), RZ.NOT_RZ, 0);
        nerveManager.init(true, false, false, true, 0, 0);
        nerveManager.insertModelParameter(wordTwoVectorModel.getModelParameter());
    }

    public MyWordFeature getEmbedding(String word, long eventId, boolean once) throws Exception {//做截断
        MyWordFeature myWordFeature = new MyWordFeature();
        int wordDim = wordVectorDimension;//
        Matrix matrix = null;
        for (int i = 0; i < word.length(); i++) {
            WordMatrix wordMatrix = new WordMatrix(wordDim);
            String myWord;
            if (!once) {
                myWord = word.substring(i, i + 1);
            } else {
                myWord = word;
            }
            int index = getID(myWord);
            studyDNN(eventId, index, 0, wordMatrix, true, false);
            if (matrix == null) {
                myWordFeature.setFirstFeatureList(wordMatrix.getList());
                matrix = wordMatrix.getVector();
            } else {
                matrix = pushVector(matrix, wordMatrix.getVector(), true);
            }
            if (once) {
                break;
            }
        }
        myWordFeature.setFeatureMatrix(matrix);
        return myWordFeature;
    }

    private void studyDNN(long eventId, int featureIndex, int resIndex, OutBack outBack, boolean isEmbedding, boolean isStudy) throws Exception {
        List<SensoryNerve> sensoryNerves = nerveManager.getSensoryNerves();
        int size = sensoryNerves.size();
        Map<Integer, Double> map = new HashMap<>();
        if (resIndex > 0) {
            map.put(resIndex + 1, 1D);
        }
        for (int i = 0; i < size; i++) {
            double feature = 0;
            if (i == featureIndex) {
                feature = 1;
            }
            sensoryNerves.get(i).postMessage(eventId, feature, isStudy, map, outBack, isEmbedding, null);
        }
    }


    public WordTwoVectorModel start() throws Exception {//开始进行词向量训练
        List<String[]> sentenceList = sentenceModel.getSentenceList();
        int size = sentenceList.size();
        System.out.println("词嵌入训练启动...");
        int allTimes = studyTimes * size;
        int index = 0;
        for (int k = 0; k < studyTimes; k++) {
            for (int i = 0; i < size; i++) {
                index++;
                long start = System.currentTimeMillis();
                study(sentenceList.get(i));
                long end = System.currentTimeMillis() - start;
                double r = (double) index / allTimes * 100;
                String result = String.format("%.6f", r);
                System.out.println("size:" + size + ",index:" + i + ",耗时:" + end + ",完成度:" + result + "%");
            }
        }
        WordTwoVectorModel wordTwoVectorModel = new WordTwoVectorModel();
        wordTwoVectorModel.setModelParameter(nerveManager.getModelParameter());
        wordTwoVectorModel.setWordList(wordList);
        //词向量训练结束
        return wordTwoVectorModel;
    }

    private void study(String[] word) throws Exception {
        int[] indexArray = new int[word.length];
        for (int i = 0; i < word.length; i++) {
            int index = getID(word[i]);
            indexArray[i] = index;
        }
        for (int i = 0; i < indexArray.length; i++) {
            int index = indexArray[i];
            for (int j = 0; j < indexArray.length; j++) {
                if (i != j) {
                    int resIndex = indexArray[j];
                    studyDNN(1, index, resIndex, null, false, true);
                }
            }
        }
    }

    public int getID(String word) {
        int index = 0;
        int size = wordList.size();
        for (int i = 0; i < size; i++) {
            if (wordList.get(i).equals(word)) {
                index = i;
                break;
            }
        }
        return index;
    }
}
