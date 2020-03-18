package org.wlld;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.wlld.MatrixTools.Matrix;
import org.wlld.config.StudyPattern;
import org.wlld.function.Sigmod;
import org.wlld.imageRecognition.Operation;
import org.wlld.imageRecognition.Picture;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.imageRecognition.border.Frame;
import org.wlld.imageRecognition.border.FrameBody;
import org.wlld.nerveCenter.NerveManager;
import org.wlld.nerveEntity.ModelParameter;
import org.wlld.nerveEntity.SensoryNerve;

import java.util.*;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws Exception {
        test3();
    }

    public static void test3() throws Exception {
        NerveManager nerveManager = new NerveManager(3, 6, 3
                , 3, new Sigmod(), false, true);
        nerveManager.init(true, false, false);//初始化
        List<Map<Integer, Double>> data = new ArrayList<>();//正样本
        List<Map<Integer, Double>> dataB = new ArrayList<>();//负样本
        List<Map<Integer, Double>> dataC = new ArrayList<>();//负样本
        Random random = new Random();
        for (int i = 0; i < 4000; i++) {
            Map<Integer, Double> map1 = new HashMap<>();
            Map<Integer, Double> map2 = new HashMap<>();
            Map<Integer, Double> map3 = new HashMap<>();
            map1.put(0, 1 + random.nextDouble());
            map1.put(1, 1 + random.nextDouble());
            map1.put(2, 0.0);
            //产生鲜明区分
            map2.put(0, random.nextDouble());
            map2.put(1, random.nextDouble());
            map2.put(2, 0.0);
            //
            map3.put(0, 2 + random.nextDouble());
            map3.put(1, 2 + random.nextDouble());
            map3.put(2, 0.0);
            data.add(map1);
            dataB.add(map2);
            dataC.add(map3);
        }
        Map<Integer, Double> right = new HashMap<>();
        Map<Integer, Double> wrong = new HashMap<>();
        Map<Integer, Double> other = new HashMap<>();
        right.put(1, 1.0);

        wrong.put(2, 1.0);

        other.put(3, 1.0);
        for (int i = 0; i < data.size(); i++) {
            Map<Integer, Double> map1 = data.get(i);
            Map<Integer, Double> map2 = dataB.get(i);
            Map<Integer, Double> map3 = dataC.get(i);
            post(nerveManager.getSensoryNerves(), map1, right, null, true);
            post(nerveManager.getSensoryNerves(), map2, wrong, null, true);
            post(nerveManager.getSensoryNerves(), map3, other, null, true);
        }

        List<Map<Integer, Double>> data2 = new ArrayList<>();
        List<Map<Integer, Double>> data2B = new ArrayList<>();
        List<Map<Integer, Double>> data2C = new ArrayList<>();//负样本
        for (int i = 0; i < 20; i++) {
            Map<Integer, Double> map1 = new HashMap<>();
            Map<Integer, Double> map2 = new HashMap<>();
            Map<Integer, Double> map3 = new HashMap<>();
            map1.put(0, 1 + random.nextDouble());
            map1.put(1, 1 + random.nextDouble());
            map1.put(2, 0.0);

            map2.put(0, random.nextDouble());
            map2.put(1, random.nextDouble());
            map2.put(2, 0.0);

            map3.put(0, 2 + random.nextDouble());
            map3.put(1, 2 + random.nextDouble());
            map3.put(2, 0.0);
            data2.add(map1);
            data2B.add(map2);
            data2C.add(map3);
        }
        Back back = new Back();
        for (Map<Integer, Double> map : data2) {
            post(nerveManager.getSensoryNerves(), map, null, back, false);
            System.out.println("=====================");
        }
    }

    public static void post(List<SensoryNerve> sensoryNerveList, Map<Integer, Double> data
            , Map<Integer, Double> tagging, Back back, boolean isStudy) throws Exception {
        int size = sensoryNerveList.size();
        for (int i = 0; i < size; i++) {
            sensoryNerveList.get(i).postMessage(1, data.get(i), isStudy, tagging, back);
        }
    }

}
