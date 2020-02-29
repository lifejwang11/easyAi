package org.wlld.randomForest;

/**
 * @author lidapeng
 * @description每棵树分类的结果和可信度
 * @date 8:37 下午 2020/2/28
 */
public class TreeWithTrust {
    private int type;//类别
    private double trust;//可信度

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getTrust() {
        return trust;
    }

    public void setTrust(double trust) {
        this.trust = trust;
    }
}
