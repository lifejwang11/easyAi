package org.dromara.easyai.resnet;

import org.dromara.easyai.batchNerve.BatchInputBlock;
import org.dromara.easyai.batchNerve.FeatureBody;
import org.dromara.easyai.conv.DymStudy;
import org.dromara.easyai.conv.ResConvCount;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixNorm;
import org.dromara.easyai.resnet.entity.BatchBody;
import org.dromara.easyai.resnet.entity.ResBlockModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author lidapeng
 * @time 2025/4/11 13:40
 * @des resnet残差模块
 */
public class ResBlock extends ResConvCount {
    private ConvLay firstConvPower;//第一层卷积
    private final ResConvPower firstResConvPower = new ResConvPower();
    private final ResConvPower secondResConvPower = new ResConvPower();
    private final int channelNo;
    private final int deep;
    private final float studyRate;//全局学习率
    private final int imageSize;//图像大小
    private ResBlock fatherResBlock;
    private ResBlock sonResBlock;
    private final BatchInputBlock inputBlock;//输出神经元
    private final float gaMa;
    private final float gMaxTh;
    private final boolean auto;
    private final int batchSize;

    public ResBlockModel getModel() {
        ResBlockModel model = new ResBlockModel();
        model.setFirstResConvModel(firstResConvPower.getModel());
        model.setSecondResConvModel(secondResConvPower.getModel());
        if (deep == 1) {
            model.setFirstConvModel(firstConvPower.getModel());
        }
        return model;
    }

    public void insertModel(ResBlockModel resBlockModel) {
        firstResConvPower.insertModel(resBlockModel.getFirstResConvModel());
        secondResConvPower.insertModel(resBlockModel.getSecondResConvModel());
        if (deep == 1) {
            firstConvPower.insertModel(resBlockModel.getFirstConvModel());
        }
    }

    public ResBlock(int channelNo, int deep, float studyRate, int imageSize, BatchInputBlock inputBlock, float gaMa, float gMaxTh
            , boolean auto, int batchSize) throws Exception {
        this.imageSize = imageSize;
        this.batchSize = batchSize;
        this.inputBlock = inputBlock;
        this.channelNo = channelNo;
        this.deep = deep;
        this.gMaxTh = gMaxTh;
        this.studyRate = studyRate;
        this.auto = auto;
        this.gaMa = gaMa;
        boolean initOneConv = true;
        Random random = new Random();
        if (deep == 1) {
            initOneConv = false;
            firstConvPower = initMatrixPower(random, 7, channelNo, true);
        }
        initBlock(firstResConvPower, random, initOneConv);//初始化两个残差块
        initBlock(secondResConvPower, random, false);//初始化两个残差块
    }

    private void fillZero(List<Matrix> matrixList, boolean fill) throws Exception {
        int size = matrixList.size();
        for (int i = 0; i < size; i++) {
            Matrix matrix = matrixList.get(i);
            if (fill) {
                matrixList.set(i, padding2(matrix, 1));
            } else {
                matrixList.set(i, unPadding2(matrix, 1));
            }
        }
    }

    public void backError(List<BatchBody> batchBodies) throws Exception {//返回误差
        List<BatchBody> errorList = backOneConvMatrix(batchBodies, secondResConvPower, 2);
        if (deep > 1) {
            List<BatchBody> errorFinalMatrix = backOneConvMatrix(errorList, firstResConvPower, 1);
            backFatherError(errorFinalMatrix);
        } else {
            List<BatchBody> errorFinalMatrix = backOneConvMatrix(errorList, firstResConvPower, 2);
            for (BatchBody batchBody : errorFinalMatrix) {
                List<Matrix> errorMatrix = backDownPoolingByList(batchBody.getFeatureList());
                batchBody.setFeatureList(errorMatrix);
            }
            if (fill(deep, imageSize, false)) {//判断是否需要补0
                for (BatchBody batchBody : errorFinalMatrix) {
                    fillZero(batchBody.getFeatureList(), false);
                }
            }
            DymStudy dymStudy = new DymStudy(gaMa, gMaxTh, auto);
            ResBlockError(errorFinalMatrix, firstConvPower.getBackParameterList(), firstConvPower.getMatrixNormList(),
                    firstConvPower.getConvPower(), studyRate, 7, firstConvPower.getDymStudyRateList(),
                    dymStudy);
        }
    }

    private void backFatherError(List<BatchBody> errobatchList) throws Exception {//返回上一层误差
        if (fill(deep, imageSize, true)) {//不是偶数,要补0
            for (BatchBody batchBody : errobatchList) {
                fillZero(batchBody.getFeatureList(), false);
            }
        }
        fatherResBlock.backError(errobatchList);
    }

