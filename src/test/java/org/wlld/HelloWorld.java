package org.wlld;

import com.alibaba.fastjson.JSON;
import org.wlld.MatrixTools.Matrix;
import org.wlld.config.Classifier;
import org.wlld.config.StudyPattern;
import org.wlld.imageRecognition.Operation;
import org.wlld.imageRecognition.Picture;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.nerveEntity.ModelParameter;
import org.wlld.tools.ArithUtil;

import java.io.InputStream;

/**
 * @author lidapeng
 * @description 图像测试入口类
 * @date 11:35 上午 2020/1/18
 */
public class HelloWorld {
    static TempleConfig templeConfig = new TempleConfig(false, true);

    static {//初始化静态配置模板
        try {
            //使用DNN 分类器
            templeConfig.setClassifier(Classifier.DNN);
            //初始化
            templeConfig.init(StudyPattern.Accuracy_Pattern, true, 640, 640, 4);
            //从数据库里把模型拿出来，并反序列化成模型
            ModelParameter modelParameter2 = JSON.parseObject(ModelData.DATA, ModelParameter.class);
            //模型注入配置模板
            templeConfig.insertModel(modelParameter2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        pictureDemo1();
    }

    public static void pictureDemo1() throws Exception {//图像学习DEMO
        //easyAI 包持续更新，现阶段一直在优化
        Picture picture = new Picture();
        //使用精度计算
        TempleConfig templeConfig = new TempleConfig();
        //使用DNN分类器
        templeConfig.setClassifier(Classifier.DNN);
        //打印学习过程中产生的参数
        templeConfig.isShowLog(true);
        //初始化配置模板，使用精确模式，第一次学习，640宽度，640高度的训练照片，有四种分类的物体学习
        templeConfig.init(StudyPattern.Accuracy_Pattern, true, 640, 640, 4);
        //初始化运算类
        Operation operation = new Operation(templeConfig);
        // 一阶段学习
        for (int i = 1; i < 1900; i++) {//一阶段
            System.out.println("study1===================" + i);
            //读取本地URL地址图片,并转化成矩阵
            Matrix a = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/a" + i + ".jpg");
            Matrix b = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/b" + i + ".jpg");
            Matrix c = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/c" + i + ".jpg");
            Matrix d = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/d" + i + ".jpg");
            operation.learning(a, 1, false);
            operation.learning(b, 2, false);
            operation.learning(c, 3, false);
            operation.learning(d, 4, false);
        }

        //二阶段学习
        for (int i = 1; i < 1900; i++) {//特征归一化
            System.out.println("avg==" + i);
            Matrix a = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/a" + i + ".jpg");
            Matrix b = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/b" + i + ".jpg");
            Matrix c = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/c" + i + ".jpg");
            Matrix d = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/d" + i + ".jpg");
            operation.normalization(a, templeConfig.getConvolutionNerveManager());
            operation.normalization(b, templeConfig.getConvolutionNerveManager());
            operation.normalization(c, templeConfig.getConvolutionNerveManager());
            operation.normalization(d, templeConfig.getConvolutionNerveManager());
        }
        templeConfig.getNormalization().avg();
        //三阶段学习
        for (int i = 1; i < 1900; i++) {
            System.out.println("study2==================" + i);
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
        templeConfig.finishStudy();//结束学习
        ModelParameter modelParameter = templeConfig.getModel();
        String model = JSON.toJSONString(modelParameter);
        //我把学到的模型拿出来啦！
        System.out.println("my model is:" + model);
        //验证一下准确率
        int wrong = 0;
        int allNub = 0;
        for (int i = 1900; i <= 1998; i++) {
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
        System.out.println("错误率1：" + (wrongPoint * 100) + "%");
    }

    public static int testPicDemo(InputStream imageStream) throws Exception {//识别图片
        //配置模板塞入运算类
        Operation operation = new Operation(templeConfig);
        //图片解析类
        Picture picture = new Picture();
        //获取矩阵
        Matrix a = picture.getImageMatrixByIo(imageStream);
        int type = operation.toSee(a);//返回图片类别
        return type;
    }
}
