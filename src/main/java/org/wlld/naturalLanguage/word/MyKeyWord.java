package org.wlld.naturalLanguage.word;

import org.wlld.matrixTools.Matrix;
import org.wlld.matrixTools.MatrixOperation;
import org.wlld.config.RZ;
import org.wlld.config.SentenceConfig;
import org.wlld.entity.KeyWordForSentence;
import org.wlld.entity.WordBack;
import org.wlld.function.Tanh;
import org.wlld.i.OutBack;
import org.wlld.rnnNerveCenter.ModelParameter;
import org.wlld.rnnNerveCenter.NerveManager;
import org.wlld.rnnNerveEntity.SensoryNerve;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyKeyWord {
    private final WordEmbedding wordEmbedding;
    private final NerveManager typeNerveManager;
    private final int nerveLength;
    private final int maxWordLength;//最高句子长度

    public MyKeyWord(SentenceConfig config, WordEmbedding wordEmbedding) throws Exception {
        int vectorDimension = config.getWordVectorDimension();
        int nerveLength = config.getKeyWordNerveDeep();
        double studyPoint = config.getWeStudyPoint();
        boolean isShowLog = config.isShowLog();
        this.maxWordLength = config.getMaxWordLength();
        typeNerveManager = new NerveManager(vectorDimension, vectorDimension, 2, nerveLength - 1, new Tanh(), false,
                studyPoint, RZ.L1, studyPoint * 0.2);
        typeNerveManager.initRnn(true, isShowLog);
        this.wordEmbedding = wordEmbedding;
        this.nerveLength = nerveLength;
    }

    public ModelParameter study(List<KeyWordForSentence> keyWordForSentenceList) throws Exception {//进行学习
        for (KeyWordForSentence keyWordForSentence : keyWordForSentenceList) {
            String word = keyWordForSentence.getSentence();
            String keyWord = keyWordForSentence.getKeyWord();
            myStudy(true, 1, word, keyWord, new HashMap<>());
        }
        return typeNerveManager.getModelParameter();
    }

    public void insertModel(ModelParameter modelParameter) throws Exception {
        typeNerveManager.insertModelParameter(modelParameter);
    }

    public boolean isKeyWord(String word, long eventId) throws Exception {//是否存在关键词
        boolean isKeyWord = false;
        int key = myStudy(false, eventId, word, null, null);
        if (key == 1) {
            isKeyWord = true;
        }
        return isKeyWord;
    }

    private int myStudy(boolean isStudy, long eventId, String word, String keyWord, Map<Integer, Double> E) throws Exception {
        int key = 0;
        if (word.length() > 1) {
            if (word.length() > maxWordLength) {
                word = word.substring(0, maxWordLength);
            }
            WordBack wordBack = null;
            int startIndex = -1;
            int endIndex = -1;
            if (!isStudy) {
                wordBack = new WordBack();
            } else if (keyWord != null && !keyWord.isEmpty()) {
                startIndex = word.indexOf(keyWord);
                endIndex = startIndex + keyWord.length() - 1;
            }
            int times = 1;
            if (nerveLength < word.length()) {
                times = word.length() - nerveLength + 1;
            }
            Matrix allFeature = wordEmbedding.getEmbedding(word, eventId, false).getFeatureMatrix();
            for (int i = 0; i < times; i++) {
                if (isStudy) {
                    E.clear();
                    if (i > endIndex || (i + nerveLength - 1) < startIndex) {//不相交
                        E.put(2, 1D);
                    } else {//相交
                        E.put(1, 1D);
                    }
                } else {
                    wordBack.clear();
                }
                Matrix feature;
                if (times == 1) {
                    feature = allFeature;//0,0,5,21-6 21
                } else {
                    feature = allFeature.getSonOfMatrix(i, 0, nerveLength, allFeature.getY());
                }
                List<Double> firstFeature = MatrixOperation.rowVectorToList(feature.getRow(0));//首行特征
                studyNerve(eventId, typeNerveManager.getSensoryNerves(), firstFeature, feature, E, isStudy, wordBack);
                if (!isStudy) {//需要返回结果
                    key = wordBack.getId();
                    if (key == 1) {
                        break;
                    }
                }
            }
        }
        return key;
    }

    private void studyNerve(long eventId, List<SensoryNerve> sensoryNerves, List<Double> featureList, Matrix rnnMatrix, Map<Integer, Double> E, boolean isStudy, OutBack convBack) throws Exception {
        if (sensoryNerves.size() == featureList.size()) {
            for (int i = 0; i < sensoryNerves.size(); i++) {
                sensoryNerves.get(i).postMessage(eventId, featureList.get(i), isStudy, E, convBack, false, rnnMatrix);
            }
        } else {
            throw new Exception("size not equals,feature size:" + featureList.size() + "," +
                    "sensorySize:" + sensoryNerves.size());
        }
    }
}
