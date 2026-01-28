package org.dromara.easyai.yolo;


import org.dromara.easyai.config.ResnetConfig;
import org.dromara.easyai.entity.Box;
import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.function.ReLu;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.nerveCenter.NerveManager;
import org.dromara.easyai.nerveEntity.SensoryNerve;
import org.dromara.easyai.resnet.ResnetInput;
import org.dromara.easyai.resnet.ResnetManager;
import org.dromara.easyai.resnet.entity.BatchBody;
import org.dromara.easyai.tools.NMS;
import org.dromara.easyai.tools.Picture;

import java.util.*;

public class FastYolo {//yolo
    private final YoloConfig yoloConfig;
    private final NerveManager typeNerveManager;//识别网络
    private final ResnetManager resnetManager;//resnet分类网络
    private final List<TypeBody> typeBodies = new ArrayList<>();//样本数据及集合
    private final int winWidth;
    private final int winHeight;
    private final int widthStep;
    private final int heightStep;
    private final float trustTh;
    private final boolean resnet;
    private final ResYoloConfig resYoloConfig;
    private final int enh;//循环次数
    private final float stepReduce;//训练步长
    private final float iouTh;//交并比阈值
    private final float containIouTh;//判断是否为检测物阈值
    private final float positionContainIouTh;//位置网络判断是否为检测物
    private final float pth;//概率阈值
    private final float otherPTh;
    private final Map<Integer, Float> pd = new HashMap<>();

    public FastYolo(YoloConfig yoloConfig) throws Exception {
        float stepReduce = yoloConfig.getCheckStepReduce();
        this.yoloConfig = yoloConfig;
        this.enh = yoloConfig.getEnhance();
        this.iouTh = yoloConfig.getIouTh();
        this.containIouTh = yoloConfig.getContainIouTh();
        this.pth = yoloConfig.getPth();
        this.otherPTh = yoloConfig.getOtherPth();
        resnet = false;
        this.stepReduce = yoloConfig.getStepReduce();
        winHeight = yoloConfig.getWindowHeight();
        winWidth = yoloConfig.getWindowWidth();
        widthStep = (int) (winWidth * stepReduce);
        heightStep = (int) (winHeight * stepReduce);
        resYoloConfig = null;
        resnetManager = null;
        trustTh = yoloConfig.getTrustTh();
        positionContainIouTh = containIouTh;
        float pdRate = yoloConfig.getBackGroundPD();
        if (pdRate <= 0.9f && pdRate > 0) {
            pd.put(yoloConfig.getTypeNub() + 1, pdRate);
        }
        if (stepReduce <= 1 && widthStep > 0 && heightStep > 0) {
            typeNerveManager = new NerveManager(3, yoloConfig.getHiddenNerveNub(), yoloConfig.getTypeNub() + 1,
                    yoloConfig.getHiddenDeep(), new ReLu(), yoloConfig.getStudyRate(), yoloConfig.getRegularModel(), yoloConfig.getRegular()
                    , yoloConfig.getCoreNumber(), yoloConfig.getGaMa(), yoloConfig.getGMaxTh(), yoloConfig.isAuto());
            typeNerveManager.initImageNet(yoloConfig.getChannelNo(), yoloConfig.getKernelSize(), winHeight, winWidth, true,
                    yoloConfig.isShowLog(), yoloConfig.getStudyRate(), new ReLu(), yoloConfig.getMinFeatureValue(), yoloConfig.getStudyRate()
                    , yoloConfig.isNorm());
        } else {
            throw new Exception("The stepReduce must be (0,1] and widthStep ,heightStep must Greater than 0");
        }
    }

