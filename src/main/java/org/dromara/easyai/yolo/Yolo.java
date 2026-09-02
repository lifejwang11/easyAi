package org.dromara.easyai.yolo;

import org.dromara.easyai.config.FpnConfig;
import org.dromara.easyai.config.ResnetConfig;
import org.dromara.easyai.entity.Box;
import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.function.ReLu;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.resnet.ResnetManager;
import org.dromara.easyai.resnet.entity.BatchBody;
import org.dromara.easyai.resnet.entity.ResnetModel;
import org.dromara.easyai.resnet.fpn.FpnTag;
import org.dromara.easyai.tools.NMS;
import org.dromara.easyai.tools.Picture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @time 2026/9/1 17:07
 * @des 传统yolo目标检测
 */
public class Yolo {
    private final ResnetManager resnetManager;
    private final int startDeep;
    private final int allDeep;
    private final int imageSize;
    private final int batchSize;
    private final float containIouTh;//是否包含样本交并比阈值
    private final Map<Integer, Integer> mappingID = new HashMap<>();
    private final Map<Integer, Float> pd = new HashMap<>();

    public Yolo(YoloFpnConfig yoloFpnConfig, ResnetConfig resnetConfig) throws Exception {
        FpnConfig fpnConfig = new FpnConfig();
        fpnConfig.setPth(yoloFpnConfig.getPth());
        fpnConfig.setTrustTh(yoloFpnConfig.getTrustTh());
        fpnConfig.setIouTh(yoloFpnConfig.getIouTh());
        fpnConfig.setBatchSize(yoloFpnConfig.getFpnBatchSize());
        fpnConfig.setStartDeep(yoloFpnConfig.getStartDeep());
        resnetConfig.setSize(yoloFpnConfig.getSize());
        resnetConfig.setFpn(true);
        resnetConfig.setTypeNumber(yoloFpnConfig.getTypeNumber());
        containIouTh = yoloFpnConfig.getContainIouTh();
        resnetManager = new ResnetManager(resnetConfig, fpnConfig, new ReLu());
        startDeep = yoloFpnConfig.getStartDeep();
        allDeep = resnetManager.getDeep();
        imageSize = yoloFpnConfig.getSize();
        batchSize = resnetConfig.getBatchSize();
        float myPd = yoloFpnConfig.getBackGroundPD();
        if (myPd < 0.9 && myPd > 0) {
            pd.put(yoloFpnConfig.getTypeNumber() + 1, myPd);
        }
    }

    private int getRealTypeID(int mapID) {
        int k = -1;
        for (Map.Entry<Integer, Integer> entry : mappingID.entrySet()) {
            int key = entry.getKey();
            if (entry.getValue() == mapID) {
                k = key;
                break;
            }
        }
        return k;
    }

    public Map<Integer, List<OutBox>> look(ThreeChannelMatrix pic, long eventID) throws Exception {
        if (pic.getX() == pic.getY() && pic.getX() == imageSize) {
            FpnOut fpnOut = new FpnOut();
            resnetManager.getRestNetInput().postFeature(pic, fpnOut, eventID, false, null);
            Map<Integer, List<OutBox>> outMap = fpnOut.getOutMap();
            for (Map.Entry<Integer, List<OutBox>> entry : outMap.entrySet()) {
                List<OutBox> outBoxes = entry.getValue();
                for (OutBox outBox : outBoxes) {
                    int type = Integer.parseInt(outBox.getTypeID());
                    int realID = getRealTypeID(type);
                    outBox.setTypeID(String.valueOf(realID));
                }
            }
            return fpnOut.getOutMap();
        } else {
            throw new IllegalAccessException("使用本类，图像必须为正方形（可以通过填充短边处理），且大小必须为配置的指定尺寸：" + imageSize);
        }
    }

