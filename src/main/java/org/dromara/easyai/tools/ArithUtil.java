package org.dromara.easyai.tools;


import java.math.BigDecimal;

public class ArithUtil {
    private static final int DEF_DIV_SCALE = 10;

    private ArithUtil() {
    }

    public static float add(float d1, float d2) {//加法
        BigDecimal b1 = new BigDecimal(Float.toString(d1));
        BigDecimal b2 = new BigDecimal(Float.toString(d2));
        return b1.add(b2).floatValue();

    }

    public static float sub(float d1, float d2) {//减法
        BigDecimal b1 = new BigDecimal(Float.toString(d1));
        BigDecimal b2 = new BigDecimal(Float.toString(d2));
        return b1.subtract(b2).floatValue();

    }

    public static float mul(float d1, float d2) {//乘法
        BigDecimal b1 = new BigDecimal(Float.toString(d1));
        BigDecimal b2 = new BigDecimal(Float.toString(d2));
        return b1.multiply(b2).floatValue();

    }

    public static float div(float d1, float d2) {//除法

        return div(d1, d2, DEF_DIV_SCALE);

    }

    public static float div(float d1, float d2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Float.toString(d1));
        BigDecimal b2 = new BigDecimal(Float.toString(d2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).floatValue();

    }

}
