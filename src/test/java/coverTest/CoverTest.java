package coverTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.wlld.MatrixTools.Matrix;
import org.wlld.ModelData;
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
        templeConfig.init(StudyPattern.Cover_Pattern, true, 400, 400, 3);
        ModelParameter modelParameter = JSONObject.parseObject(ModelData.DATA, ModelParameter.class);
        templeConfig.insertModel(modelParameter);
        return new Operation(templeConfig);//初始化运算类
    }

    public static void test(Operation operation, int poolSize, int sqlNub, int regionSize) throws Exception {
        Picture picture = new Picture();
        ThreeChannelMatrix threeChannelMatrix = picture.getThreeMatrix("D:\\db/a.jpg");
        ThreeChannelMatrix threeChannelMatrix2 = picture.getThreeMatrix("D:\\db/b.jpg");
        ThreeChannelMatrix threeChannelMatrix3 = picture.getThreeMatrix("D:\\db/c.jpg");

        Map<Integer, Double> map1 = operation.coverPoint(threeChannelMatrix, poolSize, sqlNub, regionSize);
        Map<Integer, Double> map2 = operation.coverPoint(threeChannelMatrix2, poolSize, sqlNub, regionSize);
        Map<Integer, Double> map3 = operation.coverPoint(threeChannelMatrix3, poolSize, sqlNub, regionSize);

        for (Map.Entry<Integer, Double> entry : map1.entrySet()) {
            System.out.println("1type==" + entry.getKey() + ",1value==" + entry.getValue());
        }
        System.out.println("=============================");
        for (Map.Entry<Integer, Double> entry : map2.entrySet()) {
            System.out.println("2type==" + entry.getKey() + ",2value==" + entry.getValue());
        }
        System.out.println("=============================");
        for (Map.Entry<Integer, Double> entry : map3.entrySet()) {
            System.out.println("2type==" + entry.getKey() + ",2value==" + entry.getValue());
        }
    }

    public static void cover() throws Exception {
        //创建图片解析类 桔梗覆盖，桔梗焚烧，土壤扰动
        Picture picture = new Picture();
        //创建模版类，参数选false就可以
        TempleConfig templeConfig = new TempleConfig();
        //初始化模板 注意 width height参数是你训练图片的实际尺寸需要改，其他不用动
        //创建运算类进行标注
        //templeConfig.setActiveFunction(new Sigmod());
        templeConfig.isShowLog(true);
        templeConfig.setStudyPoint(0.01);//不动
        templeConfig.setSoftMax(true);
        templeConfig.setDeep(2);
        templeConfig.setSensoryNerveNub(2);
        templeConfig.setRzType(RZ.L1);//不动
        templeConfig.setlParam(0.015);//不动
        templeConfig.init(StudyPattern.Cover_Pattern, true, 400, 400, 3);
        Operation operation = new Operation(templeConfig);
        for (int i = 1; i < 2; i++) {
            Map<Integer, ThreeChannelMatrix> matrixMap = new HashMap<>();
            ThreeChannelMatrix threeChannelMatrix1 = picture.getThreeMatrix("D:\\db/a.jpg");
            ThreeChannelMatrix threeChannelMatrix2 = picture.getThreeMatrix("D:\\db/b.jpg");
            ThreeChannelMatrix threeChannelMatrix3 = picture.getThreeMatrix("D:\\db/c.jpg");
            matrixMap.put(1, threeChannelMatrix1);
            matrixMap.put(2, threeChannelMatrix2);
            matrixMap.put(3, threeChannelMatrix3);
            operation.coverStudy(matrixMap, 4, 2, 10, 10);
        }
        ModelParameter modelParameter = templeConfig.getModel();
        String model = JSON.toJSONString(modelParameter);
        System.out.println(model);
        test(operation, 4, 2, 10);
    }
}
