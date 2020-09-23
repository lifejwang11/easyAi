package coverTest;

import com.alibaba.fastjson.JSONObject;
import org.wlld.MatrixTools.Matrix;
import org.wlld.ModelData;
import org.wlld.config.Classifier;
import org.wlld.config.RZ;
import org.wlld.config.StudyPattern;
import org.wlld.imageRecognition.*;
import org.wlld.nerveEntity.ModelParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lidapeng
 * @description
 * @date 12:54 下午 2020/3/16
 */
public class CoverTest {
    public static void main(String[] args) throws Exception {

        cover();
    }

    public static void insertModel(String model) throws Exception {//注入模型
        //模型服务启动注入一次，内存长期持有静态化 TempleConfig配置类
        //每个TempleConfig 要单例
        //创建模版类，参数选false就可以
        ModelParameter modelParameter = JSONObject.parseObject(model, ModelParameter.class);
        TempleConfig templeConfig = new TempleConfig();
        templeConfig.setSoftMax(true);
        templeConfig.init(StudyPattern.Cover_Pattern, true, 400, 400, 3);
        templeConfig.insertModel(modelParameter);
        Operation operation1 = new Operation(templeConfig);
    }

    public static Operation getModel() throws Exception {
        //覆盖率计算,计算以前，内存中已经注入过模型了
        TempleConfig templeConfig = new TempleConfig();
        templeConfig.setSensoryNerveNub(3);
        templeConfig.setSoftMax(true);
        templeConfig.setDeep(2);
        templeConfig.setHiddenNerveNub(9);
        templeConfig.init(StudyPattern.Cover_Pattern, true, 400, 400, 4);
        ModelParameter modelParameter = JSONObject.parseObject(ModelData.DATA, ModelParameter.class);
        templeConfig.insertModel(modelParameter);
        return new Operation(templeConfig);//初始化运算类
    }

    public static void test(Operation operation, int sqlNub, int regionSize,
                            String name, int t) throws Exception {
        Picture picture = new Picture();
        int wrong = 0;
        if (operation == null) {
            operation = getModel();
        }
        for (int i = 1; i < 20; i++) {
            String na = "/Users/lidapeng/Desktop/myDocument/food/" + name + i + ".jpg";
            //System.out.println("name======================" + na);
            ThreeChannelMatrix threeChannelMatrix = picture.getThreeMatrix(na);
            Map<Integer, Double> map1 = operation.coverPoint(threeChannelMatrix, sqlNub, regionSize);
            int id = 0;
            double point = 0;
            for (Map.Entry<Integer, Double> entry : map1.entrySet()) {
                int key = entry.getKey();
                double value = entry.getValue();
                // System.out.println("key==" + key + ",value==" + value);
                if (value > point) {
                    point = value;
                    id = key;
                }
            }
            if (id != t) {
                wrong++;
            }
        }
        System.out.println("错误率：" + wrong + "%");
    }

    public static void cover2(Operation operation, String url) throws Exception {
        Picture picture = new Picture();
        Matrix matrix = picture.getImageMatrixByLocal(url);
        double type = operation.toSeeById(matrix, 1);
        System.out.println("type==" + type);
    }

    public static void cover() throws Exception {
        int sqNub = 1;
        int regionSize = 20;
        //创建图片解析类 桔梗覆盖，桔梗焚烧，土壤扰动
        Picture picture = new Picture();
        //创建模版类，参数选false就可以
        TempleConfig templeConfig = new TempleConfig();
        //初始化模板 注意 width height参数是你训练图片的实际尺寸需要改，其他不用动
        //创建运算类进行标注
        templeConfig.isShowLog(true);
        templeConfig.setStudyPoint(0.005);//不动
        //templeConfig.setSoftMax(true);
        templeConfig.setDeep(2);
        templeConfig.setHiddenNerveNub(9);
        templeConfig.setSensoryNerveNub(sqNub);//多出来的
        templeConfig.setRzType(RZ.L1);//不动//3 18
        templeConfig.setlParam(0.015);//不动
        templeConfig.setClassifier(Classifier.DNN);
        templeConfig.init(StudyPattern.Cover_Pattern, true, 400, 400, 4);
        Operation operation = new Operation(templeConfig);
        for (int i = 1; i < 20; i++) {
            System.out.println("i====================================" + i);
            Map<Integer, ThreeChannelMatrix> matrixMap = new HashMap<>();
            ThreeChannelMatrix threeChannelMatrix1 = picture.getThreeMatrix("/Users/lidapeng/Desktop/myDocument/food/a" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix2 = picture.getThreeMatrix("/Users/lidapeng/Desktop/myDocument/food/b" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix3 = picture.getThreeMatrix("/Users/lidapeng/Desktop/myDocument/food/c" + i + ".jpg");
            ThreeChannelMatrix threeChannelMatrix4 = picture.getThreeMatrix("/Users/lidapeng/Desktop/myDocument/food/d" + i + ".jpg");
            matrixMap.put(1, threeChannelMatrix1);//桔梗覆盖
            matrixMap.put(2, threeChannelMatrix2);//土地
            matrixMap.put(3, threeChannelMatrix3);//桔梗覆盖
            matrixMap.put(4, threeChannelMatrix4);//土地
            operation.coverStudy(matrixMap, sqNub, regionSize);
        }
//        ModelParameter modelParameter = templeConfig.getModel();
//        String model = JSON.toJSONString(modelParameter);
//        System.out.println(model);
        test(operation, sqNub, regionSize, "a", 1);
        test(operation, sqNub, regionSize, "b", 2);
        test(operation, sqNub, regionSize, "c", 3);
        test(operation, sqNub, regionSize, "d", 4);

    }
}
