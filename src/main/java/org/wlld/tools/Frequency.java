package org.wlld.tools;

public abstract class Frequency {//统计频数

    public double average(double... m) {//计算平均值
        int len = m.length;
        double allNub = 0;
        for (int i = 0; i < len; i++) {
            allNub = allNub + m[i];
        }
        allNub = ArithUtil.div(allNub, len);
        return allNub;
    }

    public double getPointLength(double x, double y, double i, double j) {//获取两个二维坐标之间的欧式距离
        return Math.sqrt(ArithUtil.add(Math.pow(ArithUtil.sub(x, i), 2), Math.pow(ArithUtil.sub(y, j), 2)));
    }

    public double variance(double... m) {//计算方差
        double ave = average(m);//先计算出平均值
        double allNub = 0;
        for (int i = 0; i < m.length; i++) {
            allNub = allNub + Math.pow(m[i] - ave, 2);
        }
        double var = ArithUtil.div(allNub, m.length);
        return var;
    }

    public double sd(double... m) {//计算标准差
        double var = variance(m);
        return Math.sqrt(var);
    }

    public double dc(double... m) {//计算离散系数
        double ave = average(m);//先计算出平均值
        double allNub = 0;
        for (int i = 0; i < m.length; i++) {
            allNub = allNub + Math.pow(m[i] - ave, 2);
        }
        double dc = ArithUtil.div(Math.sqrt(ArithUtil.div(allNub, m.length)), ave);//离散系数
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
}
