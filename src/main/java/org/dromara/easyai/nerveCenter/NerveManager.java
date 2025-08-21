package org.dromara.easyai.nerveCenter;

import org.dromara.easyai.conv.ConvCount;
import org.dromara.easyai.i.CustomEncoding;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.i.ActiveFunction;
import org.dromara.easyai.nerveEntity.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 神经网络管理工具
 * 创建神经网络
 *
 * @author lidapeng
 * &#064;date  11:05 上午 2019/12/21
 */
public class NerveManager extends ConvCount {
    private final int hiddenNerveNub;//隐层神经元个数
    private final int sensoryNerveNub;//输入神经元个数
    private final int outNerveNub;//输出神经元个数
    private final int hiddenDepth;//隐层深度
    private final List<SensoryNerve> sensoryNerves = new ArrayList<>();//感知神经元
    private SensoryNerve convInput;//卷积网络输入神经元
    private final List<List<Nerve>> depthNerves = new ArrayList<>();//隐层神经元
    private List<Nerve> convDepthNerves = new ArrayList<>();//卷积隐层神经元
    private final List<Nerve> outNerves = new ArrayList<>();//输出神经元
    private final List<Nerve> softMaxList = new ArrayList<>();//softMax层
    private boolean initPower;
    private float studyPoint = 0.001f;//学习率
    private float convStudyPoint = 0.001f;//卷积学习率
    private float oneConvRate = 0.001f;
    private final ActiveFunction activeFunction;
    private final int rzType;//正则化类型，默认不进行正则化
    private final float lParam;//正则参数
    private final int coreNumber;
    private final float gaMa;//自适应学习率
    private final float gMaxTh;//梯度裁剪阈值
    private final boolean auto;

    public SensoryNerve getConvInput() {
        return convInput;
    }

