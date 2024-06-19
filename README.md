### <div align=center>![logo](./zf/EasyAi.png)</div>

### 软件说明：

* 本软件对物体在图像中进行训练及识别，切割，定位的轻量级，面向小白的框架。
* 本软件对中文输入语句，对输入语句的类别进行分类，关键词抓取，词延伸，以及集成智能客服功能在逐渐扩展中
* 若有想扩充的功能请进微信群提意见，若是通用场景我会陆续补充，微信群信息在文档下方。
* 本软件永久免费商业使用，但作者已拥有本软件相关的知识产权，任何个人与集体不可擅自申请本软件内的技术与代码的知识产权。
* 目前easyAI只有微信交流群，QQ交流群已经不再拉新，凡是QQ交流群拉进去的，都不是我的群！大家注意！！

### github同步链接

https://github.com/lifejwang11/easyAi

## 详细视频教程地址：

* 框架视频教程地址：https://www.bilibili.com/video/BV1W7411J7zr/?vd_source=20cf39c973b43e60c3bdbe8d47dc9e71
* 人工智能0基础JAVA程序员速成课（基础篇） 本课程为基础篇内容，后续更新还会有图像篇与自然语言篇，三部分构成。
  地址：https://www.bilibili.com/cheese/play/ss17600

## 框架效果演示结果:

* 因为是框架没有图像化界面，演示结果就是控制台输出的数据，只能用视频展示，想看演示结果请看教学视频
* api文档远程地址： http://myeasyai.cn/
### 强大的自主智能客服工具，支持自动与用户对话，解答疑问，引导消费与对话，并捕捉用户对话中的需求后自动生成订单！基于easyAi算法引擎构建智脑——SayOrder(原名myJecs，现已正式更名为SayOrder)

链接:https://gitee.com/dromara/sayOrder (gitee) https://github.com/lifejwang11/SayOrder (github)

### 目前拥有的核心功能（若对您的学习或生产有帮助，请留下您的STAR）

* 对图片中得物体进行识别。
* 对中文语言进行分类语义识别,判断用户说话的语义是什么，关键词抓取，以及要做什么
* 游戏内交互策略机器人
* 若有想扩充的功能请进群提意见，若是通用场景我会陆续补充，微信技术交流群：thenk008

#### 目前市场上的AI应用的问题

* 高门槛：
  现在随着人工智能技术的兴起，很多场景需要开发人员添加相应的功能，但是大多是JAVA开发程序员，JAVA却在AI领域的开源比较匮乏。
  现在比较火的大模型的应用只能帮助人去提高工作效率，而不能帮助业务系统，去自定义用户自己的业务来内嵌进系统进而帮助业务系统的智能化。
* 高成本：
  人工智能往往要求大算力，大训练样本，使得大多数中小企业和个人开发者自定义AI入手成本非常高昂。

#### easyAI要怎么解决

* easyAI是百分之百Java代码编写，并且无任何依赖，开箱即用，对广大JAVA程序员排除掉任何环境配置依赖问题，做到最友善。
* easyAI并不是对主流算法JAVA的无差别重新实现，而是根据应用场景对主流算法进行优化与魔改，让用户即便使用一台普通的电脑，就可开箱直接跑起来。并且保证普通服务器或个人电脑CPU下依然达到可用性能的流畅运行。
  所以我的“easy”并不是只是指的简单，而是对算法进行了廉价，低成本方向的优化。如果我没有办法对某种算法做到廉价优化的算法实现，我也不会放入easyAI里面。
* 只通过最简单的API调用，就可以实现部分人工智能应用，并面向覆盖面最广的JAVA程序员使用的，且能满足大部分AI业务场景实现的算法引擎。
* 部署简单：
  本引擎所有底层函数及数学库都是作者JAVA手写，不依赖任何第三方库，所以开发者只需要将本包下载到本地后，打成JAR包
  引入到自己的POM文件中，就可以独立使用所有功能。
* 功能还在扩展：
  本项目现在的功能还在逐步扩展中

### 微信交流群

* 加微信技术交流群（目前只有微信交流群，QQ交流群已经不再拉新，凡是QQ交流群拉进去的，都不是我的群！大家注意！！）
* ![加交流群](./zf/wxt.png)

## HELLO WORLD说明：

