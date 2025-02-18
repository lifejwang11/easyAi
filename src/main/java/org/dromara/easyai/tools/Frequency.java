package org.dromara.easyai.tools;

import java.util.List;

public abstract class Frequency {//统计频数

    public float average(float... m) {//计算平均值
        int len = m.length;
        float allNub = 0;
        for (int i = 0; i < len; i++) {
            allNub = allNub + m[i];
        }
        allNub = allNub / len;
        return allNub;
    }

    public static float getEDist(float[] x1, float[] x2) {//返回两个等长数组之间的欧式距离
        float[] y = new float[x1.length];
        for (int i = 0; i < y.length; i++) {
            y[i] = x1[i] - x2[i];
        }
        float sigma = 0;
        for (int i = 0; i < y.length; i++) {
            sigma = sigma + (float)Math.pow(y[i], 2);
        }
        return (float)Math.sqrt(sigma);
    }

    public float averageByList(List<Float> m) {//计算平均值
        int len = m.size();
        float allNub = 0;
        for (int i = 0; i < len; i++) {
            allNub = allNub + m.get(i);
        }
        allNub = ArithUtil.div(allNub, len);
        return allNub;
    }

    public float sigma(float... m) {//求和
        int len = m.length;
        float allNub = 0;
        for (int i = 0; i < len; i++) {
            allNub = ArithUtil.add(allNub, m[i]);
        }
        return allNub;
    }

    public float getPointLength(float x, float y, float i, float j) {//获取两个二维坐标之间的欧式距离
        return (float)Math.sqrt(ArithUtil.add((float)Math.pow(ArithUtil.sub(x, i), 2), (float)Math.pow(ArithUtil.sub(y, j), 2)));
    }

    public float variance(float... m) {//计算方差
        float ave = average(m);//先计算出平均值
        float allNub = 0;
        for (int i = 0; i < m.length; i++) {
            allNub = allNub + (float)Math.pow(m[i] - ave, 2);
        }
        return allNub / m.length;
    }

    public float varianceByAve(float[] m, float ave) {// 计算方差，依赖平均值
        float allNub = 0;
        for (int i = 0; i < m.length; i++) {
            allNub = allNub + (float)Math.pow(m[i] - ave, 2);
        }
        return allNub / m.length;
    }

    public float sdByAvg(float[] m, float avg) {//计算标准差，带平均值
        float var = varianceByAve(m, avg);
        return (float)Math.sqrt(var);
    }

    public float sd(float... m) {//计算标准差
        float var = variance(m);
        return (float)Math.sqrt(var);
    }

    public float dcByAvg(float[] m, float ave) {//带均值算离散
        float allNub = 0;
        for (int i = 0; i < m.length; i++) {
            allNub = allNub + (float)Math.pow(m[i] - ave, 2);
        }
        return ArithUtil.div((float)Math.sqrt(ArithUtil.div(allNub, m.length)), ave);//离散系数
    }

    public float dc(float... m) {//计算离散系数
        float ave = average(m);//先计算出平均值
        float dc = 0;
        if (ave > 0) {
            float allNub = 0;
            for (int i = 0; i < m.length; i++) {
                allNub = allNub + (float)Math.pow(m[i] - ave, 2);
            }
            dc = ArithUtil.div((float)Math.sqrt(ArithUtil.div(allNub, m.length)), ave);//离散系数
        }
        return dc;
    }

    public float softMax(int t, float... m) {//下标和数组
        float my = (float)Math.exp(m[t]);
        float all = 0.0f;
        int allLength = m.length;
        for (int i = 0; i < allLength; i++) {
            all = all + (float)Math.exp(m[i]);
        }
        return ArithUtil.div(my, all);
    }

    public float[] getLimit(float[] m) {//获取数组中的最大值和最小值，最小值在前，最大值在后
        float[] limit = new float[2];
        float max = 0;
        float min = -1;
        int l = m.length;
        for (int i = 0; i < l; i++) {
            float nub = m[i];
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
