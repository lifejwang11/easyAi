package org.dromara.easyai.randomForest;

/**
 * @author lidapeng
 * @description每棵树分类的结果和可信度
 * @date 8:37 下午 2020/2/28
 */
public class TreeWithTrust {
    private int type;//类别
    private float trust;//可信度

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public float getTrust() {
        return trust;
    }

    public void setTrust(float trust) {
        this.trust = trust;
    }
}
