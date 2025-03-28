package org.dromara.easyai.rnnJumpNerveCenter;


import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.config.SentenceConfig;
import org.dromara.easyai.entity.CreatorModel;
import org.dromara.easyai.entity.SemanticsBack;
import org.dromara.easyai.entity.TalkBody;
import org.dromara.easyai.function.Tanh;
import org.dromara.easyai.naturalLanguage.word.WordEmbedding;
import org.dromara.easyai.rnnJumpNerveEntity.MyWordFeature;
import org.dromara.easyai.rnnJumpNerveEntity.NerveCenter;
import org.dromara.easyai.rnnJumpNerveEntity.SensoryNerve;

import java.util.*;

@Deprecated()
public class CustomManager {//
    private final WordEmbedding wordEmbedding;
    private NerveJumpManager semanticsManager;//网络
    private final int vectorDimension;//特征纵向维度
    private final int maxFeatureLength;//特征最长长度
    private final float studyPoint;//词向量学习学习率
    private final int minLength;//最小长度
    private final int answerMaxLength;//回答最长长度
    private int times;
    private final float param;
    private final float powerTh;
    private final int rzModel;

    public CustomManager(WordEmbedding wordEmbedding, SentenceConfig config) {
        this.minLength = config.getMinLength();
        this.wordEmbedding = wordEmbedding;
        this.vectorDimension = config.getQaWordVectorDimension();
        this.maxFeatureLength = config.getMaxWordLength();
        this.studyPoint = config.getWeStudyPoint();
        this.answerMaxLength = config.getMaxAnswerLength();
        this.powerTh = config.getSentenceTrustPowerTh();
        this.times = config.getTimes();
        this.param = config.getParam();
        this.rzModel = config.getRzModel();
        if (this.times < 1) {
            this.times = 1;
        }
    }

    public void init() throws Exception {
        semanticsManager = new NerveJumpManager(vectorDimension, vectorDimension, wordEmbedding.getWordList().size(), maxFeatureLength + answerMaxLength - 1, new Tanh(), false,
                studyPoint, rzModel, param);
        semanticsManager.setPowerTh(powerTh);
        semanticsManager.initRnn(true, true, true, true, maxFeatureLength);
        List<NerveCenter> nerveCenterList = semanticsManager.getNerveCenterList();
        for (NerveCenter nerveCenter : nerveCenterList) {
            nerveCenter.setWordEmbedding(wordEmbedding);
        }
    }

    public void insertModel(CreatorModel creatorModel) throws Exception {
        semanticsManager.insertModelParameter(creatorModel.getSemanticsModel());
    }

    public String getAnswer(String question, long eventID) throws Exception {
        SemanticsBack semanticsBack = new SemanticsBack();
        if (question.length() > maxFeatureLength) {
            question = question.substring(0, maxFeatureLength);
        }
        MyWordFeature myWordFeature = wordEmbedding.getEmbedding(question, eventID, false);
        List<Float> featureList = myWordFeature.getFirstFeatureList();
        Matrix featureMatrix = myWordFeature.getFeatureMatrix();
        featureMatrix = insertZero(featureMatrix, featureMatrix.getX());
        int[] storeys = getStoreys2(question.length(), maxFeatureLength);
        studySemanticsNerve(eventID, featureList, false, null, semanticsBack, featureMatrix, storeys, question.length());
        return semanticsBack.getWord();
    }

    public CreatorModel study(List<TalkBody> talkBodies) throws Exception {
        Random random = new Random();
        CreatorModel creatorModel = new CreatorModel();
        int maxTime = maxFeatureLength * answerMaxLength * times;
        int wordSize = talkBodies.size() * maxTime;
        int index = 0;
        for (int i = 0; i < maxTime; i++) {//第一阶段学习
            System.out.println("生成模型学习完成次数：" + (i + 1) + "总共次数:" + maxTime);
            for (TalkBody talkBody : talkBodies) {
                index++;
                String question = talkBody.getQuestion();
                String answer = talkBody.getAnswer();
                if (question.length() > maxFeatureLength) {
                    question = question.substring(0, maxFeatureLength);
                }
                if (answer.length() > answerMaxLength) {
                    answer = answer.substring(0, answerMaxLength);
                }
                String word = question + answer;
                MyWordFeature myWordFeature = wordEmbedding.getEmbedding(word, 1, false);
                semanticsStudy(myWordFeature, question, answer, 1, random);
                float point = (float) index / (float) (wordSize) * 100;
                String result = String.format("%.6f", point);
                System.out.println("训练进度：" + result + "%");
            }
        }
        creatorModel.setSemanticsModel(semanticsManager.getModelParameter());
        return creatorModel;
    }

