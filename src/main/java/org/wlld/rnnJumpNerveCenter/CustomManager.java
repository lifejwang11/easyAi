package org.wlld.rnnJumpNerveCenter;


import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.config.RZ;
import org.wlld.config.SentenceConfig;
import org.wlld.entity.CreatorModel;
import org.wlld.entity.SemanticsBack;
import org.wlld.entity.TalkBody;
import org.wlld.function.Tanh;
import org.wlld.i.OutBack;
import org.wlld.naturalLanguage.word.WordEmbedding;
import org.wlld.rnnJumpNerveEntity.MyWordFeature;
import org.wlld.rnnJumpNerveEntity.NerveCenter;
import org.wlld.rnnJumpNerveEntity.SensoryNerve;

import java.util.*;

public class CustomManager {//
    private final WordEmbedding wordEmbedding;
    private NerveJumpManager semanticsManager;//类别网络
    private NerveJumpManager customManager;//客服网络
    private final int vectorDimension;//特征纵向维度
    private final int maxFeatureLength;//特征最长长度
    private final double studyPoint;//词向量学习学习率
    private final int minLength;//最小长度
    private final int answerMaxLength;//回答最长长度
    private final SentenceConfig config;
    private int times;
    private final double param;

    public CustomManager(WordEmbedding wordEmbedding, SentenceConfig config) throws Exception {
        this.config = config;
        this.minLength = config.getMinLength();
        this.wordEmbedding = wordEmbedding;
        this.vectorDimension = config.getWordVectorDimension();
        this.maxFeatureLength = config.getMaxWordLength();
        this.studyPoint = config.getWeStudyPoint();
        this.answerMaxLength = config.getMaxAnswerLength();
        this.times = config.getTimes();
        this.param = config.getParam();
        if (this.times < 2) {
            this.times = 2;
        }
    }

    public void init() throws Exception {
        semanticsManager = new NerveJumpManager(vectorDimension, vectorDimension, vectorDimension, maxFeatureLength - 1, new Tanh(), false,
                studyPoint, RZ.L1, studyPoint * param);
        semanticsManager.setSemanticsLay(true);
        semanticsManager.initRnn(true, false, false, false, 1);
        List<SemanticsNerve> semanticsNerves = semanticsManager.getSemanticsNerves();
        for (SemanticsNerve semanticsNerve : semanticsNerves) {//语义层进行初始化
            semanticsNerve.init(config, wordEmbedding);
        }
        customManager = new NerveJumpManager(vectorDimension, vectorDimension, wordEmbedding.getWordList().size(), answerMaxLength + 1, new Tanh(), false,
                studyPoint, RZ.L1, studyPoint * param);
        customManager.setPowerTh(config.getSentenceTrustPowerTh());
        customManager.initRnn(true, false, true, false, 1);
        List<NerveCenter> nerveCenterList = customManager.getNerveCenterList();
        for (NerveCenter nerveCenter : nerveCenterList) {
            nerveCenter.setWordEmbedding(wordEmbedding);
        }
    }

    public String getAnswer(String question, long eventID) throws Exception {//生成返回语句
        Matrix semanticsMatrix = getSemanticsMatrix(question, eventID);
        List<Double> myFeatureList = MatrixOperation.rowVectorToList(semanticsMatrix);//语义特征
        Matrix matrix = new Matrix(2, vectorDimension);
        SemanticsBack semanticsBack = new SemanticsBack();
        createSentence(eventID, customManager.getSensoryNerves(), myFeatureList, matrix, semanticsBack, semanticsMatrix);
        return semanticsBack.getWord();
    }

    public void insertModel(CreatorModel model) throws Exception {
        semanticsManager.insertModelParameter(model.getSemanticsModel());
        customManager.insertModelParameter(model.getCustomModel());
    }

    public CreatorModel study(List<TalkBody> talkBodies, long eventID) throws Exception {
        Random random = new Random();
        CreatorModel model = new CreatorModel();
        for (int i = 0; i < maxFeatureLength; i++) {//第一阶段学习
            System.out.println("第一阶段完成次数：" + (i + 1) + "总共次数:" + maxFeatureLength);
            for (TalkBody talkBody : talkBodies) {
                String question = talkBody.getQuestion();
                String answer = talkBody.getAnswer();
                if (question.length() > maxFeatureLength) {
                    question = question.substring(0, maxFeatureLength);
                }
                MyWordFeature myWordFeature = wordEmbedding.getEmbedding(question, eventID);
                semanticsStudy(myWordFeature, answer, eventID, random);
            }
        }
        int tm = answerMaxLength * times;
        for (int i = 0; i < tm; i++) {//第二阶段学习
            System.out.println("第二阶段完成次数：" + (i + 1) + "总共次数:" + tm);
            for (TalkBody talkBody : talkBodies) {
                String question = talkBody.getQuestion();
                String answer = talkBody.getAnswer();
                if (question.length() > maxFeatureLength) {
                    question = question.substring(0, maxFeatureLength);
                }
                if (answer.length() > answerMaxLength) {
                    answer = answer.substring(0, answerMaxLength);
                }
                sentenceStudy(question, answer, eventID, random);
            }
        }
        model.setSemanticsModel(semanticsManager.getModelParameter());
        model.setCustomModel(customManager.getModelParameter());
        return model;
    }

    private Matrix getAllFeatrueMatrix(Matrix myMatrix, Matrix semanticsMatrix) throws Exception {
        Matrix matrix = new Matrix(myMatrix.getX() + 2, myMatrix.getY());
        for (int i = 2; i < matrix.getX(); i++) {
            for (int j = 0; j < matrix.getY(); j++) {
                double value = (semanticsMatrix.getNumber(0, j) + myMatrix.getNumber(i - 2, j)) * 0.5;
                matrix.setNub(i, j, value);
            }
        }
        return matrix;
    }

