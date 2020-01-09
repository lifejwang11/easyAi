package org.wlld;

import org.wlld.MatrixTools.Matrix;
import org.wlld.imageRecognition.Operation;
import org.wlld.imageRecognition.Picture;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.test.Ma;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试入口类!
 */
public class App {
    public static void main(String[] args) throws Exception {
        //createNerveTest();
        testPic();
    }

    public static void testPic() throws Exception {
        //初始化图像转矩阵类
        Picture picture = new Picture();
        //初始化配置模板类
        TempleConfig templeConfig = new TempleConfig();
        //创建一个回调类
        Ma ma = new Ma();//创建一个回调类
        //注册输出结果回调类 必写
        templeConfig.setOutBack(ma);
        //全连接层深度,默认为2 选填
        templeConfig.setDeep(2);
        //要学习几种分类 默认为1 选填
        templeConfig.setClassificationNub(1);
        //设置图像行列比例的行，默认为5 选填
        templeConfig.setRow(5);
        //设置图像行列比例的列，默认为3 选填
        templeConfig.setColumn(3);
        //对模版进行初始化 Ps:初始化一定要在所有参数设置完成后设置，否则设置无效。
        // 使用默认值（模型参数注入除外）
        templeConfig.initNerveManager(true);//对模板初始化
        //初始化计算类
        Operation operation = new Operation(templeConfig);
        //初始化模板标注
        Map<Integer, Double> rightTagging = new HashMap<>();//分类标注
        Map<Integer, Double> wrongTagging = new HashMap<>();//分类标注
        rightTagging.put(1, 1.0);
        wrongTagging.put(1, 0.0);
        for (int i = 1; i < 999; i++) {
            System.out.println("开始学习1==" + i);
            Matrix right = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/c/c" + i + ".png");
            Matrix wrong = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/b/b" + i + ".png");
            operation.study(right, rightTagging);
            operation.study(wrong, wrongTagging);
        }
        Matrix right = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/c/c1000.png");
        Matrix wrong = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/b/b1000.png");
        ma.setNub(1);
        operation.look(right, 2);
        ma.setNub(2);
        operation.look(wrong, 3);
    }

}
