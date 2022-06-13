package org.wlld.config;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class Config {
    private int boxSize = 125;//检测物体大致大小(像素)
    private double pth = 0.7;//概率阈值
    private boolean isShowLog = false;//是否要打印学习输出
    private int typeNub = 2;//类别数量
    private int pictureNumber = 5;//训练图片数量
    private final double allLineStudyPoint = 0.05;//全链接层学习率0.02
    private final int typeHiddenNub = 5;//分类器隐层神经元数量5 改了
    private final double LParam = 0.01;//正则系数0.004
    private final double convStudyPoint = 0.01;//卷积层学习率0.005
    private final int kernLen = 5;//核大小
    private final int step = 2;//步长

    public double getPth() {
        return pth;
    }

    public void setPth(double pth) {
        this.pth = pth;
    }

    public int getPictureNumber() {
        return pictureNumber;
    }

    public void setPictureNumber(int pictureNumber) {
        this.pictureNumber = pictureNumber;
    }

    public double getAllLineStudyPoint() {
        return allLineStudyPoint;
    }

    public int getTypeHiddenNub() {
        return typeHiddenNub;
    }

    public double getLParam() {
        return LParam;
    }

    public double getConvStudyPoint() {
        return convStudyPoint;
    }

    public int getKernLen() {
        return kernLen;
    }

    public int getStep() {
        return step;
    }

    public int getBoxSize() {
        return boxSize;
    }

    public void setBoxSize(int boxSize) {
        this.boxSize = boxSize;
    }

    public boolean isShowLog() {
        return isShowLog;
    }

    public void setShowLog(boolean showLog) {
        isShowLog = showLog;
    }

    public int getTypeNub() {
        return typeNub;
    }

    public void setTypeNub(int typeNub) {
        this.typeNub = typeNub;
    }
}
