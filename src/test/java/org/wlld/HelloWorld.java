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
        testPic();
        //testModel();
    }

    public static void testPic() throws Exception {
        //测试SPEED模式学习过程
        //初始化图像转矩阵类:作用就是说将一个图片文件转化为矩阵类
        Picture picture = new Picture();
        //初始化配置模板类,设置模式为SPEED_PATTERN模式 即速度模式
        TempleConfig templeConfig = getTemple(true, StudyPattern.Speed_Pattern);
        //初始化计算类，并将配置模版载入计算类
        Operation operation = new Operation(templeConfig);
        //标注主键为 第几种分类，值为标注 1 是TRUE 0是FALSE
        //给训练图像进行标注，健是分类的ID,对应的就是输出结果的ID值，值要么写0要么写1
        // 1就是 是这种分类，0就是不是这种分类
        Map<Integer, Double> rightTagging = new HashMap<>();//分类标注
        Map<Integer, Double> wrongTagging = new HashMap<>();//分类标注
        rightTagging.put(1, 1.0);
        wrongTagging.put(1, 0.0);
        // 例如上面的标注了 只有一种分类，第一个MAP是true标注，第二个map是false标注
        for (int i = 1; i < 999; i++) {
            System.out.println("开始学习1==" + i);
            //读取本地URL地址图片(适用于电脑本地图片),并转化成矩阵
            //注意学习图片至少要一千张+同物体的不同图片，学习的越多就越准，拿同样的图片反复循环学习是没用的
            //picture.getImageMatrixByIo(InputStream) 另外一个api,是通过字节流读取图片矩阵,适用于网络传输的图片
            Matrix right = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/c/c" + i + ".png");
            Matrix wrong = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/b/b" + i + ".png");
            //将图像矩阵和标注加入进行学习，正确的图片配置正确的标注true，错误的图片配置错误的标注false
            //right这个矩阵是 正确的图片 所以要配置上面正确的标注1.0 学习告诉计算机这个图片是正确的
            operation.study(right, rightTagging);
            //wrong这个矩阵是错误的图片，所以要配置上面错误的标注0.0 学习 告诉计算机这个图片是错误的
            operation.study(wrong, wrongTagging);
        }
        //获取训练结束的模型参数，提取出来转化成JSON保存数据库，下次服务启动时不用学习
        //直接将模型参数注入
        //获取模型MODLE 这个模型就是我们程序学习的目的，学习结束后我们要拿到这个模型
        ModelParameter modelParameter = templeConfig.getModel();
        //将模型MODEL转化成JSON 字符串 保存到数据库 留待下次服务启动的时候，识别提取用
        String model = JSON.toJSONString(modelParameter);
        //以上就是SPEED模式下的学习全过程，识别的过程就是再次初始化，将学习结果注入之后使用

        //识别过程
        //将从数据库取出的JSON字符串转化为模型MODEL
        ModelParameter modelParameter1 = JSONObject.parseObject(model, ModelParameter.class);
        //初始化模型配置
        TempleConfig templeConfig1 = getTemple(false, StudyPattern.Speed_Pattern);
        //注入之前学习结果的模型MODEL到配置模版里面，将学习结果注入就可以使用识别了
        templeConfig1.insertModel(modelParameter1);
        //将配置模板配置到运算类
        Operation operation1 = new Operation(templeConfig1);
        //获取本地图片字节码转化成降纬后的灰度矩阵
        Matrix right = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/test/a101.png");
        Matrix wrong = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/b/b1000.png");
        //进行图像识别 参数说明 eventId,事件id,因为输出结果是在回调类回调的，所以必须有个主键去判断事件
        //说明你回调是响应的哪一次调用的ID,所以每一次识别调用，请用不同的id
        operation1.look(wrong, 3);
        operation1.look(right, 2);
    }

    public static TempleConfig getTemple(boolean isFirst, int pattern) throws Exception {
        //创建一个配置模板类，作用：主要是保存及载入一些配置参数用
        TempleConfig templeConfig = new TempleConfig();
        //创建一个回调类，图像识别最后输出结果，在这个类输出，这个类要实现 OutBack接口
        Ma ma = new Ma();//创建一个回调类
        //将这个回调类注册到配置模版里 必写
        templeConfig.setOutBack(ma);
        //全连接层深度,选填可不填 不填默认值为2
        //这就像人类大脑的意识深度原理一样，深度学习越深，训练结果越准，但是训练量成几何倍数增加
        //比如默认深度是2 需要 正负模板各一千+张照片进行训练。识别率70%（数值只是举个例子，不是具体数值）
        //当深度改成3，则需要正负模板各三千+张照片进行训练,识别率 80%，深度4，八千+90%
        //以此类推，，内存允许的情况下，深度无限 识别率无限接近与百分之百
        //但是有极限，即超过某个深度，即使再增加深度，识别率反而会下降。需要具体不断尝试找到 合适的深度
        //注意：若深度提升，训练量没有成倍增长，则准确度反而更低！
        templeConfig.setDeep(2);
        //初始化配置模版，参数说明(int studyPattern, boolean initPower, int width, int height
        //, int classificationNub)
        //studyPattern 学习模式：常量值 StudyPattern.Accuracy_Pattern;StudyPattern.Speed_Pattern
        //第一种模式精准模式，第二种模式是速度模式
        //精准模式顾名思义，准确相对高很多，但是缺点也很明显学习速度慢，不是一般的慢，CPU学习1000张图片
        //24小时都不够用！它学习速度比速度模式学习速度慢十倍都不止！但是执行识别速度上，却比速度模式还要快一点！
        //第二种速度模式，学习速度明显很快，一千张图片的学习大概一个半小时左右，但是精准度上差了一些
        //但是依然还是比较精准的，尤其做分类判断的时候，问题不大。
        //如何选择模式：在大部分情况下速度模式就够用了，在分类一张图片，比如这张图片有苹果的概率是多少
        //有橘子的概率是多少，精准度已经足够，它不是不精准，只是相对于精准模式要差一些
        //所以在大部分情况下，还是建议用速度模式，满足很多识别分类需求
        //initPower,是否是第一次初始化
        //学习就是学的模型参数，学完了要把模型参数类拿出来，序列化成JSON字符串，保存数据库
        //下次服务启动，读取JSON字符串，反序列化为MODEL模型 直接注入就可，无需再次学习
        //如果说你是要学习就写true,如果已经有学习结果了，你要注入之前的学习结果就是false
        //如果你选了false还没有进行注入的话，你取模型参数你可以看到所有参数都是0
        //width heigth ,要学习的图片宽高，注意：这个宽高不是严格图片尺寸，而是一个大致尺寸
        //要识别和学习的图片尺寸与这个宽高比 必要相差太大就好，而且宁长勿短
        //classificationNub 要识别的有几个分类，比如我就识别苹果，就是1 有两种苹果橘子就是 2
        templeConfig.init(pattern, isFirst, 3204, 4032, 1);

        return templeConfig;
    }

    public static void testModel() throws Exception {
        // 模型参数获取及注入 实例
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

    public static void testPic2() throws Exception {
        //测试Accuracy_Pattern 模式学习过程，跟SPEED模式相同的部分就不再说明了
        Picture picture = new Picture();
        TempleConfig templeConfig = getTemple(true, StudyPattern.Accuracy_Pattern);
        Operation operation = new Operation(templeConfig);
        //标注主键为 第几种分类，值为标注 1 是TRUE 0是FALSE
        Map<Integer, Double> rightTagging = new HashMap<>();//分类标注
        Map<Integer, Double> wrongTagging = new HashMap<>();//分类标注
        rightTagging.put(1, 1.0);
        wrongTagging.put(1, 0.0);
        for (int i = 1; i < 2; i++) {
            System.out.println("开始学习1==" + i);
            //读取本地URL地址图片,并转化成矩阵
            Matrix right = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/c/c" + i + ".png");
            Matrix wrong = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/b/b" + i + ".png");
            //将图像矩阵和标注加入进行学习 注意的是 Accuracy_Pattern 模式 要学习两次
            //这里使用learning方法，前两个参数与SPEED模式相同，多了一个第三个参数
            //第一次学习的时候 这个参数必须是 false
            operation.learning(right, rightTagging, false);
            operation.learning(wrong, wrongTagging, false);
        }
        for (int i = 1; i < 2; i++) {//神经网络学习
            System.out.println("开始学习2==" + i);
            //读取本地URL地址图片,并转化成矩阵
            Matrix right = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/c/c" + i + ".png");
            Matrix wrong = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/b/b" + i + ".png");
            //将图像矩阵和标注加入进行学习，Accuracy_Pattern 模式 进行第二次学习
            //第二次学习的时候，第三个参数必须是 true
            operation.learning(right, rightTagging, true);
            operation.learning(wrong, wrongTagging, true);
        }
        Matrix right = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/test/a101.png");
        Matrix wrong = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/b/b1000.png");
        //进行图像识别，Accuracy_Pattern 模式学习结果获取和注入，跟SPEED模式一致
        //若有疑问可以参考一下 testModel()方法
        operation.look(right, 2);
        operation.look(wrong, 3);
    }
}
