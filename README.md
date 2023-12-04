# easyAi
本包说明：
* 本包对物体在图像中进行训练及识别，切割，定位的轻量级，面向小白的框架。
* 本包对中文输入语句，对输入语句的类别进行分类，关键词抓取，词延伸，以及集成智能客服功能在逐渐扩展中
* 若有想扩充的功能请进群提意见，若是通用场景我会陆续补充，技术交流群：561433236
## 详细视频教程地址：
* 视频教程地址：https://www.bilibili.com/video/av89134035
## 基于easyAi智能客服助手-JECS
链接:https://gitee.com/ldp_dpsmax/JECS
## 框架效果演示结果:
* 因为是框架没有图像化界面，演示结果就是控制台输出的数据，只能用视频展示，想看演示结果请看教学视频
* 详细api文档见项目html文档:https://wlhlwl.com/gw/easyAi.html
### 目前拥有的核心功能（若对您的学习生产有帮助，请留下您的STAR）
* 对单张图片物体进行识别。
* 对中文语言进行分类语义识别,判断用户说话的语义是什么，关键词抓取，以及要做什么
* 游戏内交互机器人
* 若有想扩充的功能请进群提意见，若是通用场景我会陆续补充，技术交流群：222475213，561433236（满）
### 目的是
* 低硬件成本，CPU可快速学习运行，面向jAVA开发的程序员，经过简单API调用就可实现物体在图像中的识别，定位及中文语言分类，抓取关键词等功能
* 努力为中小企业提供AI场景成套解决技术方案
* easyAi交流1群：561433236（满了进不去）,easyAi交流2群：222475213
### 特点是
入手门槛低，简单配置，快速上手
#### 为什么做这个包
* 低门槛化：
现在随着人工智能技术的兴起，很多场景需要开发人员添加相应的功能，但是在二三线城市算法人才匮乏。
并且大多是JAVA开发程序员，业务做的更多，因为作者本人就是三线城市程序员，所以深知这一点。
所以我本人认为需要一款部署简单，不需要学习任何算法知识，
只通过最简单的API调用，就可以实现部分人工智能应用，并面向覆盖面最广的JAVA程序员使用的，且
能满足大部分AI业务场景实现的技术包。
* 面向用户：广大没接触过算法知识，人才相对匮乏的二三线JAVA业务开发程序员，实现人工智能应用
* 部署简单：
本包所有底层函数及数学库都是作者JAVA手写，不依赖任何第三方库，所以开发者只需要将本包下载到本地后，打成JAR包
引入到自己的POM文件中，就可以独立使用所有功能。
* 功能还在扩展：
本包现在的功能还在逐步扩展中
* 抛错捕获暂时还没有做全，若有抛错请进群交流：561433236，我来做一下错误定位
* 若您有相对复杂的人工智能业务（开源功能无法满足的，包括但不限于图像识别，自然语言）请联系作者 vx:thenk008 进行基于easyAi定制化业务情景开发（即java人工智能开发）
## HELLO WORLD说明：
* 以下为最简API文档，所有非必设参数都使用本引擎默认值
* 要注意的是使用最简API，及参数默认值准确度远不能达到最佳状态
### 图像学习部分最简API 说明:
``` java
       训练过程
       Picture picture = new Picture();//图片解析类
        Config config = new Config();//配置文件
        config.setTypeNub(2);//设置训练种类数
        config.setBoxSize(125);//设置物体大致大小 单位像素 即 125*125 的矩形
        config.setPictureNumber(5);//设置每个种类训练图片数量 某个类别有几张照片，注意所有种类照片数量要保持一致
        config.setPth(0.7);//设置可信概率，只有超过可信概率阈值，得出的结果才是可信的 数值为0-1之间
        config.setShowLog(true);//输出学习时打印数据
        Distinguish distinguish = new Distinguish(config);//创建识别类
        distinguish.setBackGround(picture.getThreeMatrix("E:\\ls\\fp15\\back.jpg"));//设置识别的背景图片(该api为固定背景)
        List<FoodPicture> foodPictures = new ArrayList<>();//创建训练模板集合
        for (int i = 1; i < 3; i++) {
            FoodPicture foodPicture = new FoodPicture();//创建每一类图片的训练模板类
            foodPictures.add(foodPicture);//将该类模板加入集合
            List<PicturePosition> picturePositionList = new ArrayList<>();//创建该类模板的训练集合类
            foodPicture.setId(i + 1);//设置该图片类别id
            foodPicture.setPicturePositionList(picturePositionList);
            for (int j = 1; j < 6; j++) {//训练图片数量为 每种五张 注意跟config 中的 pictureNumber 要一致
                String name;
                if (i == 1) {//加载图片url地址名称
                    name = "a";
                } else {
                    name = "b";
                }
                PicturePosition picturePosition = new PicturePosition();
                picturePosition.setUrl("E:\\ls\\fp15\\" + name + i + ".jpg");//加载该类别图片地址
                picturePosition.setNeedCut(false);//是否需要剪切，若训练素材为充满全图图片，则充满全图不需要剪切 写false
                picturePositionList.add(picturePosition);//加载
            }
        }
        distinguish.studyImage(foodPictures);//进行学习
        System.out.println(JSON.toJSONString(distinguish.getModel()));//输出模型保存,将模型实体类序列化为json保存
       ///////////////////////////////////////////////////////////////////////
       初始化过程
        Picture picture = new Picture();//图片解析类
        Config config = new Config();//配置文件
        config.setTypeNub(2);//设置类别数量
        config.setBoxSize(125);//设置物体大小 单位像素
        config.setPictureNumber(5);//设置每个种类训练图片数量
        config.setPth(0.7);//设置可信概率，只有超过可信概率阈值，得出的结果才是可信的
        config.setShowLog(true);//输出学习时打印数据
        Distinguish distinguish = new Distinguish(config);//识别类
        distinguish.insertModel(JSONObject.parseObject(ModelData.DATA, Model.class));//将之前训练时保存的训练模型反序列化为实体类后，注入模型
        完成后请单例Distinguish类，即完成系统启动时初始化过程
        ///////////////////////////////////////////////////////////////////////
        识别过程
        Distinguish distinguish; 此识别类为系统启动时已经初始化的 单例distinguish，识别过程请不要 "new" 这个类
         for (int i = 1; i < 8; i++) {
            System.out.println("i====" + i);
            ThreeChannelMatrix t = picture.getThreeMatrix("E:\\ls\\fp15\\t" + i + ".jpg");//将识别图片转化为矩阵
            Map<Integer, Double> map = distinguish.distinguish(t);//识别结果
            for (Map.Entry<Integer, Double> entry : map.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());//识别结果打印
            }
        }
        ////////////////////////////////////////////////////////////////////////////////////
        识别结果打印说明(此为本包提供的测试图片样本的 输出结果说明，在之前的训练中橘子设置的id为2，苹果为3)
        i====1//第一张图 结果为 橘子，出现2：代表类别。:0.8874306751020916，带表该类别权重，权重越高说明该类别的物品在当前 图片中数量越多或者面积越大。
        2:0.8874306751020916 说明（图1有橘子，权重为：0.8874306751020916）
        i====2
        2:0.8878192183606407
        i====3
        3:0.7233916245920673说明（图3有苹果，权重为：0.7233916245920673）
        i====4
        2:0.9335699571468958说明（图4有橘子，权重为：0.9335699571468958）
        3:0.7750825597199661说明（图4有苹果，权重为：0.7750825597199661）
        i====5
        3:0.8481590575557582
        i====6
        2:0.7971025523095067
        i====7
        2:1.5584968376080388（图7有橘子，权重为：1.5584968376080388）
        3:0.8754957897385587（图7有苹果，权重为：0.8754957897385587）
        本演示样例代码位置在： src/test/java/org/wlld/ImageTest.java
        本演示训练素材位置在： src/test/image
        注意：以上图片识别代码样例为训练素材为物品全图充满图片(自己看能看到橘子训练图片为全图充满，苹果也是).自行开发时用以上代码样例时，请也使用全图充满训练物品的图片来做训练，非全图充满训练素材图训练api有变化！
```
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
### 自然语言分类最简API 说明:
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
