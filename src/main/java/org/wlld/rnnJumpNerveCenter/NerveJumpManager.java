package org.wlld.rnnJumpNerveCenter;


import org.wlld.MatrixTools.Matrix;
import org.wlld.i.ActiveFunction;
import org.wlld.rnnJumpNerveEntity.*;

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
public class NerveJumpManager {
    private final int hiddenNerveNub;//隐层神经元个数
    private final int sensoryNerveNub;//输入神经元个数
    private final int outNerveNub;//输出神经元个数
    private final int hiddenDepth;//隐层深度
    private final List<SensoryNerve> sensoryNerves = new ArrayList<>();//感知神经元
    private final List<List<Nerve>> depthNerves = new ArrayList<>();//隐层神经元
    private final List<Nerve> outNerves = new ArrayList<>();//输出神经元
    private final List<Nerve> softMaxList = new ArrayList<>();//softMax层
    private final List<RnnOutNerveBody> rnnOutNerveBodies = new ArrayList<>();//rnn输出层神经元
    private boolean initPower;
    private double studyPoint = 0.1;//学习率
    private final ActiveFunction activeFunction;
    private Map<Integer, Matrix> matrixMap = new HashMap<>();//主键与期望矩阵的映射
    private final boolean isDynamic;//是否是动态神经网络
    private boolean isRnn = false;//是否为rnn网络
    private final int rzType;//正则化类型，默认不进行正则化
    private final double lParam;//正则参数
    private final List<NerveCenter> nerveCenterList = new ArrayList<>();
    private double powerTh = 0.2;//权重阈值

    public void setPowerTh(double powerTh) {
        this.powerTh = powerTh;
    }

    public void setMatrixMap(Map<Integer, Matrix> matrixMap) {
        this.matrixMap = matrixMap;
    }

    private Map<String, Double> conversion(Map<Integer, Double> map) {
        Map<String, Double> cMap = new HashMap<>();
        for (Map.Entry<Integer, Double> entry : map.entrySet()) {
            cMap.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return cMap;
    }

    private Map<Integer, Double> unConversion(Map<String, Double> map) {
        Map<Integer, Double> cMap = new HashMap<>();
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            cMap.put(Integer.parseInt(entry.getKey()), entry.getValue());
        }
        return cMap;
    }

    private ModelParameter getDymModelParameter() throws Exception {//获取动态神经元参数
        ModelParameter modelParameter = new ModelParameter();
        List<DymNerveStudy> dymNerveStudies = new ArrayList<>();//动态神经元隐层
        DymNerveStudy dymOutNerveStudy = new DymNerveStudy();//动态神经元输出层
        modelParameter.setDymNerveStudies(dymNerveStudies);
        modelParameter.setDymOutNerveStudy(dymOutNerveStudy);
        for (List<Nerve> nerve : depthNerves) {
            Nerve depthNerve = nerve.get(0);//隐层神经元
            DymNerveStudy deepNerveStudy = new DymNerveStudy();//动态神经元输出层
            List<Double> list = deepNerveStudy.getList();
            Matrix matrix = depthNerve.getNerveMatrix();
            insertWList(matrix, list);
            dymNerveStudies.add(deepNerveStudy);
        }
        Nerve outNerve = outNerves.get(0);
        Matrix matrix = outNerve.getNerveMatrix();
        List<Double> list = dymOutNerveStudy.getList();
        insertWList(matrix, list);
        return modelParameter;
    }

    private void insertWList(Matrix matrix, List<Double> list) throws Exception {//
        for (int i = 0; i < matrix.getX(); i++) {
            for (int j = 0; j < matrix.getY(); j++) {
                list.add(matrix.getNumber(i, j));
            }
        }
    }

    public ModelParameter getModelParameter() throws Exception {
        if (isRnn) {
            return getRnnModelParameter();
        } else if (isDynamic) {
            return getDymModelParameter();
        } else {
            return getStaticModelParameter();
        }
    }

