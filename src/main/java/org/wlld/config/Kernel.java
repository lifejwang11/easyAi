package org.wlld.config;

import org.wlld.MatrixTools.Matrix;
import org.wlld.tools.ArithUtil;

public class Kernel {
    private static final String Vertical_Number = "[-1,0,1]#[-2,0,2]#[-1,0,1]#";//竖卷积核
    private static final String Horizontal_Number = "[-1,-2,-1]#[0,0,0]#[1,2,1]#";//横卷积核
    private static final String All_Number = "[1,-2,1]#[-2,4,-2]#[1,-2,1]#";//角卷积
    private static final String All_Number2 = "[-1,0,-1]#[0,4,0]#[-1,0,-1]#";
    public static final int rainNub = 10;//分水岭初始降雨点的数量
    public static final double th = 0.7;//分水岭灰度阈值
    public static final double rgbN = 441.6729559300637;//RGB范数归一化最大值
    public static Matrix Vertical;
    public static Matrix Horizontal;
    public static Matrix All;
    public static Matrix ALL_Two;
    public static final int Unit = 100;
    public static final double Pi = ArithUtil.div(ArithUtil.div(Math.PI, 2), Unit);

    private Kernel() {
    }

    static {
        try {
            ALL_Two = new Matrix(3, 3, All_Number2);
            All = new Matrix(3, 3, All_Number);
            Vertical = new Matrix(3, 3, Vertical_Number);
            Horizontal = new Matrix(3, 3, Horizontal_Number);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
