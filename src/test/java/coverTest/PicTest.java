package coverTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.wlld.Ma;
import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.ModelData;
import org.wlld.config.Classifier;
import org.wlld.config.RZ;
import org.wlld.config.StudyPattern;
import org.wlld.function.Sigmod;
import org.wlld.function.Tanh;
import org.wlld.i.OutBack;
import org.wlld.imageRecognition.Operation;
import org.wlld.imageRecognition.Picture;
import org.wlld.imageRecognition.TempleConfig;
import org.wlld.nerveCenter.NerveManager;
import org.wlld.nerveEntity.ModelParameter;
import org.wlld.nerveEntity.SensoryNerve;
import org.wlld.tools.ArithUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PicTest {
    public static void main(String[] args) throws Exception {
        //Picture picture = new Picture();
        //Matrix right = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/a1.jpg");
        //Matrix wrong = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/b1.jpg");
        //Matrix a = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/c1.jpg");
        //Matrix b = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/d1.jpg");
        //segImage(right, wrong, a, b);

        //testImage(right, wrong, a, b);
        //test();

    }

    public static void test() throws Exception {//对图像进行识别测试
        Picture picture = new Picture();
        TempleConfig templeConfig = new TempleConfig();
        templeConfig.setClassifier(Classifier.DNN);
        templeConfig.isShowLog(true);
        templeConfig.setMatrixWidth(10);
        templeConfig.setSoftMax(true);
        templeConfig.setStudyPoint(0.003);
        templeConfig.setRzType(RZ.L2);
        templeConfig.setlParam(0.01);//0.015
        templeConfig.init(StudyPattern.Accuracy_Pattern, true, 20, 20, 2);
        Operation operation = new Operation(templeConfig);
        for (int i = 1; i < 90; i++) {
            Matrix a = picture.getImageMatrixByLocal("D:\\share\\cai/a" + i + ".jpg");
            Matrix b = picture.getImageMatrixByLocal("D:\\share\\cai/b" + i + ".jpg");
            // Matrix c = picture.getImageMatrixByLocal("D:\\share\\cai/c" + i + ".jpg");
            // Matrix d = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/d" + i + ".jpg");
            List<Matrix> rightLists = getFeatures2(a, 20);
            List<Matrix> wrongLists = getFeatures2(b, 20);
            //List<Matrix> listList1 = getFeatures2(c, 20);
            //List<Matrix> listList2 = getFeatures2(d, 18);
            int nu = rightLists.size();
            for (int j = 0; j < nu; j++) {
                System.out.println("study1==========" + i);
                Matrix aj = rightLists.get(j);
                Matrix bj = wrongLists.get(j);
                // Matrix cj = listList1.get(j);
                // Matrix dj = listList2.get(j);
                operation.learning(aj, 1, false);
                operation.learning(bj, 2, false);
                // operation.learning(cj, 3, false);
                //operation.learning(dj, 4, false);
            }
        }
        for (int i = 1; i < 90; i++) {
            Matrix a = picture.getImageMatrixByLocal("D:\\share\\cai/a" + i + ".jpg");
            Matrix b = picture.getImageMatrixByLocal("D:\\share\\cai/b" + i + ".jpg");
            //Matrix c = picture.getImageMatrixByLocal("D:\\share\\cai/c" + i + ".jpg");
            // Matrix d = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/d" + i + ".jpg");
            List<Matrix> rightLists = getFeatures2(a, 20);
            List<Matrix> wrongLists = getFeatures2(b, 20);
            //List<Matrix> listList1 = getFeatures2(c, 20);
            //List<Matrix> listList2 = getFeatures2(d, 18);
            int nu = rightLists.size();
            for (int j = 0; j < nu; j++) {
                System.out.println("study1==========" + i);
                Matrix aj = rightLists.get(j);
                Matrix bj = wrongLists.get(j);
                // Matrix cj = listList1.get(j);
                //Matrix dj = listList2.get(j);
                operation.normalization(aj, templeConfig.getConvolutionNerveManager());
                operation.normalization(bj, templeConfig.getConvolutionNerveManager());
                //operation.normalization(cj, templeConfig.getConvolutionNerveManager());
                // operation.normalization(dj, templeConfig.getConvolutionNerveManager());
            }
        }
        templeConfig.getNormalization().avg();
        for (int i = 1; i < 90; i++) {
            Matrix a = picture.getImageMatrixByLocal("D:\\share\\cai/a" + i + ".jpg");
            Matrix b = picture.getImageMatrixByLocal("D:\\share\\cai/b" + i + ".jpg");
            //Matrix c = picture.getImageMatrixByLocal("D:\\share\\cai/c" + i + ".jpg");
            //Matrix d = picture.getImageMatrixByLocal("/Users/lidapeng/Desktop/myDocment/picture/d" + i + ".jpg");
            List<Matrix> rightLists = getFeatures2(a, 20);
            List<Matrix> wrongLists = getFeatures2(b, 20);
            //List<Matrix> listList1 = getFeatures2(c, 20);
            //List<Matrix> listList2 = getFeatures2(d, 18);
            int nu = rightLists.size();
            for (int j = 0; j < nu; j++) {
                System.out.println("study2==========" + i);
                Matrix aj = rightLists.get(j);
                Matrix bj = wrongLists.get(j);
                //Matrix cj = listList1.get(j);
                // Matrix dj = listList2.get(j);
                operation.learning(aj, 1, true);
                operation.learning(bj, 2, true);
                //operation.learning(cj, 3, true);
                //operation.learning(dj, 4, true);
            }
        }
    }

    public static void testImage(Matrix rightMatrix, Matrix wrongMatrix, Matrix matrix1, Matrix matrix2) throws Exception {
        rightMatrix = late(rightMatrix, 2);
        wrongMatrix = late(wrongMatrix, 2);
        matrix1 = late(matrix1, 2);
        matrix2 = late(matrix2, 2);
        List<Matrix> rightLists = getFeatures2(rightMatrix, 18);
        List<Matrix> wrongLists = getFeatures2(wrongMatrix, 18);
        List<Matrix> listList1 = getFeatures2(matrix1, 18);
        List<Matrix> listList2 = getFeatures2(matrix2, 18);
        int x = matrix1.getX();
        int y = matrix1.getY();
        int nu = listList1.size();
        TempleConfig templeConfig = new TempleConfig();
        templeConfig.setClassifier(Classifier.DNN);
        templeConfig.setMatrixWidth(1);
        templeConfig.isShowLog(true);
        templeConfig.setMatrixWidth(1);
        templeConfig.setRzType(RZ.L2);
        templeConfig.setlParam(0.015);//0.015
        templeConfig.init(StudyPattern.Accuracy_Pattern, true, 18, 18, 4);
        Operation operation = new Operation(templeConfig);
        System.out.println("x==" + x + ",y==" + y + ",nu==" + nu);
        for (int j = 0; j < 1; j++) {
            for (int i = 0; i < nu; i++) {
                System.out.println("study1==========" + i);
                Matrix a = rightLists.get(i);
                Matrix b = wrongLists.get(i);
                Matrix c = listList1.get(i);
                Matrix d = listList2.get(i);
                operation.learning(a, 1, false);
                operation.learning(b, 2, false);
                operation.learning(c, 3, false);
                operation.learning(d, 4, false);
            }
        }

        for (int i = 0; i < nu; i++) {
            System.out.println("avg==" + i);
            Matrix a = rightLists.get(i);
            Matrix b = wrongLists.get(i);
            Matrix c = listList1.get(i);
            Matrix d = listList2.get(i);
            operation.normalization(a, templeConfig.getConvolutionNerveManager());
            operation.normalization(b, templeConfig.getConvolutionNerveManager());
            operation.normalization(c, templeConfig.getConvolutionNerveManager());
            operation.normalization(d, templeConfig.getConvolutionNerveManager());
        }
        templeConfig.getNormalization().avg();
        for (int i = 1; i < nu; i++) {
            System.out.println("study2==================" + i);
            Matrix a = rightLists.get(i);
            Matrix b = wrongLists.get(i);
            Matrix c = listList1.get(i);
            Matrix d = listList2.get(i);
            operation.learning(a, 1, true);
            operation.learning(b, 2, true);
            operation.learning(c, 3, true);
            operation.learning(d, 4, true);
        }

    }

    public static void test1(Matrix matrix, Operation operation) throws Exception {
        matrix = late(matrix, 5);
        int max = matrix.getX() * matrix.getY();
        List<List<Double>> rightLists = getFeatures(matrix);
        int size = rightLists.size();

    }

    public static void segImage(Matrix rightMatrix, Matrix wrongMatrix, Matrix matrix1, Matrix matrix2) throws Exception {
        NerveManager nerveManager = new NerveManager(9, 9, 4, 2, new Tanh(),
                false, false, 0.1, RZ.L1, 0.5);
        nerveManager.init(true, false, true, false);
        rightMatrix = late(rightMatrix, 5);
        wrongMatrix = late(wrongMatrix, 5);
        matrix1 = late(matrix1, 5);
        matrix2 = late(matrix2, 5);
        Map<Integer, Double> right = new HashMap<>();
        Map<Integer, Double> wrong = new HashMap<>();
        Map<Integer, Double> lists1 = new HashMap<>();
        Map<Integer, Double> lists2 = new HashMap<>();
        right.put(1, 1.0);
        wrong.put(2, 1.0);
        lists1.put(3, 1.0);
        lists2.put(4, 1.0);
        Ma ma = new Ma();
        List<List<Double>> rightLists = getFeatures(rightMatrix);
        List<List<Double>> wrongLists = getFeatures(wrongMatrix);
        List<List<Double>> listList1 = getFeatures(matrix1);
        List<List<Double>> listList2 = getFeatures(matrix2);
        int size = rightLists.size();
        System.out.println("SIZE==" + size);

        double all = 0;
        int nub = 0;
        for (int i = 0; i < size; i++) {
            List<Double> rightList = rightLists.get(i);
            List<Double> wrongList = wrongLists.get(i);
            List<Double> list1 = listList1.get(i);
            List<Double> list2 = listList2.get(i);
            for (Double dm : rightList) {
                nub++;
                all = ArithUtil.add(all, dm);
            }
            for (Double dm : wrongList) {
                nub++;
                all = ArithUtil.add(all, dm);
            }
            for (Double dm : list1) {
                nub++;
                all = ArithUtil.add(all, dm);
            }
            for (Double dm : list2) {
                nub++;
                all = ArithUtil.add(all, dm);
            }
        }
        double avg = ArithUtil.div(all, nub);//要减掉
        for (int i = 0; i < size; i++) {
            List<Double> rightList = rightLists.get(i);
            List<Double> wrongList = wrongLists.get(i);
            List<Double> list1 = listList1.get(i);
            List<Double> list2 = listList2.get(i);
            for (int j = 0; j < rightList.size(); j++) {
                double dm1 = rightList.get(j);
                double dm2 = wrongList.get(j);
                double dm3 = list1.get(j);
                double dm4 = list2.get(j);
                rightList.set(j, ArithUtil.sub(dm1, avg));
                wrongList.set(j, ArithUtil.sub(dm2, avg));
                list1.set(j, ArithUtil.sub(dm3, avg));
                list2.set(j, ArithUtil.sub(dm4, avg));
            }
        }
        for (int j = 0; j < 1; j++) {
            for (int i = 0; i < size; i++) {
                System.out.println("i========================" + i);
                List<Double> rightList = rightLists.get(i);
                List<Double> wrongList = wrongLists.get(i);
                List<Double> list1 = listList1.get(i);
                List<Double> list2 = listList2.get(i);
                intoDnnNetwork(1, rightList, nerveManager.getSensoryNerves(), true, right, ma);
                System.out.println("xxxxxxx");
                intoDnnNetwork(1, wrongList, nerveManager.getSensoryNerves(), true, wrong, ma);
                System.out.println("xxxxxxx");
                intoDnnNetwork(1, list1, nerveManager.getSensoryNerves(), true, lists1, ma);
                System.out.println("xxxxxxx");
                intoDnnNetwork(1, list2, nerveManager.getSensoryNerves(), true, lists2, ma);
            }
        }

    }

    private static void intoDnnNetwork(long eventId, List<Double> featureList, List<SensoryNerve> sensoryNerveList
            , boolean isStudy, Map<Integer, Double> map, OutBack outBack) throws Exception {//进入DNN 神经网络
        for (int i = 0; i < sensoryNerveList.size(); i++) {
            sensoryNerveList.get(i).postMessage(eventId, featureList.get(i), isStudy, map
                    , outBack);
        }
    }

    private static List<Matrix> getFeatures2(Matrix matrix, int size) throws Exception {
        List<Matrix> lists = new ArrayList<>();
        int x = matrix.getX() - size;//求导后矩阵的行数
        int y = matrix.getY() - size;//求导后矩阵的列数
        double me = ArithUtil.div(1, 255);
        for (int i = 0; i < x; i += size) {//遍历行
            for (int j = 0; j < y; j += size) {//遍历每行的列
                Matrix myMatrix = matrix.getSonOfMatrix(i, j, size, size);
                MatrixOperation.mathMul(myMatrix, me);
                lists.add(myMatrix);
            }
        }
        return lists;
    }

    private static List<List<Double>> getFeatures(Matrix matrix) throws Exception {
        List<List<Double>> lists = new ArrayList<>();
        int x = matrix.getX() - 3;//求导后矩阵的行数
        int y = matrix.getY() - 3;//求导后矩阵的列数
        for (int i = 0; i < x; i += 3) {//遍历行
            for (int j = 0; j < y; j += 3) {//遍历每行的列
                Matrix myMatrix = matrix.getSonOfMatrix(i, j, 3, 3);
                lists.add(getListFeature(myMatrix));
            }
        }
        return lists;
    }

    private static List<Double> getListFeature(Matrix matrix) throws Exception {
        List<Double> list = new ArrayList<>();
        int x = matrix.getX();
        int y = matrix.getY();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                double nub = matrix.getNumber(i, j) / 255;
                list.add(nub);
            }
        }
        return list;
    }

    protected static Matrix late(Matrix matrix, int size) throws Exception {//迟化处理
        int xn = matrix.getX();
        int yn = matrix.getY();
        int x = xn / size;//求导后矩阵的行数
        int y = yn / size;//求导后矩阵的列数
        Matrix myMatrix = new Matrix(x, y);//迟化后的矩阵
        for (int i = 0; i < xn - size; i += size) {
            for (int j = 0; j < yn - size; j += size) {
                Matrix matrix1 = matrix.getSonOfMatrix(i, j, size, size);
                double maxNub = 0;
                for (int t = 0; t < matrix1.getX(); t++) {
                    for (int k = 0; k < matrix1.getY(); k++) {
                        double nub = matrix1.getNumber(t, k);
                        if (nub > maxNub) {
                            maxNub = nub;
                        }
                    }
                }
                //迟化的最大值是 MAXNUB
                myMatrix.setNub(i / size, j / size, maxNub);
            }
        }
        return myMatrix;
    }
}