    private Matrix getSemanticsMatrix(String question, long eventID) throws Exception {
        MyWordFeature myWordFeature = wordEmbedding.getEmbedding(question, eventID);
        List<Double> featureList = myWordFeature.getFirstFeatureList();
        Matrix featureMatrix = myWordFeature.getFeatureMatrix();
        int[] storeys = new int[featureMatrix.getX()];
        for (int i = 0; i < storeys.length; i++) {
            storeys[i] = i;
        }
        SemanticsBack semanticsBack = new SemanticsBack();
        studySemanticsNerve(eventID, semanticsManager.getSensoryNerves(), featureList, featureMatrix, false, semanticsBack, storeys);
        return semanticsBack.getMatrix();//语义
    }

    private void sentenceStudy(String question, String answer, long eventID, Random random) throws Exception {// TODO
        Matrix semanticsMatrix = getSemanticsMatrix(question, eventID);
        List<Double> myFeatureList = MatrixOperation.rowVectorToList(semanticsMatrix);//语义特征
        Matrix myAnswerFeature = wordEmbedding.getEmbedding(answer, eventID).getFeatureMatrix();
        Matrix matrix = getAllFeatrueMatrix(myAnswerFeature, semanticsMatrix);
        int[] myStoreys;
        if (answer.length() <= minLength) {
            myStoreys = new int[answer.length() + 1];
            for (int i = 1; i <= answer.length(); i++) {
                myStoreys[i] = i;
            }
        } else {
            List<Integer> list = new ArrayList<>();
            for (int i = 1; i <= answer.length(); i++) {
                int index = random.nextInt(2);
                if (index == 1) {
                    list.add(i);
                }
            }
            if (list.isEmpty()) {
                list.add(1);
            }
            myStoreys = new int[list.size() + 1];
            for (int i = 0; i < list.size(); i++) {
                myStoreys[i + 1] = list.get(i);
            }
        }
        Map<Integer, Double> E = new HashMap<>();
        for (int i = 2; i <= myStoreys.length; i++) {
            E.clear();
            int[] storey = Arrays.copyOfRange(myStoreys, 0, i);
            int index = myStoreys[i - 1] - 1;
            String finishWord = answer.substring(index, index + 1);
            int finishIndex = wordEmbedding.getID(finishWord) + 1;
            E.put(finishIndex, 1D);
            studyNerve(eventID, customManager.getSensoryNerves(), myFeatureList, matrix, E, storey);
        }
    }

    private void studyNerve(long eventId, List<SensoryNerve> sensoryNerves, List<Double> featureList, Matrix rnnMatrix, Map<Integer, Double> E, int[] storeys) throws Exception {
        if (sensoryNerves.size() == featureList.size()) {
            for (int i = 0; i < sensoryNerves.size(); i++) {
                sensoryNerves.get(i).postMessage(eventId, featureList.get(i), true, E, null, false, rnnMatrix, storeys);
            }
        } else {
            throw new Exception("1size not equals,feature size:" + featureList.size() + "," +
                    "sensorySize:" + sensoryNerves.size());
        }
    }

    private void createSentence(long eventId, List<SensoryNerve> sensoryNerves, List<Double> featureList, Matrix rnnMatrix, OutBack convBack, Matrix semanticsMatrix) throws Exception {
        if (sensoryNerves.size() == featureList.size()) {
            for (int i = 0; i < sensoryNerves.size(); i++) {
                sensoryNerves.get(i).postPowerMessage(eventId, featureList.get(i), rnnMatrix, convBack, semanticsMatrix);
            }
        } else {
            throw new Exception("1size not equals,feature size:" + featureList.size() + "," +
                    "sensorySize:" + sensoryNerves.size());
        }
    }

    private void semanticsStudy(MyWordFeature myWordFeature, String answer, long eventID, Random random) throws Exception {
        Matrix featureMatrix = myWordFeature.getFeatureMatrix();
        List<Double> firstFeatureList = myWordFeature.getFirstFeatureList();
        int len = featureMatrix.getX();//文字长度
        if (len > 1) {
            int[] storeys;
            if (len < minLength) {
                storeys = new int[len];
                for (int i = 0; i < len; i++) {
                    storeys[i] = i;
                }
            } else {
                List<Integer> list = new ArrayList<>();
                for (int i = 1; i < len; i++) {
                    list.add(i);
                }
                int myLen = (int) (minLength + Math.random() * (len - minLength + 1));
                storeys = new int[myLen];
                for (int i = 1; i < myLen; i++) {
                    int index = random.nextInt(list.size());
                    storeys[i] = list.get(index);
                    list.remove(index);
                }
                Arrays.sort(storeys);
            }
            int finishIndex = storeys[storeys.length - 1] - 1;//结束层的序号
            SemanticsNerve semanticsNerve = semanticsManager.getSemanticsNerves().get(finishIndex);
            semanticsNerve.setMatrixE(answer, eventID);
            studySemanticsNerve(1, semanticsManager.getSensoryNerves(), firstFeatureList, featureMatrix, true, null, storeys);
        }
    }

    private void studySemanticsNerve(long eventId, List<SensoryNerve> sensoryNerves, List<Double> featureList, Matrix rnnMatrix, boolean isStudy, OutBack convBack, int[] storeys) throws Exception {
        if (sensoryNerves.size() == featureList.size()) {
            for (int i = 0; i < sensoryNerves.size(); i++) {
                sensoryNerves.get(i).postMessage(eventId, featureList.get(i), isStudy, null, convBack, false, rnnMatrix, storeys);
            }
        } else {
            throw new Exception("1size not equals,feature size:" + featureList.size() + "," +
                    "sensorySize:" + sensoryNerves.size());
        }
    }
}
