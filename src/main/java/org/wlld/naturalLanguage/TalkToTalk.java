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
import java.util.List;

public class TalkToTalk {
    private final WordEmbedding wordEmbedding;
    private final TfConfig tfConfig;
    private final int maxLength;
    private final int times;
    private TransFormerManager transFormerManager;

    public TalkToTalk(WordEmbedding wordEmbedding, TfConfig tfConfig) throws Exception {
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

    private Matrix insertZero(Matrix feature, Matrix lastFeature) throws Exception {
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

    private Matrix getMyAvg(Matrix feature) throws Exception {
        Matrix myFeature = new Matrix(1, feature.getY());
        for (int j = 0; j < feature.getY(); j++) {
            Matrix col = feature.getColumn(j);
            double value = col.getAVG();
            myFeature.setNub(0, j, value);
        }
        return myFeature;
    }

    public String getAnswer(String question, long eventID) throws Exception {
        SensoryNerve sensoryNerve = transFormerManager.getSensoryNerve();
        if (question.length() > maxLength) {
            question = question.substring(0, maxLength);
        }
        Matrix qMatrix = wordEmbedding.getEmbedding(question, eventID).getFeatureMatrix();
        Matrix begin = getMyAvg(qMatrix);
        WordBack wordBack = new WordBack();
        int id;
        StringBuilder answer = new StringBuilder();
        int index = 0;
        do {
            index++;
            sensoryNerve.postMessage(eventID, qMatrix, begin, false, null, wordBack);
            id = wordBack.getId();
            if (id > 1) {
                String word = wordEmbedding.getWord(id - 2);
                Matrix next = wordEmbedding.getEmbedding(word, eventID).getFeatureMatrix();
                begin = MatrixOperation.pushVector(begin, next, true);
                answer.append(word);
            }
        } while (id > 1 && index < maxLength);
        return answer.toString();
    }

    public void insertModel(TransFormerModel transFormerModel) throws Exception {
        transFormerManager.insertModel(transFormerModel);
    }

    //我是好人  begin 我 是 好 人    我 是 好  人 end
    public TransFormerModel study(List<TalkBody> talkBodies) throws Exception {
        SensoryNerve sensoryNerve = transFormerManager.getSensoryNerve();
        int size = talkBodies.size();
        for (int k = 0; k < times; k++) {
            int index = 0;
            for (TalkBody talkBody : talkBodies) {
                index++;
                String question = talkBody.getQuestion();
                String answer = talkBody.getAnswer();
                if (question.length() > maxLength) {
                    question = question.substring(0, maxLength);
                }
                if (answer.length() > maxLength) {
                    answer = answer.substring(0, maxLength);
                }
                System.out.println("问题:"+question+", 回答:" + answer + ",训练语句下标:" + index + ",总数量:" + size + ",当前次数：" + k + ",总次数:" + times);
                Matrix qMatrix = wordEmbedding.getEmbedding(question, 1).getFeatureMatrix();
                Matrix aMatrix = wordEmbedding.getEmbedding(answer, 2).getFeatureMatrix();
                Matrix myAnswer = insertZero(aMatrix, getMyAvg(qMatrix));//第一行补0
                List<Integer> answerList = new ArrayList<>();
                for (int i = 0; i < answer.length(); i++) {
                    String word = answer.substring(i, i + 1);
                    int wordID = wordEmbedding.getID(word) + 2;
                    answerList.add(wordID);
                }
                answerList.add(1);
                sensoryNerve.postMessage(1, qMatrix, myAnswer, true, answerList, null);
            }
        }
        return transFormerManager.getModel();
    }
}
