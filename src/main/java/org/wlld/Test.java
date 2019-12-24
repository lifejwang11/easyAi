package org.wlld;

import org.wlld.i.OutBack;

/**
 * @author lidapeng
 * @description
 * @date 1:19 下午 2019/12/24
 */
public class Test implements OutBack {
    @Override
    public void getBack(double out, int id, long eventId) {
        System.out.println("out==" + out + ",id==" + id + ",eventId==" + eventId);
    }
}
