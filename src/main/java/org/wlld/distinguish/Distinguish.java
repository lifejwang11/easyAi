package org.wlld.distinguish;

import org.wlld.MatrixTools.Matrix;
import org.wlld.config.Config;
import org.wlld.config.RZ;
import org.wlld.entity.*;
import org.wlld.function.ReLu;
import org.wlld.function.Tanh;
import org.wlld.i.OutBack;
import org.wlld.nerveCenter.NerveManager;
import org.wlld.nerveEntity.SensoryNerve;
import org.wlld.tools.Picture;

import java.util.*;

//对固定背景的物体进行识别
public class Distinguish {
    private NerveManager typeNerveManger;//类别拟合
    private NerveManager convolutionNerveManagerR;
    private NerveManager convolutionNerveManagerG;
    private NerveManager convolutionNerveManagerB;
    private Map<Integer, ThreeChannelMatrix> featureMap = new HashMap<>();
    private Map<Integer, Integer> mapping = new HashMap<>();//主键映射内部自增主键
    private ThreeChannelMatrix backGround;//背景图片
    private Config config;

    public Distinguish(Config config) throws Exception {
        this.config = config;
        init();
    }

    public void setBackGround(ThreeChannelMatrix backGround) {
        this.backGround = backGround;
    }

    private void studyNerve(List<SensoryNerve> sensoryNerves, List<Double> feature, Map<Integer, Double> E, boolean isStudy, OutBack convBack) throws Exception {
        if (sensoryNerves.size() == feature.size()) {
            for (int i = 0; i < feature.size(); i++) {
                sensoryNerves.get(i).postMessage(1, feature.get(i), isStudy, E, convBack);
            }
        } else {
            throw new Exception("size not equals,feature size:" + feature.size() + "," +
                    "sensorySize:" + sensoryNerves.size());
        }
    }

