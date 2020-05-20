package coverTest;

import com.alibaba.fastjson.JSON;
import org.wlld.MatrixTools.Matrix;
import org.wlld.config.Classifier;
import org.wlld.config.RZ;
import org.wlld.config.StudyPattern;
import org.wlld.imageRecognition.*;
import org.wlld.imageRecognition.segmentation.RegionBody;
import org.wlld.imageRecognition.segmentation.Specifications;
import org.wlld.nerveEntity.ModelParameter;
import org.wlld.tools.ArithUtil;

import java.util.ArrayList;
import java.util.List;

public class FoodTest {

    public static void main(String[] args) throws Exception {
        //test2();
        test();
    }

    public static void test2(TempleConfig templeConfig) throws Exception {
        //test();
        Picture picture = new Picture();
        List<Specifications> specificationsList = new ArrayList<>();
        Specifications specifications = new Specifications();
        specifications.setWidth(400);
        specifications.setHeight(400);
        specificationsList.add(specifications);
        Operation operation = new Operation(templeConfig);
        for (int i = 1; i <= 10; i++) {
            ThreeChannelMatrix threeChannelMatrix1 = picture.getThreeMatrix("D:\\pic/a" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix2 = picture.getThreeMatrix("D:\\pic/b" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix3 = picture.getThreeMatrix("D:\\pic/c" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix4 = picture.getThreeMatrix("D:\\pic/d" + i + ".jpg");
            RegionBody regionBody1 = operation.colorLook(threeChannelMatrix1, specificationsList).get(0);
            RegionBody regionBody2 = operation.colorLook(threeChannelMatrix2, specificationsList).get(0);
            RegionBody regionBody3 = operation.colorLook(threeChannelMatrix3, specificationsList).get(0);
            RegionBody regionBody4 = operation.colorLook(threeChannelMatrix4, specificationsList).get(0);
            System.out.println("type1==" + regionBody1.getType());
            System.out.println("type2==" + regionBody2.getType());
            System.out.println("type3==" + regionBody3.getType());
            System.out.println("type4==" + regionBody4.getType());
            System.out.println("==========================================" + i);
        }
    }

    public static void test() throws Exception {
        TempleConfig templeConfig = new TempleConfig();
        Picture picture = new Picture();
        templeConfig.setStudyPoint(0.01);
        templeConfig.isShowLog(true);
        templeConfig.setMaxRain(320);
        templeConfig.setSensoryNerveNub(3);
        templeConfig.setSoftMax(true);
        templeConfig.setFeatureNub(3);
        templeConfig.sethTh(0.88);
        templeConfig.setPoolSize(2);
        templeConfig.setRzType(RZ.L1);
        templeConfig.setlParam(0.015);
        templeConfig.setClassifier(Classifier.VAvg);
        templeConfig.init(StudyPattern.Cover_Pattern, true, 400, 400, 4);
        Operation operation = new Operation(templeConfig);
        List<Specifications> specificationsList = new ArrayList<>();
        Specifications specifications = new Specifications();
        specifications.setWidth(400);
        specifications.setHeight(400);
        specificationsList.add(specifications);
        for (int j = 0; j < 1; j++) {
            for (int i = 1; i <= 10; i++) {
                ThreeChannelMatrix threeChannelMatrix1 = picture.getThreeMatrix("D:\\pic/a" + i + ".jpg");
                ThreeChannelMatrix threeChannelMatrix2 = picture.getThreeMatrix("D:\\pic/b" + i + ".jpg");
                ThreeChannelMatrix threeChannelMatrix3 = picture.getThreeMatrix("D:\\pic/c" + i + ".jpg");
                ThreeChannelMatrix threeChannelMatrix4 = picture.getThreeMatrix("D:\\pic/d" + i + ".jpg");
                operation.colorStudy(threeChannelMatrix1, 1, specificationsList);
                operation.colorStudy(threeChannelMatrix2, 2, specificationsList);
                operation.colorStudy(threeChannelMatrix3, 3, specificationsList);
                operation.colorStudy(threeChannelMatrix4, 4, specificationsList);
                System.out.println("=======================================" + i);
            }
        }
        //templeConfig.finishStudy();
        //test2(templeConfig);
    }

    public static void study() throws Exception {
        TempleConfig templeConfig = new TempleConfig();
        templeConfig.setSoftMax(true);
        templeConfig.isShowLog(true);
        templeConfig.setStudyPoint(0.01);//不动
        templeConfig.setSoftMax(true);
        //templeConfig.setDeep(2);
        //templeConfig.setHiddenNerveNub(9);
        templeConfig.setSensoryNerveNub(4);//多出来的
        templeConfig.setRzType(RZ.L1);//不动//3 18
        templeConfig.setlParam(0.015);//不动
        templeConfig.init(StudyPattern.Cover_Pattern, true, 400, 400, 2);
        Picture picture = new Picture();
        Convolution convolution = new Convolution();
        ThreeChannelMatrix threeChannelMatrix = picture.getThreeMatrix("");
       // List<Double> feature = convolution.getCenterColor(threeChannelMatrix, 2, 4);
    }

    public static void food() throws Exception {
        Picture picture = new Picture();//创建图片解析类
        TempleConfig templeConfig = new TempleConfig();//创建配置模板类
        templeConfig.setClassifier(Classifier.DNN);//使用DNN 分类器
        //templeConfig.setActiveFunction(new Sigmod());//设置激活函数
        templeConfig.setDeep(2);//设置深度 深度神经网络 深度越深速度越慢
        //数量越大越准 但是影响量比较小 不绝对 盲试
        templeConfig.setHiddenNerveNub(9);//设置隐层神经元数量
        templeConfig.isShowLog(true);//输出打印数据
        //
        //templeConfig.setSoftMax(true);//启用最后一层的SOFTMAX
        //templeConfig.setTh(-1);//设置阈值
        templeConfig.setStudyPoint(0.012);//设置学习率 0-1
        templeConfig.setRzType(RZ.L1);//设置正则函数
        templeConfig.setlParam(0.015);//设置正则参数

        templeConfig.init(StudyPattern.Accuracy_Pattern, true, 640, 480, 2);
        Operation operation = new Operation(templeConfig);//计算类
        // 一阶段
        for (int j = 0; j < 2; j++) {
            for (int i = 1; i < 101; i++) {//一阶段
                System.out.println("study1===================" + i);
                //读取本地URL地址图片,并转化成矩阵
                Matrix a = picture.getImageMatrixByLocal("D:\\pic\\1/a" + i + ".jpg");
                Matrix b = picture.getImageMatrixByLocal("D:\\pic\\2/b" + i + ".jpg");
                operation.learning(a, 1, false);
                operation.learning(b, 2, false);
            }
        }
        // 二阶段 归一化
        for (int i = 1; i < 101; i++) {
            System.out.println("avg==" + i);
            Matrix a = picture.getImageMatrixByLocal("D:\\pic\\1/a" + i + ".jpg");
            Matrix b = picture.getImageMatrixByLocal("D:\\pic\\2/b" + i + ".jpg");
            operation.normalization(a, templeConfig.getConvolutionNerveManager());
            operation.normalization(b, templeConfig.getConvolutionNerveManager());
        }
        templeConfig.getNormalization().avg();

        for (int j = 0; j < 3; j++) {
            for (int i = 1; i < 101; i++) {
                System.out.println("j==" + j + ",study2==================" + i);
                Matrix a = picture.getImageMatrixByLocal("D:\\pic\\1/a" + i + ".jpg");
                Matrix b = picture.getImageMatrixByLocal("D:\\pic\\2/b" + i + ".jpg");
                operation.learning(a, 1, true);
                operation.learning(b, 2, true);
            }
        }
        templeConfig.finishStudy();//结束学习
        ModelParameter modelParameter = templeConfig.getModel();
        String model = JSON.toJSONString(modelParameter);
        System.out.println(model);

        int wrong = 0;
        int allNub = 0;
        for (int i = 1; i <= 100; i++) {
            //读取本地URL地址图片,并转化成矩阵
            Matrix a = picture.getImageMatrixByLocal("D:\\pic\\1/a" + i + ".jpg");
            allNub++;
            int an = operation.toSee(a);
            if (an != 1) {
                wrong++;
            }
        }
        double wrongPoint = ArithUtil.div(wrong, allNub);
        System.out.println("错误率：" + (wrongPoint * 100) + "%");

    }

}
