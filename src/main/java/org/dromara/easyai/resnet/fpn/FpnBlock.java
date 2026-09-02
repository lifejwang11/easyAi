package org.dromara.easyai.resnet.fpn;

import org.dromara.easyai.batchNerve.BatchNerveManager;
import org.dromara.easyai.batchNerve.FeatureBody;
import org.dromara.easyai.config.FpnConfig;
import org.dromara.easyai.conv.ConvCount;
import org.dromara.easyai.conv.ConvResult;
import org.dromara.easyai.conv.DymStudy;
import org.dromara.easyai.entity.Box;
import org.dromara.easyai.function.ReLu;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.nerveEntity.ConvParameter;
import org.dromara.easyai.nerveEntity.ConvSize;
import org.dromara.easyai.resnet.ResBlock;
import org.dromara.easyai.resnet.entity.BatchBody;
import org.dromara.easyai.tools.NMS;
import org.dromara.easyai.yolo.OutBox;
import org.dromara.easyai.yolo.PositionBack;
import org.dromara.easyai.yolo.YoloTypeBack;

import java.util.*;

/**
 * @author lidapeng
 * @time 2026/8/26 08:35
 * @des fpn
 */
public class FpnBlock extends ConvCount {
    private final ConvParameter convParameter = new ConvParameter();//内存中卷积层模型及临时数据
    private final MatrixOperation matrixOperation = new MatrixOperation();
    private final BatchNerveManager typeManager;
    private final BatchNerveManager positionManager;
    private FpnBlock sonBlock;
    private FpnBlock fatherBlock;
    private final ReLu reLu = new ReLu();
    private final ResBlock resBlock;
    private final int channelNo;
    private final int deep;//深度
    private final int otherType;
    private FpnTag tag;
    private final float pth;//概率阈值
    private final float trustTh;//可信度阈值
    private final int batchSize;
    private int startX = 0;
    private int startY = 0;
    private int pictureIndex = 0;
    private final List<List<Matrix>> allErrorList = new ArrayList<>();
    private int pictureSize;
    private boolean firstPosition = true;
    private final float studyRate;
    private int xInput;
    private int yInput;
    private int times = 0;
    private DymStudy dymStudy;
    private final boolean first;
    private List<Matrix> gNextList;
    private final int imageSize;
    private final float iouTh;
    private final boolean showLog;


    public FpnBlock(int channelNo, ResBlock resBlock, int deep, BatchNerveManager typeManager
            , BatchNerveManager positionManager, FpnConfig fpnConfig, boolean first) throws Exception {
        super(null);
        this.first = first;
        showLog = fpnConfig.isShowLog();
        iouTh = fpnConfig.getIouTh();
        this.channelNo = channelNo;
        this.batchSize = fpnConfig.getBatchSize();
        imageSize = fpnConfig.getSize();
        pth = fpnConfig.getPth();
        trustTh = fpnConfig.getTrustTh();
        otherType = fpnConfig.getTypeNumber() + 1;
        studyRate = fpnConfig.getStudyRate();
        this.resBlock = resBlock;
        this.deep = deep;
        Random random = new Random();
        List<Matrix> upNeverMatrixList = convParameter.getUpNerveMatrixList();//上卷积采样权重
        convParameter.getUpDymStudyRateList().add(new Matrix(1, 9));
        convParameter.getUpDymStudyRate2List().add(new Matrix(1, 9));
        upNeverMatrixList.add(initUpNervePowerMatrix(random));
        initOnePower(channelNo, random);
        initMatrixPower(random);
        this.typeManager = typeManager;
        this.positionManager = positionManager;
    }

    public void insertModel(FpnBlockModel fpnBlockModel) {
        Matrix upNeverMatrix = convParameter.getUpNerveMatrixList().get(0);//上卷积采样权重
        Matrix downNerveMatrix = convParameter.getNerveMatrixList().get(0);//下卷积采样权重
        upNeverMatrix.insertMatrixModel(fpnBlockModel.getUpConvPowerList());
        downNerveMatrix.insertMatrixModel(fpnBlockModel.getDownConvPowerList());
        convParameter.setOneConvPower(fpnBlockModel.getOneConvListModel());
        typeManager.insertModel(fpnBlockModel.getTypeBatchNerveModel());
        positionManager.insertModel(fpnBlockModel.getPositionBatchNerveModel());
    }