    private List<BatchBody> backOneConvMatrix(List<BatchBody> batchBodies, ResConvPower resConvPower, int deep) throws Exception {
        ConvLay firstConv = resConvPower.getFirstConvPower();
        ConvLay secondConv = resConvPower.getSecondConvPower();
        List<List<Float>> oneConvPower = null;
        List<List<Float>> dymStudyRateList = null;
        if (deep == 1) {
            oneConvPower = resConvPower.getOneConvPower();
            dymStudyRateList = resConvPower.getDymStudyRateList();
        }
        DymStudy dymStudy = new DymStudy(gaMa, gMaxTh, auto);
        List<BatchBody> nextBatchBodies = ResBlockError2(batchBodies, secondConv.getBackParameterList(), secondConv.getMatrixNormList(),
                secondConv.getConvPower(), studyRate, true, oneConvPower, secondConv.getDymStudyRateList()
                , dymStudyRateList, dymStudy);
        List<BatchBody> errorList;
        if (deep == 2) {
            errorList = ResBlockError2(nextBatchBodies, firstConv.getBackParameterList(), firstConv.getMatrixNormList(),
                    firstConv.getConvPower(), studyRate, false, oneConvPower,
                    firstConv.getDymStudyRateList(), dymStudyRateList, dymStudy);
        } else {
            errorList = ResBlockError(nextBatchBodies, firstConv.getBackParameterList(), firstConv.getMatrixNormList(),
                    firstConv.getConvPower(), studyRate, 3, firstConv.getDymStudyRateList(), dymStudy);
        }
        return errorList;
    }

    private void convMatrix(List<BatchBody> batchBodies, int step, boolean study, OutBack outBack,
                            long eventID, boolean outFeature, Map<Integer, Float> pd) throws Exception {// feature 准备跳层用
        boolean one = step == 1;
        oneConvMatrix(batchBodies, firstResConvPower, study, one);
        oneConvMatrix(batchBodies, secondResConvPower, study, true);
        if (sonResBlock != null) {
            sonResBlock.sendMatrixList(batchBodies, outBack, study, eventID, outFeature, pd);
        } else {//最后卷积层了，求平均值
            List<FeatureBody> featureBodies = new ArrayList<>();
            for (BatchBody batchBody : batchBodies) {
                FeatureBody featureBody = getFeatureBody(batchBody);
                featureBodies.add(featureBody);
            }
            if (!study && outFeature) {
                if (outBack != null) {
                    outBack.getBackMatrix(featureBodies.get(0).getFeature(), 1, eventID);
                } else {
                    throw new Exception("没有传入OutBack输出回调类");
                }
            } else {
                inputBlock.postMessage(featureBodies, study, outBack, eventID, pd);
            }
        }
    }

    private FeatureBody getFeatureBody(BatchBody batchBody) throws Exception {
        List<Matrix> lastFeatureList = batchBody.getFeatureList();
        FeatureBody featureBody = new FeatureBody();
        Matrix featureMatrix = new Matrix(1, lastFeatureList.size());
        for (int j = 0; j < lastFeatureList.size(); j++) {
            Matrix matrix = lastFeatureList.get(j);
            featureMatrix.setNub(0, j, matrix.getAVG());
        }
        featureBody.setFeature(featureMatrix);
        featureBody.setE(batchBody.getE());
        return featureBody;
    }

    private List<BatchBody> copyFeature(List<BatchBody> batchBodies) throws Exception {
        List<BatchBody> copyBatchBodies = new ArrayList<>();
        for (BatchBody batchBody : batchBodies) {
            List<Matrix> features = batchBody.getFeatureList();
            List<Matrix> copyFeatures = new ArrayList<>();
            BatchBody copyBatchBody = new BatchBody();
            for (Matrix matrix : features) {
                copyFeatures.add(matrix.copy());
            }
            copyBatchBody.setFeatureList(copyFeatures);
            copyBatchBodies.add(copyBatchBody);
        }
        return copyBatchBodies;
    }

    private void oneConvMatrix(List<BatchBody> batchBodies, ResConvPower resConvPower, boolean study, boolean one) throws Exception {
        List<BatchBody> copyBatchBody = copyFeature(batchBodies);
        ConvLay firstConvLay = resConvPower.getFirstConvPower();
        ConvLay secondConvLay = resConvPower.getSecondConvPower();
        List<List<Float>> oneConvPower = resConvPower.getOneConvPower();
        if (one) {//步长为1
            downConvMany2(batchBodies, firstConvLay.getConvPower(), study, firstConvLay.getBackParameterList(),
                    firstConvLay.getMatrixNormList(), null, null);
        } else {//步长为2
            downConvMany(batchBodies, firstConvLay.getConvPower(), 3, study, firstConvLay.getBackParameterList(),
                    firstConvLay.getMatrixNormList());
        }
        downConvMany2(batchBodies, secondConvLay.getConvPower(), study, secondConvLay.getBackParameterList()
                , secondConvLay.getMatrixNormList(), copyBatchBody, oneConvPower);
    }

