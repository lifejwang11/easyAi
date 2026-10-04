package org.dromara.easyai.td;

/**
 * @author lidapeng
 * @time 2026/10/2 15:49
 */
public class TDState {
    private float value = 0;//价值
    private int id;//离散id
    private float prize = 0;//奖励
    private boolean finish = false;//是否为终止态

    public boolean isFinish() {
        return finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getPrize() {
        return prize;
    }

    public void setPrize(float prize) {
        this.prize = prize;
    }
}