    private void setFeature(List<Double> feature, Matrix matrix) throws Exception {
        int x = matrix.getX();
        int y = matrix.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double nub = matrix.getNumber(i, j);
                feature.add(nub);
            }
        }
    }

    public Model getModel() throws Exception {
        Model foodModel = new Model();
        foodModel.setModelR(convolutionNerveManagerR.getModelParameter());
        foodModel.setModelG(convolutionNerveManagerG.getModelParameter());
        foodModel.setModelB(convolutionNerveManagerB.getModelParameter());
        foodModel.setModelFood(typeNerveManger.getModelParameter());
        foodModel.setMapping(mapping);
        return foodModel;
    }

    public void insertModel(Model foodModel) throws Exception {
        mapping.clear();
        convolutionNerveManagerR.insertModelParameter(foodModel.getModelR());
        convolutionNerveManagerG.insertModelParameter(foodModel.getModelG());
        convolutionNerveManagerB.insertModelParameter(foodModel.getModelB());
        typeNerveManger.insertModelParameter(foodModel.getModelFood());
        Map<Integer, Integer> map = foodModel.getMapping();
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            mapping.put(entry.getKey(), entry.getValue());
        }
    }

    private ThreeChannelMatrix sampling(ThreeChannelMatrix threeChannelMatrix) throws Exception {//对目标进行取样
        Random random = new Random();
        int boxSize = config.getBoxSize();
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        int x = matrixR.getX() - boxSize - 3;
        int y = matrixR.getY() - boxSize - 3;
        int rx = random.nextInt(x);
        int ry = random.nextInt(y);
        return threeChannelMatrix.cutChannel(rx, ry, boxSize, boxSize);
    }

    private int getType(int id) {//通过映射找出真实id
        int key = 0;
        for (Map.Entry<Integer, Integer> entry : mapping.entrySet()) {
            if (entry.getValue() == id) {
                key = entry.getKey();
            }
        }
        return key;
    }

    private void createMapping(List<FoodPicture> foodPictures) throws Exception {//创建映射
        if (foodPictures.size() == config.getTypeNub()) {
            int index = 1;
            for (FoodPicture foodPicture : foodPictures) {
                index++;
                int id = foodPicture.getId();
                if (id != 1) {
                    mapping.put(id, index);
                } else {
                    throw new Exception("ID can not be 1");
                }
            }
        } else {
            throw new Exception("The config  does not match the actual category quantity");
        }
    }

    public Map<Integer, Double> distinguish(ThreeChannelMatrix threeChannelMatrix) throws Exception {//进行识别
        int x = threeChannelMatrix.getMatrixR().getX();
        int y = threeChannelMatrix.getMatrixG().getY();
        int boxSize = config.getBoxSize();
        double pTh = config.getPth();
        Map<Integer, Double> pMap = new HashMap<>();
        if (x > boxSize && y > boxSize) {
            for (int i = 0; i <= x - boxSize; i += boxSize) {
                for (int j = 0; j <= y - boxSize; j += boxSize) {
                    ThreeChannelMatrix t = threeChannelMatrix.cutChannel(i, j, boxSize, boxSize);
                    RegionBack regionBack = lookEnd(t);
                    double point = regionBack.getPoint();//概率
                    int id = regionBack.getId();//类别
                    if (id != 1 && point > pTh) {
                        if (pMap.containsKey(id)) {
                            pMap.put(id, pMap.get(id) + point);
                        } else {
                            pMap.put(id, point);
                        }
                    }
                }
            }
        } else {
            throw new Exception("The picture size must be larger than the config boxSize");
        }
        return pMap;
    }

    private RegionBack lookEnd(ThreeChannelMatrix threeChannelMatrix) throws Exception {
        ConvBack convBack = new ConvBack();
        RegionBack foodBack = new RegionBack();
        SensoryNerve sensoryNerveR = convolutionNerveManagerR.getSensoryNerves().get(0);
        SensoryNerve sensoryNerveG = convolutionNerveManagerG.getSensoryNerves().get(0);
        SensoryNerve sensoryNerveB = convolutionNerveManagerB.getSensoryNerves().get(0);
        List<Double> feature = new ArrayList<>();
        sensoryNerveR.postMatrixMessage(1, threeChannelMatrix.getMatrixR()
                , false, 0, convBack);
        Matrix matrixR = convBack.getMatrix();
        setFeature(feature, matrixR);
        sensoryNerveG.postMatrixMessage(1, threeChannelMatrix.getMatrixG()
                , false, 0, convBack);
        Matrix matrixG = convBack.getMatrix();
        setFeature(feature, matrixG);
        sensoryNerveB.postMatrixMessage(1, threeChannelMatrix.getMatrixB()
                , false, 0, convBack);
        Matrix matrixB = convBack.getMatrix();
        setFeature(feature, matrixB);
        studyNerve(typeNerveManger.getSensoryNerves(), feature, null, false, foodBack);
        int type = foodBack.getId();
        if (type != 1) {
            foodBack.setId(getType(type));
        }
        return foodBack;
    }

    //图片矩阵列表，取样数量
    public void studyImage(List<FoodPicture> foodPictures) throws Exception {
        if (backGround != null) {
            Picture picture = new Picture();
            int size = config.getPictureNumber();//训练图片数量
            int sampNub = 1000 / size;
            Random random = new Random();
            createMapping(foodPictures);
            for (int t = 0; t < 2; t++) {
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < sampNub; j++) {
                        //一次塞图
                        for (FoodPicture foodPicture : foodPictures) {
                            int id = foodPicture.getId();
                            int type = mapping.get(id);//种类
                            List<PicturePosition> picturePositionList = foodPicture.getPicturePositionList();
                            PicturePosition picturePosition = picturePositionList.get(random.nextInt(size));
                            ThreeChannelMatrix threeChannelMatrix = picture.getThreeMatrix(picturePosition.getUrl());
                            if (picturePosition.isNeedCut()) {//需要切割
                                threeChannelMatrix = threeChannelMatrix.cutChannel(picturePosition.getX(), picturePosition.getY(),
                                        picturePosition.getXSize(), picturePosition.getYSize());
                            }
                            //进行取样
                            ThreeChannelMatrix threeChannelMatrix1 = sampling(threeChannelMatrix);
                            featureMap.put(type, threeChannelMatrix1);
                        }
                        //托盘取样
                        ThreeChannelMatrix threeChannelMatrix1 = sampling(backGround);
                        featureMap.put(1, threeChannelMatrix1);
                        if (t == 0) {
                            studyCov();
                        } else {
                            studyType();
                        }
                        featureMap.clear();
                        //塞一次跑一次
                    }
                }
            }
        } else {
            throw new Exception("Learning background can not be null");
        }
    }

    private void studyCov() throws Exception {
        SensoryNerve sensoryNerveR = convolutionNerveManagerR.getSensoryNerves().get(0);
        SensoryNerve sensoryNerveG = convolutionNerveManagerG.getSensoryNerves().get(0);
        SensoryNerve sensoryNerveB = convolutionNerveManagerB.getSensoryNerves().get(0);
        for (Map.Entry<Integer, ThreeChannelMatrix> entry : featureMap.entrySet()) {
            int key = entry.getKey();
            ThreeChannelMatrix feature = entry.getValue();
            sensoryNerveR.postMatrixMessage(1, feature.getMatrixR(), true, key, null);
            sensoryNerveG.postMatrixMessage(1, feature.getMatrixG(), true, key, null);
            sensoryNerveB.postMatrixMessage(1, feature.getMatrixB(), true, key, null);
        }
    }

    private void studyType() throws Exception {//学习类别
        ConvBack convBack = new ConvBack();
        SensoryNerve sensoryNerveR = convolutionNerveManagerR.getSensoryNerves().get(0);
        SensoryNerve sensoryNerveG = convolutionNerveManagerG.getSensoryNerves().get(0);
        SensoryNerve sensoryNerveB = convolutionNerveManagerB.getSensoryNerves().get(0);
        Map<Integer, Double> E = new HashMap<>();
        for (Map.Entry<Integer, ThreeChannelMatrix> entry : featureMap.entrySet()) {
            int key = entry.getKey();
            E.clear();
            List<Double> feature = new ArrayList<>();
            ThreeChannelMatrix t = entry.getValue();
            sensoryNerveR.postMatrixMessage(1, t.getMatrixR(), false, key, convBack);
            Matrix matrixR = convBack.getMatrix();
            setFeature(feature, matrixR);
            sensoryNerveG.postMatrixMessage(1, t.getMatrixG(), false, key, convBack);
            Matrix matrixG = convBack.getMatrix();
            setFeature(feature, matrixG);
            sensoryNerveB.postMatrixMessage(1, t.getMatrixB(), false, key, convBack);
            Matrix matrixB = convBack.getMatrix();
            setFeature(feature, matrixB);
            System.out.println(feature);
            E.put(key, 1.0);
            studyNerve(typeNerveManger.getSensoryNerves(), feature, E, true, null);
        }
    }

    private void init() throws Exception {
        int classificationNub = config.getTypeNub() + 1;
        int x = config.getBoxSize();
        int kernLen = config.getKernLen();
        int step = config.getStep();
        int deep = 0;//深度
        int size = 0;//大小
        int y;
        do {
            y = (x - (kernLen - step)) / step;
            if (y >= 1) {
                x = y;
                size = x;
                deep++;
            }
        } while (y >= kernLen);
        Map<Integer, Matrix> matrixMap = new HashMap<>();//主键与期望矩阵的映射
        double oneNub;
        if (classificationNub == 1) {
            oneNub = 1D;
        } else {
            oneNub = 1D / (classificationNub - 1);
        }
        for (int k = 0; k < classificationNub; k++) {
            Matrix matrix = new Matrix(size, size);//初始化期望矩阵
            double t = k * oneNub;//期望矩阵的分类参数数值
            for (int i = 0; i < size; i++) {//给期望矩阵注入期望参数
                for (int j = 0; j < size; j++) {
                    matrix.setNub(i, j, t);
                }
            }
            matrixMap.put(k + 1, matrix);
        }
        int senNub = size * size * 3;
        convolutionNerveManagerR = initConv(deep, matrixMap, step, kernLen);
        convolutionNerveManagerG = initConv(deep, matrixMap, step, kernLen);
        convolutionNerveManagerB = initConv(deep, matrixMap, step, kernLen);
        typeNerveManger = new NerveManager(senNub, config.getTypeHiddenNub(), classificationNub, 1, new Tanh(), false,
                config.getAllLineStudyPoint(), RZ.L1, config.getLParam());
        typeNerveManger.init(true, false, config.isShowLog(), true, 0, 0);
    }

    private NerveManager initConv(int deep, Map<Integer, Matrix> matrixMap
            , int step, int kernLen) throws Exception {
        NerveManager nerveManager = new NerveManager(1, 1,
                1, deep - 1, new ReLu(),
                true, config.getConvStudyPoint(), RZ.NOT_RZ, 0);
        nerveManager.setMatrixMap(matrixMap);//给卷积网络管理器注入期望矩阵
        nerveManager.init(true, true, false, false
                , step, kernLen);
        return nerveManager;
    }
}
