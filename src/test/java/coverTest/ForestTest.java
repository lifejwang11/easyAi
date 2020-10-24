package coverTest;

import org.wlld.MatrixTools.Matrix;
import org.wlld.config.Classifier;
import org.wlld.config.StudyPattern;
import org.wlld.imageRecognition.*;
import org.wlld.imageRecognition.segmentation.RegionBody;
import org.wlld.imageRecognition.segmentation.Specifications;
import org.wlld.imageRecognition.segmentation.Watershed;
import org.wlld.nerveEntity.ModelParameter;
import org.wlld.param.Cutting;
import org.wlld.param.Food;
import org.wlld.regressionForest.RegressionForest;

import java.util.*;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class ForestTest {
    private static Convolution convolution = new Convolution();

    public static void main(String[] args) throws Exception {
        testPic();
        //int a = (int) (Math.log(4) / Math.log(2));//id22是第几层
        //double a = Math.pow(2, 5) - 1; 第五层的第一个数
        // System.out.println("a==" + a);

    }

    public static void testPic() throws Exception {
        Picture picture = new Picture();
        TempleConfig templeConfig = getTemple(null);
        Operation operation = new Operation(templeConfig);
        List<Specifications> specificationsList = new ArrayList<>();
        Specifications specifications = new Specifications();
        specifications.setMinWidth(100);
        specifications.setMinHeight(100);
        specifications.setMaxWidth(600);
        specifications.setMaxHeight(600);
        specificationsList.add(specifications);
        ThreeChannelMatrix threeChannelMatrix = picture.getThreeMatrix("/Users/lidapeng/Desktop/train/a2.jpg");
        Watershed watershed = new Watershed(threeChannelMatrix, specificationsList, templeConfig);
        List<RegionBody> regionBodies = watershed.rainfall();
        if (regionBodies.size() == 1) {
            RegionBody regionBody = regionBodies.get(0);
            int minX = regionBody.getMinX();
            int minY = regionBody.getMinY();
            int maxX = regionBody.getMaxX();
            int maxY = regionBody.getMaxY();
            int xSize = maxX - minX;
            int ySize = maxY - minY;
            ThreeChannelMatrix threeChannelMatrix1 = convolution.getRegionMatrix(threeChannelMatrix, minX, minY, xSize, ySize);

        } else {
            System.out.println("size===" + regionBodies.size());
        }

    }

    public static void picDo(ThreeChannelMatrix threeChannelMatrix) {//进行lbp特征提取


    }

    public static TempleConfig getTemple(ModelParameter modelParameter) throws Exception {
        TempleConfig templeConfig = new TempleConfig();
        //templeConfig.isShowLog(true);//是否打印日志
        Cutting cutting = templeConfig.getCutting();
        Food food = templeConfig.getFood();
        //
        cutting.setMaxRain(360);//切割阈值
        cutting.setTh(0.8);
        cutting.setRegionNub(100);
        cutting.setMaxIou(0.2);
        //knn参数
        templeConfig.setKnnNub(1);
        //池化比例
        templeConfig.setPoolSize(2);//缩小比例
        //聚类
        templeConfig.setFeatureNub(3);//聚类特征数量
        //菜品识别实体类
        food.setShrink(5);//缩紧像素
        food.setRowMark(0.15);//0.12
        food.setColumnMark(0.15);//0.25
        food.setRegressionNub(20000);
        food.setTrayTh(0.08);
        templeConfig.setClassifier(Classifier.KNN);
        templeConfig.init(StudyPattern.Cover_Pattern, true, 400, 400, 3);
        if (modelParameter != null) {
            templeConfig.insertModel(modelParameter);
        }
        return templeConfig;
    }

    public static void test() throws Exception {//对分段回归进行测试
        int size = 2000;
        RegressionForest regressionForest = new RegressionForest(size, 3, 0.01, 200);
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
            Matrix test = new Matrix(1, 3);
            test.setNub(0, 0, feature[0]);
            test.setNub(0, 1, feature[1]);
            test.setNub(0, 2, feature[2]);
            double dist = regressionForest.getDist(test, feature[2]);
            sigma = sigma + dist;
        }
        double avs = sigma / size;
        System.out.println("a误差：" + avs);
        sigma = 0;
        for (int i = 0; i < 1000; i++) {
            double[] feature = b1.get(i);
            Matrix test = new Matrix(1, 3);
            test.setNub(0, 0, feature[0]);
            test.setNub(0, 1, feature[1]);
            test.setNub(0, 2, feature[2]);
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