    public FastYolo(ResYoloConfig resYoloConfig) throws Exception {
        float stepReduce = resYoloConfig.getCheckStepReduce();
        this.stepReduce = resYoloConfig.getStepReduce();
        this.iouTh = resYoloConfig.getIouTh();
        this.containIouTh = resYoloConfig.getContainIouTh();
        this.pth = resYoloConfig.getPth();
        this.otherPTh = resYoloConfig.getOtherPth();
        winHeight = resYoloConfig.getWindowSize();
        winWidth = resYoloConfig.getWindowSize();
        trustTh = resYoloConfig.getTrustTh();
        this.enh = resYoloConfig.getEnhance();
        widthStep = (int) (winWidth * stepReduce);
        heightStep = (int) (winHeight * stepReduce);
        resnet = true;
        typeNerveManager = null;
        yoloConfig = null;
        this.resYoloConfig = resYoloConfig;
        positionContainIouTh = resYoloConfig.getPositionContainIouTh();
        float pdRate = resYoloConfig.getBackGroundPD();
        if (pdRate <= 0.9f && pdRate > 0) {
            pd.put(resYoloConfig.getTypeNub() + 1, pdRate);
        }
        if (pdRate <= 0) {
            throw new IllegalAccessException("pdRate 必须大于0");
        }
        if (stepReduce <= 1 && widthStep > 0 && heightStep > 0) {
            ResnetConfig resnetConfig = getResYoloConfig(resYoloConfig);
            resnetManager = new ResnetManager(resnetConfig, new ReLu());
        } else {
            throw new IllegalAccessException("The stepReduce must be (0,1] and widthStep ,heightStep must Greater than 0");
        }
    }

    public NerveManager getTypeNerveManager() {
        return typeNerveManager;
    }

    public ResnetManager getResnetManager() {
        return resnetManager;
    }

    private ResnetConfig getResYoloConfig(ResYoloConfig resYoloConfig) {
        ResnetConfig resnetConfig = new ResnetConfig();
        resnetConfig.setSize(resYoloConfig.getWindowSize());
        resnetConfig.setStudyRate(resYoloConfig.getStudyRate());
        resnetConfig.setRegularModel(resYoloConfig.getRegularModel());
        resnetConfig.setRegular(resYoloConfig.getRegular());
        resnetConfig.setHiddenNerveNumber(resYoloConfig.getHiddenNerveNub());
        resnetConfig.setTypeNumber(resYoloConfig.getTypeNub() + 1);
        resnetConfig.setSoftMax(true);
        resnetConfig.setShowLog(resYoloConfig.isShowLog());
        resnetConfig.setChannelNo(resYoloConfig.getChannelNo());
        resnetConfig.setHiddenDeep(resYoloConfig.getHiddenDeep());
        resnetConfig.setMinFeatureSize(resYoloConfig.getMinFeatureValue());
        resnetConfig.setGaMa(resYoloConfig.getGaMa());
        resnetConfig.setGMaxTh(resYoloConfig.getGMaxTh());
        resnetConfig.setBatchSize(resYoloConfig.getBatchSize());
        return resnetConfig;
    }

    private void insertYoloBody(YoloBody yoloBody) throws Exception {
        boolean here = false;
        for (TypeBody typeBody : typeBodies) {
            if (typeBody.getTypeID() == yoloBody.getTypeID()) {
                here = true;
                typeBody.insertYoloBody(yoloBody);
                break;
            }
        }
        if (!here) {//不存在
            TypeBody typeBody;
            if (resnet) {
                typeBody = new TypeBody(resYoloConfig, winWidth);
            } else {
                typeBody = new TypeBody(yoloConfig, winWidth, winHeight);
            }
            typeBody.setTypeID(yoloBody.getTypeID());
            typeBody.setMappingID(typeBodies.size() + 1);
            typeBody.insertYoloBody(yoloBody);
            typeBodies.add(typeBody);
        }
    }

    public void insertModel(YoloModel yoloModel) throws Exception {
        if (resnet) {
            resnetManager.insertModel(yoloModel.getResnetModel());
        } else {
            typeNerveManager.insertConvModel(yoloModel.getTypeModel());
        }
        List<TypeModel> typeModels = yoloModel.getTypeModels();
        for (TypeModel typeModel : typeModels) {
            TypeBody typeBody = getTypeBody(typeModel);
            typeBody.getPositionNerveManager().insertConvModel(typeModel.getPositionModel());
            typeBodies.add(typeBody);
        }
    }

