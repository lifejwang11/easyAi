package org.wlld.naturalLanguage;

import org.wlld.config.TfConfig;
import org.wlld.entity.TalkBody;
import org.wlld.matrixTools.Matrix;
import org.wlld.matrixTools.MatrixOperation;
import org.wlld.naturalLanguage.word.WordBack;
import org.wlld.naturalLanguage.word.WordEmbedding;
import org.wlld.transFormer.TransFormerManager;
import org.wlld.transFormer.model.TransFormerModel;
import org.wlld.transFormer.nerve.SensoryNerve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TalkToTalk extends MatrixOperation {
    private final WordEmbedding wordEmbedding;
    private final TfConfig tfConfig;
    private final int maxLength;
    private final int times;
    private final String splitWord;
    private TransFormerManager transFormerManager;
    private final boolean splitAnswer;//回答是否带隔断符

    public TalkToTalk(WordEmbedding wordEmbedding, TfConfig tfConfig) throws Exception {
        this.splitWord = tfConfig.getSplitWord();
        splitAnswer = splitWord != null && !splitWord.isEmpty();
        this.wordEmbedding = wordEmbedding;
        this.tfConfig = tfConfig;
        maxLength = tfConfig.getMaxLength();
        this.times = tfConfig.getTimes();
        if (times <= 0) {
            throw new Exception("参数times必须大于0");
        }
    }

    public void init() throws Exception {
        int wordVectorDimension = wordEmbedding.getWordVectorDimension();
        tfConfig.setFeatureDimension(wordVectorDimension);
        tfConfig.setTypeNumber(wordEmbedding.getWordList().size() + 2);
        transFormerManager = new TransFormerManager(tfConfig);
    }

    private Matrix insertStart(Matrix feature, Matrix lastFeature) throws Exception {
        Matrix matrix = new Matrix(feature.getX() + 1, feature.getY());
        int x = matrix.getX();
        int y = matrix.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if (i > 0) {
                    matrix.setNub(i, j, feature.getNumber(i - 1, j));
                } else {
                    matrix.setNub(i, j, lastFeature.getNumber(0, j));
                }
            }
        }
        return matrix;
    }


    public String getAnswer(String question, long eventID) throws Exception {
        SensoryNerve sensoryNerve = transFormerManager.getSensoryNerve();
        Matrix qMatrix = wordEmbedding.getEmbedding(question, eventID, false).getFeatureMatrix();
        WordBack wordBack = new WordBack();
        int id;
        StringBuilder answer = new StringBuilder();
        int index = 0;
        List<String> wordList = new ArrayList<>();
        do {
            Matrix qcMatrix = qMatrix.copy();
            Matrix allFeatures = transFormerManager.getStartMatrix(qMatrix);
            if (!wordList.isEmpty()) {
                for (String word : wordList) {
                    Matrix next = wordEmbedding.getEmbedding(word, eventID, true).getFeatureMatrix();
                    allFeatures = pushVector(allFeatures, next, true);
                }
            }
            index++;
            sensoryNerve.postMessage(eventID, qcMatrix, allFeatures, false, null, wordBack);
            id = wordBack.getId();
            if (id > 1) {
                String word = wordEmbedding.getWord(id - 2);
                wordList.add(word);
                if (splitAnswer) {
                    word = word + " ";
                }
                answer.append(word);
            }
        } while (id > 1 && index < maxLength);
        return answer.toString();
    }

    public void insertModel(TransFormerModel transFormerModel) throws Exception {
        transFormerManager.insertModel(transFormerModel);
    }

    private AnswerE getSentenceMatrix(String sentence) throws Exception {//返回隔断符后的字符数组
        Matrix allFeature = null;
        AnswerE answerE = new AnswerE();
        List<Integer> answerList = new ArrayList<>();
        if (splitAnswer) {
            String[] words = sentence.split(splitWord);
            if (words.length > maxLength) {
                words = Arrays.copyOfRange(words, 0, maxLength);
            }
            for (String s : words) {
                int wordID = wordEmbedding.getID(s) + 2;
                answerList.add(wordID);
            }
            for (String word : words) {
                Matrix feature = wordEmbedding.getEmbedding(word, 3, true).getFeatureMatrix();
                if (allFeature == null) {
                    allFeature = feature;
                } else {
                    allFeature = pushVector(allFeature, feature, true);
                }
            }
        } else {
            if (sentence.length() > maxLength) {
                sentence = sentence.substring(0, maxLength);
            }
            for (int i = 0; i < sentence.length(); i++) {
                String word = sentence.substring(i, i + 1);
                int wordID = wordEmbedding.getID(word) + 2;
                answerList.add(wordID);
            }
            allFeature = wordEmbedding.getEmbedding(sentence, 3, false).getFeatureMatrix();
        }
        answerList.add(1);//期望末尾补终止符
        answerE.answerMatrix = allFeature;
        answerE.answerList = answerList;
        return answerE;
    }

    public TransFormerModel study(List<TalkBody> talkBodies) throws Exception {
        SensoryNerve sensoryNerve = transFormerManager.getSensoryNerve();
        int size = talkBodies.size();
        for (int k = 0; k < times; k++) {
            int index = 0;
            for (TalkBody talkBody : talkBodies) {
                index++;
                String question = talkBody.getQuestion();
                String answer = talkBody.getAnswer();
                System.out.println("问题:" + question + ", 回答:" + answer + ",训练语句下标:" + index + ",总数量:" + size + ",当前次数：" + k + ",总次数:" + times);
                Matrix qMatrix = wordEmbedding.getEmbedding(question, 1, false).getFeatureMatrix();
                AnswerE answerE = getSentenceMatrix(answer);
                Matrix myAnswer = insertStart(answerE.answerMatrix, transFormerManager.getStartMatrix(qMatrix));//第一行补开始符
                sensoryNerve.postMessage(1, qMatrix, myAnswer, true, answerE.answerList, null);
            }
        }
        return transFormerManager.getModel();
    }

    static class AnswerE {
        List<Integer> answerList;
        Matrix answerMatrix;
    }
}
