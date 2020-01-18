package org.wlld;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.wlld.MatrixTools.Matrix;
import org.wlld.config.StudyPattern;
import org.wlld.imageRecognition.Operation;
import org.wlld.imageRecognition.Picture;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.nerveEntity.ModelParameter;
import org.wlld.test.Ma;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lidapeng
 * @description 测试入口类
 * @date 11:35 上午 2020/1/18
 */
public class HelloWorld {
    public static void main(String[] args) throws Exception {
        //testPic2();
        testModel();
    }

    public static void testModel() throws Exception {
        // 模型参数获取及注入
        TempleConfig templeConfig = getTemple(true, StudyPattern.Accuracy_Pattern);
        ModelParameter modelParameter1 = templeConfig.getModel();
        String model = JSON.toJSONString(modelParameter1);
        System.out.println(model);
        TempleConfig templeConfig2 = getTemple(false, StudyPattern.Accuracy_Pattern);
        ModelParameter modelParameter3 = JSONObject.parseObject(model, ModelParameter.class);
        templeConfig2.insertModel(modelParameter3);
        ModelParameter modelParameter2 = templeConfig2.getModel();
        String model2 = JSON.toJSONString(modelParameter2);
        System.out.println(model2);

    }

    public static void testPic2() throws Exception {//测试Accuracy_Pattern 模式学习
        Picture picture = new Picture();
        TempleConfig templeConfig = getTemple(true, StudyPattern.Accuracy_Pattern);
        Operation operation = new Operation(templeConfig);
        //标注主键为 第几种分类，值为标注 1 是TRUE 0是FALSE
        Map<Integer, Double> rightTagging = new HashMap<>();//分类标注
        Map<Integer, Double> wrongTagging = new HashMap<>();//分类标注
        rightTagging.put(1, 1.0);
        wrongTagging.put(1, 0.0);
        for (int i = 1; i < 5; i++) {
            System.out.println("开始学习1==" + i);
            //读取本地URL地址图片,并转化成矩阵
            Matrix right = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/c/c" + i + ".png");
            Matrix wrong = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/b/b" + i + ".png");
            //将图像矩阵和标注加入进行学习，
            operation.learning(right, rightTagging, false);
            operation.learning(wrong, wrongTagging, false);
        }
        for (int i = 1; i < 5; i++) {//神经网络学习
            System.out.println("开始学习2==" + i);
            //读取本地URL地址图片,并转化成矩阵
            Matrix right = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/c/c" + i + ".png");
            Matrix wrong = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/b/b" + i + ".png");
            //将图像矩阵和标注加入进行学习，
            operation.learning(right, rightTagging, true);
            operation.learning(wrong, wrongTagging, true);
        }
        Matrix right = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/test/a101.png");
        Matrix wrong = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/b/b1000.png");
        operation.look(right, 2);
        operation.look(wrong, 3);
    }

    public static void testPic() throws Exception {//测试SPEED模式学习
        //初始化图像转矩阵类
        Picture picture = new Picture();
        //初始化配置模板类
        TempleConfig templeConfig = getTemple(true, StudyPattern.Speed_Pattern);
        //初始化计算类
        Operation operation = new Operation(templeConfig);
        //标注主键为 第几种分类，值为标注 1 是TRUE 0是FALSE
        Map<Integer, Double> rightTagging = new HashMap<>();//分类标注
        Map<Integer, Double> wrongTagging = new HashMap<>();//分类标注
        rightTagging.put(1, 1.0);
        wrongTagging.put(1, 0.0);
        for (int i = 1; i < 999; i++) {
            System.out.println("开始学习1==" + i);
            //读取本地URL地址图片,并转化成矩阵
            Matrix right = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/c/c" + i + ".png");
            Matrix wrong = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/b/b" + i + ".png");
            //将图像矩阵和标注加入进行学习，
            operation.study(right, rightTagging);
            operation.study(wrong, wrongTagging);
        }
        //获取训练结束的模型参数，提取出来转化成JSON保存数据库，下次服务启动时不用学习
        //直接将模型参数注入
        //获取模型MODLE
        ModelParameter modelParameter = templeConfig.getModel();
        //将模型MODEL转化成JSON 字符串
        String model = JSON.toJSONString(modelParameter);
        //将JSON字符串转化为模型MODEL
        ModelParameter modelParameter1 = JSONObject.parseObject(model, ModelParameter.class);
        //初始化模型配置
        TempleConfig templeConfig1 = getTemple(false, StudyPattern.Speed_Pattern);
        //注入之前学习结果的模型MODEL到配置模版里面
        templeConfig1.insertModel(modelParameter1);
        //将配置模板配置到运算类
        Operation operation1 = new Operation(templeConfig1);
        //获取本地图片字节码转化成降纬后的灰度矩阵
        Matrix right = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/test/a101.png");
        Matrix wrong = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/b/b1000.png");
        //进行图像检测
        operation1.look(wrong, 3);
        operation1.look(right, 2);
    }

    public static TempleConfig getTemple(boolean isFirst, int pattern) throws Exception {
        TempleConfig templeConfig = new TempleConfig();
        //创建一个回调类
        Ma ma = new Ma();//创建一个回调类
        //注册输出结果回调类 必写
        templeConfig.setOutBack(ma);
        //全连接层深度,默认为2 选填
        //templeConfig.setDeep(2);
        //要学习几种分类 默认为1 选填
        //1,学习模式，是否需要
        templeConfig.init(pattern, isFirst, 3204, 4032, 1);

        return templeConfig;
    }
}
