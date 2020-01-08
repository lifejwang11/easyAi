package org.wlld;

import org.wlld.nerveCenter.NerveManager;

/**
 * 测试入口类!
 */
public class App {
    public static void main(String[] args) throws Exception {
        createNerveTest();
    }

    public static void createNerveTest() throws Exception {
        //构建一个全连接神经网络管理器，参数：(感知神经元个数，隐层神经元个数，输出神经元个数，输出神经元深度)
        //一个神经网络管理管理一个神经网络学习内容，
        NerveManager nerveManager =
                new NerveManager(2, 2, 1, 1);
        //开始构建神经网络，参数为是否初始化权重及阈值,若
        nerveManager.setStudyPoint(0.1);//设置学习率(取值范围是0-1开区间)，若不设置默认为0.1
        nerveManager.init(true);


    }
}
