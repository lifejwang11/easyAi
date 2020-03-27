package coverTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.wlld.MatrixTools.Matrix;
import org.wlld.config.RZ;
import org.wlld.config.StudyPattern;
import org.wlld.function.Sigmod;
import org.wlld.imageRecognition.Operation;
import org.wlld.imageRecognition.Picture;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.nerveEntity.ModelParameter;

import java.io.InputStream;
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
        TempleConfig templeConfig = new TempleConfig(false, true);
        //初始化模板 注意 width height参数是你训练图片的实际尺寸需要改，其他不用动
        templeConfig.init(StudyPattern.Cover_Pattern, true, 320, 240, 2);
        //反序列化成模型
        ModelParameter modelParameter = JSONObject.parseObject(model, ModelParameter.class);
        templeConfig.insertModel(modelParameter);//注入学习过的模型
    }

    public static void coverModel(String url, InputStream inputStream
            , TempleConfig templeConfig) throws Exception {
        //覆盖率计算,计算以前，内存中已经注入过模型了
        Operation operation = new Operation(templeConfig);//初始化运算类
        Picture picture = new Picture();
        Matrix pic1 = picture.getImageMatrixByLocal(url);//从本地磁盘读取图片
        double point = operation.coverPoint(pic1, 1);//获取覆盖率
        Matrix pic2 = picture.getImageMatrixByIo(inputStream);//从输入流读取图片
    }

    public static void fireStudy() throws Exception {//土壤扰动，桔梗焚烧等识别
        Picture picture = new Picture();
        TempleConfig templeConfig = new TempleConfig(false, true);
        //classificationNub 参数说明，识别几种东西 就写几，比如 土壤扰动，桔梗焚烧 总共2个那么就写2
        templeConfig.init(StudyPattern.Accuracy_Pattern, true, 1000, 1000, 2);
        Operation operation = new Operation(templeConfig);
        for (int i = 1; i < 300; i++) {//一阶段
            //读取本地URL地址图片,并转化成矩阵
            Matrix a = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/a" + i + ".jpg");
            Matrix c = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/c" + i + ".jpg");
            //参数说明 第一个参数是 读取图片的矩阵，第二个参数是标注ID
            //比如我认为土壤扰动是1，桔梗焚烧是2
            operation.learning(a, 1, false);
            operation.learning(c, 2, false);
        }
        for (int i = 1; i < 300; i++) {
            Matrix a = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/a" + i + ".jpg");
            Matrix c = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/c" + i + ".jpg");
            //第二次学习注意标注要与第一次一致
            operation.learning(a, 1, true);
            operation.learning(c, 2, true);
        }
        templeConfig.finishStudy();//结束学习

    }

    public static void test(TempleConfig templeConfig, String url) throws Exception {//识别桔梗或者土壤扰动
        Picture picture = new Picture();
        templeConfig.init(StudyPattern.Accuracy_Pattern, true, 1000, 1000, 2);
        Operation operation = new Operation(templeConfig);
        Matrix a = picture.getImageMatrixByLocal(url);
        //识别初的该图片所属的分类id,既为训练时设定的标注
        int an = operation.toSee(a);

    }

    //覆盖率学习
    public static void cover() throws Exception {
        //创建图片解析类
        Picture picture = new Picture();
        //创建模版类，参数选false就可以
        TempleConfig templeConfig = new TempleConfig(false, false);
        //初始化模板 注意 width height参数是你训练图片的实际尺寸需要改，其他不用动
        //创建运算类进行标注
        templeConfig.setActiveFunction(new Sigmod());
        //templeConfig.setRzType(RZ.L2);
        //templeConfig.setlParam(0.015);
        templeConfig.isShowLog(true);
        templeConfig.init(StudyPattern.Cover_Pattern, true, 640, 480, 2);
        Operation operation = new Operation(templeConfig);
        Map<Integer, Double> rightTagging = new HashMap<>();//分类标注
        Map<Integer, Double> wrongTagging = new HashMap<>();//分类标注
        rightTagging.put(1, 1.0);//100%桔梗全覆盖标注
        wrongTagging.put(2, 1.0);//0%桔梗无覆盖标注

        //读取100%全覆盖桔梗图片
        Matrix right = picture.getImageMatrixByLocal("D:/b.jpg");
        //读取0%无覆盖土地图片
        Matrix wrong = picture.getImageMatrixByLocal("D:/a.jpg");
        //开始训练覆盖率
        operation.coverStudy(right, rightTagging, wrong, wrongTagging);

        //识别初的该图片所属的分类id,既为训练时设定的标注
        Matrix pic1 = picture.getImageMatrixByLocal("D:/a.jpg");//从本地磁盘读取图片
        Matrix pic2 = picture.getImageMatrixByLocal("D:/b.jpg");//从本地磁盘读取图片
        double point = operation.coverPoint(pic1, 1);//获取覆盖率
        double point2 = operation.coverPoint(pic2, 1);//获取覆盖率

        System.out.println("point==" + point);
        System.out.println("point2==" + point2);

        //学习完成，获取学习模型参数
        ModelParameter modelParameter = templeConfig.getModel();
        //将模型model保存数据库
        String model = JSON.toJSONString(modelParameter);
        System.out.println(model);
    }
}