    private ModelParameter getRnnModelParameter() {//获取rnn当前模型参数
        ModelParameter modelParameter = new ModelParameter();
        List<List<NerveStudy>> studyDepthNerves = new ArrayList<>();//隐层神经元模型
        List<RnnOutNerveStudy> rnnOutNerveStudies = new ArrayList<>();
        modelParameter.setDepthNerves(studyDepthNerves);
        modelParameter.setRnnOutNerveStudies(rnnOutNerveStudies);
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
        for (RnnOutNerveBody rnnOutNerveBody : rnnOutNerveBodies) {
            List<NerveStudy> nerveStudies = new ArrayList<>();
            RnnOutNerveStudy rnnOutNerveStudy = new RnnOutNerveStudy();
            rnnOutNerveStudies.add(rnnOutNerveStudy);
            rnnOutNerveStudy.setDepth(rnnOutNerveBody.getDepth());
            rnnOutNerveStudy.setNerveStudies(nerveStudies);
            List<Nerve> outNerveList = rnnOutNerveBody.getOutNerves();
            getOutNerveModel(nerveStudies, outNerveList);
        }
        return modelParameter;
    }

    private void getOutNerveModel(List<NerveStudy> nerveStudies, List<Nerve> outNerveList) {
        for (Nerve nerve : outNerveList) {
            NerveStudy nerveStudy = new NerveStudy();
            nerveStudy.setThreshold(nerve.getThreshold());
            nerveStudy.setDendrites(conversion(nerve.getDendrites()));
            nerveStudies.add(nerveStudy);
        }
    }

