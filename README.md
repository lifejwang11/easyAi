<div align=center> <img src="https://myeasyai.cn/img/EasyAi.png" width="220" height="220">
<h3>我爸跟我说，我写这些东西还不如给我找个餐馆，让我去端盘子，或者给我找个超市去干收银。所以大家帮帮我，点击右上角 "Star" 支持一下，我要证明他错了，谢谢帮助！</h3>
</div>

# 前言

EasyAi对于Java的用处，等同于在JavaWeb领域spring的意义一样——做一个开箱即用，让每一个开发者都可以使用EasyAi，来开发符合自己人工智能业务需求的小微模型，这就是它的使命！

## EasyAi介绍

EasyAi无任何依赖，它是一个原生Java人工智能算法框架。首先，<strong face="微软雅黑" color=black size=5>
它可以Maven一键丝滑引入我们的Java项目，无需任何额外的环境配置与依赖，做到开箱即用。</strong >
再者，它既有一些我们已经封装好的图像目标检测及人工智能客服的模块，也提供各种深度学习，机器学习，强化学习，启发式学习，矩阵运算等底层算法工具。开发者可以通过简单的学习，就能完成根据自身业务，深度开发符合自己业务的小微模型。

## 入门教程：

* 框架视频教程地址，适用于只想调用已封装好的功能人群：https://www.bilibili.com/video/BV1W7411J7zr/
* 人工智能0基础JAVA程序员速成课,适用于想自定义开发任意ai的人群，地址：https://www.bilibili.com/cheese/play/ss17600
* 官方网站地址(及文档)： https://www.myeasyai.cn
* 部分案例Demo代码地址: https://gitee.com/ldp_dpsmax/easy-ai-demo
* 若对您的学习或生产有帮助，请留下您的STAR，您的一个简单的举动，对我来说却非常重要。

### 图像检测部分

* 使用EasyAi实现图像结算自动贩卖机视觉内核：

<div align=center> <img src="https://myeasyai.cn/img/drink.png"></div>

### 图像抠图
* 对图像语义进行像素级切割，前者输入原图像，后者进行输出抠图后的图像

<img src="https://myeasyai.cn/img/cut.png" alt="Image 1" style="vertical-align:middle; display:inline-block; width:48%; margin-right:2%;">
<img src="https://myeasyai.cn/img/cutting.png" alt="Image 2" style="vertical-align:middle; display:inline-block; width:48%;">

### 人脸识别
* 人脸识别是对人的脸部进行定位后进行识别，是常用的人工智能图像项目
* seeFace是基于EasyAi框架开发的开源社区级人脸识别内核算法
* 算法源码地址：https://gitee.com/ldp_dpsmax/see-face
* 算法封装应用地址:https://gitee.com/fushoujiang/easy-ai-face

<div align=center><img src="https://myeasyai.cn/img/face2.png"></div>

### 智能客服部分

* sayOrder是依赖EasyAi进行封装的人工智能客服系统。
* 它可以分析用户输入的语义，来识别用户的行为，并通过typeID来区分用户意图ID。并通过捕捉其后台设置的关键词类别，来抓出系统关心的用户在语句中包含的内容，比如语句中的时间，地点等。
* 它还可以与用户自主进行问答交互，进行自主解答疑问或者进行其余意图的交流等。
* 项目链接地址: https://gitee.com/dromara/sayOrder GitCode同步链接: https://gitcode.com/dromara/sayOrder/overview
* sayOrder管理后台登录

<div align=center> <img src="https://myeasyai.cn/img/index.png"></div>

* 配置业务分类及分类订单必要关键信息

<div align=center> <img src="https://myeasyai.cn/img/admin.png"></div>

* 对分类业务填写训练样本并标注关键信息

<div align=center> <img src="https://myeasyai.cn/img/worker.png"></div>

* 智能聊天/问答训练样本填充

<div align=center> <img src="https://myeasyai.cn/img/qa.png"></div>

### sayOrder智能客服沟通基本流程演示

* 用户第一次进行输入表达自己的想法

<div align=center> <img src="https://myeasyai.cn/img/a1.png"></div>

* 信息不足，所以用户接收到sayOrder的反问，用户需要进一步补充的自己的想法

<div align=center> <img src="https://myeasyai.cn/img/a2.png"></div>

* 用户第二次输入信息依然不满足，后台14分类法律咨询的订单关键信息的要求，继续补充信息，最终完成订单信息补充生成订单。

<div align=center> <img src="https://myeasyai.cn/img/a3.png"></div>

* 用户直接输入，理解并返回回答

<div align=center> <img src="https://myeasyai.cn/img/b1.png"></div>

### 软件说明：

* 本软件对物体在图像中进行训练及识别，切割，定位的轻量级，面向小白的框架。
* 本软件对中文输入语句，对输入语句的类别进行分类，关键词抓取，词延伸，以及集成智能客服功能在逐渐扩展中
* 若有想扩充的功能请进微信群提意见，若是通用场景我会陆续补充，微信群信息在文档下方。
* 本软件永久免费商业使用，但作者已拥有本软件相关的知识产权，任何个人与集体不可擅自申请本软件内的技术与代码的知识产权。
* 目前easyAI只有微信交流群，QQ交流群已经不再拉新，凡是QQ交流群拉进去的，都不是我的群！大家注意！！

### gitCode同步链接

https://gitcode.com/dromara/EasyAi/overview

## 部署

* 在pom文件中引入以下JAR包即可

```    
        <dependency>
             <groupId>org.dromara.easyai</groupId>
             <artifactId>easyAi</artifactId>
             <version>1.4.0</version>
        </dependency>
```

## 获得荣誉

* 华为云年度杰出开源开发者
<div><img src="https://myeasyai.cn/img/cup1.jpg" width="400",height="400" alt="huawei"></div>

* Gitee GVP最有价值开源项目
<div><img src="https://myeasyai.cn/img/cup3.jpg" width="400",height="400" alt="Gitee"></div>

* GitCode G-Star毕业项目
<div><img src="https://myeasyai.cn/img/cup2.jpg" width="400",height="400" alt="Gitee"></div>

### 微信交流群

* 加微信技术交流群（目前只有微信交流群，QQ交流群已经不再拉新，凡是QQ交流群拉进去的，都不是我的群！大家注意！！）
* ![加交流群](https://myeasyai.cn/img/erweima.jpg)
