package coverTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.wlld.MatrixTools.Matrix;
import org.wlld.ModelData;
import org.wlld.config.Classifier;
import org.wlld.config.RZ;
import org.wlld.config.StudyPattern;
import org.wlld.imageRecognition.*;
import org.wlld.imageRecognition.segmentation.RegionBody;
import org.wlld.imageRecognition.segmentation.Specifications;
import org.wlld.nerveEntity.ModelParameter;
import org.wlld.param.Cutting;
import org.wlld.param.Food;
import org.wlld.tools.ArithUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FoodTest {

    public static void main(String[] args) throws Exception {
        test();
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
        //templeConfig.isShowLog(true);//是否打印日志
        Cutting cutting = templeConfig.getCutting();
        Food food = templeConfig.getFood();
        //
        cutting.setMaxRain(360);//切割阈值
        cutting.setTh(0.6);
        cutting.setRegionNub(200);
        cutting.setMaxIou(2.0);
        //knn参数
        templeConfig.setKnnNub(1);
        //池化比例
        templeConfig.setPoolSize(2);//缩小比例
        //聚类
        templeConfig.setFeatureNub(5);//聚类特征数量
        //菜品识别实体类
        food.setShrink(20);//缩紧像素
        food.setTimes(2);//聚类数据增强
        food.setRowMark(0.12);//0.12
        food.setColumnMark(0.12);//0.25
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
        specifications.setMinWidth(100);
        specifications.setMinHeight(100);
        specifications.setMaxWidth(1000);
        specifications.setMaxHeight(1000);
        specificationsList.add(specifications);
        ThreeChannelMatrix threeChannelMatrix = picture.getThreeMatrix("/Users/lidapeng/Desktop/myDocument/d.jpg");
        operation.setTray(threeChannelMatrix);
        for (int i = 1; i <= 1; i++) {
            ThreeChannelMatrix threeChannelMatrix1 = picture.getThreeMatrix("/Users/lidapeng/Desktop/test/test.jpg");
            operation.colorStudy(threeChannelMatrix1, 1, specificationsList);

        }
        //    test2(templeConfig);
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
