package org.wlld;

import org.wlld.config.Config;
import org.wlld.distinguish.Distinguish;
import org.wlld.entity.FoodPicture;
import org.wlld.entity.PicturePosition;

import java.util.ArrayList;
import java.util.List;

public class ImageTest {
    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.setTypeNub(2);//设置类别数量
        config.setBoxSize(253);//设置物体大小 单位像素
        config.setPictureNumber(5);//设置每个种类训练图片数量
        config.setShowLog(true);//输出学习时打印数据
        Distinguish distinguish = new Distinguish(config);
        List<FoodPicture> foodPictures = new ArrayList<>();
        for (int i = 1; i < 3; i++) {
            FoodPicture foodPicture = new FoodPicture();
            foodPictures.add(foodPicture);
            List<PicturePosition> picturePositionList = new ArrayList<>();
            foodPicture.setId(i + 1);//该图片类别
            foodPicture.setPicturePositionList(picturePositionList);
            for (int j = 1; j < 6; j++) {
                PicturePosition picturePosition = new PicturePosition();
                picturePosition.setUrl("图片url");
                picturePosition.setNeedCut(false);//是否需要剪切，充满全图不需要
                picturePositionList.add(picturePosition);
            }
        }
        distinguish.studyImage(foodPictures);//进行学习


    }
}
