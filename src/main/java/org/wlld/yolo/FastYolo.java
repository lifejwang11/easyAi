package org.wlld.yolo;

import org.wlld.MatrixTools.Matrix;
import org.wlld.config.RZ;
import org.wlld.entity.Box;
import org.wlld.entity.ThreeChannelMatrix;
import org.wlld.function.ELu;
import org.wlld.function.ReLu;
import org.wlld.function.Tanh;
import org.wlld.i.OutBack;
import org.wlld.nerveCenter.NerveManager;
import org.wlld.nerveEntity.SensoryNerve;
import org.wlld.tools.NMS;
import org.wlld.tools.Picture;

import java.util.*;

public class FastYolo {//yolo
    private final YoloConfig yoloConfig;
    private final NerveManager typeNerveManager;//识别网络
    private final List<TypeBody> typeBodies = new ArrayList<>();//样本数据及集合
    private final int winWidth;
    private final int winHeight;

    public FastYolo(YoloConfig yoloConfig) throws Exception {
        this.yoloConfig = yoloConfig;
        winHeight = yoloConfig.getWindowHeight();
        winWidth = yoloConfig.getWindowWidth();
        typeNerveManager = new NerveManager(3, yoloConfig.getHiddenNerveNub(), yoloConfig.getTypeNub() + 1,
                1, new ReLu(), yoloConfig.getLineStudy(), RZ.NOT_RZ, 0);
        typeNerveManager.initImageNet(2, yoloConfig.getKernelSize(), winHeight, winWidth,
                true, yoloConfig.isShowLog(), yoloConfig.getConvStudy(), new ReLu());
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
            TypeBody typeBody = new TypeBody(yoloConfig, winWidth, winHeight);
            typeBody.setTypeID(yoloBody.getTypeID());
            typeBody.setMappingID(typeBodies.size() + 1);
            typeBody.insertYoloBody(yoloBody);
            typeBodies.add(typeBody);
        }
    }

    public void insertModel(YoloModel yoloModel) throws Exception {
        typeNerveManager.insertConvModel(yoloModel.getTypeModel());
        List<TypeModel> typeModels = yoloModel.getTypeModels();
        for (TypeModel typeModel : typeModels) {
            TypeBody typeBody = new TypeBody(yoloConfig, winWidth, winHeight);
            typeBody.setTypeID(typeModel.getTypeID());
            typeBody.setMappingID(typeModel.getMappingID());
            typeBody.setMinWidth(typeModel.getMinWidth());
            typeBody.setMinHeight(typeModel.getMinHeight());
            typeBody.setMaxWidth(typeModel.getMaxWidth());
            typeBody.setMaxHeight(typeModel.getMaxHeight());
            typeBody.getPositonNerveManager().insertConvModel(typeModel.getPositionModel());
            typeBodies.add(typeBody);
        }
    }

    public YoloModel getModel() throws Exception {
        YoloModel yoloModel = new YoloModel();
        yoloModel.setTypeModel(typeNerveManager.getConvModel());
        List<TypeModel> typeModels = new ArrayList<>();
        for (TypeBody typeBody : typeBodies) {
            TypeModel typeModel = new TypeModel();
            typeModel.setTypeID(typeBody.getTypeID());
            typeModel.setMappingID(typeBody.getMappingID());
            typeModel.setMinHeight(typeBody.getMinHeight());
            typeModel.setMaxWidth(typeBody.getMaxWidth());
            typeModel.setMaxHeight(typeBody.getMaxHeight());
            typeModel.setPositionModel(typeBody.getPositonNerveManager().getConvModel());
            typeModels.add(typeModel);
        }
        yoloModel.setTypeModels(typeModels);
        return yoloModel;
    }

    private Box getBox(int i, int j, int maxX, int maxY, PositionBack positionBack, TypeBody typeBody) throws Exception {
        double zhou = winHeight + winWidth;
        Box box = new Box();
        double centerX = i - positionBack.getDistX() * zhou;
        double centerY = j - positionBack.getDistY() * zhou;
        int width = (int) typeBody.getRealWidth(positionBack.getWidth());
        int height = (int) typeBody.getRealHeight(positionBack.getHeight());
        int realX = (int) (centerX - height / 2);
        int realY = (int) (centerY - width / 2);
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
        box.setX(realX);
        box.setY(realY);
        box.setxSize(height);
        box.setySize(width);
        box.setConfidence(positionBack.getTrust());
        box.setTypeID(typeBody.getTypeID());
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
            outBox.setTypeID(box.getTypeID());
            outBoxes.add(outBox);
        }
        return outBoxes;
    }

    public List<OutBox> look(ThreeChannelMatrix th, long eventID) throws Exception {
        int x = th.getX();
        int y = th.getY();
        List<Box> boxes = new ArrayList<>();
        NMS nms = new NMS(yoloConfig.getIouTh());
        double pth = yoloConfig.getPth();
        for (int i = 0; i <= x - winHeight; i += winHeight) {
            for (int j = 0; j <= y - winWidth; j += winWidth) {
                YoloTypeBack yoloTypeBack = new YoloTypeBack();
                PositionBack positionBack = new PositionBack();
                ThreeChannelMatrix myTh = th.cutChannel(i, j, winHeight, winWidth);
                study(eventID, typeNerveManager.getSensoryNerves(), myTh, false, null, yoloTypeBack);
                int mappingID = yoloTypeBack.getId();//映射id
                if (mappingID != typeBodies.size() + 1 && yoloTypeBack.getOut() > pth) {
                    TypeBody typeBody = getTypeBodyByMappingID(mappingID);
                    List<SensoryNerve> sensoryNerves = typeBody.getPositonNerveManager().getSensoryNerves();
                    study(eventID, sensoryNerves, myTh, false, null, positionBack);
                    boxes.add(getBox(i, j, x, y, positionBack, typeBody));
                }
            }
        }
        return getOutBoxList(nms.start(boxes));
    }


    public void toStudy(List<YoloSample> yoloSamples) throws Exception {
        for (YoloSample yoloSample : yoloSamples) {
            List<YoloBody> yoloBodies = yoloSample.getYoloBodies();
            for (YoloBody yoloBody : yoloBodies) {
                insertYoloBody(yoloBody);
            }
        }
        int enh = yoloConfig.getEnhance();
        for (int i = 0; i < enh; i++) {
            System.out.println("第===========================" + i + "次" + "共" + enh + "次");
            for (YoloSample yoloSample : yoloSamples) {
                study(yoloSample);
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

    private YoloMessage containSample(List<Box> boxes, Box testBox, NMS nms, int i, int j) {
        double containIouTh = yoloConfig.getContainIouTh();
        double maxIou = 0;
        Box myBox = null;
        YoloMessage yoloMessage = new YoloMessage();
        for (Box box : boxes) {
            double iou = nms.getMyIou(testBox, box);
            if (iou > containIouTh && iou > maxIou) {//有相交
                maxIou = iou;
                myBox = box;
            }
        }
        if (myBox != null) {
            int centerX = myBox.getX() + myBox.getxSize() / 2;
            int centerY = myBox.getY() + myBox.getySize() / 2;
            double zhou = winHeight + winWidth;
            double distX = (double) (i - centerX) / zhou;
            double distY = (double) (j - centerY) / zhou;
            TypeBody typeBody = getTypeBodyByTypeID(myBox.getTypeID());
            double height = typeBody.getOneHeight(myBox.getxSize());
            double width = typeBody.getOneWidth(myBox.getySize());
            double trust = 0;
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
        } else {
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

    public void study(YoloSample yoloSample) throws Exception {//
        List<YoloBody> yoloBodies = yoloSample.getYoloBodies();//集合
        List<Box> boxes = getBoxes(yoloBodies);
        String url = yoloSample.getLocationURL();//地址
        Picture picture = new Picture();
        NMS nms = new NMS(yoloConfig.getIouTh());
        ThreeChannelMatrix pic = picture.getThreeMatrix(url);
        List<YoloMessage> yoloMessageList = new ArrayList<>();
        for (int i = 0; i <= pic.getX() - winHeight; i += winHeight) {
            for (int j = 0; j <= pic.getY() - winWidth; j += winWidth) {
                Box testBox = new Box();
                testBox.setX(i);
                testBox.setY(j);
                testBox.setxSize(winHeight);
                testBox.setySize(winWidth);
                YoloMessage yoloMessage = containSample(boxes, testBox, nms, i, j);
                yoloMessage.setPic(pic.cutChannel(i, j, winHeight, winWidth));
                yoloMessageList.add(yoloMessage);
            }
        }
        if (!yoloMessageList.isEmpty()) {
            studyImage(anySort(yoloMessageList));
        }
    }

    private TypeBody getTypeBodyByMappingID(int mappingID) {
        TypeBody ty = null;
        for (TypeBody yb : typeBodies) {
            if (yb.getMappingID() == mappingID) {
                ty = yb;
                break;
            }
        }
        return ty;
    }

    private TypeBody getTypeBodyByTypeID(int typeID) {
        TypeBody ty = null;
        for (TypeBody yb : typeBodies) {
            if (yb.getTypeID() == typeID) {
                ty = yb;
                break;
            }
        }
        return ty;
    }

    private void studyImage(List<YoloMessage> yoloMessageList) throws Exception {
        for (YoloMessage yoloMessage : yoloMessageList) {
            Map<Integer, Double> typeE = new HashMap<>();
            ThreeChannelMatrix small = yoloMessage.getPic();
            int mappingID = yoloMessage.getMappingID();
            typeE.put(mappingID, 1D);
            study(1, typeNerveManager.getSensoryNerves(), small, true, typeE, null);
            if (!yoloMessage.isBackGround()) {
                Map<Integer, Double> positionE = new HashMap<>();
                positionE.put(1, yoloMessage.getDistX());
                positionE.put(2, yoloMessage.getDistY());
                positionE.put(3, yoloMessage.getWidth());
                positionE.put(4, yoloMessage.getHeight());
                positionE.put(5, yoloMessage.getTrust());
                NerveManager position = yoloMessage.getTypeBody().getPositonNerveManager();
                study(1, position.getSensoryNerves(), small, true, positionE, null);
            }
        }

    }

    private void study(long eventID, List<SensoryNerve> sensoryNerves, ThreeChannelMatrix feature, boolean isStudy, Map<Integer, Double> E, OutBack back) throws Exception {
        for (int i = 0; i < sensoryNerves.size(); i++) {
            Matrix p;
            switch (i) {
                case 0:
                    p = feature.getMatrixR();
                    break;
                case 1:
                    p = feature.getMatrixG();
                    break;
                default:
                    p = feature.getMatrixB();
                    break;
            }
            sensoryNerves.get(i).postMatrixMessage(eventID, p, isStudy, E, back);
        }
    }
}