    public FpnBlockModel getModel() {
        FpnBlockModel fpnBlockModel = new FpnBlockModel();
        Matrix upNeverMatrix = convParameter.getUpNerveMatrixList().get(0);//上卷积采样权重
        Matrix downNerveMatrix = convParameter.getNerveMatrixList().get(0);//下卷积采样权重
        List<List<Float>> oneConvPower = convParameter.getOneConvPower();
        fpnBlockModel.setUpConvPowerList(upNeverMatrix.getMatrixModel());
        fpnBlockModel.setDownConvPowerList(downNerveMatrix.getMatrixModel());
        fpnBlockModel.setOneConvListModel(oneConvPower);
        fpnBlockModel.setTypeBatchNerveModel(typeManager.getModel());
        fpnBlockModel.setPositionBatchNerveModel(positionManager.getModel());
        return fpnBlockModel;
    }

    public void sendMatrixList(List<BatchBody> batchBodies, boolean study, long eventID, OutBack outBack, boolean formFpn
            , Map<Integer, Float> pd, DymStudy dymStudy, boolean needFeature) throws Exception {
        this.dymStudy = dymStudy;
        times++;
        List<Matrix> allFeatures = new ArrayList<>();
        int size = batchBodies.size();
        pictureSize = size;
        if (formFpn) {
            upConvAndPoolingMany(batchBodies, convParameter, reLu, study);//完成一次上采样
            List<BatchBody> resBody = resBlock.getResBlockFeature(eventID);
            resBlock.removeResFeature(eventID);
            size = batchBodies.size();
            for (int i = 0; i < size; i++) {
                BatchBody batchBodyUp = batchBodies.get(i);
                BatchBody batchBodyDown = resBody.get(i);
                List<Matrix> upFeatures = batchBodyUp.getFeatureList();
                List<Matrix> downFeatures = batchBodyDown.getFeatureList();
                List<Matrix> addMatrixList = matrixOperation.addMatrixList(upFeatures, downFeatures);
                xInput = addMatrixList.get(0).getX();
                yInput = addMatrixList.get(0).getY();
                allFeatures.addAll(addMatrixList);
            }
        } else {
            for (int i = 0; i < size; i++) {
                List<Matrix> features = batchBodies.get(i).getFeatureList();
                xInput = features.get(0).getX();
                yInput = features.get(0).getY();
                allFeatures.addAll(features);
            }
        }
        ConvResult convResult = downConvCountMany(allFeatures, reLu, 3,
                convParameter.getNerveMatrixList().get(0), 1);
        List<Matrix> outMatrixList = convResult.getResultMatrixList();
        if (study) {
            List<Matrix> im2colMatrixList = convParameter.getIm2colMatrixList();
            List<Matrix> myOutMatrixList = convParameter.getOutMatrixList();
            im2colMatrixList.clear();
            myOutMatrixList.clear();
            myOutMatrixList.addAll(outMatrixList);
            im2colMatrixList.addAll(convResult.getLeftMatrixList());
        }
        for (int i = 0; i < size; i++) {//做特征拼接 准备送入检测头
            BatchBody batchBody = batchBodies.get(i);
            int startIndex = i * channelNo;
            int endIndex = startIndex + channelNo;
            List<Matrix> channelMatrixList = outMatrixList.subList(startIndex, endIndex);
            if (study) {
                FpnTag fpnTag = batchBody.getFpnTagMap().get(deep);//标注
                tag = fpnTag;
                insertFpnTag(channelMatrixList, fpnTag, eventID, outBack, pd);
            } else {
                insertFpnFeature(channelMatrixList, eventID, outBack, needFeature);
            }
        }
        //特征继续向上传
        if (sonBlock != null) {
            sonBlock.sendMatrixList(batchBodies, study, eventID, outBack, true, pd, dymStudy, needFeature);
        }
    }

