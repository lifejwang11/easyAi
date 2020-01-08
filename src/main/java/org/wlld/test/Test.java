package org.wlld.test;

/**
 * @author lidapeng
 * @description
 * @date 2:11 下午 2020/1/7
 */
public class Test {
    public static void main(String[] args) {
        long a = System.currentTimeMillis();
        for (int i = 0; i < 12100000; i++) {
            Ma ma = new Ma(1, 1);
        }
        long b = System.currentTimeMillis();
        System.out.println(b - a);
    }
}
