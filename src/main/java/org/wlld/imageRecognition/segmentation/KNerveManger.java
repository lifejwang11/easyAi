package org.wlld.imageRecognition.segmentation;

import org.wlld.config.RZ;
import org.wlld.function.Sigmod;
import org.wlld.function.Tanh;
import org.wlld.imageRecognition.modelEntity.RgbBack;
import org.wlld.nerveCenter.NerveManager;
import org.wlld.nerveEntity.SensoryNerve;

import java.awt.image.Kernel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class KNerveManger {
    private Map<Integer, List<double[]>> featureMap = new HashMap<>();
    private int sensoryNerveNub;//输出神经元个数
    private int speciesNub;//种类数
    private NerveManager nerveManager;
    private int times;
    private RgbBack rgbBack = new RgbBack();

    public KNerveManger(int sensoryNerveNub, int speciesNub, int times) throws Exception {
        this.sensoryNerveNub = sensoryNerveNub;
        this.speciesNub = speciesNub;
        this.times = times;
        nerveManager = new NerveManager(sensoryNerveNub, 24, speciesNub,
                1, new Tanh(),//0.008  l1 0.02
                false, false, 0.008, RZ.L1, 0.01);
        nerveManager.init(true, false, true, true);
    }

    private Map<Integer, Double> createTag(int tag) {//创建一个标注
        Map<Integer, Double> tagging = new HashMap<>();
        Set<Integer> set = featureMap.keySet();
        for (int key : set) {
            double value = 0.0;
            if (key == tag) {
                value = 1.0;
            }
            tagging.put(key, value);
        }
        return tagging;
    }

    public void look(List<double[]> data) throws Exception {
        int size = data.size();
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            rgbBack.clear();
            post(data.get(i), null, false);
            int type = rgbBack.getId();
            if (map.containsKey(type)) {
                map.put(type, map.get(type) + 1);
            } else {
                map.put(type, 1);
            }
        }
        double max = 0;
        int type = 0;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            int nub = entry.getValue();
            if (nub > max) {
                max = nub;
                type = entry.getKey();
            }
        }
        double point = max / size;
        System.out.println("类型是：" + type + ",总票数:" + size + ",得票率：" + point);
        System.out.println("=================================完成");
    }

    public void startStudy() throws Exception {
        for (int i = 0; i < times; i++) {
            for (Map.Entry<Integer, List<double[]>> entry : featureMap.entrySet()) {
                int type = entry.getKey();
                System.out.println("=============================" + type);
                Map<Integer, Double> tag = createTag(type);//标注
                double[] feature = entry.getValue().get(i);//数据
                post(feature, tag, true);
            }
        }

//        for (Map.Entry<Integer, List<double[]>> entry : featureMap.entrySet()) {
//            int type = entry.getKey();
//            System.out.println("=============================" + type);
//            List<double[]> list = entry.getValue();
//            look(list);
//        }
    }

    private void post(double[] data, Map<Integer, Double> tagging, boolean isStudy) throws Exception {
        List<SensoryNerve> sensoryNerveList = nerveManager.getSensoryNerves();
        int size = sensoryNerveList.size();
        for (int i = 0; i < size; i++) {
            sensoryNerveList.get(i).postMessage(1, data[i], isStudy, tagging, rgbBack);
        }
    }

    public void setFeature(int type, List<double[]> feature) {
        if (type > 0) {
            if (featureMap.containsKey(type)) {
                featureMap.get(type).addAll(feature);
            } else {
                featureMap.put(type, feature);
            }
        }
    }

}
