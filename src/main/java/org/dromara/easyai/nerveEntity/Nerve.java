package org.dromara.easyai.nerveEntity;

import org.dromara.easyai.conv.ConvCount;
import org.dromara.easyai.conv.ConvResult;
import org.dromara.easyai.conv.DymStudy;
import org.dromara.easyai.conv.MyStudy;
import org.dromara.easyai.entity.ThreeChannelMatrix;
import org.dromara.easyai.i.CustomEncoding;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixList;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.config.RZ;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.rnnJumpNerveCenter.CustomManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lidapeng
 * 神经元，所有类别神经元都要继承的类，具有公用属性
 * &#064;date  9:36 上午 2019/12/21
 */
public abstract class Nerve extends ConvCount {
    private final List<Nerve> son = new ArrayList<>();//轴突下一层的连接神经元
    private final List<Nerve> father = new ArrayList<>();//树突上一层的连接神经元
    private Nerve sonOnly;
    private Nerve fatherOnly;
    protected final float gaMa;//自适应学习率衰减系数
    protected Map<Integer, Float> dendrites = new ConcurrentHashMap<>();//上一层权重(需要取出)
    private final Map<Integer, Float> dymStudyRate = new ConcurrentHashMap<>();//动态学习率
    protected Map<Integer, Float> wg = new ConcurrentHashMap<>();//上一层权重与梯度的积
    private final int id;//同级神经元编号,注意在同层编号中ID应有唯一性
    protected int upNub;//上一层神经元数量
    protected int downNub;//下一层神经元的数量
    protected Map<Long, List<Float>> features = new ConcurrentHashMap<>();//上一层神经元输入的数值
    protected float threshold;//此神经元的阈值需要取出
    protected String name;//该神经元所属类型
    protected float outNub;//输出数值（ps:只有训练模式的时候才可保存输出过的数值）
    protected float E;//模板期望值
    protected float gradient;//当前梯度
    protected float studyPoint;
    protected float sigmaW;//对上一层权重与上一层梯度的积进行求和
    protected List<Matrix> sigmaMatrix;//对上层回传误差进行求和
    private int backNub = 0;//当前节点被反向传播的次数
    protected ActiveFunction activeFunction;
    private final int rzType;//正则化类型，默认不进行正则化
    private final float lParam;//正则参数
    private final int kernLen;//核长
    protected final int depth;//所处深度
    protected final int matrixX;//卷积输出行数列数
    protected final int matrixY;//卷积输出矩阵列数
    private final MatrixOperation matrixOperation;
    protected final int channelNo;//通道数
    private final ConvParameter convParameter = new ConvParameter();//内存中卷积层模型及临时数据
    protected final float oneConvRate;
    private final boolean norm;//是否进行1v1卷积升降维
    private final CustomEncoding customEncoding;//自定义编码模块
    private final float gMaxTh;
    private final DymStudy dymStudy;
    private final boolean auto;

    public Map<Integer, Float> getDendrites() {
        return dendrites;
    }

    public ConvParameter getConvParameter() {
        return convParameter;
    }

