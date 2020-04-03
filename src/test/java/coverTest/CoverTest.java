package coverTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.wlld.MatrixTools.Matrix;
import org.wlld.ModelData;
import org.wlld.config.RZ;
import org.wlld.config.StudyPattern;
import org.wlld.function.Sigmod;
import org.wlld.imageRecognition.Operation;
import org.wlld.imageRecognition.Picture;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.nerveEntity.ModelParameter;
import org.wlld.tools.ArithUtil;

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
        //test(null, 25, 3);
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

    public static void test(Operation operation, int poolSize, int nerveNub) throws Exception {//识别桔梗或者土壤扰动
        if (operation == null) {
            operation = getModel();
        }
        Picture picture = new Picture();
        int allNub = 0;
        int wrongNub = 0;
        for (int i = 1; i < 100; i++) {
            allNub += 3;
            Matrix pic1 = picture.getImageMatrixByLocal("D:\\share\\cai/a" + i + ".jpg");//从本地磁盘读取图片
            Matrix pic2 = picture.getImageMatrixByLocal("D:\\share\\cai/b" + i + ".jpg");//从本地磁盘读取图片
            Matrix pic3 = picture.getImageMatrixByLocal("D:\\share\\cai/c" + i + ".jpg");//从本地磁盘读取图片
            Map<Integer, Double> point1 = operation.coverPoint(pic1, poolSize, nerveNub);//获取覆盖率
            Map<Integer, Double> point2 = operation.coverPoint(pic2, poolSize, nerveNub);//获取覆盖率
            Map<Integer, Double> point3 = operation.coverPoint(pic3, poolSize, nerveNub);//获取覆盖率
            int id = 0;
            double maxOut = 0;
            for (Map.Entry<Integer, Double> entry : point1.entrySet()) {
                int key = entry.getKey();
                double out = entry.getValue();
                if (out > maxOut) {
                    maxOut = out;
                    id = key;
                }
            }
            if (id != 1) {
                wrongNub++;
            }
            id = 0;
            maxOut = 0;
            for (Map.Entry<Integer, Double> entry : point2.entrySet()) {
                int key = entry.getKey();
                double out = entry.getValue();
                if (out > maxOut) {
                    maxOut = out;
                    id = key;
                }
            }
            if (id != 2) {
                wrongNub++;
            }
            id = 0;
            maxOut = 0;
            for (Map.Entry<Integer, Double> entry : point3.entrySet()) {
                int key = entry.getKey();
                double out = entry.getValue();
                if (out > maxOut) {
                    maxOut = out;
                    id = key;
                }
            }
            if (id != 3) {
                wrongNub++;
            }
        }
        double wrongPoint = ArithUtil.div(wrongNub, allNub) * 100;
        System.out.println("wrongPoint==" + wrongPoint + "%");
    }

    //覆盖率学习 有学习才能有识别
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
        templeConfig.setRzType(RZ.L1);//不动
        templeConfig.setlParam(0.015);//不动
        templeConfig.init(StudyPattern.Cover_Pattern, true, 400, 400, 3);
        Operation operation = new Operation(templeConfig);
        for (int j = 0; j < 1; j++) {
            for (int i = 1; i < 90; i++) {
                Map<Integer, Matrix> matrixMap = new HashMap<>();
                Matrix matrix1 = picture.getImageMatrixByLocal("D:\\share\\cai/a" + i + ".jpg");
                Matrix matrix2 = picture.getImageMatrixByLocal("D:\\share\\cai/b" + i + ".jpg");
                Matrix matrix3 = picture.getImageMatrixByLocal("D:\\share\\cai/c" + i + ".jpg");

                matrixMap.put(1, matrix1);
                matrixMap.put(2, matrix2);
                matrixMap.put(3, matrix3);
                //poolSize 越大 识别速度越快，准确率相对变低，
                operation.coverStudy(matrixMap, 25, 3);
            }
        }
        ModelParameter modelParameter = templeConfig.getModel();
        String model = JSON.toJSONString(modelParameter);
        System.out.println(model);
        test(operation, 25, 3);
        //识别初的该图片所属的分类id,既为训练时设定的标注
    }
}
