package org.wlld.nerveEntity;

/**
 * @author lidapeng
 * 输出神经元
 * @date 11:25 上午 2019/12/21
 */
public class OutNerve extends Nerve {
    public OutNerve(int id, int upNub) {
        super(id, upNub);
    }

    @Override
    public void input(Nerve nerve) {
        System.out.println("最终输出：" + nerve.getMessage());
    }
}
