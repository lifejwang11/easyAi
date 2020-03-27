package coverTest;

import com.alibaba.fastjson.JSON;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.wlld.MatrixTools.Matrix;
import org.wlld.ModelData;
import org.wlld.config.Classifier;
import org.wlld.config.RZ;
import org.wlld.config.StudyPattern;
import org.wlld.imageRecognition.*;
import org.wlld.imageRecognition.segmentation.ImageSegmentation;
import org.wlld.nerveEntity.ModelParameter;
import org.wlld.tools.ArithUtil;

import java.util.*;

public class FoodTest {

    public static void main(String[] args) throws Exception {
        if (System.getProperties().getProperty("os.name").toUpperCase().contains("WINDOWS")) {
            System.out.println("win==");
        }
//        Picture picture = new Picture();
//        Matrix a = picture.getImageMatrixByLocal("D:\\share\\picture/a1.jpg");
//        ImageSegmentation imageSegmentation = new ImageSegmentation(a);
//        imageSegmentation.createMST();
    }

    public static void food() throws Exception {
        Picture picture = new Picture();
        TempleConfig templeConfig = new TempleConfig();

        templeConfig.setClassifier(Classifier.DNN);
        templeConfig.isShowLog(true);
        templeConfig.setRzType(RZ.L1);
        templeConfig.setlParam(0.015);//0.015
        templeConfig.init(StudyPattern.Accuracy_Pattern, true, 860, 860, 4);
        ModelParameter modelParameter1 = JSON.parseObject(ModelData.DATA, ModelParameter.class);
        templeConfig.insertModel(modelParameter1);
        Operation operation = new Operation(templeConfig);
        System.out.println("AAAA");
        // 一阶段
        for (int j = 0; j < 2; j++) {
            for (int i = 1; i < 1500; i++) {//一阶段
                System.out.println("study1===================" + i);
                //读取本地URL地址图片,并转化成矩阵
                Matrix a = picture.getImageMatrixByLocal("D:\\share\\picture/a" + i + ".jpg");
                Matrix b = picture.getImageMatrixByLocal("D:\\share\\picture/b" + i + ".jpg");
                Matrix c = picture.getImageMatrixByLocal("D:\\share\\picture/c" + i + ".jpg");
                Matrix d = picture.getImageMatrixByLocal("D:\\share\\picture/d" + i + ".jpg");
                //将图像矩阵和标注加入进行学习，Accuracy_Pattern 模式 进行第二次学习
                //第二次学习的时候，第三个参数必须是 true
                operation.learning(a, 1, false);
                operation.learning(b, 2, false);
                operation.learning(c, 3, false);
                operation.learning(d, 4, false);
            }
        }
        //二阶段
        for (int i = 1; i < 1500; i++) {
            System.out.println("avg==" + i);
            Matrix a = picture.getImageMatrixByLocal("D:\\share\\picture/a" + i + ".jpg");
            Matrix b = picture.getImageMatrixByLocal("D:\\share\\picture/b" + i + ".jpg");
            Matrix c = picture.getImageMatrixByLocal("D:\\share\\picture/c" + i + ".jpg");
            Matrix d = picture.getImageMatrixByLocal("D:\\share\\picture/d" + i + ".jpg");
            operation.normalization(a, templeConfig.getConvolutionNerveManager());
            operation.normalization(b, templeConfig.getConvolutionNerveManager());
            operation.normalization(c, templeConfig.getConvolutionNerveManager());
            operation.normalization(d, templeConfig.getConvolutionNerveManager());
        }
        templeConfig.getNormalization().avg();
        ModelParameter modelParameter2 = templeConfig.getModel();
        String model2 = JSON.toJSONString(modelParameter2);
        System.out.println(model2);
        for (int j = 0; j < 1; j++) {
            for (int i = 1; i < 1500; i++) {
                System.out.println("j==" + j + ",study2==================" + i);
                //读取本地URL地址图片,并转化成矩阵
                Matrix a = picture.getImageMatrixByLocal("D:\\share\\picture/a" + i + ".jpg");
                Matrix b = picture.getImageMatrixByLocal("D:\\share\\picture/b" + i + ".jpg");
                Matrix c = picture.getImageMatrixByLocal("D:\\share\\picture/c" + i + ".jpg");
                Matrix d = picture.getImageMatrixByLocal("D:\\share\\picture/d" + i + ".jpg");
                //将图像矩阵和标注加入进行学习，Accuracy_Pattern 模式 进行第二次学习
                //第二次学习的时候，第三个参数必须是 true
                operation.learning(a, 1, true);
                operation.learning(b, 2, true);
                operation.learning(c, 3, true);
                operation.learning(d, 4, true);
            }
        }
        templeConfig.finishStudy();//结束学习

        int wrong = 0;
        int allNub = 0;
        for (int i = 1500; i <= 1997; i++) {
            //读取本地URL地址图片,并转化成矩阵
            Matrix a = picture.getImageMatrixByLocal("D:\\share\\picture/a" + i + ".jpg");
            Matrix b = picture.getImageMatrixByLocal("D:\\share\\picture/b" + i + ".jpg");
            Matrix c = picture.getImageMatrixByLocal("D:\\share\\picture/c" + i + ".jpg");
            Matrix d = picture.getImageMatrixByLocal("D:\\share\\picture/d" + i + ".jpg");
            //将图像矩阵和标注加入进行学习，Accuracy_Pattern 模式 进行第二次学习
            //第二次学习的时候，第三个参数必须是 true
            allNub += 4;
            int an = operation.toSee(a);
            if (an != 1) {
                wrong++;
            }
            int bn = operation.toSee(b);
            if (bn != 2) {
                wrong++;
            }
            int cn = operation.toSee(c);
            if (cn != 3) {
                wrong++;
            }
            int dn = operation.toSee(d);
            if (dn != 4) {
                wrong++;
            }
        }
        double wrongPoint = ArithUtil.div(wrong, allNub);
        System.out.println("错误率：" + (wrongPoint * 100) + "%");
        ModelParameter modelParameter = templeConfig.getModel();
        String model = JSON.toJSONString(modelParameter);
        System.out.println(model);
    }

    public static void test1() throws Exception {//覆盖率计算
        Picture picture = new Picture();
        TempleConfig templeConfig = new TempleConfig(false, true);
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
}