    private Map<String, Float> conversion(Map<Integer, Float> map) {
        Map<String, Float> cMap = new HashMap<>();
        for (Map.Entry<Integer, Float> entry : map.entrySet()) {
            cMap.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return cMap;
    }

    private Map<Integer, Float> unConversion(Map<String, Float> map) {
        Map<Integer, Float> cMap = new HashMap<>();
        for (Map.Entry<String, Float> entry : map.entrySet()) {
            cMap.put(Integer.parseInt(entry.getKey()), entry.getValue());
        }
        return cMap;
    }

    private ModelParameter getDymModelParameter() throws Exception {//获取动态神经元参数
        ModelParameter modelParameter = new ModelParameter();
        List<ConvDymNerveStudy> convStudies = new ArrayList<>();
        modelParameter.setDymNerveStudies(convStudies);
        for (Nerve convDepthNerve : convDepthNerves) {
            ConvParameter convParameter = convDepthNerve.getConvParameter();
            List<Matrix> nerveMatrixList = convParameter.getNerveMatrixList();//权重矩阵
            ConvDymNerveStudy convDymNerveStudy = new ConvDymNerveStudy();
            List<List<Float>> oneConvList = convParameter.getOneConvPower();
            List<DymNerveStudy> dymNerveStudies = new ArrayList<>();//一个卷积层的所有权重参数
            convDymNerveStudy.setOneConvPower(oneConvList);
            convDymNerveStudy.setDymNerveStudyList(dymNerveStudies);
            for (Matrix nerveMatrix : nerveMatrixList) {
                DymNerveStudy deepNerveStudy = new DymNerveStudy();//动态神经元隐层
                List<Float> list = deepNerveStudy.getList();
                insertWList(nerveMatrix, list);
                dymNerveStudies.add(deepNerveStudy);
            }
            convStudies.add(convDymNerveStudy);
        }
        getStaticModelParameter(modelParameter);
        return modelParameter;
    }

    private void insertWList(Matrix matrix, List<Float> list) throws Exception {//
        for (int i = 0; i < matrix.getX(); i++) {
            for (int j = 0; j < matrix.getY(); j++) {
                list.add(matrix.getNumber(i, j));
            }
        }
    }

    public ModelParameter getConvModel() throws Exception {
        return getDymModelParameter();
    }

    public ModelParameter getDnnModel() throws Exception {
        ModelParameter modelParameter = new ModelParameter();
        getStaticModelParameter(modelParameter);
        return modelParameter;
    }

    private void getStaticModelParameter(ModelParameter modelParameter) {//获取当前模型参数
        List<List<NerveStudy>> studyDepthNerves = new ArrayList<>();//隐层神经元模型
        List<NerveStudy> outStudyNerves = new ArrayList<>();//输出神经元
        //隐层神经元
        for (List<Nerve> depthNerve : depthNerves) {
            //创建一层深度的隐层神经元模型
            List<NerveStudy> deepNerve = new ArrayList<>();
            for (Nerve nerve : depthNerve) {
                //遍历某一层深度的所有隐层神经元
                NerveStudy nerveStudy = new NerveStudy();
                nerveStudy.setThreshold(nerve.getThreshold());
                nerveStudy.setDendrites(conversion(nerve.getDendrites()));
                deepNerve.add(nerveStudy);
            }
            studyDepthNerves.add(deepNerve);
        }
        for (Nerve nerve : outNerves) {
            NerveStudy nerveStudy = new NerveStudy();
            nerveStudy.setThreshold(nerve.getThreshold());
            nerveStudy.setDendrites(conversion(nerve.getDendrites()));
            outStudyNerves.add(nerveStudy);
        }
        modelParameter.setDepthNerves(studyDepthNerves);
        modelParameter.setOutNerves(outStudyNerves);
    }

    public void insertConvModel(ModelParameter modelParameter) throws Exception {
        insertConvolutionModelParameter(modelParameter);//动态神经元注入
    }

    public void insertDnnModel(ModelParameter modelParameter) {
        insertBpModelParameter(modelParameter);//全连接层注入参数
    }

    //注入卷积层模型参数
    private void insertConvolutionModelParameter(ModelParameter modelParameter) throws Exception {
        List<ConvDymNerveStudy> allDymNerveStudyList = modelParameter.getDymNerveStudies();
        for (int t = 0; t < allDymNerveStudyList.size(); t++) {
            ConvParameter convParameter = convDepthNerves.get(t).getConvParameter();
            List<Matrix> nerveMatrixList = convParameter.getNerveMatrixList();
            ConvDymNerveStudy convDymNerveStudy = allDymNerveStudyList.get(t);
            List<List<Float>> oneConvPower = convDymNerveStudy.getOneConvPower();
            if (oneConvPower != null && !oneConvPower.isEmpty()) {
                convParameter.setOneConvPower(oneConvPower);
            }
            List<DymNerveStudy> dymNerveStudyList = convDymNerveStudy.getDymNerveStudyList();
            if (dymNerveStudyList.size() != nerveMatrixList.size()) {
                throw new Exception("卷积层数量参数与模型不匹配");
            }
            for (int i = 0; i < dymNerveStudyList.size(); i++) {
                List<Float> list = dymNerveStudyList.get(i).getList();
                Matrix nerveMatrix = nerveMatrixList.get(i);
                insertMatrix(nerveMatrix, list);
            }
        }
        insertBpModelParameter(modelParameter);//全连接层注入参数
    }

    private void insertMatrix(Matrix matrix, List<Float> list) throws Exception {
        for (int i = 0; i < list.size(); i++) {
            matrix.setNub(i, 0, list.get(i));
        }
    }

    //注入全连接模型参数
    private void insertBpModelParameter(ModelParameter modelParameter) {
        List<List<NerveStudy>> depthStudyNerves = modelParameter.getDepthNerves();//隐层神经元
        List<NerveStudy> outStudyNerves = modelParameter.getOutNerves();//输出神经元
        //隐层神经元参数注入
        for (int i = 0; i < depthNerves.size(); i++) {
            List<NerveStudy> depth = depthStudyNerves.get(i);//对应的学习结果
            List<Nerve> depthNerve = depthNerves.get(i);//深度隐层神经元
            for (int j = 0; j < depthNerve.size(); j++) {//遍历当前深度神经元
                Nerve nerve = depthNerve.get(j);
                NerveStudy nerveStudy = depth.get(j);
                //学习结果
                Map<Integer, Float> studyDendrites = unConversion(nerveStudy.getDendrites());
                //神经元参数注入
                Map<Integer, Float> dendrites = nerve.getDendrites();
                nerve.setThreshold(nerveStudy.getThreshold());//注入隐层阈值
                for (Map.Entry<Integer, Float> entry : dendrites.entrySet()) {
                    int key = entry.getKey();
                    dendrites.put(key, studyDendrites.get(key));//注入隐层权重
                }
            }
        }
        //输出神经元参数注入
        for (int i = 0; i < outNerves.size(); i++) {
            Nerve outNerve = outNerves.get(i);
            NerveStudy nerveStudy = outStudyNerves.get(i);
            outNerve.setThreshold(nerveStudy.getThreshold());
            Map<Integer, Float> dendrites = outNerve.getDendrites();
            Map<Integer, Float> studyDendrites = unConversion(nerveStudy.getDendrites());
            for (Map.Entry<Integer, Float> outEntry : dendrites.entrySet()) {
                int key = outEntry.getKey();
                dendrites.put(key, studyDendrites.get(key));
            }
        }
    }

    /**
     * 初始化神经元参数
     *
     * @param sensoryNerveNub 输入神经元个数
     * @param hiddenNerveNub  隐层神经元个数
     * @param outNerveNub     输出神经元个数
     * @param hiddenDepth     隐层深度
     * @param activeFunction  激活函数
     * @param studyPoint      线性分类器学习率
     * @param rzType          正则函数
     * @param lParam          正则系数
     * @param coreNumber      并行计算核心数
     * @param gaMa            自适应学习率衰减系数
     * @param gMaxTh          梯度裁剪阈值
     * @param auTo            是否使用自适应学习率
     * @throws Exception 如果参数错误则抛异常
     */
    public NerveManager(int sensoryNerveNub, int hiddenNerveNub, int outNerveNub
            , int hiddenDepth, ActiveFunction activeFunction, float studyPoint, int rzType, float lParam
            , int coreNumber, float gaMa, float gMaxTh, boolean auTo) throws Exception {
        if (sensoryNerveNub > 0 && hiddenNerveNub > 0 && outNerveNub > 0 && hiddenDepth > 0 && activeFunction != null) {
            this.coreNumber = coreNumber;
            this.gaMa = gaMa;
            this.auto = auTo;
            this.gMaxTh = gMaxTh;
            this.hiddenNerveNub = hiddenNerveNub;
            this.sensoryNerveNub = sensoryNerveNub;
            this.outNerveNub = outNerveNub;
            this.hiddenDepth = hiddenDepth;
            this.activeFunction = activeFunction;
            this.rzType = rzType;
            this.lParam = lParam;
            if (studyPoint > 0 && studyPoint < 1) {
                this.studyPoint = studyPoint;
            }
        } else {
            throw new Exception("param is null");
        }
    }

    public List<SensoryNerve> getSensoryNerves() {//获取感知神经元集合
        return sensoryNerves;
    }

    private List<Nerve> initConDepthNerve(int kernLen, int conHiddenDepth, ActiveFunction convFunction, int channelNo, boolean norm, float GRate) throws Exception {//初始化隐层神经元1
        List<Nerve> depthNerves = new ArrayList<>();
        for (int i = 0; i < conHiddenDepth; i++) {//遍历深度
            float studyPoint = this.convStudyPoint;
            if (studyPoint <= 0 || studyPoint > 1) {
                throw new Exception("studyPoint Values range from 0 to 1");
            }
            int downNub = 1;
            boolean isConvFinish = false;
            if (i == conHiddenDepth - 1) {//卷积层最后一层
                downNub = hiddenNerveNub;
                isConvFinish = true;
            }
            HiddenNerve hiddenNerve = new HiddenNerve(1, i + 1, 1, downNub, studyPoint, initPower, convFunction, true
                    , rzType, lParam, kernLen, 0, 0, isConvFinish, coreNumber, channelNo, oneConvRate, norm,
                    null, gaMa, gMaxTh, auto, GRate);
            depthNerves.add(hiddenNerve);
        }
        for (int i = 0; i < conHiddenDepth - 1; i++) {//遍历深度
            Nerve hiddenNerve = depthNerves.get(i);//当前遍历隐层神经元
            Nerve nextHiddenNerve = depthNerves.get(i + 1);//当前遍历的下一层神经元
            hiddenNerve.connectSonOnly(nextHiddenNerve);
            nextHiddenNerve.connectFatherOnly(hiddenNerve);
        }
        return depthNerves;
    }


    private int getNerveNub(int deep, int size, int kernLen) {
        int x = size;
        int step = 1;
        for (int i = 0; i < deep; i++) {
            x = (x - (kernLen - step)) / step;
            x = x / 2 + x % 2;
        }
        return x;
    }

    /**
     * 初始化卷积层神经网络
     *
     * @param channelNo       通道数，当该数值为1 则采用多通道降维模式 推荐值1
     * @param kernLen         卷积核大小 建议为3
     * @param xSize           检测窗口行高
     * @param ySize           检测窗口行宽
     * @param convStudyPoint  卷积层学习率
     * @param convFunction    卷积层激活函数
     * @param isShowLog       是否打印学习参数
     * @param isSoftMax       最后一层是否用softMax激活
     * @param minFeatureValue 卷积层最小特征数量的开方 取值范围 [1,50]
     * @param norm            是否进行维度调节，true 进行调节， false不进行维度调节
     * @param oneConvRate     降维层学习率
     */
    public void initImageNet(int channelNo, int kernLen, int xSize, int ySize, boolean isSoftMax, boolean isShowLog,
                             float convStudyPoint, ActiveFunction convFunction, int minFeatureValue, float oneConvRate
            , boolean norm, float GRate) throws Exception {
        this.initPower = true;
        this.oneConvRate = oneConvRate;
        if (minFeatureValue < 1 || minFeatureValue > 50) {
            throw new Exception("minFeatureValue 取值范围是[1,50]");
        }
        if (channelNo < 1) {
            throw new Exception("通道数不能小于1");
        }
        if (!norm) {//如果不进行维度调节，通道数必须为3
            channelNo = 3;
        }
        this.convStudyPoint = convStudyPoint;
        int deep = getConvMyDep(xSize, ySize, kernLen, minFeatureValue);//卷积层深度
        if (deep < 2) {
            throw new Exception("minFeatureValue 设置过大");
        }
        List<Nerve> myDepthNerves = initConDepthNerve(kernLen, deep, convFunction, channelNo, norm, GRate);//初始化卷积层隐层
        Nerve convFirstNerve = myDepthNerves.get(0);//卷积第一层隐层神经元
        Nerve convLastNerve = myDepthNerves.get(myDepthNerves.size() - 1);//卷积最后一层隐层神经元
        convDepthNerves = myDepthNerves;
        convInput = new SensoryNerve(1, 0, channelNo);//输入神经元
        //感知神经元与卷积第一层隐层神经元进行连接
        convInput.connectSonOnly(convFirstNerve);
        initDepthNerve(kernLen, getNerveNub(deep, xSize, kernLen), getNerveNub(deep, ySize, kernLen), channelNo, null);//初始化深度隐层神经元 depthNerves
        List<Nerve> firstNerves = depthNerves.get(0);//线性层第一层隐层神经元
        List<Nerve> lastNerveList = depthNerves.get(depthNerves.size() - 1);//线性层最后一层隐层神经元
        convLastNerve.connect(firstNerves);//卷积最后一层链接线性层第一层
        for (Nerve nerve : firstNerves) {//线性层第一层链接卷积层最后一层
            nerve.connectFatherOnly(convLastNerve);
        }
        List<OutNerve> myOutNerveList = new ArrayList<>();
        //初始化输出神经元
        for (int i = 1; i < outNerveNub + 1; i++) {
            OutNerve outNerve = new OutNerve(i, hiddenNerveNub, 0, studyPoint, initPower,
                    activeFunction, false, isShowLog, rzType, lParam, isSoftMax, 0
                    , coreNumber, gaMa, gMaxTh, auto, 1);
            //输出层神经元连接最后一层隐层神经元
            outNerve.connectFather(lastNerveList);
            outNerves.add(outNerve);
            myOutNerveList.add(outNerve);
        }
        //生成softMax层
        if (isSoftMax) {//增加softMax层
            SoftMax softMax = new SoftMax(outNerveNub, false, myOutNerveList, isShowLog, coreNumber);
            softMaxList.add(softMax);
            for (Nerve nerve : outNerves) {
                nerve.connect(softMaxList);
            }
        }
        //最后一层隐层神经元 与输出神经元进行连接
        for (Nerve nerve : lastNerveList) {
            nerve.connect(outNerves);
        }

    }

    /**
     * 初始化
     *
     * @param initPower 是否是第一次注入
     * @param isShowLog 是否打印学习参数
     * @param isSoftMax 最后一层是否用softMax激活
     */
    public void init(boolean initPower, boolean isShowLog, boolean isSoftMax, CustomEncoding customEncoding) throws Exception {//进行神经网络的初始化构建
        this.initPower = initPower;
        initDepthNerve(0, 0, 0, 0, customEncoding);//初始化深度隐层神经元
        List<Nerve> nerveList = depthNerves.get(0);//第一层隐层神经元
        //最后一层隐层神经元啊
        List<Nerve> lastNerveList = depthNerves.get(depthNerves.size() - 1);
        List<OutNerve> myOutNerveList = new ArrayList<>();
        //初始化输出神经元
        for (int i = 1; i < outNerveNub + 1; i++) {
            OutNerve outNerve = new OutNerve(i, hiddenNerveNub, 0, studyPoint, initPower,
                    activeFunction, false, isShowLog, rzType, lParam, isSoftMax, 0
                    , coreNumber, gaMa, gMaxTh, auto, 1);
            //输出层神经元连接最后一层隐层神经元
            outNerve.connectFather(lastNerveList);
            outNerves.add(outNerve);
            myOutNerveList.add(outNerve);
        }
        //生成softMax层
        if (isSoftMax) {//增加softMax层
            SoftMax softMax = new SoftMax(outNerveNub, false, myOutNerveList, isShowLog, coreNumber);
            softMaxList.add(softMax);
            for (Nerve nerve : outNerves) {
                nerve.connect(softMaxList);
            }
        }
        //最后一层隐层神经元 与输出神经元进行连接
        for (Nerve nerve : lastNerveList) {
            nerve.connect(outNerves);
        }

        //初始化感知神经元
        for (int i = 1; i < sensoryNerveNub + 1; i++) {
            SensoryNerve sensoryNerve = new SensoryNerve(i, 0, 0);
            //感知神经元与第一层隐层神经元进行连接
            sensoryNerve.connect(nerveList);
            sensoryNerves.add(sensoryNerve);
        }

    }

    private void initDepthNerve(int kernLen, int matrixX, int matrixY, int channelNo, CustomEncoding customEncoding) throws Exception {//初始化隐层神经元1
        for (int i = 0; i < hiddenDepth; i++) {//遍历深度
            List<Nerve> hiddenNerveList = new ArrayList<>();
            float studyPoint = this.studyPoint;
            if (studyPoint <= 0 || studyPoint > 1) {
                throw new Exception("studyPoint Values range from 0 to 1");
            }
            CustomEncoding myCustomEncoding = null;
            if (i == 0) {
                myCustomEncoding = customEncoding;
            }
            for (int j = 1; j < hiddenNerveNub + 1; j++) {//遍历同级
                int upNub;
                int downNub;
                int myMatrixX = 0;
                int myMatrixY = 0;
                if (i == 0) {
                    myMatrixX = matrixX;
                    myMatrixY = matrixY;
                    if (matrixX > 0 && matrixY > 0) {
                        upNub = matrixX * matrixY * channelNo;
                    } else {
                        upNub = sensoryNerveNub;
                    }
                } else {
                    upNub = hiddenNerveNub;
                }
                if (i == hiddenDepth - 1) {//最后一层隐层神经元z
                    downNub = outNerveNub;
                } else {
                    downNub = hiddenNerveNub;
                }
                HiddenNerve hiddenNerve = new HiddenNerve(j, i + 1, upNub, downNub, studyPoint, initPower, activeFunction, false
                        , rzType, lParam, kernLen, myMatrixX, myMatrixY, false, coreNumber, 0, oneConvRate, false
                        , myCustomEncoding, gaMa, gMaxTh, auto, 1);
                hiddenNerveList.add(hiddenNerve);
            }
            depthNerves.add(hiddenNerveList);
        }
        initHiddenNerve();
    }

    private void initHiddenNerve() {//初始化隐层神经元2
        for (int i = 0; i < hiddenDepth - 1; i++) {//遍历深度
            List<Nerve> hiddenNerveList = depthNerves.get(i);//当前遍历隐层神经元
            List<Nerve> nextHiddenNerveList = depthNerves.get(i + 1);//当前遍历的下一层神经元
            for (Nerve hiddenNerve : hiddenNerveList) {
                hiddenNerve.connect(nextHiddenNerveList);
            }
            for (Nerve nextHiddenNerve : nextHiddenNerveList) {
                nextHiddenNerve.connectFather(hiddenNerveList);
            }
        }
    }
}
