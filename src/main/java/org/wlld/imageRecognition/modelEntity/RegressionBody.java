package org.wlld.imageRecognition.modelEntity;

import org.wlld.tools.ArithUtil;
import org.wlld.tools.Frequency;

import java.util.ArrayList;
import java.util.List;

public class RegressionBody {
    private double w;
    private double b;
    private double[] X;

    public List<List<Double>> mappingMatrix(int size) {
        int len = X.length - size;
        List<List<Double>> lists = new ArrayList<>();
        for (int i = 0; i < len; i += size) {
            List<Double> list = new ArrayList<>();
            for (int t = i; t < i + size; t++) {
                double nub = ArithUtil.add(ArithUtil.mul(w, X[t]), b);
                list.add(nub);
            }
            lists.add(list);
        }
        return lists;
    }

    public void lineRegression(double[] Y, double[] X, Frequency frequency) {//进行二元线性回归
        double avX = frequency.average(X);//平均值
        int len = Y.length;
        this.X = X;
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