    private ModelParameter getStaticModelParameter() {//获取当前模型参数
        ModelParameter modelParameter = new ModelParameter();
        List<List<NerveStudy>> studyDepthNerves = new ArrayList<>();//隐层神经元模型
        List<NerveStudy> outStudyNerves = new ArrayList<>();//输出神经元
        //隐层神经元
        for (List<Nerve> depthNerve : depthNerves) {
            //创建一层深度的隐层神经元模型
            List<NerveStudy> deepNerve = new ArrayList<>();
            getOutNerveModel(deepNerve, depthNerve);
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
        return modelParameter;
    }

    public void insertModelParameter(ModelParameter modelParameter) throws Exception {
        if (isRnn) {
            insertRnnModelParameter(modelParameter);
        } else if (isDynamic) {
            insertConvolutionModelParameter(modelParameter);//动态神经元注入
        } else {
            insertBpModelParameter(modelParameter);//全连接层注入参数
        }

    }

    //注入卷积层模型参数
    private void insertConvolutionModelParameter(ModelParameter modelParameter) throws Exception {
        List<DymNerveStudy> dymNerveStudyList = modelParameter.getDymNerveStudies();
        DymNerveStudy dymOutNerveStudy = modelParameter.getDymOutNerveStudy();
        for (int i = 0; i < depthNerves.size(); i++) {
            Nerve depthNerve = depthNerves.get(i).get(0);
            DymNerveStudy dymNerveStudy = dymNerveStudyList.get(i);
            List<Double> list = dymNerveStudy.getList();
            Matrix nerveMatrix = depthNerve.getNerveMatrix();
            insertMatrix(nerveMatrix, list);
        }
        Nerve outNerve = outNerves.get(0);
        Matrix outNerveMatrix = outNerve.getNerveMatrix();
        List<Double> list = dymOutNerveStudy.getList();
        insertMatrix(outNerveMatrix, list);
    }

    private void insertMatrix(Matrix matrix, List<Double> list) throws Exception {
        for (int i = 0; i < list.size(); i++) {
            matrix.setNub(i, 0, list.get(i));
        }
    }

    private void insertRnnModelParameter(ModelParameter modelParameter) {
        List<List<NerveStudy>> depthStudyNerves = modelParameter.getDepthNerves();//隐层神经元
        List<RnnOutNerveStudy> rnnOutNerveStudies = modelParameter.getRnnOutNerveStudies();
        //隐层神经元参数注入
        depthNervesModel(depthStudyNerves);
        for (RnnOutNerveStudy rnnOutNerveStudy : rnnOutNerveStudies) {
            RnnOutNerveBody rnnOutNerveBody = getRnnOutNerveBody(rnnOutNerveStudy.getDepth());
            List<NerveStudy> outStudyNerves = rnnOutNerveStudy.getNerveStudies();
            List<Nerve> outNerveBody = rnnOutNerveBody.getOutNerves();
            //输出神经元参数注入
            outNerveModel(outStudyNerves, outNerveBody);
        }
    }

    private void outNerveModel(List<NerveStudy> outStudyNerves, List<Nerve> outNerveBody) {
        for (int i = 0; i < outNerveBody.size(); i++) {
            Nerve outNerve = outNerveBody.get(i);
            NerveStudy nerveStudy = outStudyNerves.get(i);
            outNerve.setThreshold(nerveStudy.getThreshold());
            Map<Integer, Double> dendrites = outNerve.getDendrites();
            Map<Integer, Double> studyDendrites = unConversion(nerveStudy.getDendrites());
            for (Map.Entry<Integer, Double> outEntry : dendrites.entrySet()) {
                int key = outEntry.getKey();
                dendrites.put(key, studyDendrites.get(key));
            }
        }
    }

    private void depthNervesModel(List<List<NerveStudy>> depthStudyNerves) {
        for (int i = 0; i < depthNerves.size(); i++) {
            List<NerveStudy> depth = depthStudyNerves.get(i);//对应的学习结果
            List<Nerve> depthNerve = depthNerves.get(i);//深度隐层神经元
            for (int j = 0; j < depthNerve.size(); j++) {//遍历当前深度神经元
                Nerve nerve = depthNerve.get(j);
                NerveStudy nerveStudy = depth.get(j);
                //学习结果
                Map<Integer, Double> studyDendrites = unConversion(nerveStudy.getDendrites());
                //神经元参数注入
                Map<Integer, Double> dendrites = nerve.getDendrites();
                nerve.setThreshold(nerveStudy.getThreshold());//注入隐层阈值
                for (Map.Entry<Integer, Double> entry : dendrites.entrySet()) {
                    int key = entry.getKey();
                    dendrites.put(key, studyDendrites.get(key));//注入隐层权重
                }
            }
        }
    }

    private RnnOutNerveBody getRnnOutNerveBody(int depth) {
        RnnOutNerveBody myRnnOutNerveBody = null;
        for (RnnOutNerveBody rnnOutNerveBody : rnnOutNerveBodies) {
            if (rnnOutNerveBody.getDepth() == depth) {
                myRnnOutNerveBody = rnnOutNerveBody;
                break;
            }
        }
        return myRnnOutNerveBody;
    }

    //注入全连接模型参数
    private void insertBpModelParameter(ModelParameter modelParameter) {
        List<List<NerveStudy>> depthStudyNerves = modelParameter.getDepthNerves();//隐层神经元
        List<NerveStudy> outStudyNerves = modelParameter.getOutNerves();//输出神经元
        //隐层神经元参数注入
        depthNervesModel(depthStudyNerves);
        //输出神经元参数注入
        outNerveModel(outStudyNerves, outNerves);
    }

    /**
     * 初始化神经元参数
     *
     * @param sensoryNerveNub 输入神经元个数
     * @param hiddenNerveNub  隐层神经元个数
     * @param outNerveNub     输出神经元个数
     * @param hiddenDepth     隐层深度
     * @param activeFunction  激活函数
     * @param isDynamic       是否是动态神经元
     * @param rzType          正则函数
     * @param lParam          正则系数
     * @throws Exception 如果参数错误则抛异常
     */
    public NerveJumpManager(int sensoryNerveNub, int hiddenNerveNub, int outNerveNub
            , int hiddenDepth, ActiveFunction activeFunction, boolean isDynamic,
                        double studyPoint, int rzType, double lParam) throws Exception {
        if (sensoryNerveNub > 0 && hiddenNerveNub > 0 && outNerveNub > 0 && hiddenDepth > 0 && activeFunction != null) {
            this.hiddenNerveNub = hiddenNerveNub;
            this.sensoryNerveNub = sensoryNerveNub;
            this.outNerveNub = outNerveNub;
            this.hiddenDepth = hiddenDepth;
            this.activeFunction = activeFunction;
            this.isDynamic = isDynamic;
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

    /**
     * 初始化
     *
     * @param initPower 是否是第一次注入
     * @param isMatrix  参数是否是一个矩阵
     * @param isShowLog 是否打印学习参数
     * @param isSoftMax 最后一层是否用softMax激活
     * @param step      卷积步长
     * @param kernLen   卷积核长
     */
    public void init(boolean initPower, boolean isMatrix, boolean isShowLog, boolean isSoftMax
            , int step, int kernLen) throws Exception {//进行神经网络的初始化构建
        this.initPower = initPower;
        initDepthNerve(isMatrix, step, kernLen);//初始化深度隐层神经元
        List<Nerve> nerveList = depthNerves.get(0);//第一层隐层神经元
        //最后一层隐层神经元啊
        List<Nerve> lastNerveList = depthNerves.get(depthNerves.size() - 1);
        //初始化输出神经元
        for (int i = 1; i < outNerveNub + 1; i++) {
            OutNerve outNerve = new OutNerve(i, studyPoint, initPower,
                    activeFunction, isMatrix, isShowLog, rzType, lParam, isSoftMax, step, kernLen, sensoryNerveNub, hiddenNerveNub, outNerveNub, hiddenDepth);
            if (isMatrix) {//是卷积层神经网络
                outNerve.setMatrixMap(matrixMap);
            }
            if (isSoftMax) {
                SoftMax softMax = new SoftMax(i, false, outNerve, isShowLog, sensoryNerveNub, hiddenNerveNub, outNerveNub, hiddenDepth);
                softMaxList.add(softMax);
            }
            //输出层神经元连接最后一层隐层神经元
            outNerve.connectFather(0, lastNerveList);
            outNerves.add(outNerve);
        }
        //生成softMax层
        if (isSoftMax) {//增加softMax层
            for (Nerve nerve : outNerves) {
                nerve.connect(0, softMaxList);
            }
        }
        //最后一层隐层神经元 与输出神经元进行连接
        for (Nerve nerve : lastNerveList) {
            nerve.connect(0, outNerves);
        }

        //初始化感知神经元
        for (int i = 1; i < sensoryNerveNub + 1; i++) {
            SensoryNerve sensoryNerve = new SensoryNerve(i, hiddenDepth);
            //感知神经元与第一层隐层神经元进行连接
            sensoryNerve.connect(0, nerveList);
            sensoryNerves.add(sensoryNerve);
        }
    }

    private void createRnnOutNerve(boolean initPower, boolean isShowLog, List<Nerve> nerveList, int depth
            , boolean toSoftMax) throws Exception {
        RnnOutNerveBody rnnOutNerveBody = new RnnOutNerveBody();
        List<Nerve> mySoftMaxList = new ArrayList<>();
        List<Nerve> rnnOutNerves = new ArrayList<>();
        rnnOutNerveBody.setDepth(depth);
        rnnOutNerveBody.setOutNerves(rnnOutNerves);
        NerveCenter nerveCenter = nerveCenterList.get(depth);
        for (int i = 1; i < outNerveNub + 1; i++) {
            OutNerve outNerve = new OutNerve(i, studyPoint, initPower,
                    activeFunction, false, isShowLog, rzType, lParam, toSoftMax, 0, 0, sensoryNerveNub, hiddenNerveNub, outNerveNub, hiddenDepth);
            if (toSoftMax) {
                SoftMax softMax = new SoftMax(i, false, outNerve, isShowLog, sensoryNerveNub, hiddenNerveNub, outNerveNub, hiddenDepth);
                mySoftMaxList.add(softMax);
            }
            outNerve.setNerveCenter(nerveCenter);
            outNerve.connectFather(depth, nerveList);//每一层的输出神经元 链接每一层的隐层神经元
            rnnOutNerves.add(outNerve);
        }
        if (toSoftMax) {
            for (Nerve nerve : rnnOutNerves) {
                nerve.connect(0, mySoftMaxList);
            }
        }
        for (Nerve nerve : nerveList) {
            nerve.connectOut(rnnOutNerves);
        }
        rnnOutNerveBodies.add(rnnOutNerveBody);
    }

    public void initRnn(boolean initPower, boolean isShowLog, boolean toSoftMax) throws Exception {
        isRnn = true;
        initDepthNerve(false, 0, 0);//初始化深度隐层神经元
        for (int i = 0; i < depthNerves.size(); i++) {
            createRnnOutNerve(initPower, isShowLog, depthNerves.get(i), i + 1, toSoftMax);
        }
        //初始化感知神经元
        for (int i = 1; i < sensoryNerveNub + 1; i++) {
            SensoryNerve sensoryNerve = new SensoryNerve(i, hiddenDepth);
            for (int j = 0; j < hiddenDepth; j++) {
                List<Nerve> hiddenNerveList = depthNerves.get(j);//当前遍历隐层神经元
                sensoryNerve.connect(j + 1, hiddenNerveList);
            }
            sensoryNerves.add(sensoryNerve);
        }
    }

    private void initDepthNerve(boolean isMatrix, int step, int kernLen) throws Exception {//初始化隐层神经元1
        if (isRnn) {
            NerveCenter nerveCenter = new NerveCenter(0, null, nerveCenterList, powerTh);
            nerveCenterList.add(nerveCenter);
        }
        for (int i = 0; i < hiddenDepth; i++) {//遍历深度
            List<Nerve> hiddenNerveList = new ArrayList<>();
            double studyPoint = this.studyPoint;
            if (studyPoint <= 0 || studyPoint > 1) {
                throw new Exception("studyPoint Values range from 0 to 1");
            }
            if (isRnn) {
                NerveCenter nerveCenter = new NerveCenter(i + 1, hiddenNerveList, nerveCenterList, powerTh);
                nerveCenterList.add(nerveCenter);
            }
            for (int j = 1; j < hiddenNerveNub + 1; j++) {//遍历同级
                HiddenNerve hiddenNerve = new HiddenNerve(j, i + 1, studyPoint, initPower, activeFunction, isMatrix
                        , rzType, lParam, step, kernLen, sensoryNerveNub, hiddenNerveNub, outNerveNub, hiddenDepth);
                hiddenNerveList.add(hiddenNerve);
            }
            depthNerves.add(hiddenNerveList);
        }
        if (isRnn) {
            initRnnHiddenNerve();
        } else {
            initHiddenNerve();
        }
    }

    private void initHiddenNerve() {
        for (int i = 0; i < hiddenDepth - 1; i++) {
            List<Nerve> hiddenNerveList = depthNerves.get(i);//当前遍历隐层神经元
            List<Nerve> nextHiddenNerveList = depthNerves.get(i + 1);
            for (Nerve nerve : hiddenNerveList) {
                nerve.connect(0, nextHiddenNerveList);
            }
            for (Nerve nerve : nextHiddenNerveList) {
                nerve.connectFather(0, hiddenNerveList);
            }
        }
    }

    private void initRnnHiddenNerve() {//初始化隐层神经元2
        for (int i = 0; i < hiddenDepth; i++) {//遍历深度
            List<Nerve> hiddenNerveList = depthNerves.get(i);//当前遍历隐层神经元
            if (i < hiddenDepth - 1) {//向前链接
                for (int j = i + 1; j < hiddenDepth; j++) {
                    List<Nerve> nextHiddenNerveList = depthNerves.get(j);
                    for (Nerve hiddenNerve : hiddenNerveList) {
                        hiddenNerve.connect(j + 1, nextHiddenNerveList);
                    }
                }
            }
            if (i > 0) {//向后链接
                for (int t = i - 1; t >= 0; t--) {
                    List<Nerve> nextHiddenNerveList = depthNerves.get(t);
                    for (Nerve hiddenNerve : hiddenNerveList) {
                        hiddenNerve.connectFather(t + 1, nextHiddenNerveList);
                    }
                }
            }
        }
    }
}
