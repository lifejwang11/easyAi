package org.dromara.easyai.conv.dcn;

/**
 * @author lidapeng
 * @time 2026/6/4 15:11
 * @des 保留分支网络训练参数
 */
public class DcnBody {
    private float nextX;
    private float nextY;
    private float m;
    private float bilinearValue;//双线性插值结果

    public float getBilinearValue() {
        return bilinearValue;
    }

    public void setBilinearValue(float bilinearValue) {
        this.bilinearValue = bilinearValue;
    }

    public float getNextX() {
        return nextX;
    }

    public void setNextX(float nextX) {
        this.nextX = nextX;
    }

    public float getNextY() {
        return nextY;
    }

    public void setNextY(float nextY) {
        this.nextY = nextY;
    }

    public float getM() {
        return m;
    }

    public void setM(float m) {
        this.m = m;
    }
}
