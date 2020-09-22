package coverTest;

import org.wlld.randomForest.Tree;
import org.wlld.regressionForest.RegressionForest;

import java.util.*;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class ForestTest {
    public static void main(String[] args) throws Exception {
        test();
        //int a = (int) (Math.log(4) / Math.log(2));//id22是第几层
        //double a = Math.pow(2, 5) - 1; 第五层的第一个数
        // System.out.println("a==" + a);

    }

    public static void test() throws Exception {//对分段回归进行测试
        int size = 2000;
        RegressionForest regressionForest = new RegressionForest(size, 3, 0.01);
        regressionForest.setCosSize(40);
        List<double[]> a = fun(0.1, 0.2, 0.3, size, 2, 1);
        List<double[]> b = fun(0.7, 0.3, 0.1, size, 2, 2);
        for (int i = 0; i < 1000; i++) {
            double[] featureA = a.get(i);
            double[] featureB = b.get(i);
            double[] testA = new double[]{featureA[0], featureA[1]};
            double[] testB = new double[]{featureB[0], featureB[1]};
            regressionForest.insertFeature(testA, featureA[2]);
            regressionForest.insertFeature(testB, featureB[2]);
        }
        regressionForest.startStudy();
        ///
        List<double[]> a1 = fun(0.1, 0.2, 0.3, size, 2, 1);
        List<double[]> b1 = fun(0.7, 0.3, 0.1, size, 2, 2);
        double sigma = 0;
        for (int i = 0; i < 1000; i++) {
            double[] feature = a1.get(i);
            double[] test = new double[]{feature[0], feature[1]};
            double dist = regressionForest.getDist(test, feature[2]);
            sigma = sigma + dist;
        }
        double avs = sigma / size;
        System.out.println("a误差：" + avs);
//        a误差：0.0017585065712555645
//        b误差：0.00761733737464547
        sigma = 0;
        for (int i = 0; i < 1000; i++) {
            double[] feature = b1.get(i);
            double[] test = new double[]{feature[0], feature[1]};
            double dist = regressionForest.getDist(test, feature[2]);
            sigma = sigma + dist;
        }
        double avs2 = sigma / size;
        System.out.println("b误差：" + avs2);

    }

    public static List<double[]> fun(double w1, double w2, double w3, int size, int region, int index) {//生成假数据
        List<double[]> list = new ArrayList<>();
        Random random = new Random();
        int nub = (index - 1) * 100;
        double max = region * 100;
        for (int i = 0; i < size; i++) {
            double b = (double) (random.nextInt(100) + nub) / max;
            double a = random.nextDouble();
            double c = w1 * a + w2 * b + w3;
            double[] data = new double[]{a, b, c};
            list.add(data);
        }
        return list;
    }
}