    public void setDendrites(Map<Integer, Float> dendrites) {
        this.dendrites = dendrites;
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    protected Nerve(int id, int upNub, String name, int downNub,
                    float studyPoint, boolean init, ActiveFunction activeFunction
            , boolean isDynamic, int rzType, float lParam, int kernLen, int depth
            , int matrixX, int matrixY, int coreNumber, int channelNo, float onConvRate, boolean norm, CustomEncoding customEncoding
            , float gaMa, float gMaxTh, boolean auto) throws Exception {//该神经元在同层神经元中的编号
        if (auto) {
            if (gaMa <= 0 || gaMa >= 1) {
                throw new IllegalArgumentException("gaMa 取值范围是(0,1)，当前值:" + gaMa);
            }
            if (gMaxTh <= 0) {
                throw new IllegalArgumentException("gMaxTh 必须比0大,当前值:" + gMaxTh);
            }
        }
        matrixOperation = new MatrixOperation(coreNumber);
        dymStudy = new DymStudy(gaMa, gMaxTh, auto);
        this.gaMa = gaMa;
        this.auto = auto;
        this.gMaxTh = gMaxTh;
        this.matrixX = matrixX;
        this.customEncoding = customEncoding;
        this.norm = norm;
        this.matrixY = matrixY;
        this.channelNo = channelNo;
        this.id = id;
        this.depth = depth;
        this.upNub = upNub;
        this.name = name;
        this.downNub = downNub;
        this.studyPoint = studyPoint;
        this.activeFunction = activeFunction;
        this.rzType = rzType;
        this.lParam = lParam;
        this.kernLen = kernLen;
        this.oneConvRate = onConvRate;
        initPower(init, isDynamic);//生成随机权重
    }

    protected void setStudyPoint(float studyPoint) {
        this.studyPoint = studyPoint;
    }

    public void sendMessage(long eventId, float parameter, boolean isStudy, Map<Integer, Float> E
            , OutBack outBack, Map<Integer, Float> pd) throws Exception {
        if (!son.isEmpty()) {
            for (Nerve nerve : son) {
                nerve.input(eventId, parameter, isStudy, E, outBack, pd);
            }
        } else {
            throw new Exception("this layer is lastIndex");
        }
    }

    protected List<Matrix> conv(List<Matrix> matrix) throws Exception {//一次正向卷积，下取样
        return downConvAndPooling(matrix, convParameter, channelNo, activeFunction, kernLen, true, -1);
    }

    protected void demRedByMatrixList(long eventId, List<Matrix> matrixList, boolean study,
                                      Map<Integer, Float> E, OutBack outBack, boolean needMatrix, Map<Integer, Float> pd) throws Exception {
        if (study) {//训练临时保存
            convParameter.setFeatureMatrixList(matrixList);
        }
        List<Matrix> feature;
        if (norm) {
            feature = manyOneConv(matrixList, convParameter.getOneConvPower());//降维后的特征矩阵
        } else {
            if (matrixList.size() != 3) {
                throw new Exception("不进行维度调节，输入的特征矩阵通道数必须为3");
            }
            feature = matrixList;
        }
        List<Matrix> convMatrix = conv(feature);
        sendMatrix(eventId, convMatrix, study, E, outBack, needMatrix, pd);
    }

    public void sendMatrixList(long eventId, List<Float> parameter, boolean isStudy,
                               Map<Integer, Float> E, OutBack outBack, Map<Integer, Float> pd) throws Exception {
        if (!son.isEmpty()) {
            for (Nerve nerve : son) {
                nerve.inputMatrixFeature(eventId, parameter, isStudy, E, outBack, pd);
            }
        } else {
            throw new Exception("this layer is lastIndex");
        }
    }

    public void sendMatrix(long eventId, List<Matrix> parameter, boolean isStudy,
                           Map<Integer, Float> E, OutBack outBack, boolean needMatrix, Map<Integer, Float> pd) throws Exception {
        if (sonOnly != null) {
            sonOnly.inputMatrix(eventId, parameter, isStudy, E, outBack, needMatrix, pd);
        } else {
            throw new Exception("this layer is lastIndex");
        }
    }

    public void sendThreeChannelMatrix(long eventId, ThreeChannelMatrix parameter, boolean isStudy,
                                       Map<Integer, Float> E, OutBack outBack, boolean needMatrix, Map<Integer, Float> pd) throws Exception {
        if (sonOnly != null) {
            sonOnly.inputThreeChannelMatrix(eventId, parameter, isStudy, E, outBack, needMatrix, pd);
        } else {
            throw new Exception("this layer is lastIndex");
        }
    }

    public void sendListMatrix(long eventId, List<Matrix> parameter, boolean isStudy,
                               Map<Integer, Float> E, OutBack outBack, boolean needMatrix, Map<Integer, Float> pd) throws Exception {
        if (sonOnly != null) {
            sonOnly.demRedByMatrixList(eventId, parameter, isStudy, E, outBack, needMatrix, pd);
        } else {
            throw new Exception("this layer is lastIndex");
        }
    }

    private void backSendMessage(long eventId) throws Exception {//反向传播
        if (!father.isEmpty()) {
            for (int i = 0; i < father.size(); i++) {
                father.get(i).backGetMessage(wg.get(i + 1), eventId);
            }
        } else if (fatherOnly != null && depth == 1) {//反矩阵误差
            List<Matrix> errorMatrixList = new ArrayList<>();
            int size = this.matrixX * this.matrixY;
            int featureSize = wg.size() / size;
            for (int i = 0; i < featureSize; ++i) {
                List<Float> list = new ArrayList<>();
                int startIndex = size * i;
                int endIndex = startIndex + size;
                for (int j = startIndex; j < endIndex; j++) {
                    list.add(this.wg.get(j + 1));
                }
                Matrix errorMatrix = this.matrixOperation.ListToMatrix(list, this.matrixX, this.matrixY);
                errorMatrixList.add(errorMatrix);
            }
            fatherOnly.backMatrix(errorMatrixList);
        } else if (customEncoding != null && depth == 1) {//最后一层返回给第一层
            customEncoding.backError(wg, id);
        }
    }

    private void backMatrixMessage(List<Matrix> g) throws Exception {//反向传播矩阵
        if (fatherOnly != null) {
            fatherOnly.backMatrix(g);
        }
    }

    protected void input(long eventId, float parameter, boolean isStudy
            , Map<Integer, Float> E, OutBack imageBack, Map<Integer, Float> pd) throws Exception {//输入参数

    }

    protected void inputMatrixFeature(long eventId, List<Float> parameters, boolean isStudy
            , Map<Integer, Float> E, OutBack imageBack, Map<Integer, Float> pd) throws Exception {//卷积层向网络发送参数

    }

    protected void inputMatrix(long eventId, List<Matrix> matrix, boolean isKernelStudy, Map<Integer, Float> E,
                               OutBack outBack, boolean needMatrix, Map<Integer, Float> pd) throws Exception {//输入动态矩阵
    }

    protected void inputThreeChannelMatrix(long eventId, ThreeChannelMatrix picture, boolean isKernelStudy, Map<Integer, Float> E,
                                           OutBack outBack, boolean needMatrix, Map<Integer, Float> pd) throws Exception {//输入动态矩阵
    }

    private void backGetMessage(float parameter, long eventId) throws Exception {//反向传播
        backNub++;
        sigmaW = sigmaW + parameter;
        if (backNub == downNub) {//进行新的梯度计算
            backNub = 0;
            gradient = activeFunction.functionG(outNub) * sigmaW;
            updatePower(eventId);//修改阈值
        }
    }

    protected void backMatrix(List<Matrix> t) throws Exception {//回传梯度
        backNub++;
        if (sigmaMatrix == null) {
            sigmaMatrix = t;
        } else {
            sigmaMatrix = matrixOperation.addMatrixList(t, sigmaMatrix);
        }
        if (backNub == downNub) {
            backNub = 0;
            List<Matrix> errorMatrix = backDownPoolingByList(sigmaMatrix, convParameter.getOutX(), convParameter.getOutY());//池化误差返回
            List<Matrix> myErrorMatrix = backAllDownConv(convParameter, errorMatrix, studyPoint, activeFunction, channelNo, kernLen,
                    gaMa, gMaxTh, auto);
            sigmaMatrix = null;
            if (depth == 1) {//1*1 卷积调整
                if (norm) {
                    backOneConvByList(myErrorMatrix, convParameter.getFeatureMatrixList(), convParameter.getOneConvPower(), oneConvRate
                            , convParameter.getOneDymStudyRateList(), gaMa, gMaxTh, auto);
                }
            } else {
                //将梯度继续回传
                backMatrixMessage(myErrorMatrix);
            }
        }
    }

    protected void updatePower(long eventId) throws Exception {//修改阈值
        float thError = dymStudy.getOneValueError(studyPoint, gradient, convParameter);
        threshold = threshold - thError;
        updateW(gradient, eventId);
        sigmaW = 0;//求和结果归零
        backSendMessage(eventId);
    }

    private float regularization(float w, float studyRate) {//正则化类型
        float re = 0.0f;
        if (rzType != RZ.NOT_RZ) {
            if (rzType == RZ.L2) {
                re = lParam * -w;
            } else if (rzType == RZ.L1) {
                if (w > 0) {
                    re = -lParam;
                } else if (w < 0) {
                    re = lParam;
                }
            }
            re = re * studyRate;
        }
        return re;
    }

    private void updateW(float h, long eventId) {//h是学习率 * 当前g（梯度）
        List<Float> list = features.get(eventId);
        for (Map.Entry<Integer, Float> entry : dendrites.entrySet()) {
            int key = entry.getKey();//上层隐层神经元的编号
            float w = entry.getValue();//接收到编号为KEY的上层隐层神经元的权重
            float bn = list.get(key - 1);//接收到编号为KEY的上层隐层神经元的输入
            MyStudy myStudy = dymStudy.getNerveStudyError(dymStudyRate, key, h, studyPoint);
            float error = myStudy.getError();
            float wp = bn * error;
            float dm = w * h;
            float regular = regularization(w, myStudy.getMyStudyRate());//正则化抑制权重s
            w = w + wp + regular;
            wg.put(key, dm);//保存上一层权重与梯度的积
            dendrites.put(key, w);//保存修正结果
        }
        features.remove(eventId); //清空当前上层输入参数参数
    }

    protected void insertParameters(long eventId, List<Float> parameters) {//添加参数
        List<Float> featuresList;
        if (features.containsKey(eventId)) {
            featuresList = features.get(eventId);
        } else {
            featuresList = new ArrayList<>();
            features.put(eventId, featuresList);
        }
        featuresList.addAll(parameters);
    }

    protected boolean insertParameter(long eventId, float parameter) {//添加参数
        boolean allReady = false;
        List<Float> featuresList;
        if (features.containsKey(eventId)) {
            featuresList = features.get(eventId);
        } else {
            featuresList = new ArrayList<>();
            features.put(eventId, featuresList);
        }
        featuresList.add(parameter);
        if (featuresList.size() >= upNub) {
            allReady = true;
        }
        return allReady;
    }

    protected void destoryParameter(long eventId) {//销毁参数
        features.remove(eventId);
    }

    protected float calculation(long eventId) throws Exception {//计算当前输出结果
        float sigma = 0;
        List<Float> featuresList = features.get(eventId);
        if (dendrites.size() != featuresList.size()) {
            throw new Exception("权重数量:" + dendrites.size() + ",特征数量:" + featuresList.size());
        }
        for (int i = 0; i < featuresList.size(); i++) {
            float value = featuresList.get(i);
            float w = dendrites.get(i + 1);
            sigma = w * value + sigma;
        }
        return sigma - threshold;
    }

    private void initPower(boolean init, boolean isDynamic) throws Exception {//初始化权重及阈值
        Random random = new Random();
        if (!isDynamic) {//静态神经元
            //设置初始化权重范围收缩系数
            if (upNub > 0) {//输入个数
                for (int i = 1; i < upNub + 1; i++) {
                    float nub = 0;
                    if (init) {
                        nub = random.nextFloat() / (float) Math.sqrt(upNub);
                    }
                    dendrites.put(i, nub);//random.nextFloat()
                    dymStudyRate.put(i, 0f);
                }
                //生成随机阈值
                float nub = 0;
                if (init) {
                    nub = random.nextFloat() / (float) Math.sqrt(upNub);
                }
                threshold = nub;
            }
        } else {//动态神经元
            initMatrixPower(random);
        }
    }

    private void initMatrixPower(Random random) throws Exception {
        int nerveNub = kernLen * kernLen;
        List<Matrix> nerveMatrixList = convParameter.getNerveMatrixList();//一层当中所有的深度卷积核
        List<Matrix> dymStudyRateList = convParameter.getDymStudyRateList();
        List<ConvSize> convSizeList = convParameter.getConvSizeList();
        List<List<Float>> onePowers = new ArrayList<>();//1*1卷积核
        List<List<Float>> oneDymStudyRateList = new ArrayList<>();
        for (int k = 0; k < channelNo; k++) {//遍历通道
            Matrix nerveMatrix = new Matrix(nerveNub, 1);//一组通道创建一组卷积核
            convSizeList.add(new ConvSize());
            for (int i = 0; i < nerveMatrix.getX(); i++) {//初始化深度卷积核权重
                float nub = random.nextFloat() / kernLen;
                nerveMatrix.setNub(i, 0, nub);
            }
            nerveMatrixList.add(nerveMatrix);
            dymStudyRateList.add(new Matrix(nerveNub, 1));
            if (depth == 1) {
                List<Float> oneConvPowerList = new ArrayList<>();
                List<Float> oneDymStudyRate = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    oneConvPowerList.add(random.nextFloat() / 3);
                    oneDymStudyRate.add(0f);
                }
                oneDymStudyRateList.add(oneDymStudyRate);
                onePowers.add(oneConvPowerList);
            }
        }
        if (depth == 1) {
            convParameter.setOneDymStudyRateList(oneDymStudyRateList);
            convParameter.setOneConvPower(onePowers);
        }
    }

    public int getId() {
        return id;
    }


    public void connect(List<Nerve> nerveList) {
        son.addAll(nerveList);//连接下一层
    }

    public void connectSonOnly(Nerve nerve) {
        sonOnly = nerve;
    }

    public void connectFatherOnly(Nerve nerve) {
        fatherOnly = nerve;
    }

    public void connectFather(List<Nerve> nerveList) {
        father.addAll(nerveList);//连接上一层
    }
}