* 以下为最简API文档，所有非必设参数都使用本引擎默认值
* 要注意的是使用最简API，及参数默认值准确度远不能达到最佳状态

### 图像学习部分最简API 说明:

``` java
    ///////////////训练部分
    YoloConfig yoloConfig = new YoloConfig();//创建配置参数类
    //配置类内的配置参数，根据实际情况修改
    private int windowWidth = 90;//检测窗口宽
    private int windowHeight = 140;//检测窗口高
    private int typeNub = 10;//需要识别的种类数
    private int hiddenNerveNub = 16;//线性层隐层神经元数量
    private double lineStudy = 0.01;//线性层学习率
    private int kernelSize = 3;//卷积核尺寸
    private boolean showLog = false;//是否打印学习过程中的log
    private double convStudy = 0.01;//卷积层学习率
    private int enhance = 800;//训练样本过少时增强效果，如果有足够训练数据量，请将其设置为1
    private double iouTh = 0.05;//NMS合并框交并比阈值
    private double containIouTh = 0.15;//训练时是否包含样本判断交并比阈值
    
    FastYolo yolo = new FastYolo(yoloConfig); //初始化图像识别类
    List<YoloSample> data//目标标注类集合
    YoloSample data;//标注类
    //标注类属性
    private String locationURL;//标注图片的本地url
    private List<YoloBody> yoloBodies;//该图片的内部标注
    YoloBody yoloBody;//内部标注类
    //内部标注类属性
    private int x;//目标左上角x的值
    private int y;//目标左上角得y的值
    private int width;//目标的宽度
    private int height;//目标的高度
    private int typeID;//标注类别id
    //开始训练
    yolo.toStudy(data);
    //训练结束将模型写出
    YoloModel yoloModel = yolo.getModel();
    writeModel(JSON.toJSONString(yoloModel), "D:\\lesson/yoloModel.json");
    /////////////////////初始化部分（单例且只初始化一次）
    FastYolo yolo = new FastYolo(yoloConfig); //初始化图像识别类
    YoloModel yoloModel = readModelParameter();//从训练的模型中JSON反序列化读取模型
    yolo.insertModel(yoloModel);//识别类注入模型
    //////////////////////识别部分
     long eventID;//需要生成一个唯一线程id来保证并发时的线程安全问题，可直接用雪花算法id
     Picture picture = new Picture();//初始化图像解析类
     ThreeChannelMatrix th = picture.getThreeMatrix("D:\\lesson/number.png");//将图像解析为矩阵
     List<OutBox> list = yolo.look(th,eventID);//对该图像矩阵进行识别，并返回识别结果
     List<OutBox> list;//识别结果集合
    ////识别结果类
    OutBox outBox;
    private int x;//检测到物体在该图片中的横坐标
    private int y;//检测到物体在该图片中的纵坐标
    private int width;//检测到物体在该图片中的宽度
    private int height;//检测到物体在该图片中的高度
    private int typeID;//检测到物体在该图片中的类别id
```

#### 图像识别结果展示

![图像识别结果展示](/zf/abc.png)

### 通过给图片生成摘要id进行快速相似度对比

``` java
//参数分别为：
//第一个参数：threeChannelMatrix,图片矩阵（图片矩阵如何提取，上文有讲不在阐述）
//第二个参数:boxSize,将一张图片横纵各分为几个区域提取特征
参数说明：该值越大，摘要id敏感度越高，该参数有最大值。最大值为图片：图片最小边长/5,超过会报错数组越界
//第三个参数:regionSize,相似特征区域分区种类数量
参数说明：该值越大，摘要id敏感度越高
//返回name 即为该图片摘要id，通过id逐位对比即可对比相似程度
//什么是id敏感度：
//id敏感度越高，对图片变化越敏感，越适合越大的检索区域匹配，即特征越细致，但缺点id长度越长。
//id敏感度越低，对图片变化越不敏感，越适合越小的检索区域匹配,特征越粗，优点是id长度越短。
FastPictureExcerpt fastPictureExcerpt = new FastPictureExcerpt();
String name = fastPictureExcerpt.creatImageName(threeChannelMatrix, 5, 10);
```

### 自然语言分类最简API 说明（已过时，自然语言处理请移步SayOrder 链接：https://gitee.com/dromara/sayOrder ）:

``` java
         //通过txt默认格式进行读取
        TemplateReader templateReader = new TemplateReader();
        WordTemple wordTemple = new WordTemple();//初始化语言模版，该语言模板训练结束后一定要static出来,在内存中长期持有，Talk识别构造参数进行复用
        //wordTemple.setTreeNub(9);
        //wordTemple.setTrustPunishment(0.5);
        //读取语言模版，第一个参数是模版地址，第二个参数是编码方式 (教程里的第三个参数已经省略)
        //同时也是学习过程
        templateReader.read("/Users/lidapeng/Desktop/myDocument/model.txt", "UTF-8", wordTemple);
        Talk talk = new Talk(wordTemple);
        //输入语句进行识别，若有标点符号会形成LIST中的每个元素
        //返回的集合中每个值代表了输入语句，每个标点符号前语句的分类
        List<Integer> list = talk.talk("空调坏了，帮我修一修");
        System.out.println(list);
        /////////////////////////////////自定义输入训练语句
        WordTemple wordTemple = new WordTemple();//初始化语言模版，该语言模板训练结束后一定要static出来,在内存中长期持有，Talk识别构造参数进行复用
        Tokenizer tokenizer = new Tokenizer(wordTemple);//学习类
        //训练模板 主键为类别id,值为该类别id的语句集合
        //注意
        //1，若训练量不足，建议训练语句通过标点符号拆分为若干句，且不要将标点符号带入训练语句
        //2，包含数字的语句用统一的占位符代替 例如 35,3,36% 变为 #,#,#%
        Map<Integer, List<String>> model = new HashMap<>();
        //开始训练
        tokenizer.start(model);
        ///////////////////////////////////单纯对输入语句进行切词结果返回，不进行识别
        wordTemple.setSplitWord(true);//将模板设置成纯切词模式
        List<List<String>> lists = talk.getSplitWord("空调坏了，帮我修一修");
        for (List<String> list : lists) {
            System.out.println(list);
        }
   ```

### 神经网络最简API说明

``` java
     //创建一个DNN神经网络管理器
     NerveManager nerveManager = new NerveManager(...);
     //构造参数
     //sensoryNerveNub 感知神经元数量 即输入特征数量
     //hiddenNerverNub  每一层隐层神经元的数量
     //outNerveNub 输出神经元数量 即分类的类别
     //hiddenDepth 隐层神经元深度，即学习深度
     //activeFunction 激活函数
     //isDynamic 是否启用动态神经元数量(没有特殊需求建议为静态，动态需要专业知识)
     public NerveManager(int sensoryNerveNub, int hiddenNerverNub, int outNerveNub, int hiddenDepth, ActiveFunction activeFunction, boolean isDynamic)
     nerveManager.getSensoryNerves()获取感知神经元集合
     //eventId:事件ID
     //parameter:输入特征值
     //isStudy:是否是学习
     //E:特征标注
     //OutBack 回调类
     SensoryNerv.postMessage(long eventId, double parameter, boolean isStudy, Map<Integer, Double> E, OutBack outBack)
     //每一次输出结果都会返回给回调类，通过回调类拿取输出结果，并通过eventId来对应事件
```

### 随机森林最简API说明

``` java
        //创建一个内存中的数据表
        DataTable dataTable = new DataTable(column);
        //构造参数是列名集合
        public DataTable(Set<String> key)
        //指定主列名集合中该表的主键
        dataTable.setKey("point");
        //创建一片随机森林
        RandomForest randomForest = new RandomForest(7);
        //构造参数为森林里的树木数量
        public RandomForest(int treeNub)
        //唤醒随机森林里的树木
        randomForest.init(dataTable);
        //将加入数据的实体类一条条插入森林中
        randomForest.insert(Object object);
        //森林进行学习
        randomForest.study();
        //插入特征数据，森林对该数据的最终分类结果进行判断
        randomForest.forest(Object objcet);
```

### 如果该项目对你有用，请赞助一下作者的劳动力支持开源，请作者吃一顿早饭就好！给作者持续更新easyAi引擎，同时封装更多依赖easyAi的常用应用提供动力！为大家低成本部署AI应用添砖加瓦！万谢!好心人！

![支付宝支付](/zf/zs.jpg)![微信支付](/zf/ws.jpg)