    private TypeBody getTypeBody(TypeModel typeModel) throws Exception {
        TypeBody typeBody;
        if (resnet) {
            typeBody = new TypeBody(resYoloConfig, winWidth);
        } else {
            typeBody = new TypeBody(yoloConfig, winWidth, winHeight);
        }
        typeBody.setTypeID(typeModel.getTypeID());
        typeBody.setMappingID(typeModel.getMappingID());
        typeBody.setMinWidth(typeModel.getMinWidth());
        typeBody.setMinHeight(typeModel.getMinHeight());
        typeBody.setMaxWidth(typeModel.getMaxWidth());
        typeBody.setMaxHeight(typeModel.getMaxHeight());
        return typeBody;
    }

    public YoloModel getModel() throws Exception {
        YoloModel yoloModel = new YoloModel();
        if (resnet) {
            yoloModel.setResnetModel(resnetManager.getModel());
        } else {
            yoloModel.setTypeModel(typeNerveManager.getConvModel());
        }
        List<TypeModel> typeModels = new ArrayList<>();
        for (TypeBody typeBody : typeBodies) {
            TypeModel typeModel = new TypeModel();
            typeModel.setTypeID(typeBody.getTypeID());
            typeModel.setMappingID(typeBody.getMappingID());
            typeModel.setMinHeight(typeBody.getMinHeight());
            typeModel.setMinWidth(typeBody.getMinWidth());
            typeModel.setMaxWidth(typeBody.getMaxWidth());
            typeModel.setMaxHeight(typeBody.getMaxHeight());
            typeModel.setPositionModel(typeBody.getPositionNerveManager().getConvModel());
            typeModels.add(typeModel);
        }
        yoloModel.setTypeModels(typeModels);
        return yoloModel;
    }

    private Box getBox(int i, int j, int maxX, int maxY, PositionBack positionBack, TypeBody typeBody) throws Exception {
        float zhou = winHeight + winWidth;
        Box box = null;
        float centerX = i - positionBack.getDistX() * zhou;
        float centerY = j - positionBack.getDistY() * zhou;
        int width = (int) typeBody.getRealWidth(positionBack.getWidth());
        int height = (int) typeBody.getRealHeight(positionBack.getHeight());
        int realX = (int) (centerX - height / 2f);
        int realY = (int) (centerY - width / 2f);
        if (realX < 0) {
            realX = 0;
        }
        if (realY < 0) {
            realY = 0;
        }
        if (realX + height > maxX) {
            realX = maxX - height;
        }
        if (realY + width > maxY) {
            realY = maxY - width;
        }
        float trust = positionBack.getTrust();
        if (trust > trustTh) {
            box = new Box();
            box.setX(realX);
            box.setY(realY);
            box.setxSize(height);
            box.setySize(width);
            box.setConfidence(trust);
            box.setTypeID(typeBody.getTypeID());
        }
        return box;
    }

    private List<OutBox> getOutBoxList(List<Box> boxes) {
        List<OutBox> outBoxes = new ArrayList<>();
        for (Box box : boxes) {
            OutBox outBox = new OutBox();
            outBox.setX(box.getY());
            outBox.setY(box.getX());
            outBox.setHeight(box.getxSize());
            outBox.setWidth(box.getySize());
            outBox.setTypeID(String.valueOf(box.getTypeID()));
            outBox.setTrust(box.getConfidence());
            outBoxes.add(outBox);
        }
        return outBoxes;
    }

