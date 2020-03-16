package org.wlld;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.wlld.MatrixTools.Matrix;
import org.wlld.config.Classifier;
import org.wlld.config.StudyPattern;
import org.wlld.imageRecognition.Operation;
import org.wlld.imageRecognition.Picture;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.imageRecognition.ThreeChannelMatrix;
import org.wlld.imageRecognition.border.Frame;
import org.wlld.imageRecognition.border.FrameBody;
import org.wlld.nerveEntity.ModelParameter;
import org.wlld.tools.ArithUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lidapeng
 * @description 测试入口类
 * @date 11:35 上午 2020/1/18
 */
public class HelloWorld {
    public static void main(String[] args) throws Exception {
        //test1();
        food();
    }

    public static void test() throws Exception {
        Picture picture = new Picture();
        TempleConfig templeConfig = new TempleConfig(true);
        templeConfig.setClassifier(Classifier.DNN);
        templeConfig.init(StudyPattern.Accuracy_Pattern, true, 1000, 1000, 3);
        Operation operation = new Operation(templeConfig);
        for (int i = 1; i < 300; i++) {//一阶段
            System.out.println("study1===================" + i);
            //读取本地URL地址图片,并转化成矩阵
            ThreeChannelMatrix a = picture.getThreeMatrix("/Users/lidapeng/Desktop/myDocment/picture/a" + i + ".jpg");
            //Matrix b = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/b" + i + ".jpg");
            ThreeChannelMatrix c = picture.getThreeMatrix("/Users/lidapeng/Desktop/myDocment/picture/c" + i + ".jpg");
            //将图像矩阵和标注加入进行学习，Accuracy_Pattern 模式 进行第二次学习
            //第二次学习的时候，第三个参数必须是 true
            // operation.learning(f, 0, false);
            operation.threeLearning(a, 1, false);
            //operation.learning(b, 2, false);
            operation.threeLearning(c, 2, false);
        }
        for (int i = 1; i < 300; i++) {
            ThreeChannelMatrix a = picture.getThreeMatrix("/Users/lidapeng/Desktop/myDocment/picture/a" + i + ".jpg");
            //Matrix b = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/b" + i + ".jpg");
            ThreeChannelMatrix c = picture.getThreeMatrix("/Users/lidapeng/Desktop/myDocment/picture/c" + i + ".jpg");
            operation.threeNormalization(a);
            //operation.normalization(b);
            operation.threeNormalization(c);
        }
        templeConfig.getNormalization().avg();
        for (int i = 1; i < 300; i++) {
            System.out.println("study2==================" + i);
            //读取本地URL地址图片,并转化成矩阵
            ThreeChannelMatrix a = picture.getThreeMatrix("/Users/lidapeng/Desktop/myDocment/picture/a" + i + ".jpg");
            // Matrix b = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/b" + i + ".jpg");
            ThreeChannelMatrix c = picture.getThreeMatrix("/Users/lidapeng/Desktop/myDocment/picture/c" + i + ".jpg");
            //Matrix d = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/d" + i + ".jpg");
            operation.threeLearning(a, 1, true);
            operation.threeLearning(c, 2, true);

        }
        int wrong = 0;
        int allNub = 0;
        for (int i = 300; i <= 320; i++) {
            //读取本地URL地址图片,并转化成矩阵
            ThreeChannelMatrix a = picture.getThreeMatrix("/Users/lidapeng/Desktop/myDocment/picture/a" + i + ".jpg");
            // Matrix b = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/b" + i + ".jpg");
            ThreeChannelMatrix c = picture.getThreeMatrix("/Users/lidapeng/Desktop/myDocment/picture/c" + i + ".jpg");
            //将图像矩阵和标注加入进行学习，Accuracy_Pattern 模式 进行第二次学习
            //第二次学习的时候，第三个参数必须是 true
            allNub += 2;
            int an = operation.toThreeSee(a);
            int cn = operation.toThreeSee(c);
            if (an != 1) {
                wrong++;
            }
            if (cn != 2) {
                wrong++;
            }
        }
        double wrongPoint = ArithUtil.div(wrong, allNub);
        System.out.println("错误率：" + (wrongPoint * 100) + "%");
    }

