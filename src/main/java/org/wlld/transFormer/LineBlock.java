package org.wlld.transFormer;

import org.wlld.function.Tanh;
import org.wlld.i.OutBack;
import org.wlld.matrixTools.Matrix;
import org.wlld.matrixTools.MatrixOperation;
import org.wlld.transFormer.model.LineBlockModel;
import org.wlld.transFormer.nerve.HiddenNerve;
import org.wlld.transFormer.nerve.Nerve;
import org.wlld.transFormer.nerve.OutNerve;
import org.wlld.transFormer.nerve.SoftMax;

import java.util.ArrayList;
import java.util.List;

public class LineBlock {//线性层模块
    private final List<HiddenNerve> hiddenNerveList = new ArrayList<>();
    private final List<OutNerve> outNerveList = new ArrayList<>();//输出层
    private final CodecBlock lastCodecBlock;//最后一层解码块
    private Matrix allError;
    private final int featureDimension;
    private int backNumber = 0;//误差返回次数
    private final MatrixOperation matrixOperation;

    public LineBlockModel getModel() {
        LineBlockModel lineBlockModel = new LineBlockModel();
        List<double[][]> hiddenNerveModel = new ArrayList<>();
        List<double[][]> outNerveModel = new ArrayList<>();
        for (HiddenNerve hiddenNerve : hiddenNerveList) {
            hiddenNerveModel.add(hiddenNerve.getModel());
        }
        for (OutNerve outNerve : outNerveList) {
            outNerveModel.add(outNerve.getModel());
        }
        lineBlockModel.setHiddenNervesModel(hiddenNerveModel);
        lineBlockModel.setOutNervesModel(outNerveModel);
        return lineBlockModel;
    }

    public void insertModel(LineBlockModel lineBlockModel) throws Exception {
        List<double[][]> hiddenNerveModel = lineBlockModel.getHiddenNervesModel();
        List<double[][]> outNerveModel = lineBlockModel.getOutNervesModel();
        for (int i = 0; i < hiddenNerveList.size(); i++) {
            hiddenNerveList.get(i).insertModel(hiddenNerveModel.get(i));
        }
        for (int i = 0; i < outNerveList.size(); i++) {
            outNerveList.get(i).insertModel(outNerveModel.get(i));
        }
    }

    public LineBlock(int typeNumber, int featureDimension, double studyPoint, CodecBlock lastCodecBlock,
                     boolean showLog, int regularModel, double regular, int coreNumber) throws Exception {
        this.featureDimension = featureDimension;
        this.lastCodecBlock = lastCodecBlock;
        matrixOperation = new MatrixOperation(coreNumber);
        SoftMax softMax = new SoftMax(outNerveList, showLog, typeNumber, typeNumber, typeNumber);
        //隐层
        List<Nerve> hiddenNerves = new ArrayList<>();
        for (int i = 0; i < featureDimension; i++) {
            HiddenNerve hiddenNerve = new HiddenNerve(i + 1, 1, studyPoint, new Tanh(), featureDimension,
                    typeNumber, this, regularModel, regular, coreNumber);
            hiddenNerves.add(hiddenNerve);
            hiddenNerveList.add(hiddenNerve);
        }
        //输出层
        List<Nerve> outNerves = new ArrayList<>();
        for (int i = 0; i < typeNumber; i++) {
            OutNerve outNerve = new OutNerve(i + 1, studyPoint, featureDimension, featureDimension, typeNumber, softMax
                    , regularModel, regular, coreNumber);
            outNerve.connectFather(hiddenNerves);
            outNerves.add(outNerve);
            outNerveList.add(outNerve);
        }
        for (Nerve nerve : hiddenNerves) {
            nerve.connect(outNerves);
        }
    }

    public void sendParameter(long eventID, Matrix feature, boolean isStudy, OutBack outBack, List<Integer> E) throws Exception {
        for (HiddenNerve hiddenNerve : hiddenNerveList) {
            hiddenNerve.postMessage(eventID, feature, isStudy, outBack, E);
        }
    }

    public void backError(long eventID, Matrix errorMatrix) throws Exception {//从线性层返回的误差
        backNumber++;
        if (allError == null) {
            allError = errorMatrix;
        } else {
            allError = matrixOperation.add(errorMatrix, allError);
        }
        if (backNumber == featureDimension) {
            backNumber = 0;
            Matrix error = allError.getSonOfMatrix(0, 0, allError.getX(), allError.getY() - 1);
            allError = null;
            //将误差矩阵回传
            lastCodecBlock.backError(eventID, error);
        }
    }

}