    public List<OutBox> look(ThreeChannelMatrix th, long eventID) throws Exception {
        int x = th.getX();
        int y = th.getY();
        List<Box> boxes = new ArrayList<>();
        NMS nms = new NMS(iouTh);
        for (int i = 0; i <= x - winHeight; i += heightStep) {
            for (int j = 0; j <= y - winWidth; j += widthStep) {
                YoloTypeBack yoloTypeBack = new YoloTypeBack();
                PositionBack positionBack = new PositionBack();
                ThreeChannelMatrix myTh = th.cutChannel(i, j, winHeight, winWidth);
                if (resnet) {
                    resnetManager.getRestNetInput().postFeature(myTh, yoloTypeBack, eventID, false);
                } else {
                    study(eventID, typeNerveManager.getConvInput(), myTh, false, null, yoloTypeBack);
                }
                int mappingID = yoloTypeBack.getId();//映射id
                float out = yoloTypeBack.getOut();
                if (mappingID != typeBodies.size() + 1 && out > pth) {
                    TypeBody typeBody = getTypeBodyByMappingID(mappingID);
                    SensoryNerve convInput = typeBody.getPositionNerveManager().getConvInput();
                    study(eventID, convInput, myTh, false, null, positionBack);
                    Box box = getBox(i, j, x, y, positionBack, typeBody);
                    if (box != null) {
                        boxes.add(box);
                    }
                }
            }
        }
        if (boxes.isEmpty()) {
            return null;
        }
        return getOutBoxList(nms.start(boxes));
    }

    public List<OutBox> testType(ThreeChannelMatrix th, long eventID) throws Exception {
        int x = th.getX();
        int y = th.getY();
        List<Box> boxes = new ArrayList<>();
        for (int i = 0; i <= x - winHeight; i += heightStep) {
            for (int j = 0; j <= y - winWidth; j += widthStep) {
                YoloTypeBack yoloTypeBack = new YoloTypeBack();
                ThreeChannelMatrix myTh = th.cutChannel(i, j, winHeight, winWidth);
                if (resnet) {
                    resnetManager.getRestNetInput().postFeature(myTh, yoloTypeBack, eventID, false);
                } else {
                    study(eventID, typeNerveManager.getConvInput(), myTh, false, null, yoloTypeBack);
                }
                int mappingID = yoloTypeBack.getId();//映射id
                float out = yoloTypeBack.getOut();
                if (mappingID != typeBodies.size() + 1 && out > pth) {
                    TypeBody typeBody = getTypeBodyByMappingID(mappingID);
                    Box box = new Box();
                    box.setX(i);
                    box.setY(j);
                    box.setxSize(winHeight);
                    box.setySize(winWidth);
                    box.setTypeID(typeBody.getTypeID());
                    boxes.add(box);
                }
            }
        }
        if (boxes.isEmpty()) {
            return null;
        }
        return getOutBoxList(boxes);
    }

    public void toStudy(List<YoloSample> yoloSamples, OutBack logOutBack, boolean studyType) throws Exception {
        for (YoloSample yoloSample : yoloSamples) {
            List<YoloBody> yoloBodies = yoloSample.getYoloBodies();
            for (YoloBody yoloBody : yoloBodies) {
                insertYoloBody(yoloBody);
            }
        }
        for (int i = 0; i < enh; i++) {
            System.out.println("第===========================" + i + "次" + "共" + enh + "次");
            int index = 0;
            for (YoloSample yoloSample : yoloSamples) {
                index++;
                System.out.println("index:" + index + ",size:" + yoloSamples.size());
                study(yoloSample, logOutBack, studyType);
            }
        }
    }

    private Box changeBox(YoloBody yoloBody) {
        Box box = new Box();
        box.setX(yoloBody.getY());
        box.setY(yoloBody.getX());
        box.setTypeID(yoloBody.getTypeID());
        box.setxSize(yoloBody.getHeight());
        box.setySize(yoloBody.getWidth());
        return box;
    }