    public static void food2() throws Exception {
        Picture picture = new Picture();
        TempleConfig templeConfig = new TempleConfig(false);
        templeConfig.init(StudyPattern.Speed_Pattern, true, 1000, 1000, 2);
        Operation operation = new Operation(templeConfig);
        Map<Integer, Double> right = new HashMap<>();
        Map<Integer, Double> wrong = new HashMap<>();
        right.put(1, 1.0);
        wrong.put(2, 1.0);
        for (int j = 0; j < 20; j++) {
            for (int i = 1; i < 1500; i++) {//一阶段
                System.out.println("study1===================" + i);
                //读取本地URL地址图片,并转化成矩阵
                Matrix a = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/a" + i + ".jpg");
                Matrix c = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/c" + i + ".jpg");
                operation.study(a, right);
                operation.study(c, wrong);

            }
        }

    }

    public static void food() throws Exception {
        Picture picture = new Picture();
        TempleConfig templeConfig = new TempleConfig(false);
        //templeConfig.setHavePosition(true);
//        Frame frame = new Frame();
//        frame.setWidth(640);
//        frame.setHeight(640);
//        frame.setLengthHeight(640);
//        frame.setLengthWidth(640);
//        templeConfig.setFrame(frame);
        templeConfig.setClassifier(Classifier.DNN);
        templeConfig.init(StudyPattern.Accuracy_Pattern, true, 1000, 1000, 2);
//        ModelParameter modelParameter2 = JSON.parseObject(ModelData.DATA, ModelParameter.class);
//        templeConfig.insertModel(modelParameter2);
        Operation operation = new Operation(templeConfig);
        //a b c d 物品  e是背景
        // 一阶段
        for (int j = 0; j < 2; j++) {

            for (int i = 1; i < 1500; i++) {//一阶段
                System.out.println("study1===================" + i);
                //读取本地URL地址图片,并转化成矩阵
                Matrix a = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/a" + i + ".jpg");
                //Matrix b = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/b" + i + ".jpg");
                Matrix c = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/c" + i + ".jpg");
                //Matrix d = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/d" + i + ".jpg");
                //Matrix f = picture.getImageMatrixByLocal("D:\\share\\picture/f" + i + ".png");
                //将图像矩阵和标注加入进行学习，Accuracy_Pattern 模式 进行第二次学习
                //第二次学习的时候，第三个参数必须是 true
                // operation.learning(f, 0, false);
                operation.learning(a, 1, false);
                // operation.learning(b, 2, false);
                operation.learning(c, 2, false);
                //operation.learning(d, 4, false);
            }
        }
//        ModelParameter modelParameter = JSON.parseObject(ModelData.DATA8, ModelParameter.class);
//        templeConfig.insertModel(modelParameter);
        //二阶段
        for (int i = 1; i < 1500; i++) {
            Matrix a = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/a" + i + ".jpg");
            //Matrix b = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/b" + i + ".jpg");
            Matrix c = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/c" + i + ".jpg");
            //Matrix d = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/d" + i + ".jpg");

            operation.normalization(a, templeConfig.getConvolutionNerveManager());
            //operation.normalization(b, templeConfig.getConvolutionNerveManager());
            operation.normalization(c, templeConfig.getConvolutionNerveManager());
            //operation.normalization(d, templeConfig.getConvolutionNerveManager());
        }
        templeConfig.getNormalization().avg();
        for (int j = 0; j < 1; j++) {
            for (int i = 1; i < 1500; i++) {
                System.out.println("study2==================" + i);
                //读取本地URL地址图片,并转化成矩阵
                Matrix a = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/a" + i + ".jpg");
                //Matrix b = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/b" + i + ".jpg");
                Matrix c = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/c" + i + ".jpg");
                //Matrix d = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/d" + i + ".jpg");
                //Matrix f = picture.getImageMatrixByLocal("D:\\share\\picture/f" + i + ".png");
                //将图像矩阵和标注加入进行学习，Accuracy_Pattern 模式 进行第二次学习
                //第二次学习的时候，第三个参数必须是 true
                // operation.learning(f, 0, true);
                //System.out.println("1===============");
                operation.learning(a, 1, true);
                //System.out.println("2===============");
                //operation.learning(b, 2, true);
                operation.learning(c, 2, true);
                //operation.learning(d, 4, true);
            }
        }

        templeConfig.finishStudy();//结束学习
        ModelParameter modelParameter = templeConfig.getModel();
        String model = JSON.toJSONString(modelParameter);
        System.out.println(model);
//        ModelParameter modelParameter2 = JSON.parseObject(model, ModelParameter.class);
//        TempleConfig templeConfig2 = new TempleConfig(false);
//        templeConfig2.init(StudyPattern.Accuracy_Pattern, true, 1000, 1000, 2);
//        templeConfig2.insertModel(modelParameter2);
//
//        Operation operation2 = new Operation(templeConfig2);
        int wrong = 0;
        int allNub = 0;
        for (int i = 1500; i <= 1572; i++) {
            //读取本地URL地址图片,并转化成矩阵
            Matrix a = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/a" + i + ".jpg");
            //Matrix b = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/b" + i + ".jpg");
            Matrix c = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/c" + i + ".jpg");
            //Matrix d = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/d" + i + ".jpg");
            //将图像矩阵和标注加入进行学习，Accuracy_Pattern 模式 进行第二次学习
            //第二次学习的时候，第三个参数必须是 true
            allNub += 2;
            int an = operation.toSee(a);
            int cn = operation.toSee(c);
            if (an != 1) {
                wrong++;
            }
            if (cn != 2) {
                wrong++;
            }
        }
        double wrongPoint = ArithUtil.div(wrong, allNub);
        System.out.println("错误率1：" + (wrongPoint * 100) + "%");

//        wrong = 0;
//        allNub = 0;
//        for (int i = 300; i <= 320; i++) {
//            //读取本地URL地址图片,并转化成矩阵
//            Matrix a = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/a" + i + ".jpg");
//            //Matrix b = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/b" + i + ".jpg");
//            Matrix c = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/c" + i + ".jpg");
//            //Matrix d = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/d" + i + ".jpg");
//            //将图像矩阵和标注加入进行学习，Accuracy_Pattern 模式 进行第二次学习
//            //第二次学习的时候，第三个参数必须是 true
//            allNub += 2;
//            int an = operation2.toSee(a);
//            int cn = operation2.toSee(c);
//            if (an != 1) {
//                wrong++;
//            }
//            if (cn != 2) {
//                wrong++;
//            }
//        }
//        wrongPoint = ArithUtil.div(wrong, allNub);
//        System.out.println("错误率2：" + (wrongPoint * 100) + "%");
//        ModelParameter modelParameter2 = templeConfig.getModel();
//        String model1 = JSON.toJSONString(modelParameter2);
//        System.out.println("完成阶段==" + model1);
    }


