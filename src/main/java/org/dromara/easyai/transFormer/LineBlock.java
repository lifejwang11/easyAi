package org.dromara.easyai.transFormer;

import org.dromara.easyai.function.ReLu;
import org.dromara.easyai.function.Tanh;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.transFormer.model.LineBlockModel;
import org.dromara.easyai.transFormer.nerve.HiddenNerve;
import org.dromara.easyai.transFormer.nerve.Nerve;
import org.dromara.easyai.transFormer.nerve.OutNerve;
import org.dromara.easyai.transFormer.nerve.SoftMax;

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

    public LineBlockModel getModel() throws Exception {
        LineBlockModel lineBlockModel = new LineBlockModel();
        List<float[][]> hiddenNerveModel = new ArrayList<>();
        List<float[][]> outNerveModel = new ArrayList<>();
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
        List<float[][]> hiddenNerveModel = lineBlockModel.getHiddenNervesModel();
        List<float[][]> outNerveModel = lineBlockModel.getOutNervesModel();
        for (int i = 0; i < hiddenNerveList.size(); i++) {
            hiddenNerveList.get(i).insertModel(hiddenNerveModel.get(i));
        }
        for (int i = 0; i < outNerveList.size(); i++) {
            outNerveList.get(i).insertModel(outNerveModel.get(i));
        }
    }

    public LineBlock(int typeNumber, int featureDimension, float studyPoint, CodecBlock lastCodecBlock,
                     boolean showLog, int regularModel, float regular, int coreNumber, float timePunValue) throws Exception {
        this.featureDimension = featureDimension;
        this.lastCodecBlock = lastCodecBlock;
        matrixOperation = new MatrixOperation(coreNumber);
        SoftMax softMax = new SoftMax(outNerveList, showLog, typeNumber, typeNumber, typeNumber, timePunValue);
        //隐层
        List<Nerve> hiddenNerves = new ArrayList<>();
        for (int i = 0; i < featureDimension; i++) {
            HiddenNerve hiddenNerve = new HiddenNerve(i + 1, 1, studyPoint, new ReLu(), featureDimension,
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

    public void sendParameter(long eventID, Matrix feature, boolean isStudy, OutBack outBack, List<Integer> E, boolean outAllPro) throws Exception {
        for (HiddenNerve hiddenNerve : hiddenNerveList) {
            hiddenNerve.postMessage(eventID, feature, isStudy, outBack, E, outAllPro);
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
