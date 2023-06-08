package org.wlld;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.wlld.MatrixTools.Matrix;
import org.wlld.config.Config;
import org.wlld.config.RZ;
import org.wlld.distinguish.Distinguish;
import org.wlld.entity.FoodPicture;
import org.wlld.entity.Model;
import org.wlld.entity.PicturePosition;
import org.wlld.entity.ThreeChannelMatrix;
import org.wlld.function.Tanh;
import org.wlld.i.OutBack;
import org.wlld.nerveCenter.NerveManager;
import org.wlld.nerveEntity.SensoryNerve;
import org.wlld.pso.PSO;
import org.wlld.tools.FastPictureExcerpt;
import org.wlld.tools.Picture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;

public class ImageTest {


    public static void main(String[] args) throws Exception {
        long a = System.currentTimeMillis();
        int[] money = test(200, 50);
        long b = System.currentTimeMillis() - a;
        System.out.println("耗时:" + b);
        int sigma = 0;
        for (int i = 0; i < money.length; i++) {
            sigma = sigma + money[i];
        }
        System.out.println("sigma:" + sigma);
    }

    public static int[] test(double value, int peopleNumber) throws Exception {
        int fen = (int) (value * 100);
        int[] money = new int[peopleNumber];
        if (fen > peopleNumber) {
            TestPso testPso = new TestPso();
            Random random = new Random();
            double subAvgE = value / peopleNumber * random.nextDouble();
            testPso.setTimes(value);
            testPso.setMyAvgSub(subAvgE);
            double[] min = new double[peopleNumber];
            double[] max = new double[peopleNumber];
            for (int i = 0; i < peopleNumber; i++) {
                min[i] = 0.01;
                max[i] = value;
            }
            PSO pso = new PSO(peopleNumber, min, max, 200, 200
                    , testPso, 0.5, 2, 2, false, 10, 1);
            pso.start();
            double[] parameter = pso.getAllBest();
            int sigma = 0;
            for (int i = 0; i < parameter.length; i++) {
                int three = (int) (parameter[i] * 100);
                sigma = sigma + three;
                money[i] = three;
            }
            System.out.println("分配结果：" + sigma);
            if (fen > sigma) {//还有剩余 找到最小金额者，加上
                int minMoney = fen;
                int minIndex = 0;
                int sub = fen - sigma;
                for (int i = 0; i < peopleNumber; i++) {
                    if (money[i] < minMoney) {
                        minIndex = i;
                        minMoney = money[i];
                    }
                }
                money[minIndex] = money[minIndex] + sub;
            } else if (fen < sigma) {//多了 平均减少
                int sub = sigma - fen;
                while (sub > 0) {
                    for (int i = 0; i < peopleNumber; i++) {
                        if (money[i] > 1) {
                            money[i] = money[i] - 1;
                            sub--;
                            if (sub == 0) {
                                break;
                            }
                        }
                    }
                }
            }
        } else if (fen == peopleNumber) {
            for (int i = 0; i < peopleNumber; i++) {
                money[i] = 1;
            }
        } else {
            money = null;
        }
        return money;
    }

    public ByteArrayOutputStream narrow2(String inputStream, int size) throws Exception {//图片缩小
        Picture picture = new Picture();
        ThreeChannelMatrix imageMatrix = picture.getThreeMatrix(inputStream);
        Matrix matrix = imageMatrix.getMatrixB();
        int maxXSize = matrix.getX();//图像x大小
        int maxYSize = matrix.getY();//图像y大小
        int yKern = maxYSize / size;//定y
        int xKern = maxXSize / size;
        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) bi.getGraphics();
        int xIndex = 0;
        int yIndex = 0;
        for (int i = 0; i < maxXSize - xKern; i += xKern) {
            for (int j = 0; j < maxYSize - yKern; j += yKern) {
                ThreeChannelMatrix threeChannelMatrix = imageMatrix.cutChannel(i, j, xKern, yKern);
                Matrix matrixR = threeChannelMatrix.getMatrixR();
                Matrix matrixG = threeChannelMatrix.getMatrixG();
                Matrix matrixB = threeChannelMatrix.getMatrixB();
                int r = (int) (matrixR.getAVG() * 255);
                int g = (int) (matrixG.getAVG() * 255);
                int b = (int) (matrixB.getAVG() * 255);
                g2.setColor(new Color(r, g, b));
                g2.drawRect(yIndex, xIndex, 1, 1);
                yIndex++;
            }
            yIndex = 0;
            xIndex++;
        }
        ByteArrayOutputStream ar = new ByteArrayOutputStream();
        ImageIO.write(bi, "PNG", ar);
        return ar;
    }

    public static void dish() throws Exception {//识别
        //初始化过程，在服务启动时候 初始化一次 且进行单例化
        Config config = new Config();
        config.setTypeNub(2);//设置类别数量
        config.setBoxSize(125);//设置物体大小 单位像素
        config.setPictureNumber(5);//设置每个种类训练图片数量
        config.setPth(0.55);//设置可信概率，只有超过可信概率阈值，得出的结果才是可信的
        config.setShowLog(true);//输出学习时打印数据
        Distinguish distinguish = new Distinguish(config);
        distinguish.insertModel(JSONObject.parseObject(ModelData.DATA, Model.class));
        //识别过程 是直接拿单例化的识别类进行操作 而不进行初始化
        Picture picture = new Picture();
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
        Picture picture = new Picture();//图片解析类 图片（文件）-三通道矩阵
        Config config = new Config();//现有的环境业务进行参数配置
        config.setTypeNub(2);//设置类别数量
        config.setBoxSize(125);//设置物体大小 单位像素 125*125 矩形
        config.setPictureNumber(5);//设置每个种类训练图片数量
        config.setPth(0.55);//设置可信概率，只有超过可信概率阈值，得出的结果才是可信的0-1
        config.setShowLog(true);//输出学习时打印数据
        Distinguish distinguish = new Distinguish(config);//识别类
        distinguish.setBackGround(picture.getThreeMatrix("E:\\ls\\fp15\\back.jpg"));//塞入背景图片
        List<FoodPicture> foodPictures = new ArrayList<>();//所有识别物体的标注集合
        for (int i = 1; i < 3; i++) {//加载图片全过程
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
        distinguish.studyImage(foodPictures);//进行学习 耗时较长、、、、
        Model model = distinguish.getModel();
        System.out.println(JSON.toJSONString(model));
        //识别过程
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
