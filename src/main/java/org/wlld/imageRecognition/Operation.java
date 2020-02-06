package org.wlld.imageRecognition;


import org.wlld.MatrixTools.Matrix;
import org.wlld.MatrixTools.MatrixOperation;
import org.wlld.config.StudyPattern;
import org.wlld.i.OutBack;
import org.wlld.imageRecognition.border.*;
import org.wlld.nerveEntity.SensoryNerve;
import org.wlld.tools.ArithUtil;

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
        int maxNub = 0;
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

    //模板学习
    public void study(Matrix matrix, Map<Integer, Double> tagging) throws Exception {
        if (templeConfig.getStudyPattern() == StudyPattern.Speed_Pattern) {
            List<Double> list = convolution(matrix, tagging);
            intoNerve(1, list, templeConfig.getSensoryNerves(), true, tagging, null);
        } else {
            throw new Exception("pattern is wrong");
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
            intoNerve2(1, matrix, templeConfig.getConvolutionNerveManager().getSensoryNerves(),
                    isKernelStudy, tagging, matrixBack);
            if (isNerveStudy) {
                //卷积后的结果
                Matrix myMatrix = matrixBack.getMatrix();
                if (templeConfig.isHavePosition() && tagging > 0) {
                    border.end(myMatrix, tagging);
                }
                //进行聚类
                LVQ lvq = templeConfig.getLvq();
                Matrix vector = MatrixOperation.matrixToVector(myMatrix, true);
                MatrixBody matrixBody = new MatrixBody();
                matrixBody.setMatrix(vector);
                matrixBody.setId(tagging);
                lvq.insertMatrixBody(matrixBody);
            }
        } else {
            throw new Exception("pattern is wrong");
        }
    }

    //图像视觉 speed 模式
    public void look(Matrix matrix, long eventId) throws Exception {
        if (templeConfig.getStudyPattern() == StudyPattern.Speed_Pattern) {
            List<Double> list = convolution(matrix, null);
            intoNerve(eventId, list, templeConfig.getSensoryNerves(), false, null, outBack);
        } else if (templeConfig.getStudyPattern() == StudyPattern.Accuracy_Pattern) {
            throw new Exception("studyPattern not right");
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
                    intoNerve(eventId, list, templeConfig.getSensoryNerves(), false, null, imageBack);
                }
                return toPosition(frameBodies, frame.getWidth(), frame.getHeight());
            } else if (templeConfig.getStudyPattern() == StudyPattern.Accuracy_Pattern) {
                for (FrameBody frameBody : frameBodies) {
                    intoNerve2(eventId, frameBody.getMatrix(), templeConfig.getConvolutionNerveManager().getSensoryNerves(),
                            false, -1, matrixBack);
                    Matrix myMatrix = matrixBack.getMatrix();
                    //卷积层输出即边框回归的输入的特征向量 TODO 这地有BUG 要改
                    frameBody.setEndMatrix(myMatrix);
                    int id = getClassificationId2(myMatrix);
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
        double endDist = 0;
        for (Map.Entry<Integer, Box> entry : boxMap.entrySet()) {
            Box box = entry.getValue();
            Matrix boxMatrix = box.getMatrix();
            double dist = MatrixOperation.getEDist(matrix, boxMatrix);
            if (endDist == 0 || dist < endDist) {
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
            KClustering kClustering = templeConfig.getkClusteringMap().get(id);
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

    //图像视觉 Accuracy 模式
    public int toSee(Matrix matrix) throws Exception {
        if (templeConfig.getStudyPattern() == StudyPattern.Accuracy_Pattern) {
            intoNerve2(2, matrix, templeConfig.getConvolutionNerveManager().getSensoryNerves(),
                    false, -1, matrixBack);
            Matrix myMatrix = matrixBack.getMatrix();
            Matrix vector = MatrixOperation.matrixToVector(myMatrix, true);
            return getClassificationId2(vector);
        } else {
            throw new Exception("pattern is wrong");
        }
    }

    private int getClassificationId2(Matrix myVector) throws Exception {
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

    private void intoNerve(long eventId, List<Double> featureList, List<SensoryNerve> sensoryNerveList
            , boolean isStudy, Map<Integer, Double> map, OutBack imageBack) throws Exception {
        for (int i = 0; i < sensoryNerveList.size(); i++) {
            sensoryNerveList.get(i).postMessage(eventId, featureList.get(i), isStudy, map
                    , imageBack);
        }
    }

    private void intoNerve2(long eventId, Matrix featur, List<SensoryNerve> sensoryNerveList
            , boolean isKernelStudy, int E, OutBack outBack) throws Exception {
        for (int i = 0; i < sensoryNerveList.size(); i++) {
            sensoryNerveList.get(i).postMatrixMessage(eventId, featur, isKernelStudy, E, outBack);
        }
    }
}