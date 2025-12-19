package org.dromara.easyai.config;

/**
 * @author lidapeng
 * @time 2025/3/3 10:51
 * @des unet参数配置类
 */
public class UNetConfig {
    private int XSize;//图像的高度
    private int YSize;//图像宽
    private float studyRate = 0.0001f;//学习率
    private float oneStudyRate = 0.0001f;//1*1学习率
    private int minFeatureValue = 30;//最小特征维度
    private int kerSize = 3;//卷积核大小
    private int channelNo = 1;//通道数
    private float cutTh = 0.5f;//裁切阈值像素 大于该亮度的进行裁切
    private boolean cutting = true;//是否要输出裁切图像
    private float gaMa = 0.9f;//自适应学习率衰减系数
    private float GMaxTh = 100f;//梯度阈值
    private boolean auto = false;

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public float getGMaxTh() {
        return GMaxTh;
    }

    public void setGMaxTh(float GMaxTh) {
        this.GMaxTh = GMaxTh;
    }

    public float getGaMa() {
        return gaMa;
    }

    public void setGaMa(float gaMa) {
        this.gaMa = gaMa;
    }

    public float getCutTh() {
        return cutTh;
    }

    public void setCutTh(float cutTh) {
        this.cutTh = cutTh;
    }

    public boolean isCutting() {
        return cutting;
    }

    public void setCutting(boolean cutting) {
        this.cutting = cutting;
    }

    public float getOneStudyRate() {
        return oneStudyRate;
    }

    public void setOneStudyRate(float oneStudyRate) {
        this.oneStudyRate = oneStudyRate;
    }

    public int getChannelNo() {
        return channelNo;
    }

    public void setChannelNo(int channelNo) {
        this.channelNo = channelNo;
    }

    public int getXSize() {
        return XSize;
    }

    public void setXSize(int XSize) {
        this.XSize = XSize;
    }

    public int getYSize() {
        return YSize;
    }

    public void setYSize(int YSize) {
        this.YSize = YSize;
    }

    public float getStudyRate() {
        return studyRate;
    }

    public void setStudyRate(float studyRate) {
        this.studyRate = studyRate;
    }

    public int getMinFeatureValue() {
        return minFeatureValue;
    }

    public void setMinFeatureValue(int minFeatureValue) {
        this.minFeatureValue = minFeatureValue;
    }

    public int getKerSize() {
        return kerSize;
    }

    public void setKerSize(int kerSize) {
        this.kerSize = kerSize;
    }
}
