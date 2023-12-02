package org.wlld.rnnJumpNerveCenter;


import org.wlld.MatrixTools.Matrix;
import org.wlld.config.RZ;
import org.wlld.config.SentenceConfig;
import org.wlld.entity.TypeMapping;
import org.wlld.entity.WordBack;
import org.wlld.function.Tanh;
import org.wlld.i.OutBack;
import org.wlld.naturalLanguage.word.WordEmbedding;
import org.wlld.rnnJumpNerveEntity.MyWordFeature;
import org.wlld.rnnJumpNerveEntity.SensoryNerve;

import java.util.*;

public class RRNerveManager {
    private final WordEmbedding wordEmbedding;
    private final Map<Integer, Integer> mapping = new HashMap<>();//主键是真实id,值是映射识别用id
    private NerveJumpManager typeNerveManager;//类别网络
    private final int typeNub;//分类数量
    private final int vectorDimension;//特征纵向维度
    private final int maxFeatureLength;//特征最长长度
    private final double studyPoint;//词向量学习学习率
    private final boolean showLog;//是否输出学习数据
    private final int minLength;//最小长度
    private final int dateAug;//数据增广

    public RRNerveManager(SentenceConfig config, WordEmbedding wordEmbedding, boolean initPower) throws Exception {
        if (config.getTypeNub() > 0) {
            this.minLength = config.getMinLength();
            this.wordEmbedding = wordEmbedding;
            this.dateAug = config.getDateAug();
            this.typeNub = config.getTypeNub();
            this.vectorDimension = config.getWordVectorDimension();
            this.maxFeatureLength = config.getMaxWordLength();
            this.studyPoint = config.getWeStudyPoint();
            this.showLog = config.isShowLog();
            initNerveManager(initPower, config.getTrustPowerTh());
        } else {
            throw new Exception("分类种类数量必须大于0");
        }
    }

    private void initNerveManager(boolean initPower, double powerTh) throws Exception {
        typeNerveManager = new NerveJumpManager(vectorDimension, vectorDimension, typeNub, maxFeatureLength - 1, new Tanh(), false,
                studyPoint, RZ.L1, studyPoint * 0.2);
        typeNerveManager.initRnn(initPower, showLog, true);
    }

    private int getMappingType(int key) {//通过自增主键查找原映射
        int id = 0;
        for (Map.Entry<Integer, Integer> entry : mapping.entrySet()) {
            if (entry.getValue() == key) {
                id = entry.getKey();
                break;
            }
        }
        return id;
    }

    private int balance(Map<Integer, List<String>> model) {//强行均衡
        int maxNumber = 300;
        int index = 1;
        for (Map.Entry<Integer, List<String>> entry : model.entrySet()) {//查找最大数量
            mapping.put(entry.getKey(), index);
            if (entry.getValue().size() > maxNumber) {
                maxNumber = entry.getValue().size();
            }
            index++;
        }
        for (Map.Entry<Integer, List<String>> entry : model.entrySet()) {
            int size = entry.getValue().size();
            if (maxNumber > size) {
                int times = maxNumber / size - 1;//循环几次
                int sub = maxNumber % size;//余数
                List<String> list = entry.getValue();
                List<String> otherList = new ArrayList<>(list);
                for (int i = 0; i < times; i++) {
                    list.addAll(otherList);
                }
                list.addAll(otherList.subList(0, sub));
            }
        }
        return maxNumber;
    }

    private void studyNerve(long eventId, List<SensoryNerve> sensoryNerves, List<Double> featureList, Matrix rnnMatrix, Map<Integer, Double> E, boolean isStudy, OutBack convBack, int[] storeys) throws Exception {
        if (sensoryNerves.size() == featureList.size()) {
            for (int i = 0; i < sensoryNerves.size(); i++) {
                sensoryNerves.get(i).postMessage(eventId, featureList.get(i), isStudy, E, convBack, false, rnnMatrix, storeys);
            }
        } else {
            throw new Exception("1size not equals,feature size:" + featureList.size() + "," +
                    "sensorySize:" + sensoryNerves.size());
        }
    }

    public int getType(String sentence, long eventID) throws Exception {//进行理解
        if (sentence.length() > maxFeatureLength) {
            sentence = sentence.substring(0, maxFeatureLength);
        }
        MyWordFeature myWordFeature = wordEmbedding.getEmbedding(sentence, eventID);
        List<Double> featureList = myWordFeature.getFirstFeatureList();
        Matrix featureMatrix = myWordFeature.getFeatureMatrix();
        int[] storeys = new int[featureMatrix.getX()];
        for (int i = 0; i < storeys.length; i++) {
            storeys[i] = i;
        }
        WordBack wordBack = new WordBack();
        studyNerve(eventID, typeNerveManager.getSensoryNerves(), featureList, featureMatrix, null, false, wordBack, storeys);
        return getMappingType(wordBack.getId());
    }

    public void insertModel(RandomModel randomModel) throws Exception {
        typeNerveManager.insertModelParameter(randomModel.getTypeModelParameter());
        List<TypeMapping> typeMappings = randomModel.getTypeMappings();
        mapping.clear();
        for (TypeMapping typeMapping : typeMappings) {
            mapping.put(typeMapping.getType(), typeMapping.getMapping());
        }
    }

    public RandomModel getModel() throws Exception {
        RandomModel randomModel = new RandomModel();
        randomModel.setTypeModelParameter(typeNerveManager.getModelParameter());
        List<TypeMapping> typeMappings = new ArrayList<>();
        randomModel.setTypeMappings(typeMappings);
        for (Map.Entry<Integer, Integer> entry : mapping.entrySet()) {
            TypeMapping typeMapping = new TypeMapping();
            typeMapping.setType(entry.getKey());
            typeMapping.setMapping(entry.getValue());
            typeMappings.add(typeMapping);
        }
        return randomModel;
    }

    public RandomModel studyType(Map<Integer, List<String>> model) throws Exception {
        int maxNumber = balance(model);//平衡样本
        for (int i = 0; i < dateAug; i++) {//第一阶段学习
            System.out.println("1第：" + (i + 1) + "次。共:" + dateAug + "次");
            myStudy(maxNumber, model, i + 1);
        }
        return getModel();
    }

    private void myStudy(int maxNumber, Map<Integer, List<String>> model, int time) throws Exception {
        int index = 0;
        Map<Integer, Double> E = new HashMap<>();
        do {
            for (Map.Entry<Integer, List<String>> entry : model.entrySet()) {
                System.out.println("index======" + index + "," + time + "次");
                E.clear();
                List<String> sentence = entry.getValue();
                int key = mapping.get(entry.getKey());
                E.put(key, 1D);
                String word = sentence.get(index);
                if (word.length() > maxFeatureLength) {
                    word = word.substring(0, maxFeatureLength);
                }
                randomTypeStudy(wordEmbedding.getEmbedding(word, 1), E);
            }
            index++;
        } while (index < maxNumber);
    }

    private void randomTypeStudy(MyWordFeature myWordFeature, Map<Integer, Double> E) throws Exception {
        Matrix featureMatrix = myWordFeature.getFeatureMatrix();
        List<Double> firstFeatureList = myWordFeature.getFirstFeatureList();
        int len = featureMatrix.getX();//文字长度
        Random random = new Random();
        if (len > 1) {//长度大于1才可以进行训练
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
            studyNerve(1, typeNerveManager.getSensoryNerves(), firstFeatureList, featureMatrix
                    , E, true, null, storeys);
        }
    }

}
