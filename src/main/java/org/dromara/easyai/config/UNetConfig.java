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
    private float oneStudyRate = 0.01f;//1*1学习率
    private int minFeatureValue = 10;//最小特征维度
    private int kerSize = 3;//卷积核大小
    private int convTimes = 1;//单层卷积数量

    public float getOneStudyRate() {
        return oneStudyRate;
    }

    public void setOneStudyRate(float oneStudyRate) {
        this.oneStudyRate = oneStudyRate;
    }

    public int getConvTimes() {
        return convTimes;
    }

    public void setConvTimes(int convTimes) {
        this.convTimes = convTimes;
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
