package org.wlld;

import org.wlld.nerveCenter.NerveManager;
import org.wlld.nerveEntity.SensoryNerve;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                new NerveManager(2, 3, 1, 2);
        //开始构建神经网络，参数为是否初始化权重及阈值,若
        nerveManager.init(true);
        nerveManager.setStudyPoint(0.2);//设置学习率(取值范围是0-1开区间)，若不设置默认为0.1
        nerveManager.setOutBack(new Test());//添加判断回调输出类
        List<SensoryNerve> sensoryNerves = nerveManager.getSensoryNerves();
        Map<Integer, Double> E = new HashMap<>();
        for (int i = 0; i < nerveManager.getOutNerveNub(); i++) {
            E.put(i + 1, 1.0);
        }
        for (int i = 0; i < sensoryNerves.size(); i++) {
            sensoryNerves.get(i).postMessage(1, 2 + i, true, E);
        }
        for (int i = 0; i < sensoryNerves.size(); i++) {
            sensoryNerves.get(i).postMessage(1, 2 + i, false, E);
        }
    }
}
