# 图像超市
本包功能说明：本包对物体在图像中进行训练及识别，切割，定位的轻量级，面向小白的框架,功能在逐渐扩展中
### 目的是
低硬件成本，CPU可快速学习运行，面向jAVA开发的程序员，经过简单API调用就可实现物体在图像中的识别，及定位等功能
### 特点是
入手门槛低，简单配置，快速上手
#### 为什么做这个包
* 因为图像属于超大浮点运算，亿对亿级，任何一点操作都会被扩大一千万倍以上，所以目前市面上的框架大都针对GPU运算。</br>
* 深度学习GPU价格昂贵，动则几万一块，这也是图像识别的费用门槛，而JAVA的用户一般都是使用CPU运算。</br>
* JAVA开发者很少会使用JCUDA 包的GPU浮点操作,目前的主流算法大都使用GPU运算（速度快）。</br>
* 为了保证用户对本包的使用性能，且降低部署成本，面向JAVA开发的程序员对图像的CPU快速处理，可以在CPU部署。</br>
* 所以本包对一些算法进行了部分功能阉割，部分精度忽略来保证速度，并且做到可CPU快速运算。</br>
* 阉割的代价，在某些精度上会有所下降，所以本包建议使用方案是对图像识别的分类。</br>
* 比如你要分辨当前图像 是 苹果或是香蕉或是桃子，对图像进行判断分类，精准度更高，对图像的切割，针对占比比较大的物体切割，定位。</br>
## 好的让我们从HELLO WORLD 开始:
public static void testPic() throws Exception {</br>
        //初始化图像转矩阵类</br>
       `Picture picture = new Picture();`</br>
        //初始化配置模板类</br>
        `TempleConfig templeConfig = getTemple(true);`</br>
        //初始化计算类</br>
        `Operation operation = new Operation(templeConfig);`</br>
        //标注主键为 第几种分类，值为标注 1 是TRUE 0是FALSE</br>
        `Map<Integer, Double> rightTagging = new HashMap<>();`//分类标注</br>
       `Map<Integer, Double wrongTagging = new HashMap<>();`//分类标注</br>
        `rightTagging.put(1, 1.0);`//标注为编号为1分类为正确，例如是否苹果，是</br>
        `wrongTagging.put(1, 0.0);`//编号为1的分类为错误，例如是否是苹果，否</br>
        `for (int i = 1; i < 999; i++) {`</br>
            System.out.println("开始学习1==" + i);</br>
            //读取本地URL地址图片,并转化成矩阵</br>
            //训练一个物体我建议是准备一万张这个物体的图片，只留物体周围要扣空，目前只支持PNG格式图片</br>
            //训练的时候光有正模板不行，你除了要告诉他true,必须还要告诉他false,正负模板数量相同</br>
            `Matrix right = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/c/c" + i + ".png");`</br>
            `Matrix wrong = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/b/b" + i + ".png");`</br>
            //将图像矩阵和正负标注加入进行学习，</br>
           `operation.study(right, rightTagging);`</br>
            `operation.study(wrong, wrongTagging);`</br>
       ` }`
        //获取训练结束的模型参数，提取出来转化成JSON保存数据库，下次服务启动时不用学习</br>
        //直接将模型参数注入</br>
        //获取模型MODLE</br>
       ` ModelParameter modelParameter = templeConfig.getModel();`</br>
        //将模型MODEL转化成JSON 字符串</br>
        //String model = JSON.toJSONString(modelParameter);</br>
        //将JSON字符串转化为模型MODEL</br>
        //ModelParameter modelParameter1 = JSONObject.parseObject(model, ModelParameter.class);</br>
        //初始化模型配置
        `TempleConfig templeConfig1 = getTemple(false);`</br>
        //注入之前学习结果的模型MODEL到配置模版里面</br>
        `templeConfig1.insertModel(modelParameter);`</br>
        //将配置模板配置到运算类</br>
       ` Operation operation1 = new Operation(templeConfig1);`</br>
        //获取本地图片字节码转化成降纬后的灰度矩阵</br>
       ` Matrix right = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/test/a101.png");`</br>
       ` Matrix wrong = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/b/b1000.png");`</br>
        //进行图像检测</br>
        `operation1.look(wrong, 3);`</br>
        `operation1.look(right, 2);`</br>
    }</br>
    `public static TempleConfig getTemple(boolean isFirst) throws Exception {`</br>
        `TempleConfig templeConfig = new TempleConfig();`</br>
        //创建一个回调类</br>
        `Ma ma = new Ma();`//创建一个回调类</br>
        //注册输出结果回调类 必写</br>
        `templeConfig.setOutBack(ma);`</br>
        //全连接层深度,默认为2 选填</br>
        `templeConfig.setDeep(2);`</br>
        //要学习几种分类 默认为1 选填</br>
        `templeConfig.setClassificationNub(1);`</br>
        //设置图像行列比例的行，默认为5 选填</br>
        `templeConfig.setRow(5);`</br>
        //设置图像行列比例的列，默认为3 选填</br>
        `templeConfig.setColumn(3);`</br>
        //对模版进行初始化 Ps:初始化一定要在所有参数设置完成后设置，否则设置无效。</br>
        // 使用默认值（模型参数注入除外）若无需注入参数 选择TRU，若注入模型参数选择FALSE</br>
        //相似说明见 文档1</br>
        `templeConfig.initModelVision(isFirst);`//对模板初始化 使用模板视觉</br>
        `return templeConfig;`</br>
   ` }`</br>
    参数详情请看说明1
