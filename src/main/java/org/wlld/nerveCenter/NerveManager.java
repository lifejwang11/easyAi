package org.wlld.nerveCenter;

import org.wlld.nerveEntity.HiddenNerve;
import org.wlld.nerveEntity.Nerve;
import org.wlld.nerveEntity.OutNerve;
import org.wlld.nerveEntity.SensoryNerve;

import java.util.ArrayList;
import java.util.List;

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
    private List<SensoryNerve> sensoryNerves = new ArrayList<>();
    private List<List<Nerve>> depthNerves = new ArrayList<>();

    public NerveManager(int sensoryNerveNub, int hiddenNerverNub, int outNerveNub
            , int hiddenDepth) {
        this.hiddenNerverNub = hiddenNerverNub;
        this.sensoryNerveNub = sensoryNerveNub;
        this.outNerveNub = outNerveNub;
        this.hiddenDepth = hiddenDepth;
    }

    public List<SensoryNerve> getSensoryNerves() {//获取感知神经元集合
        return sensoryNerves;
    }

    public void init() {//进行神经网络的初始化构建
        initDepthNerve();//初始化深度隐层神经元
        List<Nerve> nerveList = depthNerves.get(0);//第一层隐层神经元
        //最后一层隐层神经元啊
        List<Nerve> lastNeveList = depthNerves.get(depthNerves.size() - 1);
        //初始化输出神经元
        List<Nerve> outNevers = new ArrayList<>();
        for (int i = 1; i < outNerveNub + 1; i++) {
            OutNerve outNerve = new OutNerve(i, hiddenNerverNub);
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

    private void initDepthNerve() {//初始化隐层神经元1
        for (int i = 0; i < hiddenDepth; i++) {//遍历深度
            List<Nerve> hiddenNerveList = new ArrayList<>();
            for (int j = 1; j < hiddenNerverNub + 1; j++) {//遍历同级
                int upNub = 0;
                if (i == 0) {
                    upNub = sensoryNerveNub;
                } else {
                    upNub = hiddenNerverNub;
                }
                HiddenNerve hiddenNerve = new HiddenNerve(j, i + 1, upNub);
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
