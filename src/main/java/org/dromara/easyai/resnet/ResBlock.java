package org.dromara.easyai.resnet;

import org.dromara.easyai.conv.ResConvCount;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixNorm;
import org.dromara.easyai.nerveEntity.SensoryNerve;

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
    private final ResConvPower fistResConvPower = new ResConvPower();
    private final ResConvPower secondResConvPower = new ResConvPower();
    private final int channelNo;
    private final int deep;
    private final float studyRate;//全局学习率
    private final int imageSize;//图像大小
    private ResBlock fatherResBlock;
    private ResBlock sonResBlock;
    private final List<SensoryNerve> sensoryNerves;//输出神经元

    public ResBlock(int channelNo, int deep, float studyRate, int imageSize, List<SensoryNerve> sensoryNerves) throws Exception {
        this.imageSize = imageSize;
        this.sensoryNerves = sensoryNerves;
        this.channelNo = channelNo;
        this.deep = deep;
        this.studyRate = studyRate;
        boolean initOneConv = true;
        Random random = new Random();
        if (deep == 1) {
            initOneConv = false;
            firstConvPower = initMatrixPower(random, 7, channelNo, true);
        }
        initBlock(fistResConvPower, random, initOneConv);//初始化两个残差块
        initBlock(secondResConvPower, random, false);//初始化两个残差块
    }

    private void fillZero(List<Matrix> matrixList) throws Exception {
        int size = matrixList.size();
        for (int i = 0; i < size; i++) {
            Matrix matrix = matrixList.get(i);
            matrixList.set(i, padding2(matrix, 1));
        }
    }

    public void backError(List<Matrix> errorMatrixList) throws Exception {//返回误差

    }

    private void convMatrix(List<Matrix> feature, int step, boolean study, OutBack outBack, Map<Integer, Float> E, long eventID) throws Exception {// feature 准备跳层用
        boolean one = step == 1;
        List<Matrix> featureList = oneConvMatrix(feature, fistResConvPower, study, one);
        List<Matrix> lastFeatureList = oneConvMatrix(featureList, secondResConvPower, study, true);
        if (sonResBlock != null) {
            sonResBlock.sendMatrixList(lastFeatureList, outBack, study, E, eventID);
        } else {//最后卷积层了，求平均值
            List<Float> outFeatures = new ArrayList<>();
            for (Matrix matrix : lastFeatureList) {
                outFeatures.add(matrix.getAVG());
            }
            if (sensoryNerves.size() == outFeatures.size()) {
                int size = sensoryNerves.size();
                for (int i = 0; i < size; i++) {
                    sensoryNerves.get(i).postMessage(eventID, outFeatures.get(i), study, E, outBack);
                }
            } else {
                throw new Exception("线性层与特征层特征维度不相等");
            }
        }
    }

    private List<Matrix> oneConvMatrix(List<Matrix> feature, ResConvPower resConvPower, boolean study, boolean one) throws Exception {
        ConvLay firstConvLay = resConvPower.getFirstConvPower();
        ConvLay secondConvLay = resConvPower.getSecondConvPower();
        List<List<Float>> oneConvPower = resConvPower.getOneConvPower();
        List<Matrix> firstOutMatrix;
        if (one) {//步长为1
            firstOutMatrix = downConvMany2(feature, firstConvLay.getConvPower(), study, firstConvLay.getBackParameter(),
                    firstConvLay.getMatrixNormList(), null, null);
        } else {//步长为2
            firstOutMatrix = downConvMany(feature, firstConvLay.getConvPower(), 3, study, firstConvLay.getBackParameter(),
                    firstConvLay.getMatrixNormList());
        }
        return downConvMany2(firstOutMatrix, secondConvLay.getConvPower(), study, secondConvLay.getBackParameter()
                , secondConvLay.getMatrixNormList(), feature, oneConvPower);
    }

    public void sendMatrixList(List<Matrix> matrixList, OutBack outBack, boolean study, Map<Integer, Float> E, long eventID) throws Exception {
        //判定特征大小是否为偶数
        if (fill(deep, imageSize, true)) {//不是偶数,要补0
            fillZero(matrixList);
        }
        if (deep == 1) {
            List<Matrix> myMatrixList = downConvMany(matrixList, firstConvPower.getConvPower(), 7, study, firstConvPower.getBackParameter(),
                    firstConvPower.getMatrixNormList());
            if (fill(deep, imageSize, false)) {//池化需要补0
                fillZero(myMatrixList);
            }
            //池化
            List<Matrix> nextMatrixList = new ArrayList<>();
            for (Matrix matrix : myMatrixList) {
                nextMatrixList.add(downPooling(matrix));
            }
            convMatrix(nextMatrixList, 1, study, outBack, E, eventID);
        } else {
            convMatrix(matrixList, 2, study, outBack, E, eventID);
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
            resConvPower.setOneConvPower(onePowers);
            int length = featureLength / 2;
            for (int i = 0; i < featureLength; i++) {
                List<Float> oneConvPowerList = new ArrayList<>();
                for (int j = 0; j < length; j++) {
                    oneConvPowerList.add(random.nextFloat() / length);
                }
                onePowers.add(oneConvPowerList);
            }
        }
    }

    private ConvLay initMatrixPower(Random random, int kernLen, int channelNo, boolean seven) throws Exception {
        int nerveNub = kernLen * kernLen;
        ConvLay convLay = new ConvLay();
        List<Matrix> nerveMatrixList = new ArrayList<>();//一层当中所有的深度卷积核
        List<MatrixNorm> matrixNormList = new ArrayList<>();
        int size = getFeatureSize(deep, imageSize, seven);
        for (int k = 0; k < channelNo; k++) {//遍历通道
            Matrix nerveMatrix = new Matrix(nerveNub, 1);//一组通道创建一组卷积核
            for (int i = 0; i < nerveMatrix.getX(); i++) {//初始化深度卷积核权重
                float nub = random.nextFloat() / kernLen;
                nerveMatrix.setNub(i, 0, nub);
            }
            nerveMatrixList.add(nerveMatrix);
            MatrixNorm matrixNorm = new MatrixNorm(size, studyRate);
            matrixNormList.add(matrixNorm);
        }
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
