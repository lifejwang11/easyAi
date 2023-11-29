package org.wlld.rnnNerveCenter;

import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.config.RZ;
import org.wlld.config.SentenceConfig;
import org.wlld.entity.TypeMapping;
import org.wlld.entity.WordBack;
import org.wlld.function.Tanh;
import org.wlld.i.OutBack;
import org.wlld.naturalLanguage.word.Trust;
import org.wlld.naturalLanguage.word.TypeBody;
import org.wlld.naturalLanguage.word.TypeSort;
import org.wlld.naturalLanguage.word.WordEmbedding;
import org.wlld.rnnNerveEntity.SensoryNerve;

import java.util.*;

public class RandomNerveManager {//随机神经网络管理
    private final WordEmbedding wordEmbedding;
    private final List<RandomNerveBody> randomNerveBodyList = new ArrayList<>();
    private final Map<Integer, Integer> mapping = new HashMap<>();//主键是真实id,值是映射识别用id
    private final int typeNub;//分类数量
    private final int vectorDimension;//特征纵向维度
    private final int maxFeatureLength;//特征最长长度
    private final double studyPoint;//词向量学习学习率
    private final boolean showLog;//是否输出学习数据
    private int randomNumber;//随机数量
    private int nerveNumber = 4;//神经元数量
    private final int dateAug;//数据增广
    private final int topNumber;//去最高几种类别

    public RandomNerveManager(SentenceConfig config, WordEmbedding wordEmbedding) throws Exception {
        if (config.getNerveDeep() > 3) {
            nerveNumber = config.getNerveDeep();
        }
        if (config.getTypeNub() > 0 && config.getMaxWordLength() > nerveNumber && config.getRandomNumber() > 1) {
            this.wordEmbedding = wordEmbedding;
            this.dateAug = config.getDateAug();
            this.topNumber = config.getTopNumber();
            this.typeNub = config.getTypeNub();
            this.vectorDimension = config.getWordVectorDimension();
            this.maxFeatureLength = config.getMaxWordLength();
            this.studyPoint = config.getWeStudyPoint();
            this.showLog = config.isShowLog();
            this.randomNumber = config.getRandomNumber();
        } else {
            throw new Exception("parameter invalid,typeNub must be greater than 0 and maxFeatureLength greater than " + nerveNumber + " and randomNumber than 1");
        }
    }

    private NerveManager initNerveManager(boolean initPower) throws Exception {
        NerveManager typeNerveManger = new NerveManager(vectorDimension, vectorDimension, typeNub, nerveNumber - 1, new Tanh(), false,
                studyPoint, RZ.L1, studyPoint * 0.2);
        typeNerveManger.initRnn(initPower, showLog);
        return typeNerveManger;
    }

    private void studyNerve(long eventId, List<SensoryNerve> sensoryNerves, List<Double> featureList, Matrix rnnMatrix, Map<Integer, Double> E, boolean isStudy, OutBack convBack) throws Exception {
        studyMyNerve(eventId, sensoryNerves, featureList, rnnMatrix, E, isStudy, convBack);
    }

    public static void studyMyNerve(long eventId, List<SensoryNerve> sensoryNerves, List<Double> featureList, Matrix rnnMatrix, Map<Integer, Double> E, boolean isStudy, OutBack convBack) throws Exception {
        if (sensoryNerves.size() == featureList.size()) {
            for (int i = 0; i < sensoryNerves.size(); i++) {
                sensoryNerves.get(i).postMessage(eventId, featureList.get(i), isStudy, E, convBack, false, rnnMatrix);
            }
        } else {
            throw new Exception("size not equals,feature size:" + featureList.size() + "," +
                    "sensorySize:" + sensoryNerves.size());
        }
    }

    public void insertModel(RandomModel randomModel) throws Exception {
        List<RandomModelParameter> randomModelParameters = randomModel.getRandomModelParameters();
        List<TypeMapping> typeMappings = randomModel.getTypeMappings();
        for (TypeMapping typeMapping : typeMappings) {
            mapping.put(typeMapping.getType(), typeMapping.getMapping());
        }
        randomNumber = randomModelParameters.size();
        for (RandomModelParameter modelParameter : randomModelParameters) {
            RandomNerveBody randomNerveBody = new RandomNerveBody();
            int[] featureIndexes = modelParameter.getFeatureIndexes();
            randomNerveBody.setKey(modelParameter.getKey());
            randomNerveBody.setFeatureIndexes(featureIndexes);
            NerveManager nerveManager = initNerveManager(false);
            nerveManager.insertModelParameter(modelParameter.getModelParameter());
            randomNerveBody.setNerveManager(nerveManager);
            randomNerveBodyList.add(randomNerveBody);
        }
    }

    private RandomModel getModel() throws Exception {//获取模型
        RandomModel randomModel = new RandomModel();
        List<RandomModelParameter> randomModelParameters = new ArrayList<>();
        randomModel.setRandomModelParameters(randomModelParameters);
        for (RandomNerveBody randomNerveBody : randomNerveBodyList) {
            RandomModelParameter randomModelParameter = new RandomModelParameter();
            randomModelParameter.setKey(randomNerveBody.getKey());
            randomModelParameter.setFeatureIndexes(randomNerveBody.getFeatureIndexes());
            randomModelParameter.setModelParameter(randomNerveBody.getNerveManager().getModelParameter());
            randomModelParameters.add(randomModelParameter);
        }
        return randomModel;
    }

