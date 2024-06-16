package org.wlld.rnnJumpNerveCenter;


import org.wlld.MatrixTools.Matrix;
import org.wlld.config.RZ;
import org.wlld.config.SentenceConfig;
import org.wlld.entity.CreatorModel;
import org.wlld.entity.SemanticsBack;
import org.wlld.entity.TalkBody;
import org.wlld.function.ReLu;
import org.wlld.function.Tanh;
import org.wlld.naturalLanguage.word.WordEmbedding;
import org.wlld.rnnJumpNerveEntity.MyWordFeature;
import org.wlld.rnnJumpNerveEntity.NerveCenter;
import org.wlld.rnnJumpNerveEntity.SensoryNerve;

import java.util.*;

public class CustomManager {//
    private final WordEmbedding wordEmbedding;
    private NerveJumpManager semanticsManager;//网络
    private final int vectorDimension;//特征纵向维度
    private final int maxFeatureLength;//特征最长长度
    private final double studyPoint;//词向量学习学习率
    private final int minLength;//最小长度
    private final int answerMaxLength;//回答最长长度
    private int times;
    private final double param;
    private final double powerTh;

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
        if (this.times < 1) {
            this.times = 1;
        }
    }

    public void init() throws Exception {
        semanticsManager = new NerveJumpManager(vectorDimension, vectorDimension, wordEmbedding.getWordList().size(), maxFeatureLength + answerMaxLength - 1, new Tanh(), false,
                studyPoint, RZ.L1, studyPoint * param);
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
        MyWordFeature myWordFeature = wordEmbedding.getEmbedding(question, eventID);
        List<Double> featureList = myWordFeature.getFirstFeatureList();
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
                MyWordFeature myWordFeature = wordEmbedding.getEmbedding(word, 1);
                semanticsStudy(myWordFeature, question, answer, 1, random);
                double point = (double) index / (double) (wordSize) * 100;
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
            int myLen = (int) (minLength + Math.random() * (len - minLength + 1));
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
        List<Double> firstFeatureList = myWordFeature.getFirstFeatureList();
        System.out.println("训练question:" + question + ",answer:" + answer);
        if (question.length() > 1 && !answer.isEmpty()) {
            int[] questionStoreys = getStoreys(question.length(), random, 0);
            int[] answerStoreys = getStoreys(answer.length() + 1, random, maxFeatureLength);
            Map<Integer, Double> E = new HashMap<>();
            for (int i = 0; i < answerStoreys.length - 1; i++) {
                //System.out.println("=====================================");
                E.clear();
                int index = answerStoreys[i + 1] - maxFeatureLength;
                questionStoreys = pushArray(questionStoreys, answerStoreys[i]);
                String myAnswer = answer.substring(index - 1, index);
                int wordID = wordEmbedding.getID(myAnswer) + 1;
                E.put(wordID, 1D);
                studySemanticsNerve(eventID, firstFeatureList, true, E, null, featureMatrix, questionStoreys, question.length());
            }
        }
    }

    private void studySemanticsNerve(long eventId, List<Double> featureList, boolean isStudy, Map<Integer, Double> E, SemanticsBack semanticsBack,
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
