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
                new NerveManager(2, 4, 1, 4);
        //开始构建神经网络，参数为是否初始化权重及阈值,若
        nerveManager.setStudyPoint(0.1);//设置学习率(取值范围是0-1开区间)，若不设置默认为0.1
        nerveManager.init(true);
        nerveManager.setOutBack(new Test());//添加判断回调输出类
        List<SensoryNerve> sensoryNerves = nerveManager.getSensoryNerves();
        Map<Integer, Double> E1 = new HashMap<>();
        E1.put(1, 1.0);
        Map<Integer, Double> E2 = new HashMap<>();
        E2.put(1, 0.0);
        Random random = new Random();
        List<List<Double>> testList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            List<Double> dm = new ArrayList<>();
            dm.add(1.0);
            dm.add(0.8);
            dm.add(0.03);
            dm.add(0.06);
            testList.add(dm);
        }
        for (int i = 0; i < 1000; i++) {
            List<Double> ds = testList.get(i);
            sensoryNerves.get(0).postMessage(1, ds.get(0), true, E1);
            sensoryNerves.get(1).postMessage(1, ds.get(1), true, E1);
            sensoryNerves.get(0).postMessage(1, ds.get(2), true, E2);
            sensoryNerves.get(1).postMessage(1, ds.get(3), true, E2);
        }

        Nerve hiddenNerve = nerveManager.getDepthNerves().get(0).get(0);
        Nerve outNerver = nerveManager.getOutNevers().get(0);
        double hiddenTh = hiddenNerve.getThreshold();//隐层阈值
        double outTh = outNerver.getThreshold();//输出阈值
        System.out.println("hiddenTh==" + hiddenTh + ",outTh==" + outTh);
        sensoryNerves.get(0).postMessage(1, 1.0, false, E1);
        sensoryNerves.get(1).postMessage(1, 0.8, false, E1);
        sensoryNerves.get(0).postMessage(1, 0.03, false, E2);
        sensoryNerves.get(1).postMessage(1, 0.06, false, E2);
    }
}
