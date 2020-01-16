package org.wlld.nerveCenter;

import org.wlld.i.ActiveFunction;
import org.wlld.i.OutBack;
import org.wlld.nerveEntity.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * 神经网络管理工具
 * 创建神经网络
 * @date 11:05 上午 2019/12/21
 */
public class NerveManager {
    private int hiddenNerverNub;//隐层神经元个数
    private int sensoryNerveNub;//输入神经元个数
    private int outNerveNub;//输出神经元个数
    private int hiddenDepth;//隐层深度
    private List<SensoryNerve> sensoryNerves = new ArrayList<>();//感知神经元
    private List<List<Nerve>> depthNerves = new ArrayList<>();//隐层神经元
    private List<Nerve> outNevers = new ArrayList<>();//输出神经元
    private boolean initPower;
    private OutBack outBack;
    private double studyPoint = 0.1;//学习率
    private ActiveFunction activeFunction;

    public double getStudyPoint() {
        return studyPoint;
    }

    public void setStudyPoint(double studyPoint) throws Exception {
        //设置学习率
        if (studyPoint < 1 && studyPoint > 0) {
            this.studyPoint = studyPoint;
        } else {
            throw new Exception("studyPoint Values range from 0 to 1");
        }
    }

    public ModelParameter getModelParameter() {//获取当前模型参数
        ModelParameter modelParameter = new ModelParameter();
        List<List<NerveStudy>> studyDepthNerves = new ArrayList<>();//隐层神经元模型
        List<NerveStudy> outStudyNevers = new ArrayList<>();//输出神经元
        for (int i = 0; i < depthNerves.size(); i++) {
            //创建一层深度的隐层神经元模型
            List<Nerve> depthNerve = depthNerves.get(i);//隐层神经元
            List<NerveStudy> deepNerve = new ArrayList<>();
            for (int j = 0; j < depthNerve.size(); j++) {
                //遍历某一层深度的所有隐层神经元
                NerveStudy nerveStudy = new NerveStudy();
                Nerve hiddenNerve = depthNerve.get(j);
                nerveStudy.setThreshold(hiddenNerve.getThreshold());
                nerveStudy.setDendrites(hiddenNerve.getDendrites());
                deepNerve.add(nerveStudy);
            }
            studyDepthNerves.add(deepNerve);
        }
        for (int i = 0; i < outNevers.size(); i++) {
            NerveStudy nerveStudy = new NerveStudy();
            Nerve outNerve = outNevers.get(i);
            nerveStudy.setThreshold(outNerve.getThreshold());
            nerveStudy.setDendrites(outNerve.getDendrites());
            outStudyNevers.add(nerveStudy);
        }
        modelParameter.setDepthNerves(studyDepthNerves);
        modelParameter.setOutNevers(outStudyNevers);
        return modelParameter;
    }