    public void sendMatrixList(List<BatchBody> batchBodies, OutBack outBack, boolean study, long eventID,
                               boolean outFeature, Map<Integer, Float> pd) throws Exception {
        //判定特征大小是否为偶数
        if (fill(deep, imageSize, true)) {//不是偶数,要补0
            for (BatchBody batchBody : batchBodies) {
                fillZero(batchBody.getFeatureList(), true);
            }
        }
        if (deep == 1) {
            downConvMany(batchBodies, firstConvPower.getConvPower(), 7, study, firstConvPower.getBackParameterList(),
                    firstConvPower.getMatrixNormList());
            if (fill(deep, imageSize, false)) {//池化需要补0
                for (BatchBody batchBody : batchBodies) {
                    fillZero(batchBody.getFeatureList(), true);
                }
            }
            //池化
            for (BatchBody batchBody : batchBodies) {
                List<Matrix> nextMatrixList = new ArrayList<>();
                List<Matrix> myMatrixList = batchBody.getFeatureList();
                for (Matrix matrix : myMatrixList) {
                    nextMatrixList.add(downPooling(matrix));
                }
                batchBody.setFeatureList(nextMatrixList);
            }
            convMatrix(batchBodies, 1, study, outBack, eventID, outFeature, pd);
        } else {
            convMatrix(batchBodies, 2, study, outBack, eventID, outFeature, pd);
        }

    }

    private int getChannelNo() {
        return (int) (channelNo * Math.pow(2, deep - 1));//卷积层输出特征大小
    }

    private void initBlock(ResConvPower resConvPower, Random random, boolean initOneConv) throws Exception {
        resConvPower.setFirstConvPower(initMatrixPower(random, 3, getChannelNo(), false));
        resConvPower.setSecondConvPower(initMatrixPower(random, 3, getChannelNo(), false));
        if (deep > 1 && initOneConv) {//初始化11卷积层
            int featureLength = getChannelNo();//卷积层输出特征大小
            List<List<Float>> onePowers = new ArrayList<>();
            List<List<Float>> dymStudyRateList = new ArrayList<>();
            resConvPower.setOneConvPower(onePowers);
            resConvPower.setDymStudyRateList(dymStudyRateList);
            int length = featureLength / 2;
            for (int i = 0; i < featureLength; i++) {
                List<Float> oneConvPowerList = new ArrayList<>();
                List<Float> dymStudyRage = new ArrayList<>();
                for (int j = 0; j < length; j++) {
                    oneConvPowerList.add(random.nextFloat() / length);
                    dymStudyRage.add(0f);
                }
                dymStudyRateList.add(dymStudyRage);
                onePowers.add(oneConvPowerList);
            }
        }
    }

    private ConvLay initMatrixPower(Random random, int kernLen, int channelNo, boolean seven) throws Exception {
        int nerveNub = kernLen * kernLen;
        ConvLay convLay = new ConvLay(batchSize);
        List<Matrix> nerveMatrixList = new ArrayList<>();//一层当中所有的深度卷积核
        List<Matrix> sumOfSquares = new ArrayList<>();//动态学习率
        List<MatrixNorm> matrixNormList = new ArrayList<>();
        int size = getFeatureSize(deep, imageSize, seven);
        for (int k = 0; k < channelNo; k++) {//遍历通道
            Matrix nerveMatrix = new Matrix(nerveNub, 1);//一组通道创建一组卷积核
            Matrix dymStudyRate = new Matrix(nerveNub, 1);
            sumOfSquares.add(dymStudyRate);
            for (int i = 0; i < nerveMatrix.getX(); i++) {//初始化深度卷积核权重
                float nub = random.nextFloat() / kernLen;
                nerveMatrix.setNub(i, 0, nub);
            }
            nerveMatrixList.add(nerveMatrix);
            MatrixNorm matrixNorm = new MatrixNorm(size, studyRate, gaMa, gMaxTh, auto);
            matrixNormList.add(matrixNorm);
        }
        convLay.setDymStudyRateList(sumOfSquares);
        convLay.setConvPower(nerveMatrixList);
        convLay.setMatrixNormList(matrixNormList);
        return convLay;
    }

    public void setSonResBlock(ResBlock sonResBlock) {
        this.sonResBlock = sonResBlock;
    }

    public void setFatherResBlock(ResBlock fatherResBlock) {
        this.fatherResBlock = fatherResBlock;
    }
}
