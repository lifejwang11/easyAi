package coverTest;

import com.alibaba.fastjson.JSON;
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

        food();
    }

    public static void food() throws Exception {
        Picture picture = new Picture();
        TempleConfig templeConfig = new TempleConfig();

        templeConfig.setClassifier(Classifier.DNN);
        templeConfig.isShowLog(true);
        templeConfig.setRzType(RZ.L2);
        templeConfig.setlParam(0.01);//0.015
        templeConfig.init(StudyPattern.Accuracy_Pattern, true, 400, 400, 3);
//      ModelParameter modelParameter1 = JSON.parseObject(ModelData.DATA, ModelParameter.class);
//      templeConfig.insertModel(modelParameter1);
        Operation operation = new Operation(templeConfig);
        // 一阶段
        for (int j = 0; j < 2; j++) {
            for (int i = 1; i < 101; i++) {//一阶段
                System.out.println("study1===================" + i);
                //读取本地URL地址图片,并转化成矩阵
                Matrix a = picture.getImageMatrixByLocal("D:\\share\\cai/a" + i + ".jpg");
                Matrix b = picture.getImageMatrixByLocal("D:\\share\\cai/b" + i + ".jpg");
                Matrix c = picture.getImageMatrixByLocal("D:\\share\\cai/c" + i + ".jpg");
                //Matrix d = picture.getImageMatrixByLocal("D:\\share\\cai/d" + i + ".jpg");
                //将图像矩阵和标注加入进行学习，Accuracy_Pattern 模式 进行第二次学习
                //第二次学习的时候，第三个参数必须是 true
                operation.learning(a, 1, false);
                operation.learning(b, 2, false);
                operation.learning(c, 3, false);
                //operation.learning(d, 4, false);
            }
        }
        //二阶段
        for (int i = 1; i < 101; i++) {
            System.out.println("avg==" + i);
            Matrix a = picture.getImageMatrixByLocal("D:\\share\\cai/a" + i + ".jpg");
            Matrix b = picture.getImageMatrixByLocal("D:\\share\\cai/b" + i + ".jpg");
            Matrix c = picture.getImageMatrixByLocal("D:\\share\\cai/c" + i + ".jpg");
            //Matrix d = picture.getImageMatrixByLocal("D:\\share\\picture/d" + i + ".jpg");
            operation.normalization(a, templeConfig.getConvolutionNerveManager());
            operation.normalization(b, templeConfig.getConvolutionNerveManager());
            operation.normalization(c, templeConfig.getConvolutionNerveManager());
            //operation.normalization(d, templeConfig.getConvolutionNerveManager());
        }
        templeConfig.getNormalization().avg();
//        ModelParameter modelParameter2 = templeConfig.getModel();
//        String model2 = JSON.toJSONString(modelParameter2);
//        System.out.println(model2);
        for (int j = 0; j < 10; j++) {
            for (int i = 1; i < 101; i++) {
                System.out.println("j==" + j + ",study2==================" + i);
                //读取本地URL地址图片,并转化成矩阵
                Matrix a = picture.getImageMatrixByLocal("D:\\share\\cai/a" + i + ".jpg");
                Matrix b = picture.getImageMatrixByLocal("D:\\share\\cai/b" + i + ".jpg");
                Matrix c = picture.getImageMatrixByLocal("D:\\share\\cai/c" + i + ".jpg");
                // Matrix d = picture.getImageMatrixByLocal("D:\\share\\picture/d" + i + ".jpg");
                //将图像矩阵和标注加入进行学习，Accuracy_Pattern 模式 进行第二次学习
                //第二次学习的时候，第三个参数必须是 true
                operation.learning(a, 1, true);
                operation.learning(b, 2, true);
                operation.learning(c, 3, true);
                //operation.learning(d, 4, true);
            }
        }
        templeConfig.finishStudy();//结束学习

        int wrong = 0;
        int allNub = 0;
        for (int i = 10; i <= 20; i++) {
            //读取本地URL地址图片,并转化成矩阵
            Matrix a = picture.getImageMatrixByLocal("D:\\share\\cai/a" + i + ".jpg");
            Matrix b = picture.getImageMatrixByLocal("D:\\share\\cai/b" + i + ".jpg");
            Matrix c = picture.getImageMatrixByLocal("D:\\share\\cai/c" + i + ".jpg");
            //Matrix d = picture.getImageMatrixByLocal("D:\\share\\picture/d" + i + ".jpg");
            //将图像矩阵和标注加入进行学习，Accuracy_Pattern 模式 进行第二次学习
            //第二次学习的时候，第三个参数必须是 true
            allNub += 3;
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
            /*int dn = operation.toSee(d);
            if (dn != 4) {
                wrong++;
            }*/
        }
        double wrongPoint = ArithUtil.div(wrong, allNub);
        System.out.println("错误率：" + (wrongPoint * 100) + "%");
        ModelParameter modelParameter = templeConfig.getModel();
        String model = JSON.toJSONString(modelParameter);
        System.out.println(model);
    }
}
