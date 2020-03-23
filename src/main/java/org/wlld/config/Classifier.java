package org.wlld.config;

public class Classifier {//分类器
    public static final int LVQ = 1;//LVQ分类 你的训练模版量非常少 比如 一种只有几十一百张照片/分类少
    public static final int DNN = 2; //使用DNN分类 训练量足够大，一个种类1500+训练图片
    public static final int VAvg = 3;//使用特征向量均值分类 一种只有几十一百张照片
}