    private YoloMessage containSample(List<Box> boxes, Box testBox, NMS nms, int i, int j, boolean studyType) {
        float maxIou = 0;
        boolean isBackGround = true;
        Box myBox = null;
        YoloMessage yoloMessage = null;
        float myIouTh;
        if (studyType) {
            myIouTh = containIouTh;
        } else {
            myIouTh = positionContainIouTh;
        }
        for (Box box : boxes) {
            float iou = nms.getSRatio(testBox, box, false);
            if (iou > otherPTh) {
                isBackGround = false;
            }
            if (iou > myIouTh && iou > maxIou) {//有相交
                maxIou = iou;
                myBox = box;
            }
        }
        if (myBox != null) {
            yoloMessage = new YoloMessage();
            int centerX = myBox.getX() + myBox.getxSize() / 2;
            int centerY = myBox.getY() + myBox.getySize() / 2;
            float zhou = winHeight + winWidth;
            float distX = (float) (i - centerX) / zhou;
            float distY = (float) (j - centerY) / zhou;
            TypeBody typeBody = getTypeBodyByTypeID(myBox.getTypeID());
            float height = typeBody.getOneHeight(myBox.getxSize());
            float width = typeBody.getOneWidth(myBox.getySize());
            float trust = 0;
            if (centerX >= i && centerX <= (i + winHeight) && centerY >= j && centerY <= (j + winWidth)) {
                trust = 1;
            }
            yoloMessage.setWidth(width);
            yoloMessage.setHeight(height);
            yoloMessage.setDistX(distX);
            yoloMessage.setDistY(distY);
            yoloMessage.setTrust(trust);
            yoloMessage.setMappingID(typeBody.getMappingID());
            yoloMessage.setTypeBody(typeBody);
        } else if (isBackGround) {
            yoloMessage = new YoloMessage();
            yoloMessage.setBackGround(true);
            yoloMessage.setMappingID(typeBodies.size() + 1);
        }
        return yoloMessage;
    }

    private List<Box> getBoxes(List<YoloBody> yoloBodies) {
        List<Box> boxes = new ArrayList<>();
        for (YoloBody yoloBody : yoloBodies) {
            boxes.add(changeBox(yoloBody));
        }
        return boxes;
    }

    private List<YoloMessage> anySort(List<YoloMessage> sentences) {//做乱序
        Random random = new Random();
        List<YoloMessage> sent = new ArrayList<>();
        int time = sentences.size();
        for (int i = 0; i < time; i++) {
            int size = sentences.size();
            int index = random.nextInt(size);
            sent.add(sentences.get(index));
            sentences.remove(index);
        }
        return sent;
    }

    private List<BatchBody> balance(List<YoloMessage> yoloMessageList) throws Exception {//样本强制均衡
        Map<Integer, List<YoloMessage>> yoloMessageMapping = new HashMap<>();
        for (YoloMessage yoloMessage : yoloMessageList) {
            int mappingID = yoloMessage.getMappingID();
            if (yoloMessageMapping.containsKey(mappingID)) {
                yoloMessageMapping.get(mappingID).add(yoloMessage);
            } else {
                List<YoloMessage> yoloMessages = new ArrayList<>();
                yoloMessages.add(yoloMessage);
                yoloMessageMapping.put(mappingID, yoloMessages);
            }
        }
        int index = 0;
        int batchSize = resYoloConfig.getBatchSize();
        List<BatchBody> batchBodies = new ArrayList<>();
        do {
            for (Map.Entry<Integer, List<YoloMessage>> entry : yoloMessageMapping.entrySet()) {
                List<YoloMessage> yoloMessages = entry.getValue();
                int size = yoloMessages.size();
                BatchBody batchBody = new BatchBody();
                Map<Integer, Float> EMap = new HashMap<>();
                if (size > index) {
                    YoloMessage yoloMessage = yoloMessages.get(index);
                    EMap.put(yoloMessage.getMappingID(), 1f);
                    batchBody.insertPicture(yoloMessage.getPic());
                } else {
                    int myIndex = index % size;
                    YoloMessage yoloMessage = yoloMessages.get(myIndex);
                    EMap.put(yoloMessage.getMappingID(), 1f);
                    batchBody.insertPicture(yoloMessage.getPic().copy());
                }
                batchBody.setE(EMap);
                batchBodies.add(batchBody);
                index++;
                if (index == batchSize) {
                    break;
                }
            }
        } while (index < batchSize);
        return batchBodies;
    }