    private void insertFpnFeature(List<Matrix> channelMatrix, long eventID, OutBack outBack, boolean needFeature) throws Exception {
        int x = channelMatrix.get(0).getX();
        int y = channelMatrix.get(0).getY();
        YoloTypeBack yoloTypeBack = new YoloTypeBack();
        PositionBack positionBack = new PositionBack();
        int step = imageSize / x;
        NMS nms = new NMS(iouTh);
        List<Box> boxes = new ArrayList<>();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                yoloTypeBack.clear();
                List<FeatureBody> features = new ArrayList<>();
                FeatureBody featureBody = new FeatureBody();
                featureBody.setFeature(getFeature(channelMatrix, i, j));
                features.add(featureBody);
                //推理发送给线性层
                typeManager.getInputBlock().postMessage(features, false, yoloTypeBack, eventID, null);
                int id = yoloTypeBack.getId();
                float out = yoloTypeBack.getOut();
                if (id < otherType && out > pth) {
                    positionManager.getInputBlock().postMessage(features, false, positionBack, eventID, null);
                    Box box = getBox(i * step, j * step, imageSize, positionBack, step, id);
                    if (box != null) {
                        boxes.add(box);
                    }
                }
            }
        }
        if (!boxes.isEmpty()) {
            List<Box> outBoxList = nms.start(boxes);
            List<OutBox> myOutBox = getOutBoxList(outBoxList);
            outBack.outBackBox(myOutBox, eventID, deep);
        }
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
            outBox.setSoftmax(box.getSoftMax());
            outBoxes.add(outBox);
        }
        return outBoxes;
    }

    private Box getBox(int i, int j, int max, PositionBack positionBack, int step, int type) {
        float maxSize = step * 2;
        Box box = null;
        float centerX = i - positionBack.getDistX() * maxSize;
        float centerY = j - positionBack.getDistY() * maxSize;
        int width = (int) (positionBack.getWidth() * maxSize);
        int height = (int) (positionBack.getHeight() * maxSize);
        int realX = (int) (centerX - height / 2f);
        int realY = (int) (centerY - width / 2f);
        if (realX < 0) {
            realX = 0;
        }
        if (realY < 0) {
            realY = 0;
        }
        if (realX + height > max) {
            realX = max - height;
        }
        if (realY + width > max) {
            realY = max - width;
        }
        float trust = positionBack.getTrust();
        if (trust > trustTh) {
            box = new Box();
            box.setX(realX);
            box.setY(realY);
            box.setxSize(height);
            box.setySize(width);
            box.setConfidence(trust);
            box.setTypeID(type);
        }
        return box;
    }

    protected void backByTypeLine(List<Matrix> nextErrorMatrixList) {
        Matrix typMatrix = tag.getTypeMatrix();
        int maxX = typMatrix.getX();
        for (Matrix error : nextErrorMatrixList) {
            insertError(error, maxX);
            updateIndex(maxX);
        }
    }

    protected void backByPositionLine(List<Matrix> nextErrorMatrixList) throws Exception {
        Matrix typMatrix = tag.getTypeMatrix();
        int maxX = typMatrix.getX();
        boolean finish = false;
        for (Matrix error : nextErrorMatrixList) {
            boolean last = updatePositionIndex();
            insertError(error, maxX);
            if (last) {
                startX = 0;
                startY = 0;
                firstPosition = true;
                pictureIndex++;
                if (pictureIndex == pictureSize) {
                    pictureIndex = 0;
                    //接收线性层误差完毕
                    finish = true;
                }
            }
        }
        if (finish) {
            backDownConv();
        }

    }

    private void backDownConv() throws Exception {
        List<Matrix> dymStudyRateList = convParameter.getDymStudyRateList();
        List<Matrix> dymStudyRate2List = convParameter.getDymStudyRate2List();
        List<Matrix> allError = new ArrayList<>();
        for (List<Matrix> matrixList : allErrorList) {
            allError.addAll(matrixList);
        }
        List<Matrix> im2colMatrixList = convParameter.getIm2colMatrixList();
        List<Matrix> myOutMatrixList = convParameter.getOutMatrixList();
        ConvResult convResult = backDownConvMany(allError, myOutMatrixList, reLu, im2colMatrixList, convParameter.getNerveMatrixList().get(0)
                , studyRate, 3, xInput, yInput, dymStudyRateList.get(0), dymStudyRate2List.get(0), dymStudy, times, 1);
        Matrix powerMatrix = convResult.getNervePowerMatrix();
        List<Matrix> gNextList = convResult.getResultMatrixList();
        convParameter.getNerveMatrixList().set(0, powerMatrix);
        if (fatherBlock != null) {
            resBlock.backErrorFpn(gNextList);
        }
        if (first) {//无需等待直接向下层返回误差
            if (fatherBlock != null) {
                List<Matrix> errorMatrixList = backUpAndPool(gNextList);
                fatherBlock.backErrorFromSon(errorMatrixList);
            } else {
                resBlock.backErrorFormFpn(gNextList);
            }
        } else {//需要先等待上层传回的误差
            this.gNextList = gNextList;
        }

    }

    List<Matrix> backUpAndPool(List<Matrix> gNextList) throws Exception {
        List<Matrix> errorList = getBackOneConvPool(gNextList, studyRate, dymStudy, times, convParameter, channelNo);
        return backUpConvMany(errorList, 3, convParameter, studyRate, reLu, dymStudy, times);
    }

    void backErrorFromSon(List<Matrix> gList) throws Exception {//接收上层传过来的误差
        List<Matrix> gMatrixList = matrixOperation.addMatrixList(gList, this.gNextList);
        if (fatherBlock != null) {
            List<Matrix> gNextList = backUpAndPool(gMatrixList);
            fatherBlock.backErrorFromSon(gNextList);
        } else {//走到最深层了 发送给resnet
            resBlock.backErrorFormFpn(gMatrixList);
        }
    }

    private boolean updatePositionIndex() {
        Matrix typMatrix = tag.getTypeMatrix();
        int x = typMatrix.getX();
        int y = typMatrix.getY();
        boolean first = true;
        boolean here = false;
        boolean last = false;
        for (int i = startX; i < x; i++) {
            for (int j = startY; j < y; j++) {
                if (typMatrix.getValue(i, j) > 0.5) {//有类别
                    if (firstPosition) {
                        startX = i;
                        startY = j;
                        firstPosition = false;
                        here = true;
                    } else if (first) {
                        first = false;
                    } else {
                        startX = i;
                        startY = j;
                        here = true;
                    }
                }
                if (i == x - 1 && j == y - 1) {//一张图处理结束
                    last = true;
                } else if (here) {
                    break;
                }
            }
            if (here) {
                break;
            }
        }
        return last;
    }

    private void updateIndex(int maX) {
        startY++;
        if (startY == maX) {
            startY = 0;
            startX++;
        }
        if (startX == maX) {
            startX = 0;
            startY = 0;
            pictureIndex++;
            if (pictureIndex == pictureSize) {
                pictureIndex = 0;
            }
        }
    }

    private void insertFpnTag(List<Matrix> channelMatrix, FpnTag fpnTag, long eventID, OutBack outBack
            , Map<Integer, Float> pd) throws Exception {
        int x = channelMatrix.get(0).getX();
        int y = channelMatrix.get(0).getY();
        Matrix typMatrix = fpnTag.getTypeMatrix();
        if (x != y || typMatrix.getX() != x) {
            throw new IllegalAccessException("fpn训练异常x:" + x + ",y:" + y);
        }
        List<FeatureBody> positionFeatures = new ArrayList<>();
        List<FeatureBody> typeFeatures = new ArrayList<>();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float type = typMatrix.getValue(i, j);
                FeatureBody typeFeature = new FeatureBody();
                Map<Integer, Float> typeE = new HashMap<>();
                Map<Integer, Float> positionE;
                Matrix feature = getFeature(channelMatrix, i, j);
                if (type > 0.5) {//是属于该层的类别id
                    FeatureBody positionFeature = new FeatureBody();
                    typeE.put((int) type, 1f);
                    positionE = getPositionE(i, j, fpnTag);
                    positionFeature.setE(positionE);
                    positionFeature.setFeature(feature);
                    positionFeatures.add(positionFeature);
                } else {//噪音
                    typeE.put(otherType, 1f);
                }
                typeFeature.setE(typeE);
                typeFeature.setFeature(feature);
                typeFeatures.add(typeFeature);
            }
        }
        sendLineStudy(typeFeatures, outBack, eventID, pd, typeManager);
        sendLineStudy(positionFeatures, outBack, eventID, pd, positionManager);
    }

    private void sendLineStudy(List<FeatureBody> features, OutBack outBack, long eventID, Map<Integer, Float> pd, BatchNerveManager manager) throws Exception {
        if (showLog) {
            System.out.println("deep:" + deep + "训练：");
        }
        int typeSize = features.size();
        int typeTimes = typeSize / batchSize;
        if (typeTimes > 0) {
            for (int i = 0; i < typeTimes; i++) {
                int startIndex = i * batchSize;
                int endIndex = startIndex + batchSize;
                manager.getInputBlock().postMessage(features.subList(startIndex, endIndex), true, outBack, eventID, pd);
            }
            int sub = typeSize % batchSize;
            if (sub > 0) {
                int startIndex = typeTimes * batchSize;
                int endIndex = features.size();
                manager.getInputBlock().postMessage(features.subList(startIndex, endIndex), true, outBack, eventID, pd);
            }
        } else if (typeSize > 0) {//一次性全部发送
            //发送线性层
            manager.getInputBlock().postMessage(features, true, outBack, eventID, pd);
        }
    }

    private Matrix getFeature(List<Matrix> channelMatrixList, int x, int y) {
        int size = channelMatrixList.size();
        Matrix feature = new Matrix(1, size);
        for (int i = 0; i < size; i++) {
            float value = channelMatrixList.get(i).getValue(x, y);
            feature.setValue(0, i, value);
        }
        return feature;
    }

    private void insertError(Matrix error, int maxSize) {
        List<Matrix> channelErrors;
        if (allErrorList.size() == pictureIndex) {//集合里面是空的
            channelErrors = new ArrayList<>();
            for (int i = 0; i < channelNo; i++) {
                Matrix matrix = new Matrix(maxSize, maxSize);
                channelErrors.add(matrix);
            }
            allErrorList.add(channelErrors);
        } else {
            channelErrors = allErrorList.get(pictureIndex);
        }
        int size = error.getY();
        for (int i = 0; i < size; i++) {
            Matrix myError = channelErrors.get(i);
            float value = error.getValue(0, i);
            float v = myError.getValue(startX, startY);
            myError.setValue(startX, startY, value + v);
        }
    }

    private Map<Integer, Float> getPositionE(int i, int j, FpnTag fpnTag) {
        Map<Integer, Float> e = new HashMap<>();
        float width = fpnTag.getWidthMatrix().getValue(i, j);
        float height = fpnTag.getHeightMatrix().getValue(i, j);
        float distX = fpnTag.getDistXMatrix().getValue(i, j);
        float distY = fpnTag.getDistYMatrix().getValue(i, j);
        float trust = fpnTag.getTrustMatrix().getValue(i, j);
        e.put(1, distX);
        e.put(2, distY);
        e.put(3, width);
        e.put(4, height);
        e.put(5, trust);
        return e;
    }

    private void initOnePower(int channelNo, Random random) {
        List<List<Float>> oneConvPower = new ArrayList<>();
        List<List<Float>> oneDymStudy1 = new ArrayList<>();
        List<List<Float>> oneDymStudy2 = new ArrayList<>();
        int downNo = channelNo * 2;
        float sh = (float) Math.sqrt(downNo);
        for (int i = 0; i < channelNo; i++) {
            List<Float> power = new ArrayList<>();
            List<Float> study1 = new ArrayList<>();
            List<Float> study2 = new ArrayList<>();
            for (int j = 0; j < downNo; j++) {
                power.add(random.nextFloat() / sh);
                study1.add(0f);
                study2.add(0f);
            }
            oneConvPower.add(power);
            oneDymStudy1.add(study1);
            oneDymStudy2.add(study2);
        }
        convParameter.setOneConvPower(oneConvPower);
        convParameter.setOneDymStudyRateList(oneDymStudy1);
        convParameter.setOneDymStudyRate2List(oneDymStudy2);
    }

    private void initMatrixPower(Random random) throws Exception {
        List<Matrix> nerveMatrixList = convParameter.getNerveMatrixList();//一层当中所有的深度卷积核
        List<Matrix> dymStudyRateList = convParameter.getDymStudyRateList();
        List<Matrix> dymStudyRate2List = convParameter.getDymStudyRate2List();
        List<ConvSize> convSizeList = convParameter.getConvSizeList();
        Matrix nerveMatrix = new Matrix(9, 1);//一组通道创建一组卷积核
        convSizeList.add(new ConvSize());
        for (int i = 0; i < nerveMatrix.getX(); i++) {//初始化深度卷积核权重
            float nub = random.nextFloat() / 3;
            nerveMatrix.setNub(i, 0, nub);
        }
        nerveMatrixList.add(nerveMatrix);
        dymStudyRateList.add(new Matrix(9, 1));
        dymStudyRate2List.add(new Matrix(9, 1));
    }

    private Matrix initUpNervePowerMatrix(Random random) throws Exception {
        int convSize = 9;
        Matrix nervePowerMatrix = new Matrix(1, convSize);
        for (int j = 0; j < convSize; j++) {
            float power = random.nextFloat() / 3;
            nervePowerMatrix.setNub(0, j, power);
        }
        return nervePowerMatrix;
    }

    public void setSonBlock(FpnBlock sonBlock) {
        this.sonBlock = sonBlock;
    }

    public void setFatherBlock(FpnBlock fatherBlock) {
        this.fatherBlock = fatherBlock;
    }
}
