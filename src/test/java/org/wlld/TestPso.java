package org.wlld;


import org.wlld.i.PsoFunction;
import org.wlld.tools.Frequency;

public class TestPso implements PsoFunction {
    private double times;

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
        double avg = sigma / size;
        double allNub = 0;
        for (int i = 0; i < size; i++) {
            allNub = allNub + Math.abs(parameter[i] - avg);
        }
        return Math.abs(sigma - times);
    }
}
