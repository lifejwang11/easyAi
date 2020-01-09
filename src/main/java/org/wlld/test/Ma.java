package org.wlld.test;

import org.wlld.i.OutBack;

/**
 * @author lidapeng
 * @description
 * @date 2:12 下午 2020/1/7
 */
public class Ma implements OutBack {
    private int nub;

    public void setNub(int nub) {
        this.nub = nub;
    }

    @Override
    public void getBack(double out, int id, long eventId) {
        System.out.println("id==" + id + ",out==" + out + ",nub==" + nub);
    }
}
