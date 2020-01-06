package org.wlld;

import org.wlld.nerveCenter.NerveManager;
import org.wlld.nerveEntity.Nerve;
import org.wlld.nerveEntity.SensoryNerve;
import org.wlld.tools.ArithUtil;

import java.util.*;

/**
 * 测试入口类!
 */
public class App {
    public static void main(String[] args) throws Exception {
        createNerveTest();
    }

    public static void createNerveTest() throws Exception {
        //构建一个神经网络管理器，参数：(感知神经元个数，隐层神经元个数，输出神经元个数，输出神经元深度)
        //一个神经网络管理管理一个神经网络学习内容，
        NerveManager nerveManager =
                new NerveManager(2, 2, 1, 2);
        //开始构建神经网络，参数为是否初始化权重及阈值,若
        nerveManager.setStudyPoint(0.2);//设置学习率(取值范围是0-1开区间)，若不设置默认为0.1
        nerveManager.init(true);
        nerveManager.setOutBack(new Test());//添加判断回调输出类
        List<SensoryNerve> sensoryNerves = nerveManager.getSensoryNerves();
        Map<Integer, Double> E1 = new HashMap<>();
        E1.put(1, 1.0);
        Map<Integer, Double> E2 = new HashMap<>();
        E2.put(1, 0.0);
        List<List<Double>> testList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            double nub = ArithUtil.mul(ArithUtil.div(1, 1000), i);
            List<Double> dm = new ArrayList<>();
            dm.add(ArithUtil.add(0.5, nub));
            dm.add(ArithUtil.add(0.5, nub));
            dm.add(ArithUtil.add(-0.5, -nub));
            dm.add(ArithUtil.add(-0.5, -nub));
            testList.add(dm);
        }
        for (int i = 0; i < 1000; i++) {
            List<Double> ds = testList.get(i);
            sensoryNerves.get(0).postMessage(1, ds.get(0), true, E1);
            sensoryNerves.get(1).postMessage(1, ds.get(1), true, E1);
            sensoryNerves.get(0).postMessage(1, ds.get(2), true, E2);
            sensoryNerves.get(1).postMessage(1, ds.get(3), true, E2);
        }

        Nerve outNerver = nerveManager.getOutNevers().get(0);
        double outTh = outNerver.getThreshold();//输出阈值
        System.out.println("outTh==" + outTh);
        for (int i = 1; i < 21; i++) {
            double nub = ArithUtil.mul(ArithUtil.div(1, 100), i);
            sensoryNerves.get(0).postMessage(1, ArithUtil.add(0.5, nub), false, E1);
            sensoryNerves.get(1).postMessage(1, ArithUtil.add(0.5, nub), false, E1);
            sensoryNerves.get(0).postMessage(1, ArithUtil.add(-0.5, -nub), false, E2);
            sensoryNerves.get(1).postMessage(1, ArithUtil.add(-0.5, -nub), false, E2);
            System.out.println("====================================");
        }
    }
}
