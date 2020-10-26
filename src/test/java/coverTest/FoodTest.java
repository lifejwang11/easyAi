package coverTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.wlld.MatrixTools.Matrix;
import org.wlld.ModelData;
import org.wlld.config.Classifier;
import org.wlld.config.RZ;
import org.wlld.config.StudyPattern;
import org.wlld.imageRecognition.*;
import org.wlld.imageRecognition.segmentation.*;
import org.wlld.nerveEntity.ModelParameter;
import org.wlld.param.Cutting;
import org.wlld.param.Food;
import org.wlld.tools.ArithUtil;

import java.util.*;

public class FoodTest {

    public static void main(String[] args) throws Exception {
        test();
    }

    public static void ab(List<int[]> t) {

        //最终输入结果
        List<Set<Integer>> t1 = new ArrayList<>();

        //储存临时值
        Set<Integer> s = new HashSet<>();
        //用来校验重复游标组
        List<List<Integer>> k = new ArrayList<>();
        Boolean x = true;//对循环进行判断
        int num = 0;//循环游标初始值

        while (x) {
            //校验重复游标
            List<Integer> l = new ArrayList<>();
            //清楚临时存储值
            s.clear();
            //单个最终值
            Set<Integer> s1 = new HashSet<>();

            for (int i = num; i < t.size(); i++) {
                Boolean check = false;
                //如果存在已经验证的数组，则跳过
                for (int j = num; j < k.size(); j++) {
                    if (k.get(j).contains(i)) {
                        check = true;
                        break;

                    }
                }
                if (check) {
                    continue;
                }

                //循环判断数组是否存在重复（当i=num时，给初始的s赋值）
                //当S中包含重复值的时候，插入
                for (int info : t.get(i)) {
                    if (i == num) {
                        for (int info1 : t.get(i)) {
                            l.add(i);
                            k.add(l);
                            s.add(info1);
                        }
                    } else if (s.contains(info)) {
                        for (int info1 : t.get(i)) {
                            l.add(i);
                            k.add(l);
                            s.add(info1);
                        }
                        break;
                    }
                }

            }
            //如果临时s不为空，给s1赋值，并存入到最后输出的t1中
            if (!s.isEmpty()) {
                for (Integer info : s) {
                    s1.add(info);
                }
                t1.add(s1);
            }
            num++;
            //跳出循环
            if (num >= t.size()) {
                x = false;
            }
        }

        System.out.println(t1);
    }

    public static void test2(TempleConfig templeConfig) throws Exception {
        if (templeConfig == null) {
            ModelParameter parameter = JSON.parseObject(ModelData.DATA, ModelParameter.class);
            templeConfig = getTemple(null);
        }
        Picture picture = new Picture();
        List<Specifications> specificationsList = new ArrayList<>();
        Specifications specifications = new Specifications();
        specifications.setMinWidth(100);
        specifications.setMinHeight(100);
        specifications.setMaxWidth(950);
        specifications.setMaxHeight(950);
        specificationsList.add(specifications);
        Operation operation = new Operation(templeConfig);
        long a = System.currentTimeMillis();
        for (int i = 1; i <= 1; i++) {
            ThreeChannelMatrix threeChannelMatrix1 = picture.getThreeMatrix("/Users/lidapeng/Desktop/test/d.jpg");
            List<RegionBody> regionBody = operation.colorLook(threeChannelMatrix1, specificationsList);
            long b = System.currentTimeMillis() - a;
            System.out.println(b);
            for (int j = 0; j < regionBody.size(); j++) {
                RegionBody regionBody1 = regionBody.get(j);
                System.out.println("minX==" + regionBody1.getMinX() + ",minY==" + regionBody1.getMinY()
                        + ",maxX==" + regionBody1.getMaxX() + ",maxY==" + regionBody1.getMaxY());
                System.out.println("type==" + regionBody.get(j).getType());
            }
        }
    }

    public static TempleConfig getTemple(ModelParameter modelParameter) throws Exception {
        TempleConfig templeConfig = new TempleConfig();
        templeConfig.setEdge(10);
        //templeConfig.isShowLog(true);//是否打印日志
        Cutting cutting = templeConfig.getCutting();
        Food food = templeConfig.getFood();
        //
        cutting.setMaxRain(360);//切割阈值
        cutting.setTh(0.8);
        cutting.setRegionNub(100);
        cutting.setMaxIou(2);
        //knn参数
        templeConfig.setKnnNub(1);
        //池化比例
        templeConfig.setPoolSize(2);//缩小比例
        //聚类
        templeConfig.setFeatureNub(5);//聚类特征数量
        //菜品识别实体类
        food.setShrink(5);//缩紧像素
        food.setRegionSize(6);
        KNerveManger kNerveManger = new KNerveManger(12, 24, 6000);
        food.setkNerveManger(kNerveManger);
        food.setRowMark(0.15);//0.12
        food.setColumnMark(0.15);//0.25
        food.setRegressionNub(20000);
        food.setTrayTh(0.08);
        templeConfig.setClassifier(Classifier.KNN);
        templeConfig.init(StudyPattern.Cover_Pattern, true, 400, 400, 3);
        if (modelParameter != null) {
            templeConfig.insertModel(modelParameter);
        }
        return templeConfig;
    }

