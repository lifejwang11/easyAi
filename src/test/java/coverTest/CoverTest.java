package coverTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.wlld.MatrixTools.Matrix;
import org.wlld.ModelData;
import org.wlld.config.Classifier;
import org.wlld.config.RZ;
import org.wlld.config.StudyPattern;
import org.wlld.function.Sigmod;
import org.wlld.imageRecognition.*;
import org.wlld.nerveEntity.ModelParameter;
import org.wlld.tools.ArithUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lidapeng
 * @description
 * @date 12:54 下午 2020/3/16
 */
public class CoverTest {
    public static void main(String[] args) throws Exception {
        cover2("D:\\pic\\6/4.jpg");
        //test(null, 2, 2, 20);
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
        //templeConfig.setSoftMax(true);
        templeConfig.init(StudyPattern.Cover_Pattern, true, 400, 400, 2);
        ModelParameter modelParameter = JSONObject.parseObject(ModelData.DATA, ModelParameter.class);
        templeConfig.insertModel(modelParameter);
        return new Operation(templeConfig);//初始化运算类
    }

    public static void test(Operation operation, int poolSize, int sqlNub, int regionSize, String url) throws Exception {
        Picture picture = new Picture();
        int allNub = 0;
        int wrong = 0;
        if (operation == null) {
            operation = getModel();
        }
        ThreeChannelMatrix threeChannelMatrix = picture.getThreeMatrix(url);
        Map<Integer, Double> map1 = operation.coverPoint(threeChannelMatrix, poolSize, sqlNub, regionSize);
        for (Map.Entry<Integer, Double> entry : map1.entrySet()) {
            int key = entry.getKey();
            double value = entry.getValue();
            System.out.println("key===" + key + ",value==" + value);
        }

    }

    public static void cover2(String url) throws Exception {
        Picture picture = new Picture();
        TempleConfig templeConfig = new TempleConfig();
        templeConfig.setClassifier(Classifier.DNN);
        templeConfig.setTh(0.9);
        templeConfig.setSoftMax(true);
        templeConfig.init(StudyPattern.Accuracy_Pattern, false, 640, 480, 2);
        ModelParameter modelParameter = JSON.parseObject(ModelData.DATA3, ModelParameter.class);
        templeConfig.insertModel(modelParameter);
        Operation operation = new Operation(templeConfig);
        Matrix matrix = picture.getImageMatrixByLocal(url);
        int type = operation.toSee(matrix);
        if (type != 1) {//不是扰动
            test(null, 2, 3, 18, url);
        } else {//是扰动
            System.out.println("是扰动====");
        }
    }

    public static void cover() throws Exception {
        //创建图片解析类 桔梗覆盖，桔梗焚烧，土壤扰动
        Picture picture = new Picture();
        //创建模版类，参数选false就可以
        TempleConfig templeConfig = new TempleConfig();
        //初始化模板 注意 width height参数是你训练图片的实际尺寸需要改，其他不用动
        //创建运算类进行标注
        templeConfig.isShowLog(true);
        templeConfig.setStudyPoint(0.01);//不动
        //templeConfig.setSoftMax(true);
        //templeConfig.setDeep(2);
        //templeConfig.setHiddenNerveNub(12);
        templeConfig.setSensoryNerveNub(3);
        templeConfig.setRzType(RZ.L1);//不动//3 18
        templeConfig.setlParam(0.015);//不动
        templeConfig.init(StudyPattern.Cover_Pattern, true, 400, 400, 2);
        Operation operation = new Operation(templeConfig);
        for (int i = 1; i < 45; i++) {
            Map<Integer, ThreeChannelMatrix> matrixMap = new HashMap<>();
            //桔梗覆盖
            ThreeChannelMatrix threeChannelMatrix1 = picture.getThreeMatrix("D:\\pic\\test/b" + i + ".jpg");
            //土壤扰动
            ThreeChannelMatrix threeChannelMatrix2 = picture.getThreeMatrix("D:\\pic\\test/c" + i + ".jpg");
            //ThreeChannelMatrix threeChannelMatrix3 = picture.getThreeMatrix("D:\\pic\\test/d" + i + ".jpg");
            //ThreeChannelMatrix threeChannelMatrix4 = picture.getThreeMatrix("D:\\share\\jie/4.jpg");
            matrixMap.put(1, threeChannelMatrix1);//桔梗覆盖
            matrixMap.put(2, threeChannelMatrix2);//土壤扰动
            //matrixMap.put(3, threeChannelMatrix3);//白地
            operation.coverStudy(matrixMap, 2, 3, 18, 2);
        }
        ModelParameter modelParameter = templeConfig.getModel();
        String model = JSON.toJSONString(modelParameter);
        System.out.println(model);
    }
}
