package coverTest;

import com.alibaba.fastjson.JSON;
import org.wlld.MatrixTools.Matrix;
import org.wlld.ModelData;
import org.wlld.config.Classifier;
import org.wlld.config.RZ;
import org.wlld.config.StudyPattern;
import org.wlld.imageRecognition.Operation;
import org.wlld.imageRecognition.Picture;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.nerveEntity.ModelParameter;
import org.wlld.tools.ArithUtil;

import java.util.HashMap;
import java.util.Map;

public class FoodTest {
    public static void main(String[] args) throws Exception {
        food();
    }

    public static void food() throws Exception {
        Picture picture = new Picture();
        TempleConfig templeConfig = new TempleConfig();

        templeConfig.setClassifier(Classifier.DNN);
        templeConfig.setMatrixWidth(5);
        templeConfig.isShowLog(true);
        templeConfig.setRzType(RZ.L2);
        templeConfig.setDeep(1);
        templeConfig.setStudyPoint(0.05);
        templeConfig.setHiddenNerveNub(6);
        templeConfig.setlParam(0.015);//0.015

        templeConfig.init(StudyPattern.Accuracy_Pattern, true, 640, 640, 4);
        ModelParameter modelParameter2 = JSON.parseObject(ModelData.DATA3, ModelParameter.class);
        templeConfig.insertModel(modelParameter2);
        Operation operation = new Operation(templeConfig);


        // 一阶段
//        for (int j = 0; j < 1; j++) {
//            for (int i = 1; i < 1500; i++) {//一阶段
//                System.out.println("study1===================" + i);
//                //读取本地URL地址图片,并转化成矩阵
//                Matrix a = picture.getImageMatrixByLocal("D:\\share\\picture/a" + i + ".jpg");
//                Matrix b = picture.getImageMatrixByLocal("D:\\share\\picture/b" + i + ".jpg");
//                Matrix c = picture.getImageMatrixByLocal("D:\\share\\picture/c" + i + ".jpg");
//                Matrix d = picture.getImageMatrixByLocal("D:\\share\\picture/d" + i + ".jpg");
//                //将图像矩阵和标注加入进行学习，Accuracy_Pattern 模式 进行第二次学习
//                //第二次学习的时候，第三个参数必须是 true
//                operation.learning(a, 1, false);
//                operation.learning(b, 2, false);
//                operation.learning(c, 3, false);
//                operation.learning(d, 4, false);
//            }
//        }
//
//        //二阶段
//        for (int i = 1; i < 1500; i++) {
//            System.out.println("avg==" + i);
//            Matrix a = picture.getImageMatrixByLocal("D:\\share\\picture/a" + i + ".jpg");
//            Matrix b = picture.getImageMatrixByLocal("D:\\share\\picture/b" + i + ".jpg");
//            Matrix c = picture.getImageMatrixByLocal("D:\\share\\picture/c" + i + ".jpg");
//            Matrix d = picture.getImageMatrixByLocal("D:\\share\\picture/d" + i + ".jpg");
//            operation.normalization(a, templeConfig.getConvolutionNerveManager());
//            operation.normalization(b, templeConfig.getConvolutionNerveManager());
//            operation.normalization(c, templeConfig.getConvolutionNerveManager());
//            operation.normalization(d, templeConfig.getConvolutionNerveManager());
//        }
//        templeConfig.getNormalization().avg();
//        templeConfig.finishStudy();//结束学习
//        ModelParameter modelParameter = templeConfig.getModel();
//        String model = JSON.toJSONString(modelParameter);
//        System.out.println(model);
//        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA");


        for (int j = 0; j < 2; j++) {
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
        for (int i = 1500; i <= 1600; i++) {
            //读取本地URL地址图片,并转化成矩阵
            Matrix a = picture.getImageMatrixByLocal("D:\\share\\picture/a" + i + ".jpg");
            Matrix b = picture.getImageMatrixByLocal("D:\\share\\picture/b" + i + ".jpg");
            Matrix c = picture.getImageMatrixByLocal("D:\\share\\picture/c" + i + ".jpg");
            Matrix d = picture.getImageMatrixByLocal("D:\\share\\picture/d" + i + ".jpg");
            //将图像矩阵和标注加入进行学习，Accuracy_Pattern 模式 进行第二次学习
            //第二次学习的时候，第三个参数必须是 true
            allNub += 4;
            int an = operation.toSee(a);
            //System.out.println("an============1");
            if (an != 1) {
                //System.out.println("a错了");
                wrong++;
            }
            int bn = operation.toSee(b);
            //System.out.println("bn============2");
            if (bn != 2) {
                // System.out.println("b错了");
                wrong++;
            }
            int cn = operation.toSee(c);
            // System.out.println("cn============3");
            if (cn != 3) {
                //System.out.println("c错了");
                wrong++;
            }
            int dn = operation.toSee(d);
            // System.out.println("dn============4");
            if (dn != 4) {
                // System.out.println("d错了");
                wrong++;
            }
        }
        double wrongPoint = ArithUtil.div(wrong, allNub);
        System.out.println("错误率1：" + (wrongPoint * 100) + "%");
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