    public static void test() throws Exception {
        Picture picture = new Picture();
        TempleConfig templeConfig = getTemple(null);
        Operation operation = new Operation(templeConfig);
        List<Specifications> specificationsList = new ArrayList<>();
        Specifications specifications = new Specifications();
        specifications.setMinWidth(150);//150
        specifications.setMinHeight(150);//150
        specifications.setMaxWidth(600);
        specifications.setMaxHeight(600);
        specificationsList.add(specifications);
        //KNerveManger kNerveManger = templeConfig.getFood().getkNerveManger();
//        ThreeChannelMatrix threeChannelMatrix = picture.getThreeMatrix("/Users/lidapeng/Desktop/myDocument/d.jpg");
//        operation.setTray(threeChannelMatrix);
        String name = "/Users/lidapeng/Desktop/test/testOne/";
        for (int i = 0; i < 4; i++) {
            System.out.println("轮数============================" + i);
            ThreeChannelMatrix threeChannelMatrix1 = picture.getThreeMatrix(name + "a" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix2 = picture.getThreeMatrix(name + "b" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix3 = picture.getThreeMatrix(name + "c" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix4 = picture.getThreeMatrix(name + "d" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix5 = picture.getThreeMatrix(name + "e" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix6 = picture.getThreeMatrix(name + "f" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix7 = picture.getThreeMatrix(name + "g" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix8 = picture.getThreeMatrix(name + "h" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix9 = picture.getThreeMatrix(name + "i" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix10 = picture.getThreeMatrix(name + "j" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix11 = picture.getThreeMatrix(name + "k" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix12 = picture.getThreeMatrix(name + "l" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix13 = picture.getThreeMatrix(name + "m" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix14 = picture.getThreeMatrix(name + "n" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix15 = picture.getThreeMatrix(name + "o" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix16 = picture.getThreeMatrix(name + "p" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix17 = picture.getThreeMatrix(name + "q" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix18 = picture.getThreeMatrix(name + "r" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix19 = picture.getThreeMatrix(name + "s" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix20 = picture.getThreeMatrix(name + "t" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix21 = picture.getThreeMatrix(name + "u" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix22 = picture.getThreeMatrix(name + "v" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix23 = picture.getThreeMatrix(name + "w" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix24 = picture.getThreeMatrix(name + "x" + i + ".jpg");
            operation.colorStudy(threeChannelMatrix1, 1, specificationsList, name);
            operation.colorStudy(threeChannelMatrix2, 2, specificationsList, name);
            operation.colorStudy(threeChannelMatrix3, 3, specificationsList, name);
            operation.colorStudy(threeChannelMatrix4, 4, specificationsList, name);
            operation.colorStudy(threeChannelMatrix5, 5, specificationsList, name);
            operation.colorStudy(threeChannelMatrix6, 6, specificationsList, name);
            operation.colorStudy(threeChannelMatrix7, 7, specificationsList, name);
            operation.colorStudy(threeChannelMatrix8, 8, specificationsList, name);
            operation.colorStudy(threeChannelMatrix9, 9, specificationsList, name);
            operation.colorStudy(threeChannelMatrix10, 10, specificationsList, name);
            operation.colorStudy(threeChannelMatrix11, 11, specificationsList, name);
            operation.colorStudy(threeChannelMatrix12, 12, specificationsList, name);
            operation.colorStudy(threeChannelMatrix13, 13, specificationsList, name);
            operation.colorStudy(threeChannelMatrix14, 14, specificationsList, name);
            operation.colorStudy(threeChannelMatrix15, 15, specificationsList, name);
            operation.colorStudy(threeChannelMatrix16, 16, specificationsList, name);
            operation.colorStudy(threeChannelMatrix17, 17, specificationsList, name);
            operation.colorStudy(threeChannelMatrix18, 18, specificationsList, name);
            operation.colorStudy(threeChannelMatrix19, 19, specificationsList, name);
            operation.colorStudy(threeChannelMatrix20, 20, specificationsList, name);
            operation.colorStudy(threeChannelMatrix21, 21, specificationsList, name);
            operation.colorStudy(threeChannelMatrix22, 22, specificationsList, name);
            operation.colorStudy(threeChannelMatrix23, 23, specificationsList, name);
            operation.colorStudy(threeChannelMatrix24, 24, specificationsList, name);
        }
        DimensionMappingStudy dimensionMappingStudy = new DimensionMappingStudy(templeConfig, true);
        dimensionMappingStudy.start();//完成映射
        //dimensionMappingStudy.selfTest(6);//检查
        //System.out.println("========================");
        // kNerveManger.startStudy();
        int i = 4;
        ThreeChannelMatrix threeChannelMatrix1 = picture.getThreeMatrix(name + "a" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix2 = picture.getThreeMatrix(name + "b" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix3 = picture.getThreeMatrix(name + "c" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix4 = picture.getThreeMatrix(name + "d" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix5 = picture.getThreeMatrix(name + "e" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix6 = picture.getThreeMatrix(name + "f" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix7 = picture.getThreeMatrix(name + "g" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix8 = picture.getThreeMatrix(name + "h" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix9 = picture.getThreeMatrix(name + "i" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix10 = picture.getThreeMatrix(name + "j" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix11 = picture.getThreeMatrix(name + "k" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix12 = picture.getThreeMatrix(name + "l" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix13 = picture.getThreeMatrix(name + "m" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix14 = picture.getThreeMatrix(name + "n" + i + ".jpg");
//
        ThreeChannelMatrix threeChannelMatrix15 = picture.getThreeMatrix(name + "o" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix16 = picture.getThreeMatrix(name + "p" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix17 = picture.getThreeMatrix(name + "q" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix18 = picture.getThreeMatrix(name + "r" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix19 = picture.getThreeMatrix(name + "s" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix20 = picture.getThreeMatrix(name + "t" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix21 = picture.getThreeMatrix(name + "u" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix22 = picture.getThreeMatrix(name + "v" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix23 = picture.getThreeMatrix(name + "w" + i + ".jpg");
        ThreeChannelMatrix threeChannelMatrix24 = picture.getThreeMatrix(name + "x" + i + ".jpg");
        operation.colorLook(threeChannelMatrix1, specificationsList);
        operation.colorLook(threeChannelMatrix2, specificationsList);
        operation.colorLook(threeChannelMatrix3, specificationsList);
        operation.colorLook(threeChannelMatrix4, specificationsList);
        operation.colorLook(threeChannelMatrix5, specificationsList);
        operation.colorLook(threeChannelMatrix6, specificationsList);
        operation.colorLook(threeChannelMatrix7, specificationsList);
        operation.colorLook(threeChannelMatrix8, specificationsList);
        operation.colorLook(threeChannelMatrix9, specificationsList);
        operation.colorLook(threeChannelMatrix10, specificationsList);
        operation.colorLook(threeChannelMatrix11, specificationsList);
        operation.colorLook(threeChannelMatrix12, specificationsList);
        operation.colorLook(threeChannelMatrix13, specificationsList);
        operation.colorLook(threeChannelMatrix14, specificationsList);
        operation.colorLook(threeChannelMatrix15, specificationsList);
        operation.colorLook(threeChannelMatrix16, specificationsList);
        operation.colorLook(threeChannelMatrix17, specificationsList);
        operation.colorLook(threeChannelMatrix18, specificationsList);
        operation.colorLook(threeChannelMatrix19, specificationsList);
        operation.colorLook(threeChannelMatrix20, specificationsList);
        operation.colorLook(threeChannelMatrix21, specificationsList);
        operation.colorLook(threeChannelMatrix22, specificationsList);
        operation.colorLook(threeChannelMatrix23, specificationsList);
        operation.colorLook(threeChannelMatrix24, specificationsList);

//        test3(threeChannelMatrix1, operation, specificationsList, 1);
//        test3(threeChannelMatrix2, operation, specificationsList, 2);
//        test3(threeChannelMatrix3, operation, specificationsList, 3);
//        test3(threeChannelMatrix4, operation, specificationsList, 4);
//        test3(threeChannelMatrix5, operation, specificationsList, 5);
//        test3(threeChannelMatrix6, operation, specificationsList, 6);
//        test3(threeChannelMatrix7, operation, specificationsList, 7);
//        test3(threeChannelMatrix8, operation, specificationsList, 8);
//        test3(threeChannelMatrix9, operation, specificationsList, 9);
//        test3(threeChannelMatrix10, operation, specificationsList, 10);
//        test3(threeChannelMatrix11, operation, specificationsList, 11);
//        test3(threeChannelMatrix12, operation, specificationsList, 12);
//        test3(threeChannelMatrix13, operation, specificationsList, 13);
//        test3(threeChannelMatrix14, operation, specificationsList, 14);
//        test3(threeChannelMatrix15, operation, specificationsList, 15);
//        test3(threeChannelMatrix16, operation, specificationsList, 16);
//        test3(threeChannelMatrix17, operation, specificationsList, 17);
//        test3(threeChannelMatrix18, operation, specificationsList, 18);
//        test3(threeChannelMatrix19, operation, specificationsList, 19);
//        test3(threeChannelMatrix20, operation, specificationsList, 20);
//        test3(threeChannelMatrix21, operation, specificationsList, 21);
//        test3(threeChannelMatrix22, operation, specificationsList, 22);
//        test3(threeChannelMatrix23, operation, specificationsList, 23);
//        test3(threeChannelMatrix24, operation, specificationsList, 24);

    }

    private static void test3(ThreeChannelMatrix threeChannelMatrix, Operation operation, List<Specifications> specifications,
                              int realType) throws Exception {
        int type = operation.colorLook(threeChannelMatrix, specifications).get(0).getType();
        System.out.println(type + ",realType==" + realType);
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