    private int balance(Map<Integer, List<String>> model) {//强行均衡
        int maxNumber = dateAug;
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

    public Trust getType(String word, long eventId) throws Exception {
        Matrix myFeature = wordEmbedding.getEmbedding(word, eventId);//全部特征
        Trust trust = study(myFeature, null, false, eventId);
        List<Integer> keys = trust.getKeys();
        List<Integer> myKeys = new ArrayList<>();
        for (int key : keys) {
            int id = -1;
            for (Map.Entry<Integer, Integer> entry : mapping.entrySet()) {
                if (entry.getValue() == key) {
                    id = entry.getKey();
                    break;
                }
            }
            myKeys.add(id);
        }
        trust.setKeys(myKeys);
        return trust;
    }

    public RandomModel studyType(Map<Integer, List<String>> model) throws Exception {//学习类别
        int maxNumber = balance(model);//平衡样本
        int index = 0;
        Map<Integer, Double> E = new HashMap<>();
        do {
            for (Map.Entry<Integer, List<String>> entry : model.entrySet()) {
                System.out.println("index====================================" + index);
                E.clear();
                List<String> sentence = entry.getValue();
                E.put(mapping.get(entry.getKey()), 1D);
                String word = sentence.get(index);
                if (word.length() > maxFeatureLength) {
                    word = word.substring(0, maxFeatureLength);
                }
                study(wordEmbedding.getEmbedding(word, 1), E, true, 1);
            }
            index++;
        } while (index < maxNumber);
        RandomModel randomModel = getModel();
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

    private void insertValue(List<TypeBody> typeBodies, int key, double value) {
        boolean init = true;
        for (TypeBody typeBody : typeBodies) {
            if (typeBody.getType() == key) {
                typeBody.setPower(typeBody.getPower() + value);
                init = false;
                break;
            }
        }
        if (init) {
            TypeBody typeBody = new TypeBody();
            typeBody.setType(key);
            typeBody.setPower(value);
            typeBodies.add(typeBody);
        }
    }

    private Trust study(Matrix feature, Map<Integer, Double> E, boolean isStudy, long eventId) throws Exception {
        List<TypeBody> typeBodies = new ArrayList<>();
        Trust trust = null;
        int myNumber = 0;
        for (int i = 0; i < randomNumber; i++) {
            RandomNerveBody randomNerveBody = randomNerveBodyList.get(i);
            int[] features = randomNerveBody.getFeatureIndexes();
            NerveManager typeNerveManager = randomNerveBody.getNerveManager();
            Matrix myFeature = null;
            List<Double> firstFeature = null;
            int times = 0;
            //不一定取的到前提是有这个序列
            for (int index : features) {
                if (feature.getX() > index) {//能找的到
                    times++;
                    if (myFeature == null) {
                        myFeature = feature.getRow(index);
                        firstFeature = MatrixOperation.rowVectorToList(myFeature);//首行特征
                    } else {
                        myFeature = MatrixOperation.pushVector(myFeature, feature.getRow(index), true);
                    }
                } else {//直接跳出
                    break;
                }
            }
            if (firstFeature != null && times > 1) {
                myNumber++;
                WordBack wordBack = null;
                if (!isStudy) {
                    wordBack = new WordBack();
                }
                studyNerve(eventId, typeNerveManager.getSensoryNerves(), firstFeature, myFeature, E, isStudy, wordBack);
                if (wordBack != null) {
                    int id = wordBack.getId();
                    double value = wordBack.getOut();
                    insertValue(typeBodies, id, value);
                }
            }
        }
        if (!isStudy) {
            trust = new Trust();
            typeBodies.sort(new TypeSort());
            if (topNumber < typeBodies.size()) {
                typeBodies = typeBodies.subList(0, topNumber);
            }
            List<Integer> keys = new ArrayList<>();
            for (TypeBody typeBody : typeBodies) {
                keys.add(typeBody.getType());
            }
            trust.setKeys(keys);
            trust.setTrust(myNumber / (double) randomNumber);
        }
        return trust;
    }

    public void init() throws Exception {
        Random random = new Random();
        for (int i = 0; i < randomNumber; i++) {
            RandomNerveBody randomNerveBody = new RandomNerveBody();
            int[] featureIndexes = new int[nerveNumber];
            randomNerveBody.setKey(i + 1);
            randomNerveBody.setFeatureIndexes(featureIndexes);
            randomNerveBody.setNerveManager(initNerveManager(true));
            randomNerveBodyList.add(randomNerveBody);
            if (i > 0) {
                List<Integer> list = new ArrayList<>();
                for (int k = 0; k < maxFeatureLength; k++) {
                    list.add(k);
                }
                for (int j = 0; j < nerveNumber; j++) {
                    int index = random.nextInt(list.size());
                    featureIndexes[j] = list.get(index);
                    list.remove(index);
                }
            } else {//第一组rnn进行特征自增选取保证短句输入
                for (int j = 0; j < nerveNumber; j++) {
                    featureIndexes[j] = j;
                }
            }
        }
    }

}
