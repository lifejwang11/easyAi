package org.wlld.imageRecognition;


import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.config.Classifier;
import org.wlld.config.Kernel;
import org.wlld.config.StudyPattern;
import org.wlld.i.OutBack;
import org.wlld.imageRecognition.border.*;
import org.wlld.imageRecognition.segmentation.RegionBody;
import org.wlld.imageRecognition.segmentation.Specifications;
import org.wlld.imageRecognition.segmentation.Watershed;
import org.wlld.nerveCenter.NerveManager;
import org.wlld.nerveCenter.Normalization;
import org.wlld.nerveEntity.Nerve;
import org.wlld.nerveEntity.SensoryNerve;
import org.wlld.tools.ArithUtil;
import org.wlld.tools.IdCreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Operation {//进行计算
    private TempleConfig templeConfig;//配置初始化参数模板
    private Convolution convolution = new Convolution();
    private MatrixBack matrixBack = new MatrixBack();
    private ImageBack imageBack = new ImageBack();
    private OutBack outBack;

    public Operation(TempleConfig templeConfig) {
        this.templeConfig = templeConfig;
    }

    public Operation(TempleConfig templeConfig, OutBack outBack) {
        this.templeConfig = templeConfig;
        this.outBack = outBack;
    }

    public List<Double> convolution(Matrix matrix, Map<Integer, Double> tagging) throws Exception {
        //进行卷积
        int maxNub;
        if (templeConfig.getRow() >= templeConfig.getColumn()) {
            maxNub = templeConfig.getRow();
        } else {
            maxNub = templeConfig.getColumn();
        }
        int id = -1;
        if (tagging != null && templeConfig.isHavePosition()) {
            for (Map.Entry<Integer, Double> entry : tagging.entrySet()) {
                if (entry.getValue() == 1) {
                    id = entry.getKey();
                    break;
                }
            }
        }
        Matrix matrix1 = convolution.getFeatures(matrix, maxNub, templeConfig, id);
        return sub(matrix1);
    }

    public void colorStudy(ThreeChannelMatrix threeChannelMatrix, int tag, List<Specifications> specificationsList) throws Exception {
        Watershed watershed = new Watershed(threeChannelMatrix.getMatrixRGB(), specificationsList, templeConfig);
        RegionBody regionBody = watershed.rainfall().get(0);
        int minX = regionBody.getMinX();
        int minY = regionBody.getMinY();
        int maxX = regionBody.getMaxX();
        int maxY = regionBody.getMaxY();
        int xSize = maxX - minX;
        int ySize = maxY - minY;
        ThreeChannelMatrix threeChannelMatrix1 = convolution.getRegionMatrix(threeChannelMatrix, minX, minY, xSize, ySize);
        List<Double> feature = convolution.getCenterColor(threeChannelMatrix1, templeConfig.getPoolSize(),
                templeConfig.getSensoryNerves().size());
        if (templeConfig.isShowLog()) {
            System.out.println(feature);
        }
        int classifier = templeConfig.getClassifier();
        switch (classifier) {
            case Classifier.DNN:
                Map<Integer, Double> map = new HashMap<>();
                map.put(tag, 1.0);
                intoDnnNetwork(1, feature, templeConfig.getSensoryNerves(), true, map, null);
                break;
            case Classifier.LVQ:
                Matrix vector = MatrixOperation.listToRowVector(feature);
                lvqStudy(tag, vector);
                break;
            case Classifier.VAvg:
                Matrix vec = MatrixOperation.listToRowVector(feature);
                avgStudy(tag, vec);
                break;
        }
    }

    private void avgStudy(int tagging, Matrix myMatrix) throws Exception {//特征矩阵均值学习
        VectorK vectorK = templeConfig.getVectorK();
        vectorK.insertMatrix(tagging, myMatrix);
    }

    private void lvqStudy(int tagging, Matrix myMatrix) throws Exception {//LVQ学习
        LVQ lvq = templeConfig.getLvq();
        MatrixBody matrixBody = new MatrixBody();
        matrixBody.setMatrix(myMatrix);
        matrixBody.setId(tagging);
        lvq.insertMatrixBody(matrixBody);
    }

    public List<RegionBody> colorLook(ThreeChannelMatrix threeChannelMatrix, List<Specifications> specificationsList) throws Exception {
        Watershed watershed = new Watershed(threeChannelMatrix.getMatrixRGB(), specificationsList, templeConfig);
        List<RegionBody> regionList = watershed.rainfall();
        for (RegionBody regionBody : regionList) {
            MaxPoint maxPoint = new MaxPoint();
            int minX = regionBody.getMinX();
            int minY = regionBody.getMinY();
            int maxX = regionBody.getMaxX();
            int maxY = regionBody.getMaxY();
            int xSize = maxX - minX;
            int ySize = maxY - minY;
            ThreeChannelMatrix threeChannelMatrix1 = convolution.getRegionMatrix(threeChannelMatrix, minX, minY, xSize, ySize);
            List<Double> feature = convolution.getCenterColor(threeChannelMatrix1, templeConfig.getPoolSize(),
                    templeConfig.getSensoryNerves().size());
            if (templeConfig.isShowLog()) {
                System.out.println(feature);
            }
            int classifier = templeConfig.getClassifier();
            int id = 0;
            switch (classifier) {
                case Classifier.LVQ:
                    Matrix myMatrix = MatrixOperation.listToRowVector(feature);
                    id = getIdByLVQ(myMatrix);
                    break;
                case Classifier.DNN:
                    intoDnnNetwork(IdCreator.get().nextId(), feature, templeConfig.getSensoryNerves(), false, null, maxPoint);
                    id = maxPoint.getId();
                    break;
                case Classifier.VAvg:
                    Matrix myMatrix1 = MatrixOperation.listToRowVector(feature);
                    id = getIdByVag(myMatrix1);
                    break;
            }
            regionBody.setType(id);
        }
        return regionList;
    }

    private int getIdByVag(Matrix myVector) throws Exception {//VAG获取分类
        Map<Integer, Matrix> matrixK = templeConfig.getVectorK().getMatrixK();
        double minDist = 0;
        int id = 0;
        for (Map.Entry<Integer, Matrix> entry : matrixK.entrySet()) {
            Matrix matrix = entry.getValue();
            double dist = MatrixOperation.getEDist(matrix, myVector);
            //System.out.println("距离===" + dist + ",类别==" + entry.getKey());
            if (minDist == 0 || dist < minDist) {
                minDist = dist;
                id = entry.getKey();
            }
        }
        return id;
    }

    private int getIdByLVQ(Matrix myVector) throws Exception {//LVQ获取分类
        int id = 0;
        double distEnd = 0;
        LVQ lvq = templeConfig.getLvq();
        MatrixBody[] matrixBodies = lvq.getModel();
        for (int i = 0; i < matrixBodies.length; i++) {
            MatrixBody matrixBody = matrixBodies[i];
            Matrix vector = matrixBody.getMatrix();
            double dist = lvq.vectorEqual(myVector, vector);
            if (distEnd == 0 || dist < distEnd) {
                id = matrixBody.getId();
                distEnd = dist;
            }
        }
        return id;
    }

    public void coverStudy(Map<Integer, ThreeChannelMatrix> matrixMap, int poolSize, int sqNub, int regionSize,
                           int times) throws Exception {
        if (templeConfig.getStudyPattern() == StudyPattern.Cover_Pattern) {
            int size = 0;
            List<CoverBody> coverBodies = new ArrayList<>();
            for (Map.Entry<Integer, ThreeChannelMatrix> entry : matrixMap.entrySet()) {
                CoverBody coverBody = new CoverBody();
                Map<Integer, Double> tag = new HashMap<>();
                tag.put(entry.getKey(), 1.0);
                List<List<Double>> lists = convolution.kAvg(entry.getValue(), poolSize, sqNub, regionSize);
                size = lists.size();
                coverBody.setFeature(lists);
                coverBody.setTag(tag);
                coverBodies.add(coverBody);
            }
            //特征塞入容器完毕
            for (int j = 0; j < times; j++) {
                for (int i = 0; i < size; i++) {
                    for (CoverBody coverBody : coverBodies) {
                        List<Double> list = coverBody.getFeature().get(i);
                        if (templeConfig.isShowLog()) {
                            System.out.println("feature:" + list);
                        }
                        intoDnnNetwork(1, list, templeConfig.getSensoryNerves(), true, coverBody.getTag(), null);
                    }
                }
            }
        }
    }

    public Map<Integer, Double> coverPoint(ThreeChannelMatrix matrix, int poolSize, int sqNub, int regionSize) throws Exception {
        if (templeConfig.getStudyPattern() == StudyPattern.Cover_Pattern) {
            Map<Integer, Double> coverMap = new HashMap<>();
            Map<Integer, Integer> typeNub = new HashMap<>();
            List<List<Double>> lists = convolution.kAvg(matrix, poolSize, sqNub, regionSize);
            //特征塞入容器完毕
            int size = lists.size();
            int all = 0;
            for (int i = 0; i < size; i++) {
                List<Double> list = lists.get(i);
                MaxPoint maxPoint = new MaxPoint();
                long pid = IdCreator.get().nextId();
                intoDnnNetwork(pid, list, templeConfig.getSensoryNerves(), false, null, maxPoint);
                int id = maxPoint.getId();
                if (typeNub.containsKey(id)) {
                    typeNub.put(id, typeNub.get(id) + 1);
                } else {
                    typeNub.put(id, 1);
                }
                all++;
            }
            for (Map.Entry<Integer, Integer> entry : typeNub.entrySet()) {
                int nub = entry.getValue();
                coverMap.put(entry.getKey(), ArithUtil.div(nub, all));
            }
            return coverMap;
        } else {
            throw new Exception("PATTERN IS NOT COVER");
        }
    }

    //模板学习
    public void study(Matrix matrix, Map<Integer, Double> tagging) throws Exception {
        if (templeConfig.getStudyPattern() == StudyPattern.Speed_Pattern) {
            List<Double> list = convolution(matrix, tagging);
            intoDnnNetwork(1, list, templeConfig.getSensoryNerves(), true, tagging, null);
        } else {
            throw new Exception("pattern is wrong");
        }
    }

    public int toThreeSee(ThreeChannelMatrix threeChannelMatrix) throws Exception {//三通道
        NerveManager convolutionNerveManagerR = templeConfig.getConvolutionNerveManagerR();
        NerveManager convolutionNerveManagerB = templeConfig.getConvolutionNerveManagerB();
        NerveManager convolutionNerveManagerG = templeConfig.getConvolutionNerveManagerG();
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        Matrix matrixG = threeChannelMatrix.getMatrixG();
        Matrix matrixB = threeChannelMatrix.getMatrixB();
        //进卷积网络
        MatrixBack matrixBackR = new MatrixBack();
        MatrixBack matrixBackG = new MatrixBack();
        MatrixBack matrixBackB = new MatrixBack();
        intoConvolutionNetwork(1, matrixR, convolutionNerveManagerR.getSensoryNerves(),
                false, 0, matrixBackR);
        intoConvolutionNetwork(1, matrixG, convolutionNerveManagerG.getSensoryNerves(),
                false, 0, matrixBackG);
        intoConvolutionNetwork(1, matrixB, convolutionNerveManagerB.getSensoryNerves(),
                false, 0, matrixBackB);
        Matrix myMatrixR = matrixBackR.getMatrix();
        Matrix myMatrixG = matrixBackG.getMatrix();
        Matrix myMatrixB = matrixBackB.getMatrix();
        List<Double> featureALL = new ArrayList<>();
        List<Double> featureR = getFeature(myMatrixR);
        List<Double> featureG = getFeature(myMatrixG);
        List<Double> featureB = getFeature(myMatrixB);
        featureALL.addAll(featureR);
        featureALL.addAll(featureG);
        featureALL.addAll(featureB);
        MaxPoint maxPoint = new MaxPoint();
        long id = IdCreator.get().nextId();
        intoDnnNetwork(id, featureALL, templeConfig.getSensoryNerves(), false, null, maxPoint);
        return maxPoint.getId();
    }

    public void threeLearning(ThreeChannelMatrix threeChannelMatrix, int tagging, boolean isNerveStudy) throws Exception {
        boolean isKernelStudy = true;
        if (isNerveStudy) {
            isKernelStudy = false;
        }
        NerveManager convolutionNerveManagerR = templeConfig.getConvolutionNerveManagerR();
        NerveManager convolutionNerveManagerB = templeConfig.getConvolutionNerveManagerB();
        NerveManager convolutionNerveManagerG = templeConfig.getConvolutionNerveManagerG();
        Matrix matrixR = threeChannelMatrix.getMatrixR();
        Matrix matrixG = threeChannelMatrix.getMatrixG();
        Matrix matrixB = threeChannelMatrix.getMatrixB();
        //进卷积网络
        MatrixBack matrixBackR = new MatrixBack();
        MatrixBack matrixBackG = new MatrixBack();
        MatrixBack matrixBackB = new MatrixBack();
        intoConvolutionNetwork(1, matrixR, convolutionNerveManagerR.getSensoryNerves(),
                isKernelStudy, tagging, matrixBackR);
        intoConvolutionNetwork(1, matrixG, convolutionNerveManagerG.getSensoryNerves(),
                isKernelStudy, tagging, matrixBackG);
        intoConvolutionNetwork(1, matrixB, convolutionNerveManagerB.getSensoryNerves(),
                isKernelStudy, tagging, matrixBackB);
        if (isNerveStudy) {
            Matrix myMatrixR = matrixBackR.getMatrix();
            Matrix myMatrixG = matrixBackG.getMatrix();
            Matrix myMatrixB = matrixBackB.getMatrix();
            List<Double> featureALL = new ArrayList<>();
            List<Double> featureR = getFeature(myMatrixR);
            List<Double> featureG = getFeature(myMatrixG);
            List<Double> featureB = getFeature(myMatrixB);
            featureALL.addAll(featureR);
            featureALL.addAll(featureG);
            featureALL.addAll(featureB);
            Map<Integer, Double> map = new HashMap<>();
            map.put(tagging, 1.0);
            intoDnnNetwork(1, featureALL, templeConfig.getSensoryNerves(), true, map, null);
        }

    }

    public void threeNormalization(ThreeChannelMatrix threeChannelMatrix) throws Exception {
        normalization(threeChannelMatrix.getMatrixR(), templeConfig.getConvolutionNerveManagerR());
        normalization(threeChannelMatrix.getMatrixG(), templeConfig.getConvolutionNerveManagerG());
        normalization(threeChannelMatrix.getMatrixB(), templeConfig.getConvolutionNerveManagerB());

    }

    public void normalization(Matrix matrix, NerveManager nerveManager) throws Exception {
        Normalization normalization = templeConfig.getNormalization();
        intoConvolutionNetwork(1, matrix, nerveManager.getSensoryNerves(),
                false, 0, matrixBack);
        Matrix myMatrix = matrixBack.getMatrix();
        for (int i = 0; i < myMatrix.getX(); i++) {
            for (int j = 0; j < myMatrix.getY(); j++) {
                normalization.putFeature(myMatrix.getNumber(i, j));
            }
        }
    }

    //卷积核学习
    public void learning(Matrix matrix, int tagging, boolean isNerveStudy) throws Exception {
        if (templeConfig.getStudyPattern() == StudyPattern.Accuracy_Pattern) {
            Border border = null;
            if (templeConfig.isHavePosition() && isNerveStudy && tagging > 0) {
                outBack = imageBack;
                border = convolution.borderOnce(matrix, templeConfig);
            }
            boolean isKernelStudy = true;
            if (isNerveStudy) {
                isKernelStudy = false;
            }
            //进卷积网络
            intoConvolutionNetwork(1, matrix, templeConfig.getConvolutionNerveManager().getSensoryNerves(),
                    isKernelStudy, tagging, matrixBack);
            if (isNerveStudy) {
                //卷积后的结果
                Matrix myMatrix = matrixBack.getMatrix();
                if (templeConfig.isHavePosition() && tagging > 0) {
                    border.end(myMatrix, tagging);
                }
                int classifier = templeConfig.getClassifier();
                switch (classifier) {
                    case Classifier.DNN:
                        dnn(tagging, myMatrix);
                        break;
                    case Classifier.LVQ:
                        lvq(tagging, myMatrix);
                        break;
                    case Classifier.VAvg:
                        vectorAvg(tagging, myMatrix);
                        break;
                }
            }
        } else {
            throw new Exception("pattern is wrong");
        }
    }

    private void dnn(int tagging, Matrix myMatrix) throws Exception {//DNN网络学习
        Map<Integer, Double> map = new HashMap<>();
        map.put(tagging, 1.0);
        List<Double> feature = getFeature(myMatrix);
        if (templeConfig.isShowLog()) {
            System.out.println(feature);
        }
        intoDnnNetwork(1, feature, templeConfig.getSensoryNerves(), true, map, null);
    }

    private void vectorAvg(int tagging, Matrix myMatrix) throws Exception {//特征矩阵均值学习
        Matrix vector = MatrixOperation.matrixToVector(myMatrix, true);
        VectorK vectorK = templeConfig.getVectorK();
        vectorK.insertMatrix(tagging, vector);
    }

    private void lvq(int tagging, Matrix myMatrix) throws Exception {//LVQ学习
        LVQ lvq = templeConfig.getLvq();
        Matrix vector = MatrixOperation.matrixToVector(myMatrix, true);
        if (templeConfig.isShowLog()) {
            System.out.println(vector.getString());
        }
        MatrixBody matrixBody = new MatrixBody();
        matrixBody.setMatrix(vector);
        matrixBody.setId(tagging);
        lvq.insertMatrixBody(matrixBody);
    }

    private List<Double> getFeatures(Matrix matrix) throws Exception {
        List<Double> list = new ArrayList<>();
        for (int i = 0; i < matrix.getX(); i++) {
            for (int j = 0; j < matrix.getY(); j++) {
                double nub = matrix.getNumber(i, j);
                list.add(nub);
            }
        }
        return list;
    }

    private List<Double> getFeature(Matrix matrix) throws Exception {//将特征矩阵转化为集合并除10
        List<Double> list = new ArrayList<>();
        Normalization normalization = templeConfig.getNormalization();
        double middle = normalization.getAvg();
        for (int i = 0; i < matrix.getX(); i++) {
            for (int j = 0; j < matrix.getY(); j++) {
                double nub = matrix.getNumber(i, j);
                if (nub != 0) {
                    nub = ArithUtil.sub(nub, middle);
                    list.add(nub);
                } else {
                    list.add(0.0);
                }
            }
        }
        return list;
    }

    private List<List<Double>> getFeatures(Matrix matrix, int size) throws Exception {
        List<List<Double>> lists = new ArrayList<>();
        int x = matrix.getX() - size;//求导后矩阵的行数
        int y = matrix.getY() - size;//求导后矩阵的列数
        for (int i = 0; i < x; i += size) {//遍历行
            for (int j = 0; j < y; j += size) {//遍历每行的列
                Matrix myMatrix = matrix.getSonOfMatrix(i, j, size, size);
                lists.add(getListFeature(myMatrix));
            }
        }
        return lists;
    }

    private List<Double> getListFeature(Matrix matrix) throws Exception {
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

    //图像视觉 speed 模式
    public void look(Matrix matrix, long eventId) throws Exception {
        if (templeConfig.getStudyPattern() == StudyPattern.Speed_Pattern) {
            List<Double> list = convolution(matrix, null);
            intoDnnNetwork(eventId, list, templeConfig.getSensoryNerves(), false, null, outBack);
        } else if (templeConfig.getStudyPattern() == StudyPattern.Accuracy_Pattern) {
            throw new Exception("studyPattern not right");
        }
    }

    public List<Integer> manyLook(Matrix matrix, long eventId) throws Exception {//无定位多物体识别
        if (templeConfig.getStudyPattern() == StudyPattern.Accuracy_Pattern) {
            List<Integer> list = new ArrayList<>();
            Frame frame = templeConfig.getFrame();
            List<FrameBody> frameBodies = convolution.getRegion(matrix, frame);
            MatrixBack matrixBack = new MatrixBack();
            for (FrameBody frameBody : frameBodies) {
                intoConvolutionNetwork(eventId, frameBody.getMatrix(), templeConfig.getConvolutionNerveManager().getSensoryNerves(),
                        false, -1, matrixBack);
                Matrix myMatrix = matrixBack.getMatrix();
                int classifier = templeConfig.getClassifier();
                int id = 0;
                switch (classifier) {
                    case Classifier.LVQ:
                        id = getClassificationIdByLVQ(myMatrix);
                        break;
                    case Classifier.DNN:
                        id = getClassificationIdByDnn(myMatrix);
                        break;
                    case Classifier.VAvg:
                        id = getClassificationIdByVag(myMatrix);
                        break;
                }
                list.add(id);
            }
            return list;
        } else {
            throw new Exception("wrong model");
        }
    }

    //边框检测+识别分类
    public Map<Integer, List<FrameBody>> lookWithPosition(Matrix matrix, long eventId) throws Exception {
        Frame frame = templeConfig.getFrame();
        if (templeConfig.isHavePosition() && frame != null && frame.isReady()) {
            //区域分割
            List<FrameBody> frameBodies = convolution.getRegion(matrix, frame);
            if (templeConfig.getStudyPattern() == StudyPattern.Speed_Pattern) {
                int maxNub = 0;
                if (templeConfig.getRow() >= templeConfig.getColumn()) {
                    maxNub = templeConfig.getRow();
                } else {
                    maxNub = templeConfig.getColumn();
                }
                //坐标回调类
                for (FrameBody frameBody : frameBodies) {
                    //Speed 模式下的最后卷积结果
                    Matrix matrix1 = convolution.getFeatures(frameBody.getMatrix(), maxNub, templeConfig, -1);
                    //卷积层输出即边框回归的输入的特征向量
                    frameBody.setEndMatrix(matrix1);
                    List<Double> list = sub(matrix1);
                    imageBack.setFrameBody(frameBody);
                    //进入神经网络判断
                    intoDnnNetwork(eventId, list, templeConfig.getSensoryNerves(), false, null, imageBack);
                }
                return toPosition(frameBodies, frame.getWidth(), frame.getHeight());
            } else if (templeConfig.getStudyPattern() == StudyPattern.Accuracy_Pattern) {
                MatrixBack matrixBack = new MatrixBack();
                for (FrameBody frameBody : frameBodies) {
                    intoConvolutionNetwork(eventId, frameBody.getMatrix(), templeConfig.getConvolutionNerveManager().getSensoryNerves(),
                            false, -1, matrixBack);
                    Matrix myMatrix = matrixBack.getMatrix();
                    //卷积层输出即边框回归的输入的特征向量
                    frameBody.setEndMatrix(myMatrix);
                    int classifier = templeConfig.getClassifier();
                    int id = 0;
                    switch (classifier) {
                        case Classifier.LVQ:
                            id = getClassificationIdByLVQ(myMatrix);
                            break;
                        case Classifier.DNN:
                            id = getClassificationIdByDnn(myMatrix);
                            break;
                        case Classifier.VAvg:
                            id = getClassificationIdByVag(myMatrix);
                            break;
                    }
                    frameBody.setId(id);
                }
                return toPosition(frameBodies, frame.getWidth(), frame.getHeight());
            } else {
                throw new Exception("wrong model");
            }
        } else {
            throw new Exception("position not study or frame is not ready");
        }
    }

    private Map<Integer, List<FrameBody>> toPosition(List<FrameBody> frameBodies, int width, int height) throws Exception {//把分类都拿出来
        for (FrameBody frameBody : frameBodies) {
            if (frameBody.getPoint() > templeConfig.getTh()) {//存在一个识别分类
                getBox(frameBody, width, height);
            }
        }
        return result(frameBodies);
    }

    private Map<Integer, List<FrameBody>> result(List<FrameBody> frameBodies) {
        Map<Integer, List<FrameBody>> map = new HashMap<>();
        for (FrameBody frameBody : frameBodies) {
            if (frameBody.getPoint() > templeConfig.getTh()) {//存在一个识别分类
                int id = frameBody.getId();
                if (map.containsKey(id)) {
                    List<FrameBody> frameBodies1 = map.get(id);
                    boolean isHere = false;
                    for (FrameBody frameBody1 : frameBodies1) {
                        double iou = getIou(frameBody1, frameBody);
                        if (iou > templeConfig.getIouTh()) {
                            isHere = true;
                            break;
                        }
                    }
                    if (!isHere) {
                        frameBodies1.add(frameBody);
                    }
                } else {
                    List<FrameBody> frameBodyList = new ArrayList<>();
                    frameBodyList.add(frameBody);
                    map.put(id, frameBodyList);
                }
            }
        }
        return map;
    }

    private Matrix getBoxMatrix(Matrix matrix, Map<Integer, Box> boxMap) throws Exception {
        Matrix positionMatrix = null;
        double endDist = -1;
        for (Map.Entry<Integer, Box> entry : boxMap.entrySet()) {
            Box box = entry.getValue();
            Matrix boxMatrix = box.getMatrix();
            double dist = MatrixOperation.getEDist(matrix, boxMatrix);
            if (endDist == -1 || dist < endDist) {
                endDist = dist;
                positionMatrix = box.getMatrixPosition();
            }
        }
        return positionMatrix;
    }

    //获得预测边框
    private void getBox(FrameBody frameBody, int width, int height) throws Exception {
        if (templeConfig.isBoxReady()) {
            Matrix matrix = frameBody.getEndMatrix();
            int id = frameBody.getId();
            int x = frameBody.getX();
            int y = frameBody.getY();
            KClustering kClustering = templeConfig.getKClusteringMap().get(id);
            Map<Integer, Box> boxMap = kClustering.getPositionMap();
            //将矩阵化为向量
            matrix = MatrixOperation.matrixToVector(matrix, true);
            Matrix positionMatrix = getBoxMatrix(matrix, boxMap);
            //锚点坐标及长宽预测值
            double tx = positionMatrix.getNumber(0, 0);
            double ty = positionMatrix.getNumber(0, 1);
            double th = positionMatrix.getNumber(0, 3);
            double tw = positionMatrix.getNumber(0, 2);
            //修正相对位置
            double realX = ArithUtil.add(ArithUtil.mul(tx, height), x);
            double realY = ArithUtil.add(ArithUtil.mul(ty, width), y);
            double realWidth = ArithUtil.mul(Math.exp(tw), width);
            double realHeight = ArithUtil.mul(Math.exp(th), height);
            frameBody.setRealX(realX);
            frameBody.setRealY(realY);
            frameBody.setRealWidth(realWidth);
            frameBody.setRealHeight(realHeight);
        } else {
            throw new Exception("box is not study");
        }
    }

    //计算两个边框的IOU
    private double getIou(FrameBody frameBody1, FrameBody frameBody2) {
        double iou = 1;
        double s1 = ArithUtil.mul(frameBody1.getRealHeight(), frameBody1.getRealWidth());
        double s2 = ArithUtil.mul(frameBody2.getRealHeight(), frameBody2.getRealWidth());
        double s = ArithUtil.add(s1, s2);
        double minX1 = frameBody1.getRealX();
        double minY1 = frameBody1.getRealY();
        double maxX1 = ArithUtil.add(minX1, frameBody1.getRealHeight());
        double maxY1 = ArithUtil.add(minY1, frameBody1.getRealWidth());
        double minX2 = frameBody2.getRealX();
        double minY2 = frameBody2.getRealY();
        double maxX2 = ArithUtil.add(minX2, frameBody2.getRealHeight());
        double maxY2 = ArithUtil.add(minY2, frameBody2.getRealWidth());
        double maxMinX, minMaxX, maxMinY, minMaxY;
        if (maxX2 > maxX1) {
            maxMinX = maxX1;
        } else {
            maxMinX = maxX2;
        }
        if (minX2 > minX1) {
            minMaxX = minX2;
        } else {
            minMaxX = minX1;
        }
        if (maxY2 > maxY1) {
            maxMinY = maxY1;
        } else {
            maxMinY = maxY2;
        }
        if (minY2 > minY1) {
            minMaxY = minY2;
        } else {
            minMaxY = minY1;
        }
        double intersectX = ArithUtil.sub(maxMinX, minMaxX);//相交X
        double intersectY = ArithUtil.sub(maxMinY, minMaxY);//相交Y
        if (intersectX < 0) {
            intersectX = 0;
        }
        if (intersectY < 0) {
            intersectY = 0;
        }
        double intersectS = ArithUtil.mul(intersectX, intersectY);//相交面积
        double mergeS = ArithUtil.sub(s, intersectS);//相并面积
        iou = ArithUtil.div(intersectS, mergeS);
        return iou;
    }

    public double toSeeById(Matrix matrix, int id) throws Exception {//返回某一个分类的概率
        intoConvolutionNetwork(2, matrix, templeConfig.getConvolutionNerveManager().getSensoryNerves(),
                false, 0, matrixBack);
        List<Double> list = getFeature(matrixBack.getMatrix());
        MaxPoint maxPoint = new MaxPoint();
        maxPoint.setPid(id);
        long t = IdCreator.get().nextId();
        intoDnnNetwork(t, list, templeConfig.getSensoryNerves(), false, null, maxPoint);
        return maxPoint.getpPoint();
    }

    //图像视觉 Accuracy 模式
    public int toSee(Matrix matrix) throws Exception {
        if (templeConfig.getStudyPattern() == StudyPattern.Accuracy_Pattern) {
            intoConvolutionNetwork(2, matrix, templeConfig.getConvolutionNerveManager().getSensoryNerves(),
                    false, 0, matrixBack);
            Matrix myMatrix = matrixBack.getMatrix();
            int classifier = templeConfig.getClassifier();
            int id = -1;
            switch (classifier) {
                case Classifier.LVQ:
                    id = getClassificationIdByLVQ(myMatrix);
                    break;
                case Classifier.DNN:
                    id = getClassificationIdByDnn(myMatrix);
                    break;
                case Classifier.VAvg:
                    id = getClassificationIdByVag(myMatrix);
                    break;
            }
            return id;
        } else {
            throw new Exception("pattern is wrong");
        }
    }

    private int getClassificationIdByDnn(Matrix myMatrix) throws Exception {
        List<Double> list = getFeature(myMatrix);
        MaxPoint maxPoint = new MaxPoint();
        maxPoint.setTh(templeConfig.getTh());
        long id = IdCreator.get().nextId();
        intoDnnNetwork(id, list, templeConfig.getSensoryNerves(), false, null, maxPoint);
        return maxPoint.getId();
    }

    private int getClassificationIdByVag(Matrix myMatrix) throws Exception {//VAG获取分类
        Matrix myVector = MatrixOperation.matrixToVector(myMatrix, true);
        Map<Integer, Matrix> matrixK = templeConfig.getVectorK().getMatrixK();
        double minDist = 0;
        int id = 0;
        for (Map.Entry<Integer, Matrix> entry : matrixK.entrySet()) {
            Matrix matrix = entry.getValue();
            double dist = MatrixOperation.getEDist(matrix, myVector);
            //System.out.println("距离===" + dist + ",类别==" + entry.getKey());
            if (minDist == 0 || dist < minDist) {
                minDist = dist;
                id = entry.getKey();
            }
        }
        return id;
    }

    private int getClassificationIdByLVQ(Matrix myMatrix) throws Exception {//LVQ获取分类
        Matrix myVector = MatrixOperation.matrixToVector(myMatrix, true);
        int id = 0;
        double distEnd = 0;
        LVQ lvq = templeConfig.getLvq();
        MatrixBody[] matrixBodies = lvq.getModel();
        for (int i = 0; i < matrixBodies.length; i++) {
            MatrixBody matrixBody = matrixBodies[i];
            Matrix vector = matrixBody.getMatrix();
            double dist = lvq.vectorEqual(myVector, vector);
            if (distEnd == 0 || dist < distEnd) {
                id = matrixBody.getId();
                distEnd = dist;
            }
        }
        return id;
    }

    private List<Double> sub(Matrix matrix) throws Exception {//
        List<Double> list = new ArrayList<>();
        int x = matrix.getX() - 1;
        int y = matrix.getY() - 1;
        for (int i = 0; i < templeConfig.getRow(); i++) {
            for (int j = 0; j < templeConfig.getColumn(); j++) {
                if (i > x || j > y) {
                    list.add(0.0);
                } else {
                    list.add(matrix.getNumber(i, j));
                }
            }
        }
        return list;
    }

    /**
     * @param eventId          事件ID
     * @param featureList      特征集合
     * @param sensoryNerveList 感知神经元集合
     * @param isStudy          是否学习
     * @param map              标注
     * @param outBack          输出结果回调类
     */

    private void intoDnnNetwork(long eventId, List<Double> featureList, List<SensoryNerve> sensoryNerveList
            , boolean isStudy, Map<Integer, Double> map, OutBack outBack) throws Exception {//进入DNN 神经网络
        for (int i = 0; i < sensoryNerveList.size(); i++) {
            sensoryNerveList.get(i).postMessage(eventId, featureList.get(i), isStudy, map
                    , outBack);
        }
    }

    /**
     * @param eventId          事件ID
     * @param feature          特征矩阵
     * @param sensoryNerveList 感知神经元集合
     * @param isKernelStudy    是否进行核学习
     * @param E                期望矩阵
     * @param outBack          输出结果回调类
     */
    private void intoConvolutionNetwork(long eventId, Matrix feature, List<SensoryNerve> sensoryNerveList
            , boolean isKernelStudy, int E, OutBack outBack) throws Exception {//进入卷积神经网络
        for (int i = 0; i < sensoryNerveList.size(); i++) {
            sensoryNerveList.get(i).postMatrixMessage(eventId, feature, isKernelStudy, E, outBack);
        }
    }
}