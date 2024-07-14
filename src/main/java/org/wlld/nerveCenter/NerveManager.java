package org.wlld.nerveCenter;

import org.wlld.matrixTools.Matrix;
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
 * &#064;date  11:05 上午 2019/12/21
 */
public class NerveManager {
    private final int hiddenNerveNub;//隐层神经元个数
    private int sensoryNerveNub;//输入神经元个数
    private final int outNerveNub;//输出神经元个数
    private final int hiddenDepth;//隐层深度
    private final List<SensoryNerve> sensoryNerves = new ArrayList<>();//感知神经元
    private final List<List<Nerve>> depthNerves = new ArrayList<>();//隐层神经元
    private final List<List<Nerve>> convDepthNerves = new ArrayList<>();//卷积隐层神经元
    private final List<Nerve> outNerves = new ArrayList<>();//输出神经元
    private final List<Nerve> softMaxList = new ArrayList<>();//softMax层
    private boolean initPower;
    private double studyPoint = 0.1;//学习率
    private double convStudyPoint = 0.1;//卷积学习率
    private final ActiveFunction activeFunction;
    private final int rzType;//正则化类型，默认不进行正则化
    private final double lParam;//正则参数

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
        List<List<DymNerveStudy>> convStudies = new ArrayList<>();
        modelParameter.setDymNerveStudies(convStudies);
        for (List<Nerve> convDepthNerve : convDepthNerves) {
            List<DymNerveStudy> dymNerveStudies = new ArrayList<>();//动态神经元隐层
            for (Nerve depthNerve : convDepthNerve) {
                DymNerveStudy deepNerveStudy = new DymNerveStudy();//动态神经元隐层
                List<Double> list = deepNerveStudy.getList();
                Matrix matrix = depthNerve.getNerveMatrix();
                insertWList(matrix, list);
                dymNerveStudies.add(deepNerveStudy);
            }
            convStudies.add(dymNerveStudies);
        }
        getStaticModelParameter(modelParameter);
        return modelParameter;
    }

    private void insertWList(Matrix matrix, List<Double> list) throws Exception {//
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
        List<List<DymNerveStudy>> allDymNerveStudyList = modelParameter.getDymNerveStudies();
        for (int t = 0; t < allDymNerveStudyList.size(); t++) {
            List<DymNerveStudy> dymNerveStudyList = allDymNerveStudyList.get(t);
            for (int i = 0; i < dymNerveStudyList.size(); i++) {
                Nerve depthNerve = convDepthNerves.get(t).get(i);
                DymNerveStudy dymNerveStudy = dymNerveStudyList.get(i);
                List<Double> list = dymNerveStudy.getList();
                Matrix nerveMatrix = depthNerve.getNerveMatrix();
                insertMatrix(nerveMatrix, list);
            }
        }
        insertBpModelParameter(modelParameter);//全连接层注入参数
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
            Map<Integer, Double> studyDendrites = unConversion(nerveStudy.getDendrites());
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
     * @param studyPoint      线性分类器学习率
     * @param rzType          正则函数
     * @param lParam          正则系数
     * @throws Exception 如果参数错误则抛异常
     */
    public NerveManager(int sensoryNerveNub, int hiddenNerveNub, int outNerveNub
            , int hiddenDepth, ActiveFunction activeFunction, double studyPoint, int rzType, double lParam) throws Exception {
        if (sensoryNerveNub > 0 && hiddenNerveNub > 0 && outNerveNub > 0 && hiddenDepth > 0 && activeFunction != null) {
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

    private List<Nerve> initConDepthNerve(int step, int kernLen, int conHiddenDepth, ActiveFunction convFunction) throws Exception {//初始化隐层神经元1
        List<Nerve> depthNerves = new ArrayList<>();
        for (int i = 0; i < conHiddenDepth; i++) {//遍历深度
            double studyPoint = this.convStudyPoint;
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
                    , rzType, lParam, step, kernLen, 0, 0, isConvFinish);
            depthNerves.add(hiddenNerve);
        }
        for (int i = 0; i < conHiddenDepth - 1; i++) {//遍历深度
            Nerve hiddenNerve = depthNerves.get(i);//当前遍历隐层神经元
            Nerve nextHiddenNerve = depthNerves.get(i + 1);//当前遍历的下一层神经元
            List<Nerve> hiddenNerveList = new ArrayList<>();
            List<Nerve> nextHiddenNerveList = new ArrayList<>();
            hiddenNerveList.add(hiddenNerve);
            nextHiddenNerveList.add(nextHiddenNerve);
            hiddenNerve.connect(nextHiddenNerveList);
            nextHiddenNerve.connectFather(hiddenNerveList);
        }
        return depthNerves;
    }

    private int getConvMyDep(int xSize, int ySize, int step, int kernLen) {
        int xDeep = getConvDeep(xSize, step, kernLen);
        int yDeep = getConvDeep(ySize, step, kernLen);
        return Math.min(xDeep, yDeep);
    }

    private int getConvDeep(int size, int step, int kernLen) {//获取卷积层深度
        int x = size;
        int deep = 0;//深度
        int y;
        do {
            y = (x - (kernLen - step)) / step;
            x = y;
            deep++;
        } while (y >= kernLen);
        return deep;
    }

    private int getNerveNub(int deep, int size, int kernLen, int step) {
        int x = size;
        for (int i = 0; i < deep; i++) {
            x = (x - (kernLen - step)) / step;
        }
        return x;
    }
    /**
     * 初始化卷积层神经网络
     *
     * @param step 卷积步长 建议为2
     * @param kernLen 卷积核大小 建议为3
     * @param xSize 检测窗口行高
     * @param ySize 检测窗口行宽
     * @param convStudyPoint 卷积层学习率
     * @param convFunction 卷积层激活函数
     * @param isShowLog 是否打印学习参数
     * @param isSoftMax 最后一层是否用softMax激活
     */
    public void initImageNet(int step, int kernLen, int xSize, int ySize, boolean isSoftMax
            , boolean isShowLog, double convStudyPoint, ActiveFunction convFunction) throws Exception {
        this.initPower = true;//convDepthNerves
        this.convStudyPoint = convStudyPoint;
        int deep = getConvMyDep(xSize, ySize, step, kernLen);//卷积层深度
        List<Nerve> lastNerves = new ArrayList<>();
        sensoryNerveNub = 3;
        for (int i = 0; i < sensoryNerveNub; i++) {
            List<Nerve> depthNerves = initConDepthNerve(step, kernLen, deep, convFunction);//初始化卷积层隐层
            convDepthNerves.add(depthNerves);
            List<Nerve> firstDepthNerves = new ArrayList<>();
            firstDepthNerves.add(depthNerves.get(0));
            SensoryNerve sensoryNerve = new SensoryNerve(i + 1, 0);
            //感知神经元与第一层隐层神经元进行连接
            sensoryNerve.connect(firstDepthNerves);
            sensoryNerves.add(sensoryNerve);
            lastNerves.add(depthNerves.get(depthNerves.size() - 1));
        }
        initDepthNerve(step, kernLen, getNerveNub(deep, xSize, kernLen, step), getNerveNub(deep, ySize, kernLen, step));//初始化深度隐层神经元 depthNerves
        List<Nerve> firstNerves = depthNerves.get(0);//第一层隐层神经元
        List<Nerve> lastNerveList = depthNerves.get(depthNerves.size() - 1);//最后一层隐层神经元
        for (Nerve nerve : lastNerves) {
            nerve.connect(firstNerves);
        }
        for (Nerve nerve : firstNerves) {
            nerve.connectFather(lastNerves);
        }
        List<OutNerve> myOutNerveList = new ArrayList<>();
        //初始化输出神经元
        for (int i = 1; i < outNerveNub + 1; i++) {
            OutNerve outNerve = new OutNerve(i, hiddenNerveNub, 0, studyPoint, initPower,
                    activeFunction, false, isShowLog, rzType, lParam, isSoftMax, 0, 0);
            //输出层神经元连接最后一层隐层神经元
            outNerve.connectFather(lastNerveList);
            outNerves.add(outNerve);
            myOutNerveList.add(outNerve);
        }
        //生成softMax层
        if (isSoftMax) {//增加softMax层
            SoftMax softMax = new SoftMax(outNerveNub, false, myOutNerveList, isShowLog);
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
    public void init(boolean initPower, boolean isShowLog, boolean isSoftMax) throws Exception {//进行神经网络的初始化构建
        this.initPower = initPower;
        initDepthNerve(0, 0, 0, 0);//初始化深度隐层神经元
        List<Nerve> nerveList = depthNerves.get(0);//第一层隐层神经元
        //最后一层隐层神经元啊
        List<Nerve> lastNerveList = depthNerves.get(depthNerves.size() - 1);
        List<OutNerve> myOutNerveList = new ArrayList<>();
        //初始化输出神经元
        for (int i = 1; i < outNerveNub + 1; i++) {
            OutNerve outNerve = new OutNerve(i, hiddenNerveNub, 0, studyPoint, initPower,
                    activeFunction, false, isShowLog, rzType, lParam, isSoftMax, 0, 0);
            //输出层神经元连接最后一层隐层神经元
            outNerve.connectFather(lastNerveList);
            outNerves.add(outNerve);
            myOutNerveList.add(outNerve);
        }
        //生成softMax层
        if (isSoftMax) {//增加softMax层
            SoftMax softMax = new SoftMax(outNerveNub, false, myOutNerveList, isShowLog);
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
            SensoryNerve sensoryNerve = new SensoryNerve(i, 0);
            //感知神经元与第一层隐层神经元进行连接
            sensoryNerve.connect(nerveList);
            sensoryNerves.add(sensoryNerve);
        }

    }

    private void initDepthNerve(int step, int kernLen, int matrixX, int matrixY) throws Exception {//初始化隐层神经元1
        for (int i = 0; i < hiddenDepth; i++) {//遍历深度
            List<Nerve> hiddenNerveList = new ArrayList<>();
            double studyPoint = this.studyPoint;
            if (studyPoint <= 0 || studyPoint > 1) {
                throw new Exception("studyPoint Values range from 0 to 1");
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
                        upNub = sensoryNerveNub * matrixX * matrixY;
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
                        , rzType, lParam, step, kernLen, myMatrixX, myMatrixY, false);
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
