package org.wlld.naturalLanguage.word;


import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.config.RZ;
import org.wlld.config.SentenceConfig;
import org.wlld.entity.SentenceModel;
import org.wlld.entity.WordMatrix;
import org.wlld.entity.WordTwoVectorModel;
import org.wlld.function.Tanh;
import org.wlld.i.OutBack;
import org.wlld.rnnJumpNerveEntity.MyWordFeature;
import org.wlld.rnnNerveCenter.NerveManager;
import org.wlld.rnnNerveEntity.SensoryNerve;

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
public class WordEmbedding {
    private NerveManager nerveManager;
    private SentenceModel sentenceModel;
    private final List<String> wordList = new ArrayList<>();//单字集合
    private SentenceConfig config;
    private int wordVectorDimension;

    public void setConfig(SentenceConfig config) {
        this.config = config;
    }

    public void init(SentenceModel sentenceModel, int wordVectorDimension) throws Exception {
        this.wordVectorDimension = wordVectorDimension;
        this.sentenceModel = sentenceModel;
        wordList.addAll(sentenceModel.getWordSet());
        nerveManager = new NerveManager(wordList.size(), wordVectorDimension, wordList.size()
                , 1, new Tanh(), false, config.getWeStudyPoint(), RZ.NOT_RZ, 0);
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

    public MyWordFeature getEmbedding(String word, long eventId) throws Exception {//做截断
        MyWordFeature myWordFeature = new MyWordFeature();
        int wordDim = wordVectorDimension;//
        Matrix matrix = null;
        for (int i = 0; i < word.length(); i++) {
            WordMatrix wordMatrix = new WordMatrix(wordDim);
            String myWord = word.substring(i, i + 1);
            int index = getID(myWord);
            studyDNN(eventId, index, 0, wordMatrix, true, false);
            if (matrix == null) {
                myWordFeature.setFirstFeatureList(wordMatrix.getList());
                matrix = wordMatrix.getVector();
            } else {
                matrix = MatrixOperation.pushVector(matrix, wordMatrix.getVector(), true);
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
        List<String> sentenceList = sentenceModel.getSentenceList();
        int size = sentenceList.size();
        int index = 0;
        System.out.println("词嵌入训练启动...");
        for (int i = index; i < size; i++) {
            long start = System.currentTimeMillis();
            study(sentenceList.get(i));
            long end = System.currentTimeMillis() - start;
            index++;
            double r = (index / (double) size) * 100;
            String result = String.format("%.6f", r);
            System.out.println("size:" + size + ",index:" + index + ",耗时:" + end + ",完成度:" + result + "%");
        }
        WordTwoVectorModel wordTwoVectorModel = new WordTwoVectorModel();
        wordTwoVectorModel.setModelParameter(nerveManager.getModelParameter());
        wordTwoVectorModel.setWordList(wordList);
        //词向量训练结束
        return wordTwoVectorModel;
    }

    private void study(String word) throws Exception {
        int[] indexArray = new int[word.length()];
        for (int i = 0; i < word.length(); i++) {
            int index = getID(word.substring(i, i + 1));
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
