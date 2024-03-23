package org.wlld.tools;

import java.util.List;

public abstract class Frequency {//统计频数

    public double average(double... m) {//计算平均值
        int len = m.length;
        double allNub = 0;
        for (double v : m) {
            allNub = allNub + v;
        }
        allNub = allNub / len;
        return allNub;
    }

    public static double getEDist(double[] x1, double[] x2) {//返回两个等长数组之间的欧式距离
        double[] y = new double[x1.length];
        for (int i = 0; i < y.length; i++) {
            y[i] = x1[i] - x2[i];
        }
        double sigma = 0;
        for (int i = 0; i < y.length; i++) {
            sigma = sigma + Math.pow(y[i], 2);
        }
        return Math.sqrt(sigma);
    }

    public double sigma(double... m) {//求和
        double allNub = 0;
        for (double v : m) {
            allNub = allNub + v;
        }
        return allNub;
    }

    public double getPointLength(double x, double y, double i, double j) {//获取两个二维坐标之间的欧式距离
        return Math.sqrt(ArithUtil.add(Math.pow(ArithUtil.sub(x, i), 2), Math.pow(ArithUtil.sub(y, j), 2)));
    }

    public double variance(double... m) {//计算方差
        double ave = average(m);//先计算出平均值
        double allNub = 0;
        for (double v : m) {
            allNub = allNub + Math.pow(v - ave, 2);
        }
        return allNub / m.length;
    }

    public double varianceByAve(double[] m, double ave) {// 计算方差，依赖平均值
        double allNub = 0;
        for (double v : m) {
            allNub = allNub + Math.pow(v - ave, 2);
        }
        return allNub / m.length;
    }

    public double sd(double... m) {//计算标准差
        double var = variance(m);
        return Math.sqrt(var);
    }

    public double dcByAvg(double[] m, double ave) {//带均值算离散
        double allNub = 0;
        for (double v : m) {
            allNub = allNub + Math.pow(v - ave, 2);
        }
        return ArithUtil.div(Math.sqrt(ArithUtil.div(allNub, m.length)), ave);//离散系数
    }

    public double dc(double... m) {//计算离散系数
        double ave = average(m);//先计算出平均值
        double dc = 0;
        if (ave > 0) {
            double allNub = 0;
            for (double v : m) {
                allNub = allNub + Math.pow(v - ave, 2);
            }
            dc = ArithUtil.div(Math.sqrt(ArithUtil.div(allNub, m.length)), ave);//离散系数
        }
        return dc;
    }

    public double softMax(int t, double... m) {//下标和数组
        double my = Math.exp(m[t]);
        double all = 0.0;
        int allLength = m.length;
        for (int i = 0; i < allLength; i++) {
            all = all + Math.exp(m[i]);
        }
        return ArithUtil.div(my, all);
    }

    public double[] getLimit(double[] m) {//获取数组中的最大值和最小值，最小值在前，最大值在后
        double[] limit = new double[2];
        double max = 0;
        double min = -1;
        int l = m.length;
        for (int i = 0; i < l; i++) {
            double nub = m[i];
            if (min == -1 || nub < min) {
                min = nub;
            }
            if (nub > max) {
                max = nub;
            }
        }
        limit[0] = min;
        limit[1] = max;
        return limit;
    }
}