    public static void test1() throws Exception {//覆盖率计算
        Picture picture = new Picture();
        TempleConfig templeConfig = new TempleConfig(false);
        templeConfig.init(StudyPattern.Cover_Pattern, true, 320, 240, 2);
        Operation operation = new Operation(templeConfig);
        Map<Integer, Double> rightTagging = new HashMap<>();//分类标注
        Map<Integer, Double> wrongTagging = new HashMap<>();//分类标注
        rightTagging.put(1, 1.0);
        wrongTagging.put(2, 1.0);
        Matrix right = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/picture/yes1.jpg");
        Matrix wrong = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/picture/no4.jpg");
        int a = 1;
        for (int i = 0; i < a; i++) {
            operation.coverStudy(right, rightTagging, wrong, wrongTagging);
        }
        System.out.println("学习完成");
        long sys = System.currentTimeMillis();
        double point = operation.coverPoint(right, 1);
        long sys2 = System.currentTimeMillis();
        long sys3 = sys2 - sys;
        double point2 = operation.coverPoint(wrong, 1);
        System.out.println("识别耗时：" + sys3);
        System.out.println("测试覆盖1：" + point + ",测试覆盖2:" + point2);

    }

    private static void compare(List<Double> point, List<Double> point2) {
        int size = point.size();
        int max = 0;
        int min = 0;
        for (int i = 0; i < size; i++) {
            if (point.get(i) >= point2.get(i)) {
                max++;
            } else {
                min++;
            }
        }
        System.out.println("size==" + size + ",max==" + max + ",min==" + min);
    }
}
