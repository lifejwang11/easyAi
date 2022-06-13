package org.wlld;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.wlld.config.Config;
import org.wlld.distinguish.Distinguish;
import org.wlld.entity.FoodPicture;
import org.wlld.entity.Model;
import org.wlld.entity.PicturePosition;
import org.wlld.entity.ThreeChannelMatrix;
import org.wlld.tools.Picture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImageTest {
    public static void main(String[] args) throws Exception {
        dish();
    }

    public static void dish() throws Exception {//识别
        Picture picture = new Picture();
        Config config = new Config();
        config.setTypeNub(2);//设置类别数量
        config.setBoxSize(125);//设置物体大小 单位像素
        config.setPictureNumber(5);//设置每个种类训练图片数量
        config.setPth(0.7);//设置可信概率，只有超过可信概率阈值，得出的结果才是可信的
        config.setShowLog(true);//输出学习时打印数据
        Distinguish distinguish = new Distinguish(config);
        distinguish.insertModel(JSONObject.parseObject(ModelData.DATA, Model.class));
        for (int i = 1; i < 8; i++) {
            System.out.println("i====" + i);
            ThreeChannelMatrix t = picture.getThreeMatrix("E:\\ls\\fp15\\t" + i + ".jpg");
            Map<Integer, Double> map = distinguish.distinguish(t);
            for (Map.Entry<Integer, Double> entry : map.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }
        }
    }

    public static void study() throws Exception {//学习
        Picture picture = new Picture();
        Config config = new Config();
        config.setTypeNub(2);//设置类别数量
        config.setBoxSize(125);//设置物体大小 单位像素
        config.setPictureNumber(5);//设置每个种类训练图片数量
        config.setPth(0.6);//设置可信概率，只有超过可信概率阈值，得出的结果才是可信的
        config.setShowLog(true);//输出学习时打印数据
        Distinguish distinguish = new Distinguish(config);
        distinguish.setBackGround(picture.getThreeMatrix("E:\\ls\\fp15\\back.jpg"));
        List<FoodPicture> foodPictures = new ArrayList<>();
        for (int i = 1; i < 3; i++) {
            FoodPicture foodPicture = new FoodPicture();
            foodPictures.add(foodPicture);
            List<PicturePosition> picturePositionList = new ArrayList<>();
            foodPicture.setId(i + 1);//该图片类别
            foodPicture.setPicturePositionList(picturePositionList);
            for (int j = 1; j < 6; j++) {
                String name;
                if (i == 1) {
                    name = "a";
                } else {
                    name = "b";
                }
                PicturePosition picturePosition = new PicturePosition();
                picturePosition.setUrl("E:\\ls\\fp15\\" + name + i + ".jpg");
                picturePosition.setNeedCut(false);//是否需要剪切，充满全图不需要
                picturePositionList.add(picturePosition);
            }
        }
        distinguish.studyImage(foodPictures);//进行学习
        System.out.println(JSON.toJSONString(distinguish.getModel()));
        for (int i = 1; i < 8; i++) {
            System.out.println("i====" + i);
            ThreeChannelMatrix t = picture.getThreeMatrix("E:\\ls\\fp15\\t" + i + ".jpg");
            Map<Integer, Double> map = distinguish.distinguish(t);
            for (Map.Entry<Integer, Double> entry : map.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }
        }
    }
}