    //注入模型参数
    public void insertModelParameter(ModelParameter modelParameter) {
        List<List<NerveStudy>> depthStudyNerves = modelParameter.getDepthNerves();//隐层神经元
        List<NerveStudy> outStudyNevers = modelParameter.getOutNevers();//输出神经元
        //隐层神经元参数注入
        for (int i = 0; i < depthNerves.size(); i++) {
            List<NerveStudy> depth = depthStudyNerves.get(i);//对应的学习结果
            List<Nerve> depthNerve = depthNerves.get(i);//深度隐层神经元
            for (int j = 0; j < depthNerve.size(); j++) {//遍历当前深度神经元
                Nerve nerve = depthNerve.get(j);
                NerveStudy nerveStudy = depth.get(j);
                //学习结果
                Map<Integer, Double> studyDendrites = nerveStudy.getDendrites();
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
        for (int i = 0; i < outNevers.size(); i++) {
            Nerve outNerve = outNevers.get(i);
            NerveStudy nerveStudy = outStudyNevers.get(i);
            outNerve.setThreshold(nerveStudy.getThreshold());
            Map<Integer, Double> dendrites = outNerve.getDendrites();
            Map<Integer, Double> studyDendrites = nerveStudy.getDendrites();
            for (Map.Entry<Integer, Double> outEntry : dendrites.entrySet()) {
                int key = outEntry.getKey();
                dendrites.put(key, studyDendrites.get(key));
            }
        }
    }

    //初始化神经元参数
    public NerveManager(int sensoryNerveNub, int hiddenNerverNub, int outNerveNub
            , int hiddenDepth, ActiveFunction activeFunction) throws Exception {
        if (sensoryNerveNub > 0 && hiddenNerverNub > 0 && outNerveNub > 0 && hiddenDepth > 0 && activeFunction != null) {
            this.hiddenNerverNub = hiddenNerverNub;
            this.sensoryNerveNub = sensoryNerveNub;
            this.outNerveNub = outNerveNub;
            this.hiddenDepth = hiddenDepth;
            this.activeFunction = activeFunction;
        } else {
            throw new Exception("param is null");
        }
    }

    public void setOutBack(OutBack outBack) {//设置回调类
        this.outBack = outBack;
        for (Nerve nerve : outNevers) {
            OutNerve outNerve = (OutNerve) nerve;
            outNerve.setOutBack(outBack);
        }
    }

    public OutBack getOutBack() {//获取回调类的引用
        return outBack;
    }

    public int getHiddenNerverNub() {
        return hiddenNerverNub;
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
        return outNevers;
    }

    public List<SensoryNerve> getSensoryNerves() {//获取感知神经元集合
        return sensoryNerves;
    }

    public void init(boolean initPower, boolean isMatrix) throws Exception {//进行神经网络的初始化构建
        this.initPower = initPower;
        initDepthNerve(isMatrix);//初始化深度隐层神经元
        List<Nerve> nerveList = depthNerves.get(0);//第一层隐层神经元
        //最后一层隐层神经元啊
        List<Nerve> lastNeveList = depthNerves.get(depthNerves.size() - 1);
        //初始化输出神经元
        for (int i = 1; i < outNerveNub + 1; i++) {
            OutNerve outNerve = new OutNerve(i, hiddenNerverNub, 0, studyPoint, initPower, activeFunction, isMatrix);
            //输出层神经元连接最后一层隐层神经元
            outNerve.connectFathor(lastNeveList);
            outNevers.add(outNerve);
        }
        //最后一层隐层神经元 与输出神经元进行连接
        for (Nerve nerve : lastNeveList) {
            nerve.connect(outNevers);
        }

        //初始化感知神经元
        for (int i = 1; i < sensoryNerveNub + 1; i++) {
            SensoryNerve sensoryNerve = new SensoryNerve(i, 0);
            //感知神经元与第一层隐层神经元进行连接
            sensoryNerve.connect(nerveList);
            sensoryNerves.add(sensoryNerve);
        }

    }

    private void initDepthNerve(boolean isMatrix) throws Exception {//初始化隐层神经元1
        for (int i = 0; i < hiddenDepth; i++) {//遍历深度
            List<Nerve> hiddenNerveList = new ArrayList<>();
            for (int j = 1; j < hiddenNerverNub + 1; j++) {//遍历同级
                int upNub = 0;
                int downNub = 0;
                if (i == 0) {
                    upNub = sensoryNerveNub;
                } else {
                    upNub = hiddenNerverNub;
                }
                if (i == hiddenDepth - 1) {//最后一层隐层神经元z
                    downNub = outNerveNub;
                } else {
                    downNub = hiddenNerverNub;
                }
                HiddenNerve hiddenNerve = new HiddenNerve(j, i + 1, upNub, downNub, studyPoint, initPower, activeFunction, isMatrix);
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
                nextHiddenNerve.connectFathor(hiddenNerveList);
            }
        }
    }
}