    private List<MappingIDBody> getMappingModel() {
        List<MappingIDBody> mappingIDBodies = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : mappingID.entrySet()) {
            MappingIDBody mappingIDBody = new MappingIDBody();
            mappingIDBody.setId(entry.getKey());
            mappingIDBody.setMappingID(entry.getValue());
            mappingIDBodies.add(mappingIDBody);
        }
        return mappingIDBodies;
    }

    public void insertModel(MyYoloModel myYoloModel) {
        List<MappingIDBody> mappingIDBodyList = myYoloModel.getMappingIDBodyList();
        for (MappingIDBody mappingIDBody : mappingIDBodyList) {
            mappingID.put(mappingIDBody.getId(), mappingIDBody.getMappingID());
        }
        resnetManager.insertModel(myYoloModel.getResnetModel());
    }

    public MyYoloModel study(List<YoloSample> yoloSamples, OutBack logOutBack, int studyTimes) throws Exception {
        List<Integer> sizeList = resnetManager.calcStageOutputSizes(imageSize, allDeep).subList(startDeep - 1, allDeep);
        int size = yoloSamples.size();
        int times = size / batchSize;//训练批次
        for (int t = 0; t < studyTimes; t++) {
            for (int i = 0; i < times; i++) {
                int startIndex = i * batchSize;
                int endIndex = startIndex + batchSize;
                List<YoloSample> sampleList = yoloSamples.subList(startIndex, endIndex);
                List<BatchBody> batchBodies = new ArrayList<>();
                for (YoloSample yoloSample : sampleList) {
                    ThreeChannelMatrix pic = Picture.getThreeMatrix(yoloSample.getLocationURL(), false);
                    Map<Integer, FpnTag> fpnTagMap = assignScale(yoloSample, sizeList, pic);
                    BatchBody batchBody = new BatchBody();
                    batchBody.insertPicture(pic);
                    batchBody.setFpnTagMap(fpnTagMap);
                    batchBodies.add(batchBody);
                }
                resnetManager.getRestNetInput().studyFeature(batchBodies, logOutBack, 1, pd);
            }
        }
        MyYoloModel myYoloModel = new MyYoloModel();
        myYoloModel.setMappingIDBodyList(getMappingModel());
        myYoloModel.setResnetModel(resnetManager.getModel());
        return myYoloModel;
    }

    private Map<Integer, FpnTag> assignScale(YoloSample yoloSample, List<Integer> sizeList, ThreeChannelMatrix temp) throws Exception {//分配尺度
        if (temp.getX() == temp.getY() && temp.getX() == imageSize) {
            List<YoloBody> yoloBodies = yoloSample.getYoloBodies();
            List<Box> boxes = getCheckBox(yoloBodies);
            int size = sizeList.size();
            Map<Integer, FpnTag> fpnTagMap = new HashMap<>();
            for (int i = 0; i < size; i++) {
                fpnTagMap.put(i + startDeep, initFpn(sizeList.get(i)));
                scale(boxes, sizeList.get(i), i + startDeep);
            }
            for (int i = 0; i < size; i++) {
                multiScale(boxes, sizeList.get(i), fpnTagMap.get(i + startDeep), i + startDeep);
            }
            return fpnTagMap;
        } else {
            throw new IllegalAccessException("使用本类，图像必须为正方形（可以通过填充短边处理），且大小必须为配置的指定尺寸：" + imageSize);
        }
    }

    private FpnTag initFpn(int size) {
        int step = imageSize / size;
        FpnTag fpnTag = new FpnTag();
        fpnTag.setTypeMatrix(new Matrix(step, step));
        fpnTag.setDistXMatrix(new Matrix(step, step));
        fpnTag.setDistYMatrix(new Matrix(step, step));
        fpnTag.setWidthMatrix(new Matrix(step, step));
        fpnTag.setHeightMatrix(new Matrix(step, step));
        fpnTag.setTrustMatrix(new Matrix(step, step));
        return fpnTag;
    }

    private void multiScale(List<Box> boxes, int size, FpnTag fpnTag, int deep) throws Exception {
        int step = imageSize / size;
        int checkSize = imageSize - step;
        NMS nms = new NMS(0.01f);
        for (int i = 0; i <= checkSize; i += step) {
            for (int j = 0; j <= checkSize; j += step) {
                Box box = new Box();
                box.setX(i);
                box.setY(j);
                box.setxSize(step);
                box.setySize(step);
                insertMap(box, boxes, nms, fpnTag, deep);
            }
        }
    }

    private void insertMap(Box box, List<Box> boxes, NMS nms, FpnTag fpnTag, int deep) throws Exception {
        float maxIOU = -1;
        Box rightBox = null;
        int step = box.getxSize();
        float maxSize = step * 2;
        for (Box testBox : boxes) {
            if (testBox.getDeep() == deep) {
                float iou = nms.getSRatio(box, testBox, false);
                if (iou > containIouTh && iou > maxIOU) {
                    maxIOU = iou;
                    rightBox = testBox;
                }
            }
        }
        int x = box.getX();
        int y = box.getY();
        if (rightBox != null) {//有命中
            int type = rightBox.getTypeID();//类别id
            float width = rightBox.getySize() / maxSize;
            float height = rightBox.getxSize() / maxSize;
            int centerX = rightBox.getX() + rightBox.getxSize() / 2;
            int centerY = rightBox.getY() + rightBox.getySize() / 2;
            float distX = (x - centerX) / maxSize;
            float distY = (y - centerY) / maxSize;
            float trust = 0;
            if (centerX >= x && centerX <= (x + step) && centerY >= y && centerY <= (y + step)) {
                trust = 1;
            }
            fpnTag.getTypeMatrix().setNub(x, y, type);
            fpnTag.getWidthMatrix().setNub(x, y, width);
            fpnTag.getHeightMatrix().setNub(x, y, height);
            fpnTag.getDistXMatrix().setNub(x, y, distX);
            fpnTag.getDistYMatrix().setNub(x, y, distY);
            fpnTag.getTrustMatrix().setNub(x, y, trust);
        }
    }

    private List<Box> getCheckBox(List<YoloBody> yoloBodies) {
        List<Box> boxes = new ArrayList<>();
        for (YoloBody yoloBody : yoloBodies) {
            int id = yoloBody.getTypeID();
            int mapID;
            if (mappingID.containsKey(id)) {
                mapID = mappingID.get(id);
            } else {
                mapID = mappingID.size() + 1;
                mappingID.put(id, mapID);
            }
            Box box = new Box();
            box.setX(yoloBody.getY());
            box.setY(yoloBody.getX());
            box.setxSize(yoloBody.getHeight());
            box.setySize(yoloBody.getWidth());
            box.setTypeID(mapID);
            box.setMaxIOU(0);
            boxes.add(box);
        }
        return boxes;
    }

    private void scale(List<Box> boxes, int size, int deep) {
        List<Box> testBoxes = new ArrayList<>();
        int step = imageSize / size;
        int checkSize = imageSize - step;
        NMS nms = new NMS(0.01f);
        for (int i = 0; i <= checkSize; i += step) {
            for (int j = 0; j <= checkSize; j += step) {
                Box box = new Box();
                box.setX(i);
                box.setY(j);
                box.setxSize(step);
                box.setySize(step);
                testBoxes.add(box);
            }
        }
        for (Box box : boxes) {
            float maxIOU = box.getMaxIOU();
            float n = -1;
            for (Box testBox : testBoxes) {
                float iou = nms.getIOU(box, testBox);
                if (iou > maxIOU && iou > n) {
                    n = iou;
                }
            }
            if (n > maxIOU) {
                box.setMaxIOU(n);
                box.setDeep(deep);
            }
        }
    }


}
