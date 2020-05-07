package org.wlld.imageRecognition.modelEntity;

import org.wlld.tools.ArithUtil;
import org.wlld.tools.Frequency;

import java.util.ArrayList;
import java.util.List;

public class RegressionBody {
    private double w;
    private double b;
    private double maxDis;//最大距离

    public void getMaxDis(double[] Y, double[] X) {//获取当前的最大距离
        double allNub = 0;
        for (int i = 0; i < X.length; i++) {
            double y = ArithUtil.add(ArithUtil.mul(X[i], w), b);
            double dis = Math.abs(ArithUtil.sub(Y[i], y));
            allNub = ArithUtil.add(allNub, dis);
            System.out.println("dis======" + dis);
            if (dis > maxDis) {
                maxDis = dis;
            }
        }
        allNub = ArithUtil.div(allNub, X.length);//当前最大值：0.1955576793420405,allNub==0.035958733
        System.out.println("当前最大值：" + maxDis + ",allNub==" + allNub);
    }

    public void lineRegression(double[] Y, double[] X, Frequency frequency) {//进行二元线性回归
        double avX = frequency.average(X);//平均值
        int len = Y.length;
        double wFenZi = 0;
        double wFenMu;
        double xSigma = 0;//X求和
        double xPower = 0;//X平方和
        double bSigma = 0;
        for (int i = 0; i < len; i++) {
            double y = Y[i];
            double x = X[i];
            xSigma = ArithUtil.add(xSigma, x);
            xPower = ArithUtil.add(Math.pow(x, 2), xPower);
            wFenZi = ArithUtil.add(ArithUtil.mul(ArithUtil.sub(x, avX), y), wFenZi);
        }
        wFenMu = ArithUtil.sub(xPower, ArithUtil.div(Math.pow(xSigma, 2), len));
        w = ArithUtil.div(wFenZi, wFenMu);
        for (int i = 0; i < len; i++) {
            double y = Y[i];
            double x = X[i];
            bSigma = ArithUtil.add(ArithUtil.sub(y, ArithUtil.mul(w, x)), bSigma);
        }
        b = ArithUtil.div(bSigma, len);
    }
}
