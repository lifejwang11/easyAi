package org.wlld.nerveCenter;

import org.wlld.MatrixTools.Matrix;
import org.wlld.i.ActiveFunction;
import org.wlld.nerveEntity.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 神经网络管理工具
 * 创建神经网络
 *
 * @author lidapeng
 * @date 11:05 上午 2019/12/21
 */
public class NerveManager {
    private int hiddenNerveNub;//隐层神经元个数
    private int sensoryNerveNub;//输入神经元个数
    private int outNerveNub;//输出神经元个数
    private int hiddenDepth;//隐层深度
    private List<SensoryNerve> sensoryNerves = new ArrayList<>();//感知神经元
    private List<List<Nerve>> depthNerves = new ArrayList<>();//隐层神经元
    private List<Nerve> outNerves = new ArrayList<>();//输出神经元
    private List<Nerve> softMaxList = new ArrayList<>();//softMax层
    private boolean initPower;
    private double studyPoint = 0.1;//学习率
    private ActiveFunction activeFunction;
    private Map<Integer, Matrix> matrixMap = new HashMap<>();//主键与期望矩阵的映射
    private boolean isDynamic;//是否是动态神经网络
    private List<Double> studyList = new ArrayList<>();
    private int rzType;//正则化类型，默认不进行正则化
    private double lParam;//正则参数

    public List<Double> getStudyList() {//查看每一次的学习率
        return studyList;
    }

    public void setStudyList(List<Double> studyList) {//设置每一层的学习率
        this.studyList = studyList;
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
        for (int i = 0; i < depthNerves.size(); i++) {
            Nerve depthNerve = depthNerves.get(i).get(0);//隐层神经元
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
        if (isDynamic) {
            return getDymModelParameter();
        } else {
            return getStaticModelParameter();
        }
    }

    private ModelParameter getStaticModelParameter() {//获取当前模型参数
        ModelParameter modelParameter = new ModelParameter();
        List<List<NerveStudy>> studyDepthNerves = new ArrayList<>();//隐层神经元模型
        List<NerveStudy> outStudyNerves = new ArrayList<>();//输出神经元
        for (int i = 0; i < depthNerves.size(); i++) {
            //创建一层深度的隐层神经元模型
            List<Nerve> depthNerve = depthNerves.get(i);//隐层神经元
            List<NerveStudy> deepNerve = new ArrayList<>();
            for (int j = 0; j < depthNerve.size(); j++) {
                //遍历某一层深度的所有隐层神经元
                NerveStudy nerveStudy = new NerveStudy();
                Nerve hiddenNerve = depthNerve.get(j);
                nerveStudy.setThreshold(hiddenNerve.getThreshold());
                nerveStudy.setDendrites(conversion(hiddenNerve.getDendrites()));
                deepNerve.add(nerveStudy);
            }
            studyDepthNerves.add(deepNerve);
        }
        for (int i = 0; i < outNerves.size(); i++) {
            NerveStudy nerveStudy = new NerveStudy();
            Nerve outNerve = outNerves.get(i);
            nerveStudy.setThreshold(outNerve.getThreshold());
            nerveStudy.setDendrites(conversion(outNerve.getDendrites()));
            outStudyNerves.add(nerveStudy);
        }
        modelParameter.setDepthNerves(studyDepthNerves);
        modelParameter.setOutNerves(outStudyNerves);
        return modelParameter;
    }

    public void insertModelParameter(ModelParameter modelParameter) throws Exception {
        if (isDynamic) {
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
        //输出神经元参数注入
        for (int i = 0; i < outNerves.size(); i++) {
            Nerve outNerve = outNerves.get(i);
            NerveStudy nerveStudy = outStudyNerves.get(i);
            outNerve.setThreshold(nerveStudy.getThreshold());
            Map<Integer, Double> dendrites = outNerve.getDendrites();
            Map<Integer, Double> studyDendrites =unConversion(nerveStudy.getDendrites());
            for (Map.Entry<Integer, Double> outEntry : dendrites.entrySet()) {
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
     * @param isDynamic       是否是动态神经元
     * @param rzType          正则函数
     * @param lParam          正则系数
     * @throws Exception 如果参数错误则抛异常
     */
    public NerveManager(int sensoryNerveNub, int hiddenNerveNub, int outNerveNub
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

    public int getHiddenNerverNub() {
        return hiddenNerveNub;
    }

    public int getSensoryNerveNub() {
        return sensoryNerveNub;
    }

    public int getOutNerveNub() {
        return outNerveNub;
    }

    public int getHiddenDepth() {
        return hiddenDepth;
    }

    public List<List<Nerve>> getDepthNerves() {
        return depthNerves;
    }

    public List<Nerve> getOutNevers() {
        return outNerves;
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
     * @throws Exception
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
            OutNerve outNerve = new OutNerve(i, hiddenNerveNub, 0, studyPoint, initPower,
                    activeFunction, isMatrix, isShowLog, rzType, lParam, isSoftMax, step, kernLen);
            if (isMatrix) {//是卷积层神经网络
                outNerve.setMatrixMap(matrixMap);
            }
            if (isSoftMax) {
                SoftMax softMax = new SoftMax(i, outNerveNub, false, outNerve, isShowLog);
                softMaxList.add(softMax);
            }
            //输出层神经元连接最后一层隐层神经元
            outNerve.connectFather(lastNerveList);
            outNerves.add(outNerve);
        }
        //生成softMax层
        if (isSoftMax) {//增加softMax层
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
            SensoryNerve sensoryNerve = new SensoryNerve(i, 0);
            //感知神经元与第一层隐层神经元进行连接
            sensoryNerve.connect(nerveList);
            sensoryNerves.add(sensoryNerve);
        }

    }

    private void initDepthNerve(boolean isMatrix, int step, int kernLen) throws Exception {//初始化隐层神经元1
        for (int i = 0; i < hiddenDepth; i++) {//遍历深度
            List<Nerve> hiddenNerveList = new ArrayList<>();
            double studyPoint = this.studyPoint;
            if (studyList.contains(i)) {//加载每一层的学习率
                studyPoint = studyList.get(i);
            }
            if (studyPoint <= 0 || studyPoint > 1) {
                throw new Exception("studyPoint Values range from 0 to 1");
            }
            for (int j = 1; j < hiddenNerveNub + 1; j++) {//遍历同级
                int upNub = 0;
                int downNub = 0;
                if (i == 0) {
                    upNub = sensoryNerveNub;
                } else {
                    upNub = hiddenNerveNub;
                }
                if (i == hiddenDepth - 1) {//最后一层隐层神经元z
                    downNub = outNerveNub;
                } else {
                    downNub = hiddenNerveNub;
                }
                HiddenNerve hiddenNerve = new HiddenNerve(j, i + 1, upNub, downNub, studyPoint, initPower, activeFunction, isMatrix
                        , rzType, lParam, step, kernLen);
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