    private int[] getStoreys2(int len, int finishIndex) {
        int[] storeys = new int[len + 1];
        for (int i = 0; i < storeys.length; i++) {
            if (i < storeys.length - 1) {
                storeys[i] = i;
            } else {
                storeys[i] = finishIndex;
            }
        }
        return storeys;
    }

    private int[] getStoreys(int len, Random random, int startIndex) {
        int[] storeys;
        if (len < minLength) {
            storeys = new int[len];
            for (int i = 0; i < len; i++) {
                storeys[i] = i + startIndex;
            }
        } else {
            List<Integer> list = new ArrayList<>();
            for (int i = 1; i < len; i++) {
                list.add(i);
            }
            int myLen = (int) (minLength + (float) Math.random() * (len - minLength + 1));
            storeys = new int[myLen];
            if (startIndex > 0) {
                storeys[0] = startIndex;
            }
            for (int i = 1; i < myLen; i++) {
                int index = random.nextInt(list.size());
                storeys[i] = list.get(index) + startIndex;
                list.remove(index);
            }
            Arrays.sort(storeys);
        }
        return storeys;
    }

    private int[] pushArray(int[] arr, int value) {
        int[] myArr = new int[arr.length + 1];
        for (int i = 0; i < myArr.length; i++) {
            if (i < myArr.length - 1) {
                myArr[i] = arr[i];
            } else {
                myArr[i] = value;
            }
        }
        return myArr;
    }


    private Matrix insertZero(Matrix feature, int index) throws Exception {
        Matrix matrix = new Matrix(feature.getX() + 1, feature.getY());
        int x = matrix.getX();
        int y = matrix.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if (i < index) {
                    matrix.setNub(i, j, feature.getNumber(i, j));
                } else if (i > index) {
                    matrix.setNub(i, j, feature.getNumber(i - 1, j));
                }
            }
        }
        return matrix;
    }

    private void semanticsStudy(MyWordFeature myWordFeature, String question, String answer, long eventID, Random random) throws Exception {
        Matrix featureMatrix = insertZero(myWordFeature.getFeatureMatrix(), question.length());
        List<Float> firstFeatureList = myWordFeature.getFirstFeatureList();
        System.out.println("训练question:" + question + ",answer:" + answer);
        if (question.length() > 1 && !answer.isEmpty()) {
            int[] questionStoreys = getStoreys(question.length(), random, 0);
            int[] answerStoreys = getStoreys(answer.length() + 1, random, maxFeatureLength);
            Map<Integer, Float> E = new HashMap<>();
            for (int i = 0; i < answerStoreys.length - 1; i++) {
                //System.out.println("=====================================");
                E.clear();
                int index = answerStoreys[i + 1] - maxFeatureLength;
                questionStoreys = pushArray(questionStoreys, answerStoreys[i]);
                String myAnswer = answer.substring(index - 1, index);
                int wordID = wordEmbedding.getID(myAnswer) + 1;
                E.put(wordID, 1f);
                studySemanticsNerve(eventID, firstFeatureList, true, E, null, featureMatrix, questionStoreys, question.length());
            }
        }
    }

    private void studySemanticsNerve(long eventId, List<Float> featureList, boolean isStudy, Map<Integer, Float> E, SemanticsBack semanticsBack,
                                     Matrix rnnMatrix, int[] storeys, int questionLength) throws Exception {
        List<SensoryNerve> sensoryNerves = semanticsManager.getSensoryNerves();
        if (sensoryNerves.size() == featureList.size()) {
            for (int i = 0; i < sensoryNerves.size(); i++) {
                sensoryNerves.get(i).postMessage(eventId, featureList.get(i), isStudy, E, semanticsBack, rnnMatrix, storeys, questionLength);
            }
        } else {
            throw new Exception("1size not equals,feature size:" + featureList.size() + "," +
                    "sensorySize:" + sensoryNerves.size());
        }
    }
}
