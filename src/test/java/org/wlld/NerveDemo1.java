package org.wlld;

import org.wlld.MatrixTools.Matrix;
import org.wlld.function.Sigmod;
import org.wlld.i.OutBack;
import org.wlld.nerveCenter.NerveManager;
import org.wlld.nerveEntity.SensoryNerve;

import java.util.*;

/**
 * 简单神经网络<br>
 *
 * 需求: 需要一个神经网络来帮我识别 <i>大于0</i> 的数字 <br>
 *
 * 设计: 这里使用了4层神经网络以及1万个训练量<br>
 *
 * 训练完成后,需要对数据进行检验<br>
 *
 * @Author: Timeless小帅
 * @Date: 2020-03-12 11:50
 */
public class NerveDemo1 {

    public static void main(String[] args) throws Exception {
        /* NerveManager 的构造函数参考
         *
         * @param sensoryNerveNub 输入神经元个数
         * @param hiddenNerverNub 隐层神经元个数
         * @param outNerveNub 输出神经元个数
         * @param hiddenDepth 隐层深度
         * @param activeFunction 激活函数
         * @param isDynamic 是否是动态神经元
         */
        NerveManager nerveManager = new NerveManager(2,6,1,4,new Sigmod(),false);
        nerveManager.init(true,false);


        //创建训练
        List<Map<Integer,Double>> list_right = new LinkedList<>();//存放正确的值
        List<Map<Integer,Double>> list_wrong = new LinkedList<>();//存放错误的值
        Random random = new Random();
        for (int i = 0; i < 10000; i++) {
            Map<Integer,Double> mp1 = new HashMap<>();
            Map<Integer,Double> mp2 = new HashMap<>();
            mp1.put(0,random.nextDouble());
            mp1.put(1,random.nextDouble());
            mp2.put(0,-random.nextDouble());//负样本:负数永远小于0
            mp2.put(1,-random.nextDouble());
            list_right.add(mp1);
            list_wrong.add(mp2);
        }

        //做一个正标注和负标注
        Map<Integer,Double> right = new HashMap<>();
        Map<Integer,Double> wrong = new HashMap<>();
        right.put(1,1.0);
        wrong.put(1,0.0);

        //开始训练
        for (int i = 0; i < list_right.size(); i++) {
            Map<Integer,Double> mp1 = list_right.get(i);
            Map<Integer,Double> mp2 = list_wrong.get(i);
            //这里的post的训练
            post(nerveManager.getSensoryNerves(),mp1,right,null,true);
            post(nerveManager.getSensoryNerves(),mp2,wrong,null,true);
        }

        //测试 这里测试10个数据
        List<Map<Integer,Double>> test1 = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            Map<Integer,Double> mp1 = new HashMap<>();
            if (i==4){//在第五次的时候给一个错误的值//看看能否正确识别
                mp1.put(0,-random.nextDouble());
                mp1.put(1,-random.nextDouble());
                test1.add(mp1);
                continue;
            }
            mp1.put(0,random.nextDouble());
            mp1.put(1,random.nextDouble());
            test1.add(mp1);
        }
        //查看结果
        Back back = new Back();
        for (Map<Integer, Double> test_data : test1) {
            //这里的post是进行学习
            post(nerveManager.getSensoryNerves(),test_data,null,back,false);
        }
        /*
         * 输出结果:
         *
         * eventId = 1 , id = 1 , out = 0.9853120208
         * eventId = 1 , id = 1 , out = 0.9834875798
         * eventId = 1 , id = 1 , out = 0.9851957982
         * eventId = 1 , id = 1 , out = 0.9850682959
         * eventId = 1 , id = 1 , out = 0.028913533
         * eventId = 1 , id = 1 , out = 0.9840823386
         * eventId = 1 , id = 1 , out = 0.9846590694
         * eventId = 1 , id = 1 , out = 0.9853884496
         * eventId = 1 , id = 1 , out = 0.9765962636
         * eventId = 1 , id = 1 , out = 0.8927546188
         *
         * 很显然第五个数值非常小,意味着不是我们想要的结果
         */


    }

    /**
     * 提交函数
     * @param sensoryNerveList 感知神经元输入层集合
     * @param data 输入点的数据
     * @param tagging 标注
     * @param back 返回结果 (训练的时候可以写null)
     * @param isStudy 是否是学习模式(训练的时候为true)
     */
    public static void post(List<SensoryNerve> sensoryNerveList,Map<Integer,Double>data,Map<Integer,Double>tagging,Back back,boolean isStudy) throws Exception {
        final int size = sensoryNerveList.size();
        for (int i = 0; i < size; i++) {
            sensoryNerveList.get(i).postMessage(1,data.get(i),isStudy,tagging,back);
        }
    }



    /** 回调(查看结果) **/
    static class Back implements OutBack{

        /**
         * 回调
         * @param out     输出数值
         * @param id      输出神经元ID
         * @param eventId 事件ID
         */
        @Override
        public void getBack(double out, int id, long eventId) {
            System.out.println("eventId = " + eventId+ " , id = " + id+" , out = " + out);
        }

        @Override
        public void getBackMatrix(Matrix matrix, long eventId) {

        }
    }


}
