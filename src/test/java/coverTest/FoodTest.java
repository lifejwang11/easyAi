package coverTest;

import com.alibaba.fastjson.JSON;
import org.wlld.MatrixTools.Matrix;
import org.wlld.config.Classifier;
import org.wlld.config.RZ;
import org.wlld.config.StudyPattern;
import org.wlld.imageRecognition.*;
import org.wlld.imageRecognition.segmentation.Watershed;
import org.wlld.nerveEntity.ModelParameter;
import org.wlld.tools.ArithUtil;

public class FoodTest {

    public static void main(String[] args) throws Exception {
        //food();
        rain();
    }

    public static void rain() throws Exception {//降雨
        Picture picture = new Picture();
        Matrix matrix = picture.getImageMatrixByLocal("D:\\share\\cai/i1.jpg");
        Watershed watershed = new Watershed(matrix);
        watershed.rainfall();
    }

    public static void food() throws Exception {
        Picture picture = new Picture();
        TempleConfig templeConfig = new TempleConfig();
        templeConfig.setClassifier(Classifier.DNN);
        templeConfig.isShowLog(true);
        templeConfig.setSoftMax(true);
        templeConfig.setTh(0);
        //templeConfig.setMatrixWidth(5);
        templeConfig.setStudyPoint(0.02);
        templeConfig.setRzType(RZ.L1);
        templeConfig.setlParam(0.016);//0.015
        templeConfig.init(StudyPattern.Accuracy_Pattern, true, 640, 480, 2);
        Operation operation = new Operation(templeConfig);
        // 一阶段
        for (int j = 0; j < 2; j++) {
            for (int i = 1; i < 233; i++) {//一阶段
                System.out.println("study1===================" + i);
                //读取本地URL地址图片,并转化成矩阵
                Matrix a = picture.getImageMatrixByLocal("D:\\pic\\6\\a/a" + i + ".jpg");
                Matrix b = picture.getImageMatrixByLocal("D:\\pic\\6\\b/b" + i + ".jpg");
                operation.learning(a, 1, false);
                operation.learning(b, 2, false);
            }
        }
        // 二阶段
        for (int i = 1; i < 233; i++) {
            System.out.println("avg==" + i);
            Matrix a = picture.getImageMatrixByLocal("D:\\pic\\6\\a/a" + i + ".jpg");
            Matrix b = picture.getImageMatrixByLocal("D:\\pic\\6\\b/b" + i + ".jpg");
            operation.normalization(a, templeConfig.getConvolutionNerveManager());
            operation.normalization(b, templeConfig.getConvolutionNerveManager());
        }
        templeConfig.getNormalization().avg();
        for (int j = 0; j < 5; j++) {
            for (int i = 1; i < 233; i++) {
                System.out.println("j==" + j + ",study2==================" + i);
                Matrix a = picture.getImageMatrixByLocal("D:\\pic\\6\\a/a" + i + ".jpg");
                Matrix b = picture.getImageMatrixByLocal("D:\\pic\\6\\b/b" + i + ".jpg");
                operation.learning(a, 1, true);
                operation.learning(b, 2, true);
            }
        }
        templeConfig.finishStudy();//结束学习
        int wrong = 0;
        int allNub = 0;
        for (int i = 1; i <= 232; i++) {
            //读取本地URL地址图片,并转化成矩阵
            Matrix a = picture.getImageMatrixByLocal("D:\\pic\\6\\a/a" + i + ".jpg");
            allNub++;
            int an = operation.toSee(a);
            if (an != 1) {
                wrong++;
            }
        }
        double wrongPoint = ArithUtil.div(wrong, allNub);
        System.out.println("错误率：" + (wrongPoint * 100) + "%");
        ModelParameter modelParameter = templeConfig.getModel();
        String model = JSON.toJSONString(modelParameter);
        System.out.println(model);
    }

}