    private void study(YoloSample yoloSample, OutBack logOutBack, boolean studyType) throws Exception {//
        List<YoloBody> yoloBodies = yoloSample.getYoloBodies();//集合
        List<YoloMessage> yoloMessageList = new ArrayList<>();
        List<Box> boxes = getBoxes(yoloBodies);
        String url = yoloSample.getLocationURL();//地址
        NMS nms = new NMS(iouTh);
        ThreeChannelMatrix pic = Picture.getThreeMatrix(url, false);
        int stepX = (int) (winHeight * stepReduce);
        int stepY = (int) (winWidth * stepReduce);
        if (stepX < 1 || stepY < 1) {
            throw new Exception("训练步长收缩后步长必须大于0");
        }
        for (int i = 0; i <= pic.getX() - winHeight; i += stepX) {
            for (int j = 0; j <= pic.getY() - winWidth; j += stepY) {
                Box testBox = new Box();
                testBox.setX(i);
                testBox.setY(j);
                testBox.setxSize(winHeight);
                testBox.setySize(winWidth);
                YoloMessage yoloMessage = containSample(boxes, testBox, nms, i, j, studyType);
                if (yoloMessage != null) {
                    yoloMessage.setPic(pic.cutChannel(i, j, winHeight, winWidth));
                    yoloMessageList.add(yoloMessage);
                }
            }
        }
        if (!yoloMessageList.isEmpty()) {
            if (resnet) {
                List<YoloMessage> myYoloMessageList = anySort(yoloMessageList);
                List<BatchBody> batchBodies = null;
                if (studyType) {
                    batchBodies = balance(myYoloMessageList);
                }
                studyImageByResnet(myYoloMessageList, logOutBack, batchBodies, studyType);
            } else {
                studyImage(anySort(yoloMessageList), logOutBack, studyType);
            }
        }
    }

    public TypeBody getTypeBodyByMappingID(int mappingID) {
        TypeBody ty = null;
        for (TypeBody yb : typeBodies) {
            if (yb.getMappingID() == mappingID) {
                ty = yb;
                break;
            }
        }
        return ty;
    }

    public TypeBody getTypeBodyByTypeID(int typeID) {
        TypeBody ty = null;
        for (TypeBody yb : typeBodies) {
            if (yb.getTypeID() == typeID) {
                ty = yb;
                break;
            }
        }
        return ty;
    }

    private void studyImageByResnet(List<YoloMessage> yoloMessageList, OutBack logOutBack,
                                    List<BatchBody> batchBodies, boolean studyType) throws Exception {
        ResnetInput resnetInput = resnetManager.getRestNetInput();
        if (studyType) {
            resnetInput.studyFeature(batchBodies, logOutBack, 1, pd);
        } else {
            for (YoloMessage yoloMessage : yoloMessageList) {//位置训练
                if (!yoloMessage.isBackGround()) {
                    studyPosition(yoloMessage, yoloMessage.getPic(), logOutBack);
                }
            }
        }
    }

    private void studyImage(List<YoloMessage> yoloMessageList, OutBack logOutBack, boolean studyType) throws Exception {
        for (YoloMessage yoloMessage : yoloMessageList) {
            ThreeChannelMatrix small = yoloMessage.getPic();
            if (studyType) {
                Map<Integer, Float> typeE = new HashMap<>();
                int mappingID = yoloMessage.getMappingID();
                typeE.put(mappingID, 1f);
                study(1, typeNerveManager.getConvInput(), small, true, typeE, logOutBack);
            } else {
                if (!yoloMessage.isBackGround()) {
                    studyPosition(yoloMessage, small, logOutBack);
                }
            }
        }

    }

    private void studyPosition(YoloMessage yoloMessage, ThreeChannelMatrix small, OutBack logOutBack) throws Exception {
        Map<Integer, Float> positionE = new HashMap<>();
        positionE.put(1, yoloMessage.getDistX());
        positionE.put(2, yoloMessage.getDistY());
        positionE.put(3, yoloMessage.getWidth());
        positionE.put(4, yoloMessage.getHeight());
        positionE.put(5, yoloMessage.getTrust());
        NerveManager position = yoloMessage.getTypeBody().getPositionNerveManager();
        study(2, position.getConvInput(), small, true, positionE, logOutBack);
    }

    private void study(long eventID, SensoryNerve convInput, ThreeChannelMatrix feature, boolean isStudy, Map<Integer, Float> E, OutBack back) throws Exception {
        convInput.postThreeChannelMatrix(eventID, feature, isStudy, E, back, false, pd);
    }
}
