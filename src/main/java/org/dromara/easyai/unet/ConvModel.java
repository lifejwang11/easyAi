package org.dromara.easyai.unet;

import java.util.List;

/**
 * @author lidapeng
 * @time 2025/3/3 14:57
 * @des 卷积模型
 */
public class ConvModel {
    private List<Float[]> downNervePower;//下卷积权重
    private List<Float[]> upNervePower;//上卷积权重
    private List<Float> oneNervePower;//1v1conv权重
    private List<List<Float>> oneNervePowerList;

    public List<List<Float>> getOneNervePowerList() {
        return oneNervePowerList;
    }

    public void setOneNervePowerList(List<List<Float>> oneNervePowerList) {
        this.oneNervePowerList = oneNervePowerList;
    }

    public List<Float[]> getDownNervePower() {
        return downNervePower;
    }

    public void setDownNervePower(List<Float[]> downNervePower) {
        this.downNervePower = downNervePower;
    }

    public List<Float[]> getUpNervePower() {
        return upNervePower;
    }

    public void setUpNervePower(List<Float[]> upNervePower) {
        this.upNervePower = upNervePower;
    }

    public List<Float> getOneNervePower() {
        return oneNervePower;
    }

    public void setOneNervePower(List<Float> oneNervePower) {
        this.oneNervePower = oneNervePower;
    }
}
