package org.wlld;


import org.wlld.i.PsoFunction;

public class TestPso implements PsoFunction {
    private double times;//总长度
    private double myAvgSub;

    public void setMyAvgSub(double myAvgSub) {
        this.myAvgSub = myAvgSub;
    }

    public void setTimes(double times) {
        this.times = times;
    }

    @Override
    public double getResult(double[] parameter, int id) {//
        int size = parameter.length;
        double sigma = 0;
        for (int i = 0; i < size; i++) {
            sigma = sigma + parameter[i];
        }
        double avg = sigma / size;//平均值
        double allNub = 0;
        for (int i = 0; i < size; i++) {
            allNub = allNub + Math.abs(parameter[i] - avg);
        }
        double vagSub = allNub / size;
        return Math.abs(sigma - times) + Math.abs(vagSub - myAvgSub);
    }
}